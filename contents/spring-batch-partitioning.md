# 수억 건의 데이터, 맛있게 쪼개 먹는 방법 (with. Partitioning)

스프링 배치를 이용해 대량의 데이터를 처리하다 보면 누구나 한 번쯤 '메모리'라는 벽에 부딪히곤 하죠. 저 역시 원장 통계 데이터를 재생성하며 OOM(Out Of Memory) 문제를 경험했는데요. 이 위기를 어떻게 Spring Batch의 기능들로 해결했는지 그 과정을 공유하고자 해요.

## 전략 세우기

원장 통계 데이터의 구조가 변경되어 데이터를 다시 생성해야하는 과제가 주어졌어요. 무려 수억건 분량의 데이터였죠.  
트래픽이 집중되는 채널의 경우, 하루치 원장 데이터만 약 250만 건에 달했어요. 이를 계산해 보면 한 달이면 7,500만 건, 일 년이면 수억 건의 데이터가 쌓여 있었던 셈이죠.

방대한 운영 데이터를 다루는 만큼, **데이터 정합성**과 **속도**를 위해 다음과 같이 단계별 전략을 세우게 되었어요.
- 1차 분할: 전체 기간을 '한 달' 단위로 나누어 배치 작업 실행
- 2차 분할: 한 달치 데이터(약 7,500만 건)에 대한 부하를 줄이기 위해 배치 내부에서 다시 '하루' 단위로 나누어 처리

그럼 이제 함께 방대한 데이터를 맛있게 먹기 위해 쪼개러 가볼까요~? 🎂🍰

## Spring Batch의 확장 및 병렬 처리

> [Scaling and Parallel Processing](https://docs.spring.io/spring-batch/reference/scalability.html)
> 
Spring Batch는 대용량 처리를 위해 다양한 확장 및 병렬 처리 기능을 제공해요. 이 기능들은 크게 **`단일 프로세스`(Single-process)** 방식과 **`다중 프로세스`(Multi-process)** 방식 두 가지로 분류할 수 있어요.

1️⃣. **단일 프로세스**: 주로 하나의 JVM 내에서 `멀티스레드`를 활용하여 성능을 최적화
- `Multi-threaded Step`: 하나의 Step 내에서 **Chunk 단위로 여러 스레드가 병렬 처리** (일반적인 방식)
- `Parallel Steps`: 서로 의존성이 없는 **독립적인 Step들을 동시에 실행**
- `Local Chunking`: Manager Step이 데이터를 읽고(Read), 내부의 Worker 스레드들이 **가공(Process)과 쓰기(Write)를 분담**
- `Local Partitioning`: Manager Step이 데이터 범위를 나누고, 각 범위를 담당하는 **Worker Step들이 로컬 스레드에서 독립적으로 실행**

2️⃣. **다중 프로세스**: `여러 대의 서버`(JVM)로 부하를 분산하여 물리적인 한계를 극복
- `Remote Chunking`: Manager Step에서 읽은 데이터를 메시지 큐를 통해 **외부 Worker 노드**들에 가공과 쓰기 처리를 전달
- `Remote Partitioning`: Local Partitioning과 동일한 논리로 데이터를 나눈 뒤, 실제 **다른 서버의 Worker Step들이 실행**하도록 위임
- `Remote Step`: 전체 Step 실행 자체를 **외부의 독립적인 프로세스나 서버에 위임**하여 실행

.

2차 분할 작업의 핵심은 **'한 달치 데이터를 하루 단위로 쪼개어 독립적으로 처리하는 것'** 이었기 때문에,
Spring Batch가 제공하는 다양한 기능 중, [Partitioning](https://docs.spring.io/spring-batch/reference/scalability.html#partitioning) 방식을 선택하게 되었어요.

단순히 스레드만 늘리는 Multi-threaded Step은 데이터 읽기 과정에서 스레드 간 경합이나 순서 보장이 어려울 수 있는 반면, Partitioning은 데이터의 범위를 명확히 나누어 각 Step이 자신만의 상태를 가지고 독립적으로 실행될 수 있다는 장점이 있어요.

따라서 약 7,500만 건에 달하는 한 달치 데이터의 방대한 부하를 안정적으로 격리하고 병렬성을 극대화하기 위해, 로컬 환경에서의 [Local Partitioning](https://docs.spring.io/spring-batch/reference/scalability.html#partitioning) 전략을 최종적으로 채택하게 되었어요.

## Partitioning 사용하기

<figure><img src="https://raw.githubusercontent.com/jihunparkme/blog/refs/heads/main/img/spring-batch/partitioning-overview.png" alt=""><figcaption></figcaption></figure>

**Partitioning** 방식은 `Manager(Master) Step`이 전체 데이터를 **작은 조각**(Partition)으로 나누고, 이 조각들을 각 스레드에서 `Worker(Slave) Step`들이 병렬로 처리하는 구조에요.

각 `Worker Step`은 독립적인 **ItemReader**, **ItemProcessor**, **ItemWriter**를 가지고 동작하므로, 서로의 작업에 영향을 주지 않고 효율적으로 대량의 데이터를 처리할 수 있어요. 이를 가능하게 하는 두 가지 핵심 인터페이스는 `Partitioner`, `PartitionHandler`이랍니다.

본격적으로 두 인터페이스를 살펴보기 전에 Partitioning의 전체적인 동작 과정을 먼저 보고 가볼까요~? 🚗🚙🚕

### Partitioning의 전체적인 동작 과정

<figure><img src="https://raw.githubusercontent.com/jihunparkme/blog/refs/heads/main/img/spring-batch/partitioning.png" alt=""><figcaption></figcaption></figure>

1️⃣. **준비 및 분할 단계**  
가장 먼저 `Manager` 역할을 하는 `PartitionStep`이 전체 작업을 어떻게 나눌지 결정하는 단계
- `Job`이 시작되면 Manager 역할을 하는 `PartitionStep`이 **execute**를 호출하며 시작
  - [void doExecute(StepExecution stepExecution)](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/core/partition/support/PartitionStep.html#doExecute(org.springframework.batch.core.StepExecution))
- `PartitionStep`은 실제 분할 로직을 관리하는 `PartitionHandler`에게 작업 위임
  - [Collection\<StepExecution\> handle(StepExecutionSplitter stepSplitter, StepExecution stepExecution)](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/core/partition/PartitionHandler.html#handle(org.springframework.batch.core.partition.StepExecutionSplitter,org.springframework.batch.core.StepExecution))
- `PartitionHandler`는 `StepExecutionSplitter`에게 분할 정보를 전달
  - [Set\<StepExecution\> split(StepExecution stepExecution, int gridSize)](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/core/partition/StepExecutionSplitter.html#split(org.springframework.batch.core.StepExecution,int))
- `StepExecutionSplitter`가 `Partitioner`를 호출하면, 설정된 gridSize에 따라 데이터를 분할
  - 이때 각 스레드가 처리할 데이터의 범위 정보가 담긴 `ExecutionContext`가 생성
  - [Map\<String, ExecutionContext\> partition(int gridSize)](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/core/partition/support/Partitioner.html#partition(int))

2️⃣. **병렬 실행 단계**  
분할된 작업들이 각자 독립적인 환경(Slave Step)에서 동시에 실행되는 단계
- `PartitionHandler`는 TaskExecutor를 통해 gridSize만큼의 워커 스레드를 생성하고, 각각에 `Slave Step`을 할당
- 각 워커 스레드는 자신만의 `ExecutionContext`를 가지고 데이터를 읽고, 쓰고, 처리하는 청크 로직을 수행
- 모든 `Slave Step`이 자신의 작업을 마치고 ExitStatus를 반환할 때까지 `PartitionHandler`는 대기

3️⃣. **합산 및 종료 단계**  
개별적으로 흩어져 처리된 결과를 하나로 모아 전체 상태를 결정하는 단계
- 모든 Slave Step의 실행 결과(읽은 건수, 성공 여부 등)가 담긴 `StepExecution`객체를 `PartitionStep`에게 반환
- `StepExecutionAggregator`의 `aggregate` 단계가 호출되어 여러 개의 Slave Step 결과들을 합산
  - [aggregate(StepExecution result, Collection\<StepExecution\> executions)](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/core/partition/support/StepExecutionAggregator.html#aggregate(org.springframework.batch.core.StepExecution,java.util.Collection))
- 합산된 결과를 바탕으로 `PartitionStep`의 최종 상태를 업데이트하고 전체 Step을 마무리

이제 본격적으로 Partitioning의 핵심 인터페이스인 `Partitioner`, `PartitionHandler`를 만나러 가보아요~!🏃🏻‍♂️🏃🏻‍♀️🏃🏻

### Partitioner Interface

> 👩🏼‍💻 작업 지시서를 만드는 기획자
> 
> 전체 데이터를 어떤 기준으로 나눌지 결정하고, 각 조각에 대한 메타데이터를 생성

|구분|설명|
|---|---|
|역할|데이터 분할 전략 정의 및 실행 정보 생성|
|핵심 메서드|`Map<String, ExecutionContext> partition(int gridSize)`|
|동작 방식|- gridSize를 참고하여 데이터 분할 범위를 계산<br/>- 각 파티션 정보를 ExecutionContext에 저장<br/>- 고유한 이름을 붙인 Map 형태로 반환|
|특징|비즈니스 로직을 실행하지 않고, **'어디서부터 어디까지 처리하라'** 는 정보만 생성|

**Partitioner 구현**
- 시작 날짜부터 종료 날짜까지 하루 단위로 데이터를 분할하는 역할

```kotlin
class DateRangePartitioner(
    private val startDate: LocalDate,
    private val endDate: LocalDate
) : Partitioner {
    override fun partition(gridSize: Int): Map<String, ExecutionContext> {
        val result = mutableMapOf<String, ExecutionContext>()
        var targetDate = startDate
        var partitionNumber = 0

        // 시작일부터 종료일까지 루프를 돌며 파티션 생성
        while (!targetDate.isAfter(endDate)) {
            val context = ExecutionContext()
            
            // 각 Worker Step이 처리해야 할 날짜 정보를 Context에 담기
            context.putString("targetDate", targetDate.toString())
            
            // 파티션에 고유한 이름을 부여하여 Map에 저장
            result["partition_$partitionNumber"] = context

            targetDate = targetDate.plusDays(1)
            partitionNumber++
        }

        return result
    }
}
```

> 참고로, 위 코드에서는 gridSize를 직접 사용하지 않았지만, 너무 많은 파티션이 생성되지 않도록 gridSize를 적절히 활용하거나 TaskExecutor의 스레드 풀 개수를 조절하여 동시 실행 스레드 수를 관리하는 것이 좋습니다.

### PartitionHandler Interface

> 👷🏼 작업을 배분하는 현장 소장
> 
> Partitioner가 만든 작업 지시서를 받아, 실제로 어떻게 실행하고 관리할지를 결정

|구분|설명|
|---|---|
|역할|파티션의 실행 방식 결정 및 전체 프로세스 관리|
|주요 설정|- **gridSize**: 생성할 파티션의 목표 개수<br/>- **taskExecutor**: 병렬 처리를 수행할 스레드 풀<br/>- **step**: 실제 로직을 수행할 Worker Step 지정|
|동작 방식|- `Partitioner`를 호출하여 분할 정보를 가져옴<br/>- `TaskExecutor`를 통해 Worker Step들에게 정보를 전달 및 실행<br/>- 모든 작업이 완료될 때까지 대기 후 최종 상태를 취합|

**PartitionHandler 동작**

```kotlin
@Bean
fun datePartitionJob(masterStep: Step): Job {
    return JobBuilder("datePartitionJob", jobRepository)
        .incrementer(RunIdIncrementer())
        .start(masterStep)
        .build()
}

@Bean
fun masterStep(
    partitionHandler: PartitionHandler,
    partitioner: Partitioner,
): Step {
    // 직접 로직을 수행하지 않고, partitioner와 partitionHandler를 조합하여 작업을 관리
    return StepBuilder("masterStep", jobRepository)
        .partitioner("workerStep", partitioner) // "어떤 데이터"를 나눌 것인지인가?
        .partitionHandler(partitionHandler) // "어떻게" 병렬로 실행할 것인가?
        .build()
}

/**
 * partitionHandler: 파티셔닝 전략의 핵심 설정
 */
@Bean
fun partitionHandler(workerStep: Step): PartitionHandler {
    val handler = TaskExecutorPartitionHandler()
    handler.setTaskExecutor(batchTaskExecutor()) // 병렬 처리를 위한 스레드 풀 주입
    handler.step = workerStep // 실제로 실행할 작업(Worker Step) 지정
    handler.gridSize = 6 // 한 번에 처리할 파티션 개수(스레드 수)
    return handler
}

@Bean
@JobScope
fun partitioner(
    @Value("#{jobParameters['startDate']}") startDate: String,
    @Value("#{jobParameters['endDate']}") endDate: String
): Partitioner {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val start = LocalDate.parse(startDate, formatter)
    val end = LocalDate.parse(endDate, formatter)

    return DateRangePartitioner(start, end)
}

@Bean
fun workerStep(
    reader: ItemReader<String>,
    writer: ItemWriter<String>
): Step {
    return StepBuilder("workerStep", jobRepository)
        .chunk<String, String>(1000, transactionManager)
        .reader(reader)
        .writer(writer)
        .build()
}

@Bean
@StepScope // 파티션마다 독립적인 빈 생성
fun reader(
    // Partitioner가 ExecutionContext에 저장해둔 targetDate를 주입
    @Value("#{stepExecutionContext['targetDate']}") targetDate: String
): ItemReader<String> {
    log.info(">>> [Thread: ${Thread.currentThread().name}] Start reading date: $targetDate")
    return ListItemReader(listOf("Data for $targetDate"))
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

Spring Batch의 partitioning 기능 덕분에 배치 내부에서 데이터를 하루 단위로 분할해 병렬로 처리하도록 쉽게 적용할 수 있었어요. 하지만 기쁨도 잠시.. 어느정도는 예상했던 난관에 봉착했답니다.

데이터를 쪼개어 처리하도록 설정했지만, 한 번에 최대 6개의 스레드가 동시에 가동되면서 문제가 발생한 것이죠.
- 계산된 데이터 부하: 하루치(250만 건) × 6개 스레드 = 약 1,500만 건

결국 1,500만 건에 달하는 방대한 데이터가 한꺼번에 메모리에 적재되면서, 그토록 피하고 싶었던 OOM(Out Of Memory)의 늪에 빠지게 되었습니다. 🥲

단순히 '병렬로 처리한다'는 전략만으로는 부족했었죠. 한정된 메모리 자원 안에서 이 거대한 데이터를 어떻게 효율적으로 제어하며 흘려보낼지, 더 세밀한 최적화가 필요한 시점이었어요.

## ItemReader 방식의 최적화: Cursor 기반 스트리밍

gridSize와 스레드 풀의 corePoolSize가 6으로 설정되어 있습니다.

만약 각 날짜별 데이터가 250만 건이라면, 6개의 스레드가 동시에 reader를 통해 데이터를 퍼 올리게 됩니다.

이때 각 Step의 chunk size가 너무 크거나, ItemReader가 데이터를 한 번에 메모리에 너무 많이 올리는 구조라면? 250만 × 6의 데이터 부하가 메모리에 가해지며 OOM이 발생하게 되는 것이죠.







하루치 데이터도 적은 양이 아니었기 때문에 기존의 전체 로드 방식 대신, 리소스를 효율적으로 사용하는 ItemReader로의 변경이 필요해졌어요.

이미 Partitioner를 통해 날짜별로 범위를 나누어 두었으므로, 이제 각 스레드(Slave Step) 내부에서 메모리 점유율을 최소화하며 데이터를 읽어오는 것이 핵심이 되었어요.

MongoDB 환경에서 선택할 수 있는 방식은 크게 두 가지가 있어요.
- `MongoPagingItemReader`: 페이지 단위로 데이터를 끊어서 조회
- `MongoCursorItemReader`: DB 서버와 커서를 유지하며 스트리밍 방식으로 데이터를 한 건씩 호출

두 가지 방식 중 제한된 메모리 내에서 대량의 데이터를 안정적으로 처리하기 위해, 데이터를 메모리에 쌓아두지 않고 즉시 흘려보내는 `MongoCursorItemReader` 방식을 채택하게 되었어요.

```kotlin
// TODO: 적용 코드로 수정

@Bean
@StepScope
fun reader(
    @Value("#{stepExecutionContext['fromDate']}") fromDate: String,
    @Value("#{stepExecutionContext['toDate']}") toDate: String
): MongoCursorItemReader<UserEntity> {
    return MongoCursorItemReaderBuilder<UserEntity>()
        .name("userItemReader")
        .template(mongoTemplate)
        .targetType(UserEntity::class.java)
        .jsonQuery("{ 'createdAt': { \$gte: ?0, \$lt: ?1 } }")
        .parameterValues(listOf(fromDate, toDate))
        .sorts(mapOf("createdAt" to Sort.Direction.ASC))
        .cursorBatchSize(1000) // MongoDB 커서가 한 번에 가져올 데이터 양
        .build()
}
```

Cursor 방식을 적용하면서 메모리 효율성과 안정성을 모두 얻을 수 있었어요.
- **메모리 효율성**: 페이징 방식은 다음 페이지를 부를 때마다 이전 데이터만큼 Skip해야 하므로 뒤로 갈수록 느려질 수 있지만, 커서는 스트리밍 방식이라 메모리 사용량이 일정하게 유지.
- **안정성**: 병렬로 Slave Step이 돌아가더라도, 각 스레드가 커서 방식으로 데이터를 조금씩 가져오기 때문에 OOM 위험을 낮출 수 있음.

## ItemWriter 최적화: Chunk 기반 Bulk Operations 적용

ItemWriter 자체가 직접적인 OOM의 주범이 되는 경우는 드물지만, 쓰기 속도가 읽기 속도를 따라가지 못할 경우, 처리 대기 중인 객체들이 메모리(JVM Heap)에 머무는 시간이 길어져 간접적으로 OOM을 유발하는 원인이 될 수 있어요.

이러한 병목을 방지하기 위해 `Bulk Operations`를 적용해보았어요. Spring Batch의 Chunk 구조를 활용하면, 설정한 청크 사이즈만큼 데이터가 모였을 때 단 한 번의 네트워크 통신으로 일괄 insert를 수행해요. 이는 개별 insert 방식보다 네트워크 I/O 비용을 획기적으로 낮추고 쓰기 속도를 극대화하여 메모리 회수 주기를 앞당길 수 있어요.

```kotlin
/**
* Writer: Spring Batch가 모아준 1,000개를 한 번에 Bulk Insert
*/
@Bean
@StepScope
fun sampleWriter(): ItemWriter<StatisticsResult> {
    return ItemWriter { chunk ->
        if (chunk.isEmpty) return@ItemWriter

        val bulkOps = mongoTemplate.bulkOps(
            BulkOperations.BulkMode.UNORDERED,
            properties.channelType.statisticsCollectionName()
        )

        // chunk.items에 이미 1,000개의 데이터가 들어있음
        bulkOps.insert(chunk.items)
        bulkOps.execute()
        
        // 별도의 list.clear()를 호출하지 않아도 
        // 메서드가 종료되면 chunk 객체는 GC 대상이 됨
    }
}
```


 

---

### 4. 1,500만 건 처리를 위한 최종 체크리스트

1. **메모리 격리:** 각 Slave Step이 `@StepScope`로 설정되어 있는지 확인하세요. 그래야 각 스레드가 자신만의 `Reader` 객체를 가져 메모리 혼선이 없습니다.
2. **인덱스 최적화:** `Partitioner`에서 사용하는 날짜 필드(`startDate`, `endDate`)와 Reader의 정렬 필드에 반드시 **복합 인덱스**가 있어야 합니다. 인덱스가 없으면 Reader가 데이터를 찾는 속도가 느려져 배치가 타임아웃될 수 있습니다.
3. **No-State 처리:** 가능하다면 `ItemProcessor`에서 엔티티의 상태를 변경하기보다, 새로운 DTO를 만들어 `ItemWriter`로 넘기는 방식이 GC(Garbage Collection) 효율에 더 좋습니다.
4. **Bulk Write 활성화:** `MongoItemWriter`를 사용하면 내부적으로 `Bulk Operations`를 수행하므로, 1,000개씩 모아서 한 번에 insert/update를 처리하여 네트워크 I/O를 최적화할 수 있습니다.

**결론적으로,** `MongoCursorItemReader`를 사용하고 **청크 사이즈를 1,000**으로 설정한 뒤, **JVM 힙 메모리를 8GB 이상** 할당하신다면 6개 스레드로 1,500만 건을 안전하게 처리할 수 있습니다.