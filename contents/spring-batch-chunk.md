# Spring Batch Chunk

스프링 배치를 통해 대량의 데이터를 여러 번 처리해야 했었는데요. 그 과정에서 발생했던 **OOM(Out Of Memory)** 문제를 어떤 방식으로 풀어냈는지 공유하고자 합니다.

## 문제의 시작

원장 통계 데이터 구조를 변경하기 위해, 17년도부터 25년도까지 약 9년치 원장 데이터에 대한 새로운 통계를 생성하는 작업을 진행하게 되었어요. 

트래픽이 가장 많은 채널에 대한 하루치 원장 데이터만 해도 대략 250만 건이 되었는데, 한달치면 약 7,500만 건, 일년치면...

방대한 양의 운영 데이터에 변경이 일어나는 작업이다보니, 작업 중간중간 데이터 정합성을 확인하며 진행하기 위해 한달 단위로 나눠서 작업을 진행하는 전략을 세우게 되었어요.

한달 단위로 나누더라도 한 번의 배치 작업에 7,500만 건의 원장 데이터가 사용되게 되므로, 배치 안에서도 하루씩 분할해서 처리가 되도록 구현하는 전략을 추가하게 되었어요.

## Partitioner 사용하기

> [partitioning](https://docs.spring.io/spring-batch/reference/scalability.html#partitioning)
>
> 일반적인 배치는 데이터를 순차적으로 처리하지만, `Partitioner`를 사용하면 하나의 마스터 스텝이 데이터를 여러 개의 범위로 나누고, 이를 여러 개의 슬레이브 스텝이 병렬로 처리

한 달치 데이터를 처리하는데 하루치씩 분할해서 병렬로 처리하기 위해 `Partitioner`를 사용하게 되었어요.

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

