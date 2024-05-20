# Lock

## Database Lock

여러 사용자나 프로세스가 동시에 데이터를 액세스할 때 발생하는 데이터 무결성 문제를 방지하기 위한 메커니즘
- 데이터베이스 락은 동시성 제어의 중요한 부분
- 데이터를 일관되고 신뢰성 있게 유지하는 데 필수

### DB Lock의 주요 유형

`공유 락(Shared Lock)`

- **데이터를 읽는 작업**을 할 때 사용
- 한 자원에 대해 **여러 프로세스가 공유 락을 획득 가능** -> 다수의 사용자가 동시에 데이터를 읽을 수 있음
- 공유 락이 설정된 데이터에는 **쓰기 작업** 불가

`배타 락(Exclusive Lock)`

- **데이터에 쓰기 작업**을 할 때 사용
- 배타 락이 설정된 자원은 해당 락을 보유한 **단 하나의 트랜잭션만이 데이터를 수정** 가능
- 다른 어떤 락도 **해당 자원에 설정 불가**

### 락의 동작 방식

- `락 획득`(Acquiring a Lock): 데이터에 접근하기 전에 적절한 락을 요청하고 획득
- `락 보유`(Holding a Lock): 필요한 작업(읽기 또는 쓰기)을 수행하는 동안 락을 유지
- `락 해제`(Releasing a Lock): 작업 완료 후 락을 해제하여 다른 트랜잭션이 자원에 접근할 수 있도록

### 락의 문제점

**`데드락`(Deadlock)**

두 개 이상의 트랜잭션이 **서로의 락 해제를 무한히 기다리는 상황**
- 시스템은 데드락을 탐지하고 하나 이상의 트랜잭션을 중단시켜 문제 해결 가능

**`락 경합`(Lock Contention)**

많은 트랜잭션이 **동시에 같은 데이터에 접근**하려고 할 때 발생
- 성능 저하로 이어질 수 있으며, 락 경합을 최소화하는 설계 필요

### 락의 최적화

**락의 크기**
- 가능한 가장 작은 단위의 락을 사용하는 것을 권장(전체 테이블 락 피하기)

**락의 시간**
- 락은 가능한 짧은 시간 동안만 유지

**비관적 락과 낙관적 락**
- 필요에 따라 비관적 락(데이터를 사용하기 전에 락을 거는 방식)과 낙관적 락(실제 충돌이 일어났을 때만 대처하는 방식)을 적절히 선택
- 데이터베이스 락은 데이터의 일관성과 무결성을 유지하는 데 매우 중요하지만, 잘못 사용되면 성능 저하나 데드락 같은 문제를 유발할 수 있음

## Application Lock

일반적으로 자바의 동기화 메커니즘을 사용하거나, Spring 특정 기능을 활용하여 구현

### @Transactional

메서드 또는 클래스 레벨에서 트랜잭션 경계를 정의
- 주로 데이터베이스 트랜잭션의 관리에 사용
- 메서드 전체를 하나의 트랜잭션으로 처리

### Java synchronized

특정 객체에 대한 동시 접근을 제한하여 한 시점에 하나의 스레드만이 특정 코드 블록을 실행할 수 있음

```java
@Service
public class MyService {

    private final Object lock = new Object();

    @Transactional
    public void updateData() {
        synchronized(lock) {
            // 데이터 업데이트 로직
            // 이 블록은 동시에 하나의 스레드만 접근 가능
        }
    }
}
```

### Spring Integration LockRegistry 

보다 복잡한 로직의 락을 관리
- 여러 인스턴스가 동일한 데이터에 접근할 때 유용

```java
@Service
public class MyService {

    @Autowired
    private LockRegistry lockRegistry;

    @Transactional
    public void updateData(String id) {
        Lock lock = lockRegistry.obtain(id);
        try {
            if (lock.tryLock()) {
                // 데이터 업데이트 로직
            } else {
                // 락 획득 실패 처리
            }
        } finally {
            lock.unlock();
        }
    }
}
```

## Distributed Lock

여러 컴퓨팅 노드가 있는 분산 시스템에서 공유 자원에 대한 동시 접근을 조율하기 위해 사용되는 메커니즘
- 단일 시스템에서의 락과 비슷하게, 분산 락은 동시에 하나의 엔티티만이 특정 자원 또는 작업에 대해 독점적인 액세스 권한을 가질 수 있도록 보장
- 단, 분산 환경은 네트워크 지연, 노드 장애, 복잡한 동기화 등의 추가적인 도전 과제를 제공

**분산 잠금의 필요성**

분산 시스템에서는 여러 서버나 노드가 같은 데이터 또는 자원을 사용하려 할 때 일관성과 순서를 유지해야 할 필요가 있음
- 금융 거래 처리나 글로벌 데이터베이스 관리와 같은 작업에서 데이터 일관성을 보장하기 위해 분산 락이 필요

### 분산 잠금의 구현

**ZooKeeper**

- 분산 코디네이션 서비스를 제공하는 오픈 소스 프로젝트
- 분산 락과 같은 분산 시스템 패턴을 구현하는 데 사용
- ZooKeeper znode(데이터 노드)를 사용하여 락을 생성하고, 세션을 통해 락의 소유권을 관리

**Redis**

- 고성능 키-값 스토어
- Redis 명령어를 사용하여 간단한 분산 락 구현 가능
- ex. SET 명령어의 NX(Not Exists) 옵션과 EXPIRE를 조합하여 락 구현

**Etcd**

- 키-값 저장소로 분산 시스템을 위해 설계
- Kubernetes의 백엔드로도 사용
- 분산 시스템의 구성 정보 관리와 서비스 디스커버리, 락 서비스 등을 제공

**Google Cloud Pub/Sub**

- 메시징 시스템으로 분산 락을 구현하기 위해
- 메시지가 한 번에 하나의 소비자에게만 전달되도록 보장하여 락을 구현

### 분산 잠금의 고려 사항

**신뢰성과 가용성**
- 분산 락 서비스 자체가 신뢰성 있고 항상 사용 가능해야 한다.

**장애 복구**
- 노드 실패나 네트워크 문제 발생 시 락 상태의 복구와 정리를 어떻게 처리할지 고려해야 한다.
  
**성능**
- 락 요청에 대한 응답 시간과 락 해제 과정이 시스템의 전반적인 성능에 미치는 영향을 평가해야 한다.

**데드락**
- 분산 시스템에서 데드락을 방지하고 관리하는 메커니즘이 필요하다.

## Reference

- [레디스와 분산 락(1/2) - 레디스를 활용한 분산 락과 안전하고 빠른 락의 구현](https://hyperconnect.github.io/2019/11/15/redis-distributed-lock-1.html)
- [멱등성이 뭔가요?](https://velog.io/@tosspayments/%EB%A9%B1%EB%93%B1%EC%84%B1%EC%9D%B4-%EB%AD%94%EA%B0%80%EC%9A%94)
- [JPA를 활용한 DataBase Lock & Redis Lock 적용](https://velog.io/@youmakemesmile/Spring-DataJPA%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-DataBase-Lock-Redis-Lock-%EC%A0%81%EC%9A%A9)