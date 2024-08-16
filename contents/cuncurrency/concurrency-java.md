# 🎯 Java Concurrency Control

**Race Condition**

> 경쟁상태는 두 개 이상의 스레드가 공유 데이터에 액세스 할 수 있고, 동시에 변경을 하려고 할 때 발생하는 문제
- Race Condition 으로 동시에 들어오는 요청들이 갱신 전 값을 읽고, 수정하면서
- 실제 갱신이 누락되는 현상이 발생

Series
> [Java Concurrency Control](https://data-make.tistory.com/790)
>
> [Database Concurrency Control]()
>
> [Redis Concurrency Control]()
>
> [Kafka Concurrency Control]()

## **🏹 Java Asynchronous**

> 동시에 들어오는 요청들이 갱신 전 값을 읽고, 수정하면서 실제 갱신이 누락
> 

```bash
before: 100, after: 99 // 갱신 전 값을 조회
before: 100, after: 99
before: 100, after: 99
...
before: 95, after: 94
before: 98, after: 97
before: 100, after: 99
before: 88, after: 87
before: 92, after: 91
before: 99, after: 98
```

[java asynchronous](https://github.com/jihunparkme/Study-project-spring-java/commit/ce56981975dabb74deb364cf383dff6d9b3601f1)

**🛠️ 성능 테스트.**

- 한정 수량: 50,000
- User: 296
- Processes: 8
- Threads: 37
- Duration: 3 min

> 결과를 보면 한정 수량은 50,000 으로 제한했지만 121,933 건이 초과된 것을 볼 수 있다.
> 
> 동시성 처리가 되고 있지 않다보니 모든 요청이 성공하는 것을 볼 수 있다.

![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/java-async-performance.png?raw=true 'Result')

## **🏹 Java Synchronized**

> synchronized 를 메서드 선언부에 붙여주면 해당 메서드는 한 개의 스레드만 접근 가능

한 개의 스레드만 접근 가능하도록 제한할 수 있지만 큰 문제가 존재

- (1) `@Transactional` 사용 시 프록시 방식의 AOP가 적용되어 **트랜잭션 종료 전에 다른 스레드가 갱신된 전 값을 읽게 되면** 결국 이전과 동일한 문제가 발생
- (2) 하나의 프로세스 안에서만 보장 → 서버가 2대 이상일 경우 **결국 여러 스레드에서 동시에 데이터에 접근**

[java synchronous](https://github.com/jihunparkme/Study-project-spring-java/commit/04d5a527e1f8908bf677cb86f0c27275a7d0916f)

**🛠️  성능 테스트.**

- 한정 수량: 50,000
- User: 296
- Processes: 8
- Threads: 37
- Duration: 3 min

> 결과를 보면 한정 수량은 50,000 으로 제한했지만 49,971 건이 초과된 것을 볼 수 있다.
>
> 다수의 프로세스에서 요청이 올 경우 결국 여러 스레드에서 동시에 데이터에 접근할 수 있다는 것을 볼 수 있다.
>
> 02:48 이후에는 모든 수량이 0으로 조회되어, 재고 부족 오류가 발생하게 된다.

![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/java-sync-performance.png?raw=true 'Result')
