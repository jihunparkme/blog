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

## Compare

### **Java**

- **Java Async**
    - **동시성 이슈 사례의 표본**
- **Java Sync**
    - 서버가 2대 이상일 경우 **동시성 이슈 발생**

> 실무에서 동시성 처리에 `절대 사용할 수 없는` 방식.

### **Database**

- **Databse Pessimistic Lock**
    - 충돌이 빈번하게 발생할 수 있는 상황이거나
    - 데이터베이스에 부하가 올 정도의 동시 트래픽을 견뎌야 하는 상황이 아니라면 적합하다고 생각
- **Databse Optimistic Lock**
    - 적당한 트래픽에서는 사용할만 하지만,
    - Ramp-Up 테스트를 통해 얼마큼 버틸 수 있을지 검증이 필요
- **Databse Named Lock**
    - 커넥션 풀 부족 현상으로 실무에서 적용은 어려워 보임

> 개인적으로 데이터베이스에 락을 적용하는 방식은 
> 
> 같은 데이터베이스를 사용하는 다른 서비스에 성능 이슈를 전파할 수 있다고 생각해서
> 
> `대량의 트래픽`을 견뎌야 하는 상황이라면 `Redis`, `Kafka` 를 활용하고
> 
> `소량의 트래픽`만 견뎌도 되는 상황이라면 `Pessimistic Lock` 을 적용하는게 적절하다고 생각

### Redis

- **Redis Incr**
    - 가장 좋은 성능을 보유
    - 메모리 기반이고 서버가 재시작되거나 장애 발생 시 데이터 유실 가능성이 존재하는 단점
- **Redis Lettuce / Redisson**
    - 재시도 로직으로 인한 부하로 대량의 트래픽에서 좋은 성능을 낼 수는 없을 것 같음
    - 분산락의 한계도 존재 → [레디스가 제공하는 분산락(RedLock)의 특징과 한계](https://mangkyu.tistory.com/311)
    - Ramp-Up 테스트를 통해 얼마큼의 트래픽에서 성능을 낼 수 있을지 검증이 필요

> `Redis Incr` 방식은 데이터가 유실되는 상황에 대한 대비만 잘 되어있다면 가장 좋은 방식이라고 생각 

### Kafka

- **Kafka + Redis**
    - 임계값 제어와 비즈니스 로직 처리의 역할을 분리
    - 부하가 분산되므로 대량의 트래픽에 적합한 방식이라고 생각

> `Redis Inc` 방식을 활용하지만, 대부분의 비즈니스 로직은 처리 시간이 다소 소요되다 보니
> 
> 임계값 제어는 Redis에서 처리하고, 비즈니스 로직은 Kafka Consumer가 처리하면서
> 
> 부하를 분산시켜 성능을 향상시킬 수 있는 가장 좋은 방법이라고 생각

# Reference.

- [Concurrency issues](https://jihunparkme.gitbook.io/docs/lecture/study/concurrency-issues)
- [First Come First Served](https://jihunparkme.gitbook.io/docs/lecture/study/first-come-first-served-event)
- [Performance Test](https://jihunparkme.gitbook.io/docs/lecture/study/performance-test)