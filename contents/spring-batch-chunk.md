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

```kotlin
class SamplePartitioner(
    private val startDate: LocalDate,
    private val endDate: LocalDate,
    private val timestamp: Long,
) : Partitioner {
    override fun partition(gridSize: Int): Map<String, ExecutionContext> {
        val partitions: MutableMap<String, ExecutionContext> = mutableMapOf<String, ExecutionContext>()
        val days: Long = ChronoUnit.DAYS.between(startDate, endDate) + 1 // 총 일자 계산
        repeat(days.toInt()) { // 하루치씩 반복
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

여기서 하루치씩 데이터는 k8s pods 기본 리소스로 충분히 가능했습니다.
