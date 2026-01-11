# 9년 치 데이터와의 사투: Spring Batch Partitioner로 OOM 탈출하기

스프링 배치를 이용해 대량의 데이터를 처리하다 보면 누구나 한 번쯤 '메모리'라는 벽에 부딪히곤 하죠. 저 역시 최근 9년 치 원장 데이터를 재처리하며 발생했던 OOM(Out Of Memory) 문제를 경험했는데요. 이 위기를 어떻게 Spring Batch의 기능들로 해결했는지 그 과정을 공유하고자 해요.

## 문제의 시작: 9년, 그리고 수억 건의 데이터

원장 통계 데이터의 구조를 변경해야 하는 과제가 주어졌어요. 대상은 2017년부터 2025년까지, 무려 9년 분량의 데이터였죠.

트래픽이 집중되는 채널의 경우, 하루치 데이터만 약 250만 건에 달했어요. 이를 계산해 보니 한 달이면 7,500만 건, 일 년이면 수억 건의 데이터가 쌓여 있었던 셈이죠.

방대한 운영 데이터를 다루는 만큼, 데이터 정합성을 실시간으로 확인하기 위해 다음과 같은 단계별 전략을 세우게 되었어요.
- 1차 분할: 전체 기간을 '한 달' 단위로 나누어 배치 작업 실행
- 2차 분할: 한 달(7,500만 건)의 부하를 줄이기 위해 배치 내부에서 다시 '하루' 단위로 나누어 처리

이러한 전략에도 불구하고, 한정된 자원 안에서 이 거대한 데이터를 처리하기 위해 함께 산을 넘어보려고 해요.

## Spring Batch의 확장성과 병렬 처리

> [Scaling and Parallel Processing](https://docs.spring.io/spring-batch/reference/scalability.html)
> 
Spring Batch는 대용량 처리를 위해 다양한 확장 및 병렬 처리 모델을 제공해요. 이 모델들은 크게 단일 프로세스(Single-process) 방식과 다중 프로세스(Multi-process) 방식 두 가지로 분류할 수 있어요

1️⃣ 단일 프로세스: 주로 하나의 JVM 내에서 멀티스레드를 활용하여 성능을 최적화
- **Multi-threaded Step**: 하나의 Step 내에서 Chunk 단위로 여러 스레드가 병렬 처리 (가장 일반적인 방식)
- **Parallel Steps**: 서로 의존성이 없는 독립적인 Step들을 동시에 실행
- **Local Chunking**: Manager Step이 데이터를 읽고(Read), 내부의 Worker 스레드들이 가공(Process)과 쓰기(Write)를 분담
- **Local Partitioning**: Manager Step이 데이터 범위를 나누고, 각 범위를 담당하는 Worker Step들이 로컬 스레드에서 독립적으로 실행

2️⃣ 다중 프로세스 (Multi-process): 여러 대의 서버(JVM)로 부하를 분산하여 물리적인 한계를 극복
- **Remote Chunking**: Manager가 읽은 데이터를 메시지 큐를 통해 외부 Worker 노드들에 Process와 Write 처리를 전달
- **Remote Partitioning**: Local Partitioning과 동일한 논리로 데이터를 나눈 뒤, 실제 다른 서버의 Worker Step들이 실행하도록 위임
- **Remote Step**: 전체 Step 실행 자체를 외부의 독립적인 프로세스나 서버에 위임하여 실행

.

이번 작업의 핵심은 **'한 달치 데이터를 하루 단위로 쪼개어 독립적으로 처리하는 것'** 이었기 때문에,
Spring Batch가 제공하는 다양한 기능 중, 저는 [partitioning](https://docs.spring.io/spring-batch/reference/scalability.html#partitioning) 방식을 선택하게 되었어요.

단순히 스레드만 늘리는 Multi-threaded Step은 데이터 읽기 과정에서 스레드 간 경합이나 순서 보장이 어려울 수 있는 반면, Partitioning은 데이터의 범위를 명확히 나누어 각 Step이 자신만의 상태를 가지고 독립적으로 실행될 수 있다는 장점이 있어요.

따라서 한 달치 데이터인 7,500만 건이라는 방대한 부하를 안정적으로 격리하고 병렬성을 극대화하기 위해, 로컬 환경에서의 [partitioning](https://docs.spring.io/spring-batch/reference/scalability.html#partitioning) 전략을 최종적으로 채택하게 되었어요.

## Partitioner 사용하기

<figure><img src="https://raw.githubusercontent.com/jihunparkme/blog/refs/heads/main/img/spring-batch/partitioning-overview.png" alt=""><figcaption></figcaption></figure>

`Partitioning` 방식은 **Manager(Master) Step**이 전체 데이터를 작은 조각(Partition)으로 나누고, 이 조각들을 **Worker(Slave) Step**들이 병렬로 처리하는 구조에요.

각 `Worker Step`은 독립적인 **ItemReader**, **ItemProcessor**, **ItemWriter**를 가지고 동작하므로, 서로의 작업에 영향을 주지 않고 효율적으로 대량의 데이터를 처리할 수 있어요. 이를 가능하게 하는 두 가지 핵심 인터페이스인 `Partitioner`, `PartitionHandler`를 살펴보고 가볼까요~?

### Partitioner

> 👩🏼‍💻 작업 지시서를 만드는 기획자
> 
> 전체 데이터를 어떤 기준으로 나눌지 결정하고, 각 조각에 대한 메타데이터를 생성

|구분|설명|
|---|---|
|역할|데이터 분할 전략 정의 및 실행 정보 생성|
|핵심 메서드|`Map<String, ExecutionContext> partition(int gridSize)`|
|동작 방식|- gridSize를 참고하여 데이터 범위를 계산<br/>- 각 파티션 정보를 ExecutionContext라는 바구니에 저장<br/>- 고유한 이름을 붙인 Map 형태로 반환|
|특징|비즈니스 로직을 실행하지 않고, **'어디서부터 어디까지 처리하라'** 는 정보만 생성|

**Partitioner 코드**

```kotlin
// TODO: 코드 다시 적용
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

### PartitionHandler

> 👷🏼 작업을 배분하는 현장 소장
> 
> Partitioner가 만든 작업 지시서를 받아, 실제로 어떻게 실행하고 관리할지를 결정

|구분|설명|
|---|---|
|역할|파티션의 실행 방식 결정 및 전체 프로세스 관리|
|주요 설정|- **gridSize**: 생성할 파티션의 목표 개수<br/>- **taskExecutor**: 병렬 처리를 수행할 스레드 풀<br/>- **step**: 실제 로직을 수행할 Worker Step 지정|
|동작 방식|- `Partitioner`를 호출하여 분할 정보를 가져옴<br/>- `TaskExecutor`를 통해 Worker Step들에게 정보를 전달 및 실행<br/>- 모든 작업이 완료될 때까지 대기 후 최종 상태를 취합|

**PartitionHandler 적용 코드**

```kotlin
// TODO: 코드 다시 적용
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

> 🔄 두 인터페이스가 협력하여 데이터를 처리하는 과정
>
> 1. **Manager Step 가동**: 배치가 시작되면 관리자 역할을 하는 Manager Step이 실행
>
> 2. **Partitioner의 데이터 분할**: Partitioner가 호출되어 전체 데이터를 n개로 나눈 **파티션 정보(ExecutionContext)**를 생성
> 
> 3. **PartitionHandler의 작업 분배**: PartitionHandler가 이 정보를 토대로 TaskExecutor에 작업을 할당
>
> 4. **Worker Step의 독립 실행**: 각 스레드에서 Worker Step이 할당받은 파티션 정보를 사용해 실제 로직(Reader-Processor-Writer)을 수행
>
> 5. **상태 수집 및 종료**: 모든 Worker Step이 완료되면 PartitionHandler가 결과를 취합하여 Master Step에 보고하고 작업을 마무

⚠️ TODO 두 인터페이스 처리에 대한 이미지 

Spring Batch의 partitioning 기능 덕분에 배치 내부에서 데이터를 하루 단위로 분할해 병렬로 처리하도록 쉽게 적용할 수 있었어요. 하지만 기쁨도 잠시.. 어느정도는 예상했던 난관에 봉착했답니다.

데이터를 쪼개어 처리하도록 설정했지만, 한 번에 최대 6개의 스레드가 동시에 가동되면서 문제가 발생한 것이죠.
- 계산된 데이터 부하: 하루치(250만 건) × 6개 스레드 = 약 1,500만 건

결국 1,500만 건에 달하는 방대한 데이터가 한꺼번에 메모리에 적재되면서, 그토록 피하고 싶었던 OOM(Out Of Memory)의 늪에 빠지게 되었습니다. 🥲

단순히 '병렬로 처리한다'는 전략만으로는 부족했었죠. 한정된 메모리 자원 안에서 이 거대한 데이터를 어떻게 효율적으로 제어하며 흘려보낼지, 더 세밀한 최적화가 필요한 시점이었어요.

## ItemReader 방식의 최적화: Cursor 기반 스트리밍

하루치 데이터도 적은 양이 아니었기 때문에 기존의 전체 로드 방식 대신, 리소스를 효율적으로 사용하는 ItemReader로의 변경이 필요해졌어요.

이미 Partitioner를 통해 날짜별로 범위를 나누어 두었으므로, 이제 각 스레드(Slave Step) 내부에서 메모리 점유율을 최소화하며 데이터를 읽어오는 것이 핵심이 되었어요.

MongoDB 환경에서 선택할 수 있는 방식은 크게 두 가지가 있어요.
- `MongoPagingItemReader`: 페이지 단위로 데이터를 끊어서 조회
- `MongoCursorItemReader`: DB 서버와 커서를 유지하며 스트리밍 방식으로 데이터를 한 건씩 호출

두 가지 방식 중 제한된 메모리 내에서 대량의 데이터를 안정적으로 처리하기 위해, 데이터를 메모리에 쌓아두지 않고 즉시 흘려보내는 `MongoCursorItemReader` 방식을 채택하게 되었어요.















## ItemWriter 방식의 변경

ItemWriter 자체가 직접적인 OOM의 주범이 되는 경우는 드물지만, 쓰기 속도가 읽기 속도를 못 따라가면 처리 대기 중인 객체들이 메모리에 오래 머물게 되어 간접적으로 OOM을 유발할 수 있어요.

MongoDB를 사용 중이므로, `MongoItemWriter`를 사용하는데 스프링 배치에서 제공하는 기본 MongoItemWriter는 내부적으로 Bulk Operations를 지원해요</br>
청크 사이즈만큼 데이터를 모았다가, 한 번의 네트워크 통신으로 하나씩 insert 하는 방식보다 속도 측면에서도 이득을 볼 수 있고, 네트워크 I/O 비용을 획기적으로 줄일 수 있어요.

```kotlin
// bulkOps.insert 쪽 코드
private fun generateCardStatistics(startDate: LocalDate, endDate: LocalDate): MutableList<StatisticsResult> {
    val results = mutableListOf<StatistcsResult>()
    val batchSize = 1_000

    mongoTemplate.aggregateStream(
        
    )
}

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