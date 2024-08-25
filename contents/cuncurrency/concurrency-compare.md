# 📊 동시성 제어 방식 비교

**Series**
> [Java Concurrency Control](https://data-make.tistory.com/790)
>
> [Database Concurrency Control](https://data-make.tistory.com/791)
>
> [Redis Concurrency Control](https://data-make.tistory.com/792)
>
> [Kafka Concurrency Control](https://data-make.tistory.com/793)
>
> [Compare Concurrency Control](https://data-make.tistory.com/794)

⚠️ 
> 로컬에서 테스트한 결과이고,
> 
> 서버 환경과 여러 요인들에 의해 결과가 달라질 수 있고, 정확하지 않을 수 있습니다.

## Case 01

**한정수량 : `50,000`**

- Total User : `296`
- Processes : `8`
- Threads : `37`

(DB Named 방식은 제외)

|                     | Java Async | Java Sync | DB Pessimistic | DB Optimistic   | DB Named                    | Redis Incr     | Redis Lettuce   | Redis Redisson  | Kafka + Redis  |
| ------------------- | ---------- | --------- | -------------- | --------------- | --------------------------- | -------------- | --------------- | --------------- | -------------- |
| 한정수량            | 50,000     | 50,000    | 50,000         | 50,000          | 10,000                      | 50,000         | 50,000          | 50,000          | 50,000         |
| Total User          | 296        | 296       | 296            | 296             | 40                          | 296            | 296             | 296             | 296            |
| Processes / Threads             | 8 / 37     | 8 / 37    | 8 / 37         | 8 / 37          | 2 / 20                      | 8 / 37         | 8 / 37          | 8 / 37          | 8 / 37         |
| Duration            | 3 min.     | 3 min.    | 3 min.         | 3 min.          | 3 min. (01:57)              | 3 min. (01:40) | 3 min.          | 3 min.          | 3 min. (01:50) |
| TPS                 | 975.5      | 567.0     | 283.6          | 218.3           | 87.5                        | 519.3          | 177.9           | 163.2           | 470.6          |
| Peak TPS            | 1,164      | 667       | 444            | 248             | 181                         | 2,082          | 218             | 190             | 1,534          |
| Mean Test Time (ms) | 301.43     | 488.02    | 750.04         | 1,350.66        | 244.13                      | 189.74         | 1,643.21        | 1,802.55        | 252.14         |
| Executed Tests      | 171,933    | 112,696   | 79,791         | 38,491          | 22,668                      | 82,720         | 31,380          | 28,816          | 82,697         |
| Successful Tests    | 171,933    | 99,971    | 50,000         | 38,491          | 10,000                      | 50,000         | 31,380          | 28,816          | 50,000         |
| Errors              | 0          | 12,725    | 29,791         | 0               | 12,668                      | 32,720         | 0               | 0               | 32,697         |
| Concurrency Control | Fail       | Fail      | Success        | Low performance | Dead Lock + Low performance | Success        | Low performance | Low performance | Success        |

<br/>.<br/>

시간 내에 모든 트래픽을 성공적으로 처리한 방식

- **DB Pessimistic**
- **Redis Incr**
- **Kafka + Redis**

<br/>.<br/>

일부 성공을 하긴 하였지만, 트래픽을 버티지 못하고 성능 문제가 발생한 방식 (성능 문제가 발생한 개인적인 생각도 담아보았다.)

- **DB Optimistic →** 충돌 처리 비용 *(충돌 시 해당 트랜잭션 재시도)* 으로 인한 성능 저하
- **DB Named →** 커넥션 풀 부족으로 인한 DeadLock 발생 및 성능 저하
- **Redis Lettuce → Spin Lock 방식** *(반복적으로 확인하면서 락 획득을 시도)* **으로 인한 CPU 부하**
- **Redis Redisson → 내부적으로 구현된 재시도 로직** *(특정 간격으로 재시도)* **에서 발생하는 부하**

<br/>.<br/>

로컬 PC에서 버틸 수 있는 트래픽이 아니라서 결과가 현실과는 다를 수도 있지만,

처음 결과를 보았을 때 빠른 성능을 자랑하는 **Redis 의 Lettuce, Redisson** 방식이 성능 저하 이슈가 있을 줄을 몰랐다.

락 획득을 위한 재시도 로직으로 인해서 발생하는 성능 저하일 수도 있을 것이라는 생각이 든다.
