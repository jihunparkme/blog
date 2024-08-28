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

![Result](https://github.com/jihunparkme/blog/blob/0f52180eb07cdddf77a1c351ad06600ae7d5cad7/img/concurrency/compare-1.png?raw=true 'Result')

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

## Case 02

**한정수량 : `1,000`**

- Total User : `99`
- Processes : `3`
- Threads : `33`

![Result](https://github.com/jihunparkme/blog/blob/0f52180eb07cdddf77a1c351ad06600ae7d5cad7/img/concurrency/compare-2.png?raw=true 'Result')

<br/>.<br/>

시간 내에 모든 트래픽을 성공적으로 처리한 방식

- **DB Pessimistic**
- **DB Optimistic**
- **Redis Incr**
- **Redis Lettuce**
- **Redis Redisson**
- **Kafka + Redis**

<br/>.<br/>

일부 성공을 하긴 하였지만, 트래픽을 버티지 못하고 성능 문제가 발생한 방식

- **DB Named →** 커넥션 풀 부족으로 인한 DeadLock 발생 및 성능 저하

<br/>.<br/>

한정수량이 50,000 건일 경우에 비해 대부분의 방식이 성공적으로 모든 트래픽을 처리한 것을 볼 수 있다.

위 결과들을 통해 지극히 개인적인 생각으로 각 방식에 대한 결론을 내보려고 한다.

