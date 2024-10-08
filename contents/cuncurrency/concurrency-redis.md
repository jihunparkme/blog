# 🎯 Redis Concurrency Control

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

## **🏹** Redis incr

> Redis의 `INCR` 명령을 이용하여 특정 키의 값을 원자적으로 증가시키면서
카운터나 임계값 제어 등의 작업에 사용하는 방법

**정의.**

- Redis는 빠른 속도와 `싱글 스레드`의 특성을 활용하여 `레이스 컨디션`을 해결
- 빠른 성능을 가진 `INCR` 명령(키에 대한 값을 1씩 증가시키는 명령어)을 이용하여 임계값을 제어

**동작 방식.**

- `INCR key`→ 주어진 `key`의 값을 1 증가
    - 키가 존재하지 않을 경우, 먼저 해당 키를 0으로 초기화한 후 1 증가

**장점.**

- **`원자적 연산`**
    - `INCR` 명령어는 Redis의 싱글 스레드 구조 덕분에 원자적으로 수행
    - 여러 클라이언트가 동시에 `INCR` 명령어를 실행해도 중복되거나 잘못된 값이 저장되지 않음
- **`간단하고 빠름`**
    - `INCR` 명령어는 매우 간단하게 사용할 수 있으며, Redis의 빠른 성능 덕분에 실시간 카운팅 작업에 매우 유리
- **`분산 환경에서 사용 가능`**
    - Redis는 분산 환경에서 사용되기 때문에, 여러 서버 또는 애플리케이션 인스턴스에서 중앙 집중식 카운팅을 쉽게 구현 가능
- **`자동 초기화`**
    - 키가 존재하지 않는 경우, `INCR` 명령어는 자동으로 키를 0으로 초기화하고 증가(초기화 작업이 불필요)

**단점.**

- **`데이터 유실 가능성`**
    - Redis는 메모리 기반 데이터 저장소이므로 지속성 옵션을 설정하지 않으면 Redis 서버가 재시작되거나 장애가 발생할 경우 데이터 유실 가능성 존재
    - 카운터 값이 중요한 경우, 데이터 유실에 대비한 추가적인 조치가 필요
- **`제어된 임계값 관리의 어려움`**
    - 특정 임계값을 넘었을 때 이를 관리하기 위해서는 추가적인 로직이 필요
- **`트랜잭션 및 복잡한 로직 구현의 어려움`**
    - 복잡한 트랜잭션을 다루거나 여러 단계의 연산이 필요한 경우, `INCR`만으로는 제어하기 어려움

**사례.**

- **API Rate Limiting →** API 요청의 횟수 제한
- **동시성 제어 →** 특정 리소스나 작업에 대해 동시에 접근할 수 있는 클라이언트의 수 제한
- **이벤트 카운팅 →** 특정 이벤트 발생 횟수를 실시간으로 기록해야 할 경우
    - ex) 광고 클릭 수, 좋아요 수, 페이지 뷰 수 등을 실시간으로 카운팅하여 추적
- **분산 환경에서의 중앙 집중식 카운팅 →** 여러 서버나 애플리케이션 인스턴스에서 공통 카운터를 유지할 경우

.

[redis incr example](https://github.com/jihunparkme/Study-project-spring-java/commit/9c3bfd7dc9cd24886ed6f10616f79bf96c7cd2ba)

.

**🛠️ 성능 테스트.**
- 한정 수량: 50,000
- User: 296
- Processes: 8
- Threads: 37
- Duration: 3 min (01:40 중단)

> 결과를 보면 정확하게 50,000 건만 성공으로 처리된 것을 볼 수 있다.
>
> Redis key 는 82,720 인데 50,000(Successful) + 32,720(Errors) = 82,720 로 정확하게 저장되었다.
> 
> 00:36 이후에는 모든 수량이 0으로 조회되어, 재고 부족 오류가 발생하게 된다.

![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/redis-incr-performance.png?raw=true 'Result')
    
## **🏹** Redis **Lettuce Lock**

> Redis의 명령어와 기능을 활용하여 락을 구현하는 방법
주로 `SET` 명령어와 TTL(Time-To-Live)을 사용하여 특정 리소스에 대한 잠금을 관리
> 

**정의.**

- Lettuce를 사용해 Redis에서 분산 락(Distributed Lock)을 구현 가능
- MySQL의 Named Lock과 유사한 방식
- `setnx`(set if not exist) 명령어를 활용하여 분산락 구현
- `Spin Lock` 방식
    - 락을 획득하려는 스레드가 락을 사용할 수 있는지 반복적으로 확인하면서 락 획득을 시도하는 방식
    - 재시도 로직 개발 필요

**동작 방식.**

- **락 획득**
  - 특정 키를 생성하고 `SET` 명령어를 사용하여 이 키에 값과 TTL을 설정
  - 이때 `NX`(Key가 존재하지 않을 때만 설정) 옵션과 `EX`(키의 만료 시간을 초 단위로 설정) 옵션을 함께 사용
  - 락을 성공적으로 획득하면 `OK`를 반환하고, 다른 프로세스는 락을 획득 실패
- **락 해제**
  - 작업이 완료되면 락을 해제하기 위해 해당 키를 삭제
  - 이 과정에서 키가 실제로 잠금을 획득한 프로세스에 의해 삭제되는지 확인하는 로직이 필요할 수 있음
- **자동 해제**
  - TTL을 설정해 두면 지정된 시간이 지나면 락이 자동으로 해제
  - 프로세스가 예기치 않게 종료되는 상황에서도 시스템이 무한정 대기하는 것을 방지

**장점.**

- **`간단한 구현`**
  - Redis의 기본 명령어만으로 간단하게 분산 락을 구현 가능
  - 복잡한 설정이나 외부 라이브러리 없이도 효율적인 락 메커니즘을 구축
  - (spring data redis 사용 시 기본적으로 Lettuce 적용)
- **`비동기 처리`**
  - 비동기 및 반응형 프로그래밍 모델을 지원
  - 높은 성능과 비동기 처리가 요구되는 시스템에서 효율적으로 작동
- **`TTL을 통한 자동 해제`**
  - TTL 설정을 통해 잠금을 자동으로 해제
  - 예기치 않은 오류나 프로세스 종료로 인한 잠금 해제를 신경 쓸 필요가 없음
- **`확장성`**
  - Lettuce Lock은 Redis를 기반으로 하기 때문에, 분산 시스템에서 손쉽게 확장 가능
  - 여러 인스턴스가 동일한 Redis 서버를 통해 락을 공유 가능

**단점.**

- `Spin Lock 방식`
  - 동시에 많은 스레드가 락 획득 대기 상태라면 레디스에 부하가 갈 수 있음
  - 락 획득 재시도 간 대기 시간도 필요
- **`분산 락의 신뢰성 문제`**
  - Redis는 기본적으로 단일 노드에서 작동
  - Redis 서버에 장애가 발생하거나 네트워크 분할(Partitioning)이 일어나면 락이 해제되지 않거나, 잘못된 프로세스에 의해 락이 해제될 가능성이 존재
- **`락 해제 경쟁 조건`**
  - 프로세스가 락을 해제할 때, 다른 프로세스가 동시에 같은 키에 대해 락을 획득하려 할 경우 Race Condition 발생 가능성 존재
  - 방지를 위해 락 해제 시 고유한 식별자를 확인하는 추가적인 로직이 필요
- **`TTL의 한계`**
  - TTL이 지나면 자동으로 락이 해제되지만, 작업이 예상보다 오래 걸릴 경우 락이 일찍 해제될 위험이 존재
  - 이로 인해 두 프로세스가 동시에 같은 리소스에 접근하게 될 수 있음
- **`네트워크 지연 문제`**
  - 분산 환경에서 네트워크 지연으로 인해 락이 시의적절하게 해제되지 않거나, 반대로 이미 해제된 락이 아직 해제되지 않은 것으로 인식될 수 있음 → 잠재적으로 데이터 일관성 문제를 초래

**사례.**

- **분산 시스템에서의 리소스 동기화**
  - 여러 서버나 애플리케이션 인스턴스에서 동일한 리소스(ex. 파일, 데이터베이스 레코드)에 접근할 때, Lettuce Lock을 사용하여 동시 접근을 제어
- **서버 간 작업 조정**
  - 분산 환경에서 서버 간의 작업을 조정하거나 특정 작업을 단일 서버에서만 수행되도록 보장할 때 유용
  - ex) 배치 작업이나 크론 작업의 중복 실행을 방지하기 위해 사용
- **API Rate Limiting**
  - 특정 사용자가 일정 기간 내에 API를 일정 횟수 이상 호출하지 못하도록 제한할 때, Redis의 TTL을 활용한 Lettuce Lock 방식으로 Rate Limiting을 구현
- **일회성 작업 보장**
  - 특정 이벤트나 작업이 한 번만 실행되어야 하는 경우, Lettuce Lock을 사용하여 동일한 이벤트가 중복 처리되지 않도록 제한
  - ex) 이메일 발송, 트랜잭션 처리 등에서 사용

.

[redis lettuce lock example](https://github.com/jihunparkme/Study-project-spring-java/commit/49bd83ee2d0bd142edaa33b4106d345c7b281d5d)

.

**🛠️ 성능 테스트.**

- 한정 수량: 50,000
- User: 296
- Processes: 8
- Threads: 37
- Duration: 3 min

> 동시에 많은 스레드가 락 획득 대기 상태에서 반복적으로 락 획득 확인을 하다 보니<br/>
> CPU 사용량이 늘게 되었는지 성능이 좋지 않았다.<br/>
> 동일한 조건으로 31,380 건밖에 처리를 못 하였다.
> 
> 그래도 동시성 처리가 잘 되고 있는지 확인은 해야 하니 한정 수량을 10,000 으로 줄여서 확인을 따로 해보았는데 문제는 없었다.

![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/redis-lettuce-performance.png?raw=true 'Result')
    

## **🏹** Redis **Redisson Lock**

> `pub-sub` 기반으로 Lock 구현 제공

**정의.**

- 채널을 만들고 락을 점유 중인 스레드가 락 획득을 대기 중인 스레드에게 락 해제라는 메시지를 전송
    - 메시지를 전달받은 스레드가 락 획득을 시도하는 방식
- 락 획득을 위해 반복적으로 락 획득을 시도하는 Lettuce 대비 부하를 줄일 수 있음

**동작 방식.**

- **락 획득**
  - 클라이언트가 `lock()` 메소드를 호출하면, Redisson은 Redis에 락을 설정하는 명령어를 전송
  - 이때 락의 만료 시간(TTL)이 설정되어, 설정된 시간이 지나면 락이 자동으로 해제
- **락 해제**
  - 작업이 완료되면 `unlock()` 메소드를 호출하여 락을 해제
  - 이 과정에서 락이 실제로 해당 클라이언트에 의해 설정된 것인지 확인하여, 다른 클라이언트가 잘못된 락 해제를 하지 않도록 보호
- **자동 연장**
  - Redisson은 락이 설정된 동안 주기적으로 만료 시간을 연장하는 기능을 제공
  - 작업이 예상보다 오래 걸리는 경우에도 락이 유지되도록 지원

**장점.**

- `pub-sub 기반 구현`
  - Lettuce Lock 대비 레디스에 적은 부하
  - 락 획득 재시도를 기본적으로 제공
- **`고가용성`**
  - Redis 클러스터 환경을 지원
  - 고가용성을 위해 여러 노드에 분산된 Redis 인스턴스를 사용 가능
  - 단일 노드 장애 시에도 락이 안정적으로 유지
- **`자동 연장 기능`**
  - Watchdog 기능을 통해 락의 만료 시간을 자동으로 연장
  - 긴 작업을 수행할 때 락이 의도치 않게 해제되는 것을 방지
- **`다양한 락 유형 지원`**
  - 기본적인 락뿐만 아니라, 재진입 락(Reentrant Lock), 공정 락(Fair Lock), 읽기-쓰기 락(ReadWrite Lock) 등 다양한 락 유형을 지원
- **`편리한 API`**
  - 간단하고 직관적인 API를 제공하여, 복잡한 분산 락 로직을 쉽게 구현
- **`분산 락 관리`**
  - 분산 락을 관리하기 위한 다양한 도구와 기능을 제공하여, 분산 환경에서 동시성을 효과적으로 제어

**단점.**

- **`복잡성`**
  - 효과적으로 사용하기 위해서는 설정과 사용법에 대한 깊은 이해가 필요 + 별도의 라이브러리 필요
  - 특히 Redis 클러스터 환경에서의 설정은 복잡
- **`네트워크 지연 및 장애`**
  - 분산 락은 네트워크 지연이나 Redis 노드 장애로 인해 성능 저하 또는 잠금 문제를 일으킬 수 있음
  - 이러한 문제를 최소화하기 위해 설계되었지만, 완전히 방지할 수는 없음
- **`오버헤드`**
  - 자동 연장 기능과 고가용성 지원 기능은 추가적인 오버헤드를 발생시킬 수 있음
  - 매우 높은 성능이 요구되는 시스템에서는 주의 필요
- **`데이터 유실 가능성`**
  - 분산 락의 정보가 Redis의 장애나 재시작으로 인해 유실될 가능성이 존재
  - 이를 방지하기 위해 추가적인 데이터 지속성 설정이 필요

**사례.**

- **분산 트랜잭션 관리**
  - 분산 시스템에서 트랜잭션을 안전하게 관리하는 데 유용
  - ex) 여러 마이크로서비스가 동일한 데이터베이스나 리소스에 접근할 때 동시성 문제를 해결하는 데 사용
- **동시 작업 제한**
  - 여러 서버에서 동시에 실행되는 작업의 수를 제한해야 할 때 사용
  - ex) 분산된 웹 서버가 동일한 파일을 동시에 수정하지 못하게 하거나, 동일한 외부 API에 대한 동시 요청 수를 제한하는 데 활용
- **재진입 락 필요시**
  - 동일한 클라이언트에서 락을 여러 번 획득하고 해제할 필요가 있는 경우, Redisson의 재진입 락 기능을 통해 이 작업을 안전하게 처리
- **복잡한 동기화 로직**
  - 읽기-쓰기 락, 공정 락 등 복잡한 동기화 요구 사항이 있는 경우 시나리오 구현 가능

> 실무에서는 보통
> 
> 재시도가 필요하지 않은 락은 Lettuce를 활용하고, 
> 
> 재시도가 필요한 경우 Redisson을 활용

참고.
- [Redisson trylock() 내부로직 살펴보기](https://incheol-jung.gitbook.io/docs/q-and-a/spring/redisson-trylock)
- [레디스가 제공하는 분산락(RedLock)의 특징과 한계](https://mangkyu.tistory.com/311)

.

[redis redisson lock example](https://github.com/jihunparkme/Study-project-spring-java/commit/1c124166b10de001659049c54970ea6a3e897c63)

.

**🛠️  성능 테스트.**

- 한정 수량: 50,000
- User: 296
- Processes: 8
- Threads: 37
- Duration: 3 min

> Lettuce 의 Spin Lock 방식 대한 단점을 해결하기 위한 차선책으로 Redisson 이 사용되는데,<br/>
> Redisson 에서는 lua script와 세마포어가 사용된 다곤 한다.
> 
> 환경에 따라 다를 수 있지만 Redisson Lock 방식도 성능이 좋지만은 않았다.<br/>
> 동일한 조건으로 28,816 건밖에 처리를 못 하였다. (Lettuce Lock 보다 더 안 좋은 성능)
> 
> 그래도 동시성 처리가 잘 되고 있는지 확인은 해야 하니 한정 수량을 10,000 으로 줄여서 확인을 따로 해보았는데 문제는 없었다.

![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/redis-redisson-performance.png?raw=true 'Result')