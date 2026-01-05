# Spring Batch Partitioner

스프링 배치를 통해 대량의 데이터를 여러 번 처리해야 했었는데요. 그 과정에서 발생했던 **OOM(Out Of Memory)** 문제를 어떤 방식으로 풀어냈는지 공유하고자 합니다.

## 문제의 시작

원장 통계 데이터 구조를 변경하기 위해, 17년도부터 25년도까지 약 9년치 원장 데이터에 대한 새로운 통계를 생성하는 작업을 진행하게 되었어요. 

트래픽이 가장 많은 채널에 대한 하루치 원장 데이터만 해도 대략 250만 건이 되었는데, 한달치면 약 7,500만 건, 일년치면...

방대한 양의 운영 데이터에 변경이 일어나는 작업이다보니, 작업 중간중간 데이터 정합성을 확인하며 진행하기 위해 한달 단위로 나눠서 작업을 진행하는 전략을 세우게 되었어요.

한달 단위로 나누더라도 한 번의 배치 작업에 7,500만 건의 원장 데이터가 사용되게 되므로, 배치 안에서도 하루씩 분할해서 처리가 되도록 구현하는 전략을 추가하게 되었어요.

## Spring Batch Scaling And Parallel Processing

Spring Batch 는 `Scaling`과 `Parallel Processing` 관련 기능을 제공하고 있는데,<br/>
병렬 처리 모드는 크게 단일 프로세스, 다중 프로세스 두 가지가 있습니다.

1️⃣ 단일 프로세스(Single-process): 주로 한 개의 JVM 내에서 멀티스레드를 활용하는 방식
- Multi-threaded Step: 하나의 Step 내에서 Chunk 단위로 여러 스레드가 병렬로 실행
- Parallel Steps: 서로 의존성이 없는 여러 개의 Step들을 동시에 실행
- Local Chunking of Step: Master 스텝이 데이터를 읽고(Read), 내부의 전용 Worker 스레드들에게 Process와 Write를 분담
- Partitioning a Step (Local): Master 스텝이 데이터를 범위를 나누고, 각 범위를 담당하는 Slave 스텝들을 로컬 스레드에서 독립적으로 실행

2️⃣ 다중 프로세스 (Multi-process): 여러 대의 서버(JVM)로 부하를 분산하여 처리하는 방식
- Remote Chunking of Step: Master가 데이터를 읽어 메시지 큐를 통해 여러 외부 Worker 노드에 Process와 Write 처리를 전달
- Partitioning a Step (Remote): 로컬 파티셔닝과 동일한 논리로 데이터를 나누되, 나뉘어진 Slave 스텝들을 실제 다른 서버에서 실행
- Remote Step: 전체 Step 실행 자체를 외부의 독립적인 프로세스나 서버에 위임하여 실행

.

이같이 Spring Batch 가 제공하는 다양한 병렬 처리 기능들 중, 한 달치 데이터를 처리하는데 하루치씩 분할해서 병렬로 처리하기 위해 [partitioning](https://docs.spring.io/spring-batch/reference/scalability.html#partitioning) 방식을 적용하게 되었어요.

## Partitioner 사용하기

**Partitioner**

```kotlin
class SamplePartitioner(
    private val startDate: LocalDate,
    private val endDate: LocalDate,
    private val timestamp: Long,
) : Partitioner {
    override fun partition(gridSize: Int): Map<String, ExecutionContext> {
        val partitions: MutableMap<String, ExecutionContext> = mutableMapOf<String, ExecutionContext>()
        val days: Long = ChronoUnit.DAYS.between(startDate, endDate) + 1 // 총 일자 계산
        repeat(days.toInt()) { // 하루치씩 반복하며 ExecutionContext를 생성
            val currentDate: LocalDate! = startDate.plusDays(it.toLong())
            val executionContext = ExecutionContext()
            // 각 파티션(슬레이브 스텝)이 읽어야 할 날짜 정보를 저장
            executionContext.putString("startDate", currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            executionContext.putString("endDate", currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            // 파티션 개수 지정
            executionContext.putLong("SimpleStepExecutionSplitter.GRID_SIZE", 6L)
            // 파티션 식별자에 유니크한 키를 부여
            partitions["MigCardStatisticsPartition_$fit_$timestamp"] = executionContext
        }
        return partitions
    }
}
```

**JobConfig**

```kotlin
@Configuration
class SampleJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val properties: SampleProperties,
    // ...
) {
    private val log by logger()

    /**
     * Master Job
     */
    @Bean
    fun SampleJob( 
        partitionHandler: PartitionHandler,
    ): Job {
        return JobBuilder("${properties.channelType}SampleJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(SampleManagerStep(partitionHandler))
            .build()
    }

    /**
     * Master Step
     */
    @Bean
    fun SampleManagerStep(
        partitionHandler: PartitionHandler,
    ): Step {
        val timestamp = System.currentTimeMillis()
        return StepBuilder("SampleManagerStep_$timestamp", jobRepository)
            .partitioner( // 작업을 어떻게 나눌지 설정
                "sampleStep",
                MiCardStatisticsPartitioner(properties.startDate, properties.endDate, System.currentTimeMillis())
            )
            .partitionHandler(partitionHandler) // 나눈 작업을 어떻게 실행할지 설정
            .build()
    }

    @Bean
    fun partitionHandler(sampleStep: Step, threadPoolExecutor: ThreadPoolTaskExecutor)
        : PartitionHandler {
        val handler = object : TaskExecutorPartitionHandler() {
            override fun handle(
                stepSplitter: StepExecutionSplitter,
                managerStepExecution: StepExecution
            ): Collection<StepExecution> {
                managerStepExecution.executionContext.putLong(
                    "SimpleStepExecutionSplitter.GRID_SIZE", this.gridSize.toLong()
                )
                return super.handle(stepSplitter, managerStepExecution)
            }
        }
        handler.gridSize = 6 // 한 번에 최대 6개의 스레드가 병렬로 작동
        handler.setTaskExecutor(threadPoolExecutor)
        handler.step = sampleStep
        handler.afterPropertiesSet()

        return handler
    }

    /**
     * Slave Step
     */
    @Bean
    fun sampleStep(
        sampleTasklet: Tasklet
    ): Step {
        return StepBuilder("sampleStep", jobRepository)
            .tasklet(sampleTasklet, transactionManager)
            .build()
    }

    /**
     * Slave Step
     */
    @Bean
    @StepScope
    @Transactional(transactionManager = ALOHA_MONGO_TRANSACTION)
    fun sampleTasklet(
        @Value("#{stepExecutionContext['startDate']}") startDate: LocalDate,
        @Value("#{stepExecutionContext['endDate']}") endDate: LocalDate,
    ): Tasklet {
        return Tasklet {
            contribution, chunkContext →
            // 기존 통계 상태 업데이트 

            // 신규 통계 생성 및 저장(1,000 개 데이터 단위로 벌크 인서트)
            // ...

            RepeatStatus.FINISHED
        }
    }
}
```

이제 배치 안에서 하루씩 분할해서 처리가 되도록 구현을 했지만,<br/>
한 번에 최대 6개의 스레드가 병렬로 처리가 되면서 결국 "250만 x 6"에 달하는 1500만 건의 데이터가 메모리에 쌓이게 되면서 OOM이 발생하게 되었어요.🥲

## ItemReader 방식의 변경

데이터베이스에 있는 수백만 건의 데이터를 한꺼번에 자바 객체로 변환하여 메모리에 적재하려고 한다면, OOM이 발생할 수 있어요.<br/>
데이터가 100만 건이라고 가정할 때, 각 객체가 차지하는 메모리 양이 합쳐지면서 설정된 최대 힙 사이즈(-Xmx)를 초과하게 되고,<br/>
메모리가 가득 차기 직전, JVM은 이를 해소하기 위해 Full GC를 빈번하게 실행하며 GC 부하로 시스템 성능이 급격히 저하되다가 결국 에러를 뿜으며 중단되게 돼요.

ItemReader 방식으로 `PagingItemReader`를 사용하거나, `CursorItemReader`를 사용해서 메모리에 방대한 데이터가 쌓여서 OOM이 발생하는 현상을 해결할 수도 있어요.

하지만, 이 방법을 사용하지는 않았는데요.<br/>
그 이유는, 작업이 필요한 모든 채널의 하루치 데이터가 모두 많은 것이 아니었고, 최소 1,000 건 ~ 최대 2,500,000 건이었어요.<br/>
데이터가 많은 특정 채널을 위해 ItemReader 방식을 변경하는 작업이 효율적인 작업이라고 생각하지 못 했어요.

## Resources

해당 배치는 k9s pods 로 동작하기 때문에 리소스를 올려도 괜찮을 것이라고 판단을 하였고, 리소스를 올리게 되었어요.

```text
// 기본 리소스
limits.cpu=4
limits.memory=3072Mi
requests.cpu=2
requests.cpu=2048Mi

// 변경 리소스
limits.cpu=8
limits.memory=4096Mi
requests.cpu=8
requests.cpu=4096Mi
```


























1,500만 건이라는 대규모 데이터를 6개의 스레드로 병렬 처리(Partitioning)하면서 OOM을 방지하고 성능을 최적화하기 위한 **ItemReader와 청크 사이즈** 전략을 추천해 드립니다.

이미 `Partitioner`를 통해 날짜별로 범위를 나누셨으므로, 각 스레드(Slave Step) 내부에서 데이터를 어떻게 읽고 쓰느냐가 핵심입니다.

---

### 1. 추천 ItemReader: `MongoCursorItemReader` 또는 `MongoPagingItemReader`

MongoDB를 사용 중이시라면, 메모리 효율을 위해 다음 두 가지 중 하나를 선택해야 합니다.

#### **A. MongoCursorItemReader (가장 추천)**

* **특징:** DB와 커서를 유지하며 스트리밍 방식으로 데이터를 읽어옵니다.
* **장점:** 데이터를 한꺼번에 메모리에 올리지 않고 한 건씩(또는 설정된 Batch Size만큼) 가져오기 때문에 **메모리 사용량이 매우 적고 일정하게 유지**됩니다.
* **주의:** DB 연결 시간이 길어질 수 있으므로 타임아웃 설정을 확인해야 합니다.

#### **B. MongoPagingItemReader**

* **특징:** 페이지 단위(예: 1,000건씩)로 `find` 쿼리를 나누어 실행합니다.
* **장점:** DB 연결을 계속 유지하지 않아도 되어 안정적입니다.
* **단점:** 페이지를 넘길수록 `skip/limit` 부하가 발생할 수 있으므로, 반드시 정렬(Sort)과 인덱스가 잘 잡혀 있어야 합니다.

---

### 2. 추천 청크 사이즈 (Chunk Size) & 페이지 사이즈

1,500만 건 처리 시 6개 스레드가 동시에 가동되는 점을 고려한 추천 값입니다.

* **추천 청크 사이즈: `500 ~ 1,000**`
* **이유:** 한 번에 너무 크게 잡으면(예: 10,000) 6개 스레드가 각각 만 개씩 객체를 들고 있게 되어 메모리 압박이 커집니다. 1,000 정도가 성능과 메모리 사이의 가장 안정적인 합의점입니다.


* **Page Size (Reader 설정):** 청크 사이즈와 동일하게 **`1,000`**으로 맞추는 것을 권장합니다.

---

### 3. 구체적인 설정 가이드 (Tasklet 대신 Chunk 구조 추천)

현재 코드는 `Tasklet`을 사용하고 계신데, 1,500만 건 정도의 대량 데이터라면 스프링 배치의 **`Chunk` 지향 처리(Reader-Processor-Writer)**로 전환하는 것이 OOM 방지에 훨씬 유리합니다.

```kotlin
@Bean
@StepScope
fun sampleStep(
    @Value("#{stepExecutionContext['startDate']}") startDate: String,
    @Value("#{stepExecutionContext['endDate']}") endDate: String
): Step {
    return StepBuilder("sampleStep", jobRepository)
        .chunk<InputDoc, OutputDoc>(1000, transactionManager) // 청크 사이즈 1,000
        .reader(mongoReader(startDate, endDate))
        .processor(sampleProcessor())
        .writer(mongoWriter())
        .build()
}

@Bean
@StepScope
fun mongoReader(
    @Value("#{stepExecutionContext['startDate']}") startDate: String,
    @Value("#{stepExecutionContext['endDate']}") endDate: String
): MongoCursorItemReader<InputDoc> {
    return MongoCursorItemReaderBuilder<InputDoc>()
        .name("mongoReader")
        .template(mongoTemplate)
        .targetType(InputDoc::class.java)
        .query(Query(Criteria.where("targetDate").gte(startDate).lte(endDate)))
        .sorts(mapOf("_id" to Sort.Direction.ASC)) // 정렬 필수
        .batchSize(1000) // 한 번에 가져올 데이터 묶음
        .build()
}

```

---

### 4. 1,500만 건 처리를 위한 최종 체크리스트

1. **메모리 격리:** 각 슬레이브 스텝이 `@StepScope`로 설정되어 있는지 확인하세요. 그래야 각 스레드가 자신만의 `Reader` 객체를 가져 메모리 혼선이 없습니다.
2. **인덱스 최적화:** `Partitioner`에서 사용하는 날짜 필드(`startDate`, `endDate`)와 Reader의 정렬 필드에 반드시 **복합 인덱스**가 있어야 합니다. 인덱스가 없으면 Reader가 데이터를 찾는 속도가 느려져 배치가 타임아웃될 수 있습니다.
3. **No-State 처리:** 가능하다면 `ItemProcessor`에서 엔티티의 상태를 변경하기보다, 새로운 DTO를 만들어 `ItemWriter`로 넘기는 방식이 GC(Garbage Collection) 효율에 더 좋습니다.
4. **Bulk Write 활성화:** `MongoItemWriter`를 사용하면 내부적으로 `Bulk Operations`를 수행하므로, 1,000개씩 모아서 한 번에 insert/update를 처리하여 네트워크 I/O를 최적화할 수 있습니다.

**결론적으로,** `MongoCursorItemReader`를 사용하고 **청크 사이즈를 1,000**으로 설정한 뒤, **JVM 힙 메모리를 8GB 이상** 할당하신다면 6개 스레드로 1,500만 건을 안전하게 처리할 수 있습니다.