# 🎯 DataBase Concurrency Controll

**Race Condition**

> 경쟁상태는 두 개 이상의 스레드가 공유 데이터에 액세스 할 수 있고, 동시에 변경을 하려고 할 때 발생하는 문제
- Race Condition 으로 동시에 들어오는 요청들이 갱신 전 값을 읽고, 수정하면서
- 실제 갱신이 누락되는 현상이 발생

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

## Database Lock

데이터베이스 락의 종류

[데이터베이스 락(Lock)의 종류와 역할](https://velog.io/@koo8624/Database-데이터베이스-락Lock의-종류와-역할)

- 공유 락(`S`hared Lock, Read Lock)
    - 데이터를 변경하지 않는 **읽기 명령에 대해 주어지는 락**
    - 공유 락끼리는 동시에 접근이 가능
- 베타 락(E`x`clusive Lock, Write Lock)
    - 데이터에 변경을 가하는 **쓰기 명령들에 대해 주어지는 락**
    - 다른 세션이 해당 자원에 접근(ex. SELECT, INSERT ..) 하는 것을 방지
    - 멀티 쓰레드 환경에서, 임계 영역을 안전하게 관리하기 위해 활용되는 뮤텍스와 유사(트랜잭션 동안 유지)
- 업데이트 락(Update Lock)
    - 데이터를 수정하기 위해 베타 락을 걸기 전, **데드락을 방지하기 위해 사용되는 락**
    - UPDATE 쿼리의 필터(WHERE)가 실행되는 과정에서 적용
    - 참고. 서로 다른 트랜잭션에서 동일한 자원에 대해 읽기 쿼리 이후, 업데이트 쿼리를 적용하는 경우 conversion deadlock이 발생하는데, 이를 막기 위해 일부 `SELECT` 퀴리에서도 업데이트 락을 적용(WITH(UPDLOCK))하기도 함
- 내재 락(Intent Lock)
    - 사용자가 요청한 범위에 대한 락(ex. 테이블 락)을 걸 수 있는지 여부를 빠르게 파악하기 위해 사용되는 락
    - 공유 락과 베타 락 앞에 `I` 기호를 붙인 `IS`, `IX`, `SIX` 등이 있음

## **🏹 DataBase Pessimistic Lock**

> 데이터(`Row` or `Table`)에 Lock 을 걸어서 정합성을 맞추는 방법

**정의.**

- 기본적으로 배타 락(E`x`clusive Lock)을 사용
- 트랜잭션이 완료될 때까지 `락을 유지`
- `충돌 가능성이 높은 환경`에서 유용
    - 데이터가 자주 변경되거나 동시에 여러 사용자가 접근하는 환경에서 데이터 무결성을 보장

**동작 방식.**

1. **락 획득**: 트랜잭션이 데이터에 접근할 때 해당 데이터에 대해 락을 획득
2. **락 유지**: 트랜잭션이 완료될 때까지 락을 유지하여 다른 트랜잭션이 해당 데이터를 읽거나 수정하지 못하게 함
3. **락 해제**: 트랜잭션이 커밋되거나 롤백되면 락을 해제하여 다른 트랜잭션이 접근할 수 있도록 함

**장점.**

- `데이터 충돌 방지` → 충돌이 빈번하게 일어난다면 Optimistic Lock 보다 성능이 좋을 수 있음
- `데이터 무결성 보장` → 데이터 충돌 전 트랜잭션의 접근을 차단

**단점.**

- `성능 저하` → 대기하는 트랜잭션이 많아지면 시스템의 동시성 처리 능력 저하
- `Deadlock` → 여러 트랜잭션이 서로 다른 리소스 작업이 끝나기만을 대기하는 상태가 발생하여 시스템 성능을 저하
- `자원 낭비` → 충돌이 드물게 발생하는 경우에도 락을 사용하여 불필요한 자원 사용
- `복잡성 증가` → 비관적 락 관리 및 교착 상태 해결을 위해 추가적인 복잡성이 발생

**사례.**

- **데이터 충돌 가능성이 높은 경우** → 동일한 상품에 대한 주문이 동시에 처리되는 경우
- **데이터 무결성이 매우 중요한 경우 →** 금융 거래 시스템이나 회계 소프트웨어
- **긴 트랜잭션 처리 →** 트랜잭션이 복잡하고 긴 시간이 소요되는 경우

.

[Database Pessimistic Lock example](https://github.com/jihunparkme/Study-project-spring-java/commit/8b8d4681224a18ac2d1cd0189a644c85087c5a71)

.

**🛠️  성능 테스트.**
- 한정 수량: 50,000
- User: 296
- Processes: 8
- Threads: 37
- Duration: 3 min

> 결과를 보면 정확하게 50,000 건만 성공으로 처리된 것을 볼 수 있다.
>
> 02:10 이후에는 모든 수량이 0으로 조회되어, 재고 부족 오류가 발생하게 된다.

![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/database-pessimistic-performance.png?raw=true 'Result')

## **🏹 DataBase Optimistic Lock**

> 트랜잭션이 데이터를 읽고 수정하는 동안 별도의 잠금을 사용하지 않고, 
> 
> 데이터 업데이트 시점에 충돌 여부를 확인

정의.

- 데이터에 대한 실제 잠금을 사용하지 않고, 데이터가 수정될 때 `충돌을 감지`하여 `데이터 무결성을 유지`
- 일반적으로 `버전 번호`나 `타임스탬프` 같은 메커니즘을 사용하여 구현
- 버전 충돌 시 재시도하거나 롤백
- 충돌이 드물게 발생하는 환경에서 사용

동작 방식.

1. **데이터 읽기**: 트랜잭션이 데이터를 읽을 때 현재의 버전 번호를 함께 조회
2. **데이터 수정 시도**: 트랜잭션이 데이터를 수정할 때, 저장 시점에 버전 번호가 조회 때와 동일한지 확인
3. **충돌 감지**: 만약 버전 번호가 변경되었으면, 충돌이 발생한 것으로 간주하고 업데이트를 거부 → 재시도 또는 롤백 수행
4. **업데이트 수행**: 충돌이 없다면 데이터를 수정하고, 새로운 버전 번호로 업데이트

**장점.**

- **`높은 동시성` →** 데이터 잠금을 사용하지 않으므로 트랜잭션이 동시에 데이터에 접근하여 시스템의 동시성을 향상
- **`성능 향상` →** 잠금으로 인한 대기 시간이 없으므로 성능 향상
    - 데이터 충돌이 적은 시스템에서 효과적
- **`교착 상태 방지` →**  잠금을 사용하지 않으므로 교착 상태가 발생하지 않음

**단점.**

- **`충돌 처리 비용` →** 충돌 발생 시 해당 트랜잭션을 재시도하거나 롤백해야 하므로 충돌 처리에 대한 비용 발생
    - 재시도를 할 경우 로직을 별도로 구현해야 하므로 개발 복잡성 증가
- **`복잡성 증가` →** 버전 관리가 필요하여 시스템의 복잡성을 증가

**사례.**

- **충돌 가능성이 낮은 환경 →** 데이터가 자주 변경되지 않고, 읽기 작업이 주로 이루어지는 환경
- **높은 동시성이 필요한 경우 →** 많은 사용자가 동시에 데이터를 읽고 처리해야 하는 경우
- **짧은 트랜잭션 →** 트랜잭션이 짧고 충돌 가능성이 낮을 때 효과적 (ex. 사용자 프로필 조회 및 업데이트)

```bash
[pool-1-thread-1] [quantity] before: 100, after: 99
[pool-1-thread-4] [quantity] before: 100, after: 99
[pool-1-thread-5] [quantity] before: 100, after: 99
...
[pool-1-thread-4] fail to update stock // 충돌이 발생하여 업데이트 거부
[pool-1-thread-5] fail to update stock
[pool-1-thread-7] fail to update stock
...
재고는 0보다 작을 수 없습니다.
재고는 0보다 작을 수 없습니다.
```

.

[database optimistic lock example](https://github.com/jihunparkme/Study-project-spring-java/commit/69c57fd43525767056251213fc3447dcf37d519a)

.

**🛠️  성능 테스트.**

- 한정 수량: 50,000
- User: 296
- Processes: 8
- Threads: 37
- Duration: 3 min

> 업데이트에 실패할 경우 재시도를 하다 보니 처리량이 **Pessimistic Lock** 방식보다 낮을 것을 볼 수 있다. 
> 
> 동일한 조건으로 테스트했음에도 38,491 건만 성공으로 처리된 것을 볼 수 있다.

![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/database-optimistic-performance.png?raw=true 'Result')

## **🏹 DataBase Named Lock**

> 데이터베이스에서 특정 이름을 가진 잠금을 획득하거나 해제하는 메커니즘

**정의.**

- 특정 데이터(`Row` or `Table`)에 락을 거는 대신 **특정 이름에 락킹**
    - 이름이 같다면 서로 다른 트랜잭션이 동일한 잠금을 공유
- 커넥션 풀 부족 현상을 막기 위해 데이터 소스를 분리해서 사용할 것을 권장
- Timeout을 쉽게 구현 가능

**장점.**

- **`유연한 잠금 제어` → 특**정 이름에 대한 잠금으로 데이터베이스 객체와는 독립적으로 동작
- **`응용 프로그램 간 협력` →** 동일한 데이터베이스를 사용하는 여러 응용 프로그램 간에 동기화 메커니즘으로 사용 가능 (주로 분산락 구현 시 사용)
- **`트랜잭션과 독립적` →** 일반적으로 트랜잭션과 독립적으로 동작하므로, 트랜잭션의 롤백이 발생하더라도 잠금 상태가 유지 (트랜잭션 간의 복잡한 동기화 시나리오 처리 가능)

**단점.**

- **`Deadlock` →** 두 개 이상의 트랜잭션이 서로 다른 Named Lock을 기다리면서 영원히 대기하는 상황
- **`성능 저하` →** 과도하게 사용 시 데이터베이스의 동시 처리 능력 저하
    - 특히 잠금이 장기간 유지되면 다른 트랜잭션이 대기하면서 성능 저하를 유발
- **`잠금 관리의 복잡성` → 잠**금 획득과 해제의 타이밍을 신중하게 관리 필요
    - 잘못된 관리로 잠금이 제대로 해제되지 않으면 시스템의 가용성이 저하
- **`트랜잭션 독립성의 위험` →** 트랜잭션과 독립적으로 동작하므로, 트랜잭션이 종료되더라도 잠금이 해제되지 않으면 의도치 않은 동작이나 데이터베이스 잠금 문제를 초래
    - 트랜잭션 종료 시 Lock 이 자동으로 해제되지 않으므로, 별도의 명령어로 락 해제와 세션 관리가 필요

**사례.**

- **리소스 동기화 →** 복수의 트랜잭션이나 프로세스가 **동일한 리소스를 동기화**해야 할 때 유용
    - ex) 다수의 프로세스가 동일한 파일을 수정, 동일한 외부 시스템에 접근할 때 충돌을 방지
- **순차 작업 처리 →** 특정 작업을 순차적으로 처리해야 하는 경우 작업의 순서를 보장
    - ex) 다수의 트랜잭션이 동일한 작업을 수행하려 할 때 작업의 순서를 제어
- **다중 서버 환경에서의 동기화 →** 여러 서버가 동일한 DB를 사용하는 환경에서, 서버 간의 작업 동기화 가능
    - ex) 서버 간의 작업 충돌을 방지하고 일관성을 유지
- **일회성 작업 보장 →** 한 번만 실행되어야 하는 작업에서 동시에 여러 트랜잭션이 동일한 작업을 수행하지 않도록 보장

.

[database Named Lock example](https://github.com/jihunparkme/Study-project-spring-java/commit/e096d50e80617910314a4c55658f024d35c24196)

.

**🛠️  성능 테스트.**
- 한정 수량: 10,000
- User: 40
- Processes: 2
- Threads: 20
- Duration: 3 min

다른 방식과 동일한 조건으로 테스트를 시도했지만,<br/>
Dead Lock 이 발생하게 되어 아래 에러를 마주하고 조건을 약하게 줄이게 되었다.

```bash
o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: null
o.h.engine.jdbc.spi.SqlExceptionHelper   : HikariPool-1 - Connection is not available, request timed out after 30004ms (total=40, active=40, idle=0, waiting=159)
```

락을 얻고 릴리즈하는 부분과 재고를 차감하는 로직이 서로 다른 트랜잭션으로 동작하게 되는데
한 요청에 두 개의 커넥션이 사용되다 보니 HikariCP에 대한 Thread 간 Dead lock이 발생한 것이었다.

Named Lock 을 적용할 경우 Connection Pool 이 많이 필요하다 보니 대량의 요청을 동시에 처리하기에는 무리가 있어 보인다.

참고.
- [HikariCP Dead lock에서 벗어나기 (이론편)](https://techblog.woowahan.com/2664/)
- [HikariCP Dead lock에서 벗어나기 (실전편)](https://techblog.woowahan.com/2663/)

> maximum-pool-size(40) 로 버틸 수 있을 정도의 트래픽으로만 테스트를 하게 되었다.
> 
> 결과를 보면 정확하게 10,000 건만 성공으로 처리되었고,
> 
> 01:04 이후에는 모든 수량이 0으로 조회되어, 재고 부족 오류가 발생하게 된다.

![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/database-named-performance.png?raw=true 'Result')