# HikariCP

**[HikariCP(Hikari Connection Pool)](https://github.com/brettwooldridge/HikariCP)**

- Connection Pool 을 관리해 주는 라이브러리
- [Spring Boot 2.0 버전부터 Tomcat connection pool 대신 HikariCP 사용](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide#configuring-a-datasource)

.

HikariCP 기본 설명은 쉽게 풀어서 설명된 [링크](https://adjh54.tistory.com/73)를 참고해 보자.

.


최근 HikariCP Connection 관련 장애를 겪고, HikariCP 의 Connection 관련 이슈와 대응방법에 대하여 알아보게 되었다.

고로... 본문에는 Hikari 기본설정, Dead lock 과 해결방법에 대해서 간단하게 다뤄보려고 한다.

## Hikari 기본설정

✅ **autoCommit**

- pool에서 반환된 connection의 `기본 자동 커밋 동작 제어`
- Default: true
 
⏳ **connectionTimeout**

- 클라이언트가 pool로부터의 `연결을 기다리는 최대 시간`
- 연결을 사용할 수 없는 상태에서 이 시간이 초과되면 SQLException 발생
- Lowest acceptable connection timeout: 250 ms
- Default: 30000ms (30sec)
 
⏳ **idleTimeout**

- Connection Pool 에서 `쉬고 있는 커넥션을 유지하는 시간`
- minimumIdle(20) 이 maximumPoolSize(30) 보다 작게 설정되어 있을 때만 설정
  - default(10min) 가 적용될 경우 10분 동안 쉬고 있는 커넥션을 회수(단, 최소 minimumIdle 사이즈만큼은 유지)
- default: 600000ms(10mi), 최솟값: 10000ms
 
⏳ **keepaliveTime**
 
- HikariCP가 데이터베이스 또는 네트워크 인프라에 의해 시간 초과되는 것을 방지하기 위해 `connection 유지를 위해 얼마나 자주 연결을 시도할 것인지 제어`
- maxLifetime 값보다 작아야 함. (connection이 살아있는지 확인하기 위해)
- keepalive는 유휴 연결에서만 발생
- connection이 keepalive 시간이 되면, connection은 pool에서 제거되고, pinged 된 후 다시 pool로 반환
- The minimum allowed value: 30000ms (30sec)
- Default: 0(비활성), 분 단위가 적절

⏳ **maxLifetime**

- Connection Pool 에서 `살아 있을 수 있는 커넥션의 최대 수명시간`
- 사용 중인 연결은 취소되지 않으며, 커넥션이 닫힌 후에만 제거
- 풀 전체가 대상이 아닌 커넥션 별로 적용
  - Connection Pool 에서 대량으로 커넥션들이 제거되는 것을 방지하기 위함
- 해당 옵션을 사용하는 것을 권장하고 `database, infra 적용 Connection time limit 보다 작아야 함`
- 0으로 설정 시 infinite lifetime 적용
  - 단, idleTimeout 설정 시 적용 불가
- default: 1800000ms (30min)
 
🔤 **connectionTestQuery**

- Connection.isValid() API를 지원하지 않는 레거시 드라이버를 위한 설정
- 드라이버가 JDBC4를 지원할 경우 설정하지 않는 것을 추천
- 데이터베이스 연결이 활성 상태인지 확인하기 위해 풀에서 연결이 제공되기 직전에 실행되는 쿼리
- Default: none
 
🔢 **minimumIdle**

- 커넥션이 `일을 하지 않아도 사이즈만큼은 커넥션 유지`
- 최적의 성능과 응답성을 요구한다면 default(same as maximumPoolSize) 사용
 
🔢 **maximumPoolSize**

- Connection Pool 에 `유지시킬 수 있는 최대 커넥션 수`
- Connection Pool 의 커넥션 수가 사이즈에 도달하게 되면 idle 상태는 존재하지 않음(default: 10)
 
🔤 **poolName**

- Connection Pool 의 사용자 정의 이름
- 로깅 및 JMX 관리 콘솔에서 식별
- Default: auto-generated

## Deadlock

부하 상황에서 Thread 간 Connection 을 차지를 하기 위한 Race Condition 발생
- 하나의 트랜젝션에서 동시에 여러 Connection 을 사용할 경우, Connection 부족으로 HikariCP 에 대한 Thread 간 Dead lock 상태에 빠질 수 있다.

.

예를 들어보면..

```text
Thread: 8개
HikariCP MaximumPoolSize: 5개
하나의 Task에서 동시에 요구되는 Connection 갯수: 2개
```

부하 상황에서 8개의 Thread 가 동시에 HikariCP 로부터 Connection 을 얻어내려 한다고 가정해 보자.

그렇다면 5개의 Thread 만이 Connection 을 가지고 Transaction 을 시작했을 것이다.

```text
실행중인 Thread: 5개
대기중인 Thread: 3개

사용중인 Connection: 5개
Connection 대기중(handOffQueue): 3개
```

.

하지만 트랜잭션 내에서 추가의 Connection 이 필요할 경우, 현재 Pool 에는 idle Connection 이 없기 때문에 handOffQueue 에서 5개의 Thread 가 추가로 대기하고 있게 될 것이다.

```text
실행중인 Thread: 5개
대기중인 Thread: 3개

사용중인 Connection: 5개
Connection 대기중(handOffQueue): 3+5개
```

.

connectionTimeout 만큼의 시간이 흐르고.. 그렇게 Connection Timeout 으로 아래 에러가 발생하고..

`HikariPool-1 - Connection is not available, request timed out after`

SQLTransientConnectionException 으로 인해 Transaction rollback 되면서 다시 Connection 이 Pool에 반납되고 handOffQueue 에서 기다리는 다른 Thread 에서 다시 Connection 을 받아가기 시작하게 된다..

## Solution

### maximumPoolSize

**유지시킬 수 있는 최대 커넥션 수를 증가시키는 방법**

```text
maximumPoolSize = Tn x (Cm - 1) + 1
Tn : 전체 Thread 갯수(8)
Cm : 하나의 Task에서 동시에 필요한 Connection 수(2)
maximumPoolSize = 9
```

[HikariCP wiki](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)에서는 이 공식대로 Maximum pool size 를 설정하면 Dead lock을 피할 수 있다고 한다.

.

[기본 공식을 확장시킨 공식](https://techblog.woowahan.com/2663/)을 활용하면 아래와 같이 설정도 가능하다.

```text
maximumPoolSize = Tn x (Cm - 1) + (Tn / 2)
Tn : 전체 Thread 갯수(8)
Cm : 하나의 Task에서 동시에 필요한 Connection 수(2)
maximumPoolSize = 12
```

.

유지시킬 수 있는 최대 커넥션 수를 증가시키는 방법은 단순한 방법이지만, 만능이라고 할 수는 없다.

트래픽이 증가한다면.. 결국 커넥션이 금방 고갈될 것이다.

### connectionTimeout

**연결을 기다리는 최대 시간을 증가시키는 방법**

클라이언트가 응답을 위해 대기하는 시간이 길어지는 게 좋은 것일까 싶긴 하다..

maximumPoolSize 와 적절하게 맞춰주는 게 중요할 것이라고 생각된다.

### Nested Transaction

**중첩 트랜젝션을 사용하지 않는 방법**

중첩 트랜젝션 사용 시, 서브 트랜젝션의 작업이 완료될 때까지 커넥션을 점유하고 있기 때문에

dead lock 을 유발시킬 수 있는 가능성이 높아진다.

## Reference

> HikariCP Dead lock 이론: https://techblog.woowahan.com/2664/
>
> HikariCP Dead lock 대처: https://techblog.woowahan.com/2663/

.

제가 잘못 알고 있는 부분이 있다면 달콤한 피드백 부탁드립니다..🙏🏻