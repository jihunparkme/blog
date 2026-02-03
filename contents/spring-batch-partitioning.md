# 수억 건의 데이터, 맛있게 잘라 먹는 방법 (with. Partitioning)

스프링 배치를 이용해 대량의 데이터를 처리하다 보면 누구나 한 번쯤 '메모리'라는 벽에 부딪히곤 하죠. 저 역시 방대한 양의 원장 데이터를 다루며 OOM(Out Of Memory) 문제를 경험했는데요. 이 문제를 어떻게 Spring Batch의 기능들로 해결했는지 그 과정을 공유하고자 해요.

## 전략 세우기

원장 통계 데이터 구조가 변경되어 원장 데이터를 새롭게 집계해야 하는 과제가 주어졌어요.   
트래픽이 집중되는 채널의 경우, 하루치 원장 데이터만 약 **250만 건**에 달했어요. 이를 계산해 보면, 한 달 **7,500만 건**, 일 년이면 무려 **수억건 분량**의 데이터를 처리해야 했어요.

방대한 양의 운영 데이터를 다뤄야 하는 만큼, **데이터 정합성**과 **속도**를 위해 크게 두 번의 분할을 통한 단계별 전략을 세우게 되었어요.
- 1차 분할: 운영에 새롭게 생성되는 통계 데이터의 정합성 확인을 위해 **전체 기간을 '한 달' 단위로 나누어 배치 실행**
- 2차 분할: 한 달치 데이터(약 7,500만 건) 작업에 대한 부하를 줄이기 위해 **배치 내부에서 다시 '하루' 단위로 분할(Partitioning)**하여 처리

그럼 이제 함께 방대한 양의 데이터를 맛있게 먹기 위해 커팅하러 가볼까요~? 🎂🍰

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

## Partitioning

<figure><img src="https://raw.githubusercontent.com/jihunparkme/blog/refs/heads/main/img/spring-batch/partitioning-overview.png" alt=""><figcaption></figcaption></figure>

**Partitioning** 방식은 `Manager(Master) Step`이 전체 데이터를 **작은 조각**(Partition)으로 나누고, 이 조각들을 각 스레드에서 `Worker(Slave) Step`들이 병렬로 처리하는 구조에요.

각 `Worker Step`은 독립적인 **ItemReader**, **ItemProcessor**, **ItemWriter**를 가지고 동작하므로, 서로의 작업에 영향을 주지 않고 효율적으로 대량의 데이터를 처리할 수 있어요. 이를 가능하게 하는 두 가지 핵심 인터페이스는 `Partitioner`, `PartitionHandler`이랍니다.

> ⚠️ 참고. 
>
>파티셔닝은 각 파티션을 독립적인 스레드가 동시에 실행하는 병렬 처리 구조이기 때문에 실행 순서를 보장할 수는 없지만, 대용량 데이터를 격리된 환경에서 빠른 속도로 처리할 수 있는 확장성을 제공합니다.
>
> 이러한 특성 때문에 1년 단위로 크게 분할하여 병렬성을 극대화할 수도 있었지만, 저는 '한 달' 단위로 더 잘게 나누는 전략을 선택했는데요. 실제 서비스에서 사용 중인 운영 데이터인 만큼, 작업 중간중간 데이터 정합성을 꼼꼼히 검증하며 리스크를 최소화하는 것이 속도보다 더 중요하다고 판단했기 때문이었어요.

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
        val daysBetween = ChronoUnit.DAYS.between(
            startDate,
            endDate.plusDays(1)
        )

        return (0 until daysBetween).associate { i ->
            val targetDate = startDate.plusDays(i)
            val context = ExecutionContext().apply {
                putString("targetDate", targetDate.toString())
            }
            "partition_$i" to context
        }
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
fun generateStatisticsJob(managerStep: Step): Job =
    JobBuilder("generateStatisticsJob", jobRepository)
        .incrementer(RunIdIncrementer())
        .start(managerStep)
        .build()

@Bean
fun managerStep(
    partitionHandler: PartitionHandler,
    partitioner: Partitioner,
): Step = StepBuilder("managerStep", jobRepository)
    .partitioner("workerStep", partitioner) // 어떻게 데이터를 쪼갤지
    .partitionHandler(partitionHandler) // 어떤 방식으로 실행할지
    .build()

@Bean
fun partitionHandler(workerStep: Step): PartitionHandler =
    TaskExecutorPartitionHandler().apply {
        setTaskExecutor(batchTaskExecutor()) // 사용할 스레드 풀
        step = workerStep // 실행할 Slave 스텝
        gridSize = GRID_SIZE // 병렬 스레드 개수
    }

@Bean
@StepScope
fun batchTaskExecutor(): TaskExecutor =
    ThreadPoolTaskExecutor().apply {
        corePoolSize = GRID_SIZE
        maxPoolSize = 10
        setThreadNamePrefix("batch-thread-")
        initialize()
    }

@Bean
@JobScope
fun partitioner(
    @Value("#{jobParameters['startDate']}") startDate: String,
    @Value("#{jobParameters['endDate']}") endDate: String
): Partitioner {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    return DateRangePartitioner(
        startDate = LocalDate.parse(startDate, formatter),
        endDate = LocalDate.parse(endDate, formatter)
    )
}

@Bean
fun workerStep(
    reader: ItemReader<String>,
    writer: ItemWriter<String>
): Step = StepBuilder("workerStep", jobRepository)
    .chunk<String, String>(CHUNK_SIZE, transactionManager)
    .reader(reader)
    .writer(writer)
    .build()
```

> 🔄 Partitioner, PartitionHandler 두 인터페이스가 협력하여 데이터를 처리하는 과정
>
> 1. **Manager Step 가동**: 배치가 시작되면 관리자 역할을 하는 `Manager Step`이 실행
>
> 2. **Partitioner의 데이터 분할**: `Partitioner`가 호출되어 전체 데이터를 n개로 나눈 **파티션 정보(ExecutionContext)** 를 생성
> 
> 3. **PartitionHandler의 작업 분배**: `PartitionHandler`가 이 정보를 토대로 `TaskExecutor`에 작업을 할당
>
> 4. **Worker Step의 독립 실행**: 각 스레드에서 `Worker Step`이 할당받은 파티션 정보를 사용해 실제 로직(Reader-Processor-Writer)을 수행
>
> 5. **상태 수집 및 종료**: 모든 `Worker Step`이 완료되면 `PartitionHandler`가 결과를 취합하여 `Master Step`에 보고하고 작업을 마무리

Spring Batch의 Partitioning 기능 덕분에 대용량 데이터를 하루 단위로 분할하여 병렬로 처리하는 구조를 설계할 수 있었어요. 하지만 기쁨도 잠시, 예상치 못한(어쩌면 예견되었던) 난관에 봉착하고 말았습니다.

분명 데이터를 날짜별로 쪼갰지만, **'병렬 처리'**라는 양날의 검이 문제를 일으킨 것이었죠.

🚨 1,500만 건의 데이터가 메모리를 점령하다<br/>
6개의 스레드가 각자의 파티션을 맡아 동시에 ItemReader를 가동하면서 메모리 사용량이 치솟기 시작했어요.
- 스레드당 데이터: 약 250만 건 (하루치)
- 병렬 실행 스레드: 6개
- 메모리 적재 시도: 250만 × 6 = 1,500만 건

결국 1,500만 건에 달하는 방대한 데이터가 한꺼번에 메모리에 적재되려 했고, 서버는 비명을 지르며 결국 **OOM(Out Of Memory)**의 늪에 빠지고 말았어요. 🥲

단순히 '데이터를 쪼개고 병렬로 돌린다'는 전략만으로는 부족했어요. 한정된 메모리 자원이라는 병목 구간을 통과하기 위해서는, 데이터를 한꺼번에 조회하는 것이 아니라 일정한 크기로 끊어서 효율적으로 흘려보내는 최적화가 필요했어요.

## ItemReader 최적화: Cursor 기반 스트리밍

하루치 250만 건의 데이터조차 결코 적은 양이 아니었기에, 데이터를 한꺼번에 로드하는 방식에서 벗어나 리소스를 효율적으로 사용하는 `ItemReader`로의 전환이 필요해졌어요.

이미 `Partitioner`를 통해 날짜별로 작업 범위는 격리해 둔 상태였고, 이제 남은 과제는 각 스레드(Worker Step) 내부에서 메모리 점유율을 최소화하며 데이터를 읽어오는 것이었어요.

MongoDB 환경에서 선택할 수 있는 선택지는 크게 두 가지가 있답니다.

1). `MongoPagingItemReader`
- 방식: 페이지 단위로 데이터를 끊어서 조회.
- 단점: 대량 데이터에서 페이지 번호가 뒤로 갈수록 이전 결과를 건너뛰는 오버헤드가 발생하며, 여전히 한 페이지 분량의 데이터를 메모리에 적재해야 함.

2). `MongoCursorItemReader`
- 방식: DB 서버와 커서를 유지하며 스트리밍 방식으로 데이터를 한 건씩 호출.
- 장점: 대량의 데이터를 메모리에 쌓아두지 않고, 읽는 즉시 처리하고 흘려보낼 수 있어 메모리 효율이 압도적.

최종적으로 제한된 메모리 환경에서 1,500만 건 이상의 데이터를 안정적으로 처리하기 위해 **MongoCursorItemReader**를 채택하게 되었어요.

```kotlin
@Bean
@StepScope
fun reader(
    @Value("#{stepExecutionContext['startDate']}") startDate: String,
    @Value("#{stepExecutionContext['endDate']}") endDate: String
): MongoCursorItemReader<PaymentLedger> {
    val query = ...

    return MongoCursorItemReaderBuilder<PaymentLedger>()
        .name("reader")
        .template(mongoTemplate)
        .collection(LEDGER_COLLECTION)
        .targetType(PaymentLedger::class.java)
        .query(query)
        .sorts(mapOf("orderNumber" to Sort.Direction.ASC))
        .build()
}
```

> 📚 **MongoCursorItemReader**
> 
> The `MongoCursorItemReader` is an ItemReader that reads documents from MongoDB by using a streaming technique. Spring Batch provides a MongoCursorItemReaderBuilder to construct an instance of the MongoCursorItemReader.
>
> *by. [Spring Batch Documentation](https://docs.spring.io/spring-batch/reference/readers-and-writers/item-reader-writer-implementations.html#databaseReaders)*

Cursor 방식을 적용하면서 메모리 효율성과 안정성을 모두 얻을 수 있었어요.
- **메모리 효율성**: 페이징 방식은 다음 페이지를 부를 때마다 이전 데이터만큼 Skip해야 하므로 뒤로 갈수록 느려질 수 있지만, 커서는 스트리밍 방식이라 메모리 사용량이 일정하게 유지.
- **안정성**: 병렬로 Slave Step이 돌아가더라도, 각 스레드가 커서 방식으로 데이터를 조금씩 가져오기 때문에 OOM 위험을 낮출 수 있음.

## ItemWriter 최적화: Chunk 기반 Bulk Operations

흔히 OOM의 주범으로 ItemReader를 지목하지만, 사실 ItemWriter 역시 간접적인 원인을 제공하곤 해요. 쓰기 속도가 읽기 속도를 따라가지 못하면, 처리된 객체들이 DB에 저장되기 위해 대기하며 메모리에 머무는 시간이 길어지기 때문이죠.

이러한 병목 현상을 해결하고 GC 주기를 앞당기기 위해, **Bulk Operations**를 적용하게 되었어요.

**왜 Bulk Operations인가?**
- Spring Batch의 Chunk 구조를 활용하면, 설정한 청크 사이즈만큼 데이터가 모였을 때 단 한 번의 네트워크 통신으로 일괄 Insert를 수행해요.
- 1,000번의 개별 Insert를 1번의 Bulk Insert로 줄여 I/O 오버헤드를 낮추기 때문에 네트워크 비용이 절감돼요.
- 쓰기 속도가 빨라지면 메모리에 머물던 객체들이 빠르게 비워지며, 전체적인 메모리 사용량이 안정화돼요

```kotlin
@Bean
fun writer(): ItemWriter<Statistics> = ItemWriter { items ->
    if (items.isEmpty) return@ItemWriter

    val bulkOps = mongoTemplate.bulkOps(
        BulkOperations.BulkMode.UNORDERED,
        Statistics::class.java,
        LEDGER_BACKUP_COLLECTION
    )

    bulkOps.insert(items.toList())
    bulkOps.execute()
}
```

> 📚 **BulkOperations**
>
> Bulk operations for insert/update/remove actions on a collection. ...
>
> This interface defines a fluent API to add multiple single operations or list of similar operations in sequence which can then eventually be executed by calling execute().
>
> by. [Interface BulkOperations](https://docs.spring.io/spring-data/mongodb/docs/current/api/org/springframework/data/mongodb/core/BulkOperations.html)

Spring Batch의 Chunk 지향 프로세싱 모델 덕분에, write() 메서드가 종료되면 해당 Chunk 리스트는 자연스럽게 스코프를 벗어나므로 별도의 list.clear()를 호출하지 않아도 해당 객체들은 즉시 GC 대상이 되어 메모리에서 해제돼요.

`mongoTemplate.bulkOps`를 사용할 때 UNORDERED 모드를 채택한 이유는, 재처리 대상인 각 데이터는 서로 독립적이며 저장 순서가 결과에 영향을 주지 않았기대문이에요.<br/>
순서를 보장할 필요가 없으므로 MongoDB 내부에서 병렬 쓰기 최적화가 가능해지며, 특정 작업이 실패하더라도 나머지 작업을 멈추지 않고 계속 진행할 수 있어 대용량 처리에 매우 유리해요.

## 성능 비교

아래 그래프는 데이터 규모가 커질수록 **기본적인 배치 방식(파란색)** 과 **최적화된 방식(Partitioning + CursorReader + Bulk Operations, 빨간색)** 간의 처리 시간 차이를 보여주고 있어요.
- 두 방식 모두 chunk size는 1000으로 테스트를 진행했어요.
- 그래프에 나타난 숫자는 보기 쉽게 10진법 분으로 나타낸 숫자에요.

<figure><img src="https://raw.githubusercontent.com/jihunparkme/blog/refs/heads/main/img/spring-batch/partitioning-compare.png" alt=""><figcaption></figcaption></figure>

**실제 분 형식의 데이터**

|데이터 양|Basic|Partitioning|
|---|:---:|:---:|
|10,000|3초|2초|
|100,000|33초|15초|
|1,000,000|11분 22초|1분 53초|
|5,000,000|1시간 37분 25초|8분 55초|

확장성(Scalability) 차이:
- 10,000건 정도의 소량 데이터에서는 두 방식의 차이가 미미하지만, 데이터가 100만 건, 500만 건으로 늘어날수록 기본 방식은 소요 시간이 기하급수적으로 증가하지만, 최적화 방식은 훨씬 완만한 증가 폭을 유지하는 것을 볼 수 있어요.

성능 격차 심화:
- 5,000,000건 처리 시 기본 방식(87.42분) 대비 최적화 방식(8.92분)은 약 10배에 가까운 속도 향상을 보이고 있어요.

최적화 전략의 효과:
- Partitioning: 데이터를 쪼개어 여러 스레드가 병렬로 처리함으로써 전체 작업 시간을 단축했어요.
- CursorReader: 스트리밍 방식으로 데이터를 읽어 메모리 부하를 줄이고, 대량 데이터에서도 일정한 읽기 속도를 유지해요.
- Bulk Operations: 일괄 쓰기를 통해 네트워크 I/O 오버헤드를 최소화하고, 처리된 객체를 빠르게 메모리에서 해제하여 성능을 극대화했어요.

결론적으로, 데이터 규모가 커질수록 단순 병렬 처리를 넘어 메모리 효율(Cursor)과 쓰기 최적화(Bulk)가 결합된 Partitioning 전략이 필수적임을 알 수 있었어요.

배치 성능에 관심이 많으시다면 [Batch Performance를 고려한 최선의 Reader](https://tech.kakaopay.com/post/ifkakao2022-batch-performance-read/#%EC%83%88%EB%A1%9C%EC%9A%B4-cursoritemreader), [Batch Performance를 고려한 최선의 Aggregation](https://tech.kakaopay.com/post/ifkakao2022-batch-performance-aggregation/#itemreader), [Spring Batch 애플리케이션 성능 향상을 위한 주요 팁](https://tech.kakaopay.com/post/spring-batch-performance/#in-update-%EC%84%B1%EB%8A%A5-%EC%B8%A1%EC%A0%95) 글도 추천드립니다.

## 마치며

처음 대용량 데이터를 처리해야 했을 때, 어느 정도 OOM을 마주할 것이라는 기대(?)는 했지만, 그 만남이 이렇게까지 길어질 줄은 몰랐습니다. 하지만 문제를 하나씩 분해하고 Spring Batch가 제공하는 도구들을 활용하면서, '메모리'라는 한정된 자원 안에서 거대한 데이터를 안전하게 통과시키는 법을 배울 수 있었어요.

이번 경험의 핵심 전략을 다시 한번 정리하자면 다음과 같습니다.
- 쪼개기(Partitioning): 거대한 덩어리를 처리하기 위해 덩어리를 조각내어 관리 가능한 단위로 분할
- 흘려보내기(CursorReader): 데이터를 쌓아두지 않고 파이프라인처럼 흐르게 하여 메모리 부하를 원천 차단
- 몰아치기(Bulk Operations): 효율적인 일괄 처리를 통해 데이터가 시스템에 머무는 시간을 최소화

단순히 병렬 스레드 수만 늘린다고 해서 성능이 선형적으로 증가하지는 않습니다. 오히려 제어되지 않은 병렬성은 OOM이라는 부메랑이 되어 돌아오기도 하죠. 중요한 것은 데이터의 유입부터 소멸까지의 전체 생명 주기를 시스템 리소스에 맞춰 세밀하게 설계하는 것임을 깨닫는 값진 계기가 되었습니다.

저와 비슷하게 대용량 데이터 처리라는 과제를 안고 고민하는 분들에게 이 글이 작은 실마리가 되기를 바랍니다. 기술적인 정답은 환경마다 다르겠지만, **'리소스를 어떻게 효율적으로 사용할 것인가'**에 대한 고민은 언제나 좋은 해답의 시작점이 될 것입니다.
