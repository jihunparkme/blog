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
- **Multi-threaded Step**: 하나의 Step 내에서 Chunk 단위로 여러 스레드가 병렬로 실행
- **Parallel Steps**: 서로 의존성이 없는 여러 개의 Step들을 동시에 실행
- **Local Chunking of Step**: Master 스텝이 데이터를 읽고(Read), 내부의 전용 Worker 스레드들에게 Process와 Write를 분담
- **Partitioning a Step (Local)**: Master 스텝이 데이터를 범위를 나누고, 각 범위를 담당하는 Slave 스텝들을 로컬 스레드에서 독립적으로 실행

2️⃣ 다중 프로세스 (Multi-process): 여러 대의 서버(JVM)로 부하를 분산하여 처리하는 방식
- **Remote Chunking of Step**: Master가 데이터를 읽어 메시지 큐를 통해 여러 외부 Worker 노드에 Process와 Write 처리를 전달
- **Partitioning a Step (Remote)**: 로컬 파티셔닝과 동일한 논리로 데이터를 나누되, 나뉘어진 Slave 스텝들을 실제 다른 서버에서 실행
- **Remote Step**: 전체 Step 실행 자체를 외부의 독립적인 프로세스나 서버에 위임하여 실행

.

이같이 Spring Batch 가 제공하는 다양한 병렬 처리 기능들 중, 한 달치 데이터를 처리하는데 하루치씩 분할해서 병렬로 처리하기 위해 [partitioning](https://docs.spring.io/spring-batch/reference/scalability.html#partitioning) 방식을 적용하게 되었어요.

### Partitioner 사용하기

<figure><img src="https://raw.githubusercontent.com/jihunparkme/blog/refs/heads/main/img/spring-batch/partitioning-overview.png" alt=""><figcaption></figcaption></figure>

`Partitioning` 방식은 **Partitioner Step**에서 데이터를 작은 파티션으로 나누어, 각 파티션을 **Worker Step**들이 병렬로 처리하는 방식이에요.
- 각 **Worker Step**은 ItemReader, ItemProcessor, ItemWriter 등을 가지고 동작해요.

.

주요 인터페이스로는 `Partitioner`, `PartitionHandler`가 있어요.

1️⃣ **Partitioner**: 전체 데이터를 어떤 기준으로 나눌지 결정하고, 나뉜 조각들에 대한 정보를 생성하는 역할

|-|설명|
|---|---|
|역할|데이터를 나누는 전략을 정의|
|핵심 메서드|Map<String, ExecutionContext> partition(int gridSize)|
|동작 방식|- 사용자가 지정한 gridSize를 활용하여 데이터 나누기<br/>- 나뉜 각 조각(파티션)의 정보를 ExecutionContext라는 바구니에 담기<br/>- 각 바구니에 고유한 이름(Key)을 붙여 Map 형태로 반환하기|
|특징|실제 로직을 실행하는 것이 아니라, 실행에 필요한 데이터 범위 정보만 생성|

2️⃣ **PartitionHandler**: `Partitioner`가 만든 작업 지시서(ExecutionContext)를 받아서 실제로 작업을 어떻게 실행할지 결정

|-|설명|
|---|---|
|역할|파티션들의 실행 방식을 결정하고 관리|
|핵심 설정|- **gridSize**: 몇 개의 파티션을 만들지 결정하는 수치<br/>- **taskExecutor**: 작업을 병렬로 돌릴 스레드 풀을 설정<br/>- **step**: 실제 비즈니스 로직을 수행할 Slave Step을 지정|
|동작 방식|- Partitioner를 호출하여 파티션 정보 조회<br/>- 설정된 TaskExecutor를 사용하여 각 파티션 정보를 Slave Step에 전달하고 실행<br/>- 모든 Slave Step이 끝날 때까지 기다렸다가 최종 결과를 수집하여 Master Step에 보고|

> 두 인터페이스의 흐름
>
> 1. **Master Step 시작**: 사용자가 배치를 실행하면 `Master Step`이 가동
> 
> 2. **Partitioner 작동**: Master Step 내의 `Partitioner`가 호출되어 **데이터를 n개로 나눈 정보를 생성**
> 
> 3. **PartitionHandler 배분**: `PartitionHandler`가 이 정보를 받아, 지정된 `TaskExecutor`의 스레드들에게 작업을 분배
> 
> 4. **Slave Step 실행**: 각 스레드에서는 실제 로직(ItemReader, Processor, Writer)이 담긴 `Slave Step`이 각자의 파티션 정보를 가지고 독립적으로 작업을 수행
> 
> 5. **종료**: 모든 스레드 작업이 완료되면 `PartitionHandler`가 상태를 취합하고 전체 스텝이 종료


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
            // 각 파티션(Slave Step)이 읽어야 할 날짜 정보를 저장
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

그렇다면 `ItemReader` 방식의 변경이 필요할 때입니다.

`Partitioner`를 통해 날짜별로 범위를 나누었으므로,<br/>
각 스레드 즉, `Slave Step` 내부에서 데이터를 어떻게 읽고 쓰느냐가 핵심이에요.

MongoDB를 사용 중이므로, ItemReader 방식으로 `MongoCursorItemReader` 또는 `MongoPagingItemReader`를 적용할 수 있는데요.<br/>
최대한 메모리 사용량을 줄이기 위해 스트리밍 방식인 `MongoCursorItemReader` 방식을 적용하게 되었어요.

```kotlin
// MongoCursorItemReader 적용 코드

// 우리는 MongoCursorReader 로 데이터 읽는 코드

// 청크 사이즈 1,000 ?
```

## ItemWriter 방식의 변경

ItemWriter 자체가 직접적인 OOM의 주범이 되는 경우는 드물지만, 쓰기 속도가 읽기 속도를 못 따라가면 처리 대기 중인 객체들이 메모리에 오래 머물게 되어 간접적으로 OOM을 유발할 수 있어요.

MongoDB를 사용 중이므로, `MongoItemWriter`를 사용하는데 스프링 배치에서 제공하는 기본 MongoItemWriter는 내부적으로 Bulk Operations를 지원해요</br>
청크 사이즈만큼 데이터를 모았다가, 한 번의 네트워크 통신으로 하나씩 insert 하는 방식보다 속도 측면에서도 이득을 볼 수 있고, 네트워크 I/O 비용을 획기적으로 줄일 수 있어요.

```kotlin
// mongoTemplate.bulkOps() 활용 코드

private fun saveAndClearResults(results: MutableList<StatisticsResult>) {
    if (results.isEmpty()) return

    val stats = results.map { it.toStatistics() }
    val bulkOps = mongoTemplate.bulkOps(
        BulkOperations.BulkMode.UNORDERED,
        properties.channelType.statisticsCollectionName()
    )

    bulkOps.insert(stats)

    val bulkWriteResult = bulkOps.exectue()
    results.clear() // 이미 저장한 결과 리스트의 메모리 비우기
}
```


 

---

### 4. 1,500만 건 처리를 위한 최종 체크리스트

1. **메모리 격리:** 각 Slave Step이 `@StepScope`로 설정되어 있는지 확인하세요. 그래야 각 스레드가 자신만의 `Reader` 객체를 가져 메모리 혼선이 없습니다.
2. **인덱스 최적화:** `Partitioner`에서 사용하는 날짜 필드(`startDate`, `endDate`)와 Reader의 정렬 필드에 반드시 **복합 인덱스**가 있어야 합니다. 인덱스가 없으면 Reader가 데이터를 찾는 속도가 느려져 배치가 타임아웃될 수 있습니다.
3. **No-State 처리:** 가능하다면 `ItemProcessor`에서 엔티티의 상태를 변경하기보다, 새로운 DTO를 만들어 `ItemWriter`로 넘기는 방식이 GC(Garbage Collection) 효율에 더 좋습니다.
4. **Bulk Write 활성화:** `MongoItemWriter`를 사용하면 내부적으로 `Bulk Operations`를 수행하므로, 1,000개씩 모아서 한 번에 insert/update를 처리하여 네트워크 I/O를 최적화할 수 있습니다.

**결론적으로,** `MongoCursorItemReader`를 사용하고 **청크 사이즈를 1,000**으로 설정한 뒤, **JVM 힙 메모리를 8GB 이상** 할당하신다면 6개 스레드로 1,500만 건을 안전하게 처리할 수 있습니다.