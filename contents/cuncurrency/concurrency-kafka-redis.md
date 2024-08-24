# 🎯 Kafka Concurrency Control

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

## 🏹 Kafka

> 분산 스트리밍 플랫폼으로, 주로 대규모 실시간 데이터 처리와 메시징에 사용

**리더 선출(Leader Election)** 또는 **협조적 소비자 패턴(Cooperative Consumer Pattern)** 을 활용하여 특정 리소스에 대한 접근을 조율

<br/>.<br/>

**동작 방식.**

- **리더 선출을 통한 락**
    - Kafka의 파티션과 오프셋을 이용하여, 특정 작업을 수행할 리더 선출
        - 하나의 Kafka 파티션에서 메시지를 소비하는 여러 컨슈머 중 하나가 리더가 되어 특정 리소스에 대한 작업을 수행 → 이 리더 역할이 일종의 락 역할
        - 만약 리더가 실패하거나 특정 시간 동안 응답하지 않으면, 다른 컨슈머가 새로운 리더로 선출되어 작업을 이어받음
- **협조적 소비자 패턴**:
    - 여러 컨슈머가 동일한 Kafka 토픽에서 메시지를 소비할 때, 서로 협력하여 특정 리소스에 대한 접근을 조율
    - 특정 리소스를 동시에 접근하지 않도록 협의하는 과정이 필요 → 락과 유사한 기능을 구현 가능
- **분산 트랜잭션 관리**:
    - Kafka 트랜잭션 기능을 활용하여, 메시지를 처리하면서 락을 설정하고 해제
    - 메시지를 처리하는 동안 트랜잭션을 유지하고, 작업이 끝나면 트랜잭션을 커밋하면서 락을 해제하는 방식

**장점.**

- **`높은 확장성`**
    - 대규모의 분산 시스템에서 높은 확장성을 제공하도록 설계
    - 여러 컨슈머가 동일한 Kafka 클러스터에서 락을 관리
- **`내결함성`**
    - 내결함성(fault-tolerance)을 지원
    - 리더 선출 방식이나 협조적 소비자 패턴을 사용하면 특정 노드가 실패하더라도 다른 노드가 작업을 이어받아 락의 안정성을 보장
- **`중앙 집중화된 관리`**
    - Kafka 토픽을 이용하여 락 상태나 작업 상태를 중앙에서 관리
    - 시스템 전반의 상태를 쉽게 모니터링하고 관리
- **`복잡한 트랜잭션 처리 가능`**
    - Kafka 트랜잭션 기능을 활용하면 메시지 처리와 함께 복잡한 트랜잭션을 관리
    - 락을 유지하면서도 복잡한 작업을 안전하게 수행

**단점.**

- **`복잡성 증가`**
    - 특히, 리더 선출이나 협조적 소비자 패턴을 구현하려면 더 많은 코딩과 설정이 필요
- **`지연 시간`**
    - Kafka 분산 아키텍처와 데이터 전달 지연으로 인해 락 해제나 리더 선출 과정에서 지연 발생
    - 실시간 처리가 중요한 환경에서는 단점
- **`오버헤드`**
    - 락 관리를 위해 메시지의 생산, 소비, 리더 선출 과정에서 추가적인 오버헤드가 발생
    - 특히, 락을 자주 설정하고 해제하는 작업이 많다면 성능에도 영향
- **`일관성 문제`**
    - 락을 관리하는 데 있어서 일관성 문제가 발생
        - 리더가 선출되기 전까지 여러 컨슈머가 동일한 리소스에 접근하려는 경합이 발생
        - 일관성 문제 해결을 위해 추가적인 제어 로직이 필요
- **`트랜잭션 관리의 복잡성`**
    - Kafka 트랜잭션 기능을 활용하여 락을 관리하려면, 복잡한 트랜잭션 제어가 필요
    - 트랜잭션이 길어지거나 많은 리소스를 잠그는 경우, 성능 저하나 데드락 같은 문제를 야기

**사례.**

- **분산 시스템에서의 리더 선출**
    - 여러 노드가 동일한 작업을 수행하려고 할 때, 하나의 노드를 리더로 선출하여 작업을 조정하는 상황
    - ex) 여러 마이크로서비스가 동일한 작업을 수행하지 않도록 리더를 선출하는 데 사용
- **협조적 작업 수행**
    - 여러 컨슈머가 동일한 Kafka 토픽에서 데이터를 처리하면서, 특정 리소스에 대한 접근을 조율해야 하는 경우
    - ex) 여러 컨슈머가 동일한 데이터베이스 테이블에 접근하여 데이터를 처리할 때, Kafka 락을 사용해 데이터 일관성을 유지
- **복잡한 분산 트랜잭션**
    - 분산 트랜잭션을 관리하는 상황에서 Kafka의 트랜잭션 기능과 락을 결합하여, 안전하게 작업을 수행
    - ex) 메시지 처리와 데이터베이스 업데이트가 동시에 이루어지는 상황에서 Kafka 락을 통해 일관성을 보장
- **대규모 데이터 처리 파이프라인**
    - 대규모 데이터 처리 파이프라인에서 여러 작업을 조율하고 관리해야 하는 경우 작업 간의 순서와 일관성을 유지

<br/>...<br/>

Start Kafka
    
- **docker-compose.yml**
    
```bash
version: '3' # Docker Compose 파일 버전 지정
services: # 여러개의 Docker 컨테이너 서비스 정의
  zookeeper: # Zookeeper 서비스 정의
    image: wurstmeister/zookeeper:3.4.6
    container_name: zookeeper
    ports:
      - "2181:2181" # 호스트의 2181 포트를 컨테이너의 2181 포트와 바인딩
  kafka: # kafka 서비스 정의
    image: wurstmeister/kafka:2.12-2.5.0
    container_name: kafka
    ports:
      - "9092:9092" # 호스트의 9092 포트를 컨테이너의 9092 포트와 바인딩
    environment: # kafka 컨테이너의 환경 변수 설정
      KAFKA_ADVERTISED_LISTENERS: INSIDE://kafka:29092,OUTSIDE://localhost:9092 # 내/외부에서 접근할 수 있는 리스너 주소 설정
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT # 리스너의 보안 프로토콜 매핑
      KAFKA_LISTENERS: INSIDE://0.0.0.0:29092,OUTSIDE://0.0.0.0:9092 # 컨테이너 내부에서 사용할 리스너 주소 설정
      KAFKA_INTER_BROKER_LISTENER_NAME: INSIDE # 브로커 간 통신에 사용할 리스너 이름
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181 # Kafka가 Zookeeper에 연결하기 위한 주소
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock # Docker 소켓을 컨테이너와 공유하여 Docker 이벤트를 관리할 수 있도록 설정
  kafka-ui:
    image: provectuslabs/kafka-ui
    container_name: kafka-ui
    ports:
      - "8989:8080"
    restart: always
    environment:
      - KAFKA_CLUSTERS_0_NAME=local
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:29092
      - KAFKA_CLUSTERS_0_ZOOKEEPER=zookeeper:2181
```
    
- **run kafka**

```bash
# 카프카 실행
$ docker-compose up -d
or
$ docker-compose -f docker-compose.yml up

# 토픽생성
$ docker exec -it kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic stock_decrease
Created topic testTopic.

# 프로듀서 실행
$ docker exec -it kafka kafka-console-producer.sh --topic stock_decrease --broker-list 0.0.0.0:9092
>Hello

# 컨슈머 실행
$ docker exec -it kafka kafka-console-consumer.sh --topic stock_decrease --bootstrap-server localhost:9092
Hello

# 카프카 종료
$ docker-compose down
```

<br/>.<br/>

commit log.

- [stock decrease kafka consumer](https://github.com/jihunparkme/Study-project-spring-java/commit/3a0bc13203f824d0f9b0873c76263ff6b428bb0c)
  - [edit kafka consumer decrease process](https://github.com/jihunparkme/Study-project-spring-java/commit/8041e4543a01ae8a5be583c7dee097a149e762bf)
- [stock decrease kafka producer](https://github.com/jihunparkme/Study-project-spring-java/commit/24c3aba7dbc9fb0a6bf547930e2a7c89b3860969)

<br/>...<br/>

🧐 kafka + redis 적용 방식

1. **Producer**는 메시지 발행 전 레디스로 임계값 제어
    - 임계값이 넘지 않았을 경우: 이후 비즈니스 로직 처리를 위해 **Consumer**에게 메시지 전송
    - 임계값이 넘었을 경우: 메시지 미발행 및 사용자에게 처리 불가 알림
2. **Consumer**는 비즈니스 로직만 수행

이렇게 할 경우 임계값은 레디스를 통해 빠르게 확인하고, 실제 처리는 컨슈머가 비동기로 처리할 수 있다.

```java
 public void decrease(final Long userId, final Long stockId) {
      final KafkaStock stock = stockRepository.findById(stockId).orElseThrow();
      // 레디스를 통해 임계값을 확인
      final Long count = redisIncrRepository.increment(stockId);
      stock.validQuantity(count);

      // 실제 처리는 컨슈머가 비동기로 처리
      stockDecreaseProducer.create(userId, stockId);
  }
```

<br/>.<br/>

**🛠️  성능 테스트.**
 - 한정 수량: 50,000
 - User: 296
 - Processes: 8
 - Threads: 37
 - Duration: 3 min (01:52)
 
 > 결과를 보면 정확하게 50,000 건만 성공으로 처리된 것을 볼 수 있다.
 > 
 > 00:46 이후에는 모든 수량이 0으로 조회되어, 재고 부족 오류가 발생하게 된다.
  
 ![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/kafka-redis-performance.png?raw=true 'Result')
 
<br/>

 Kafka 메시지도 정확히 50,000 건만 전송이 되었다.
 
 ![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/kafka-redis-message.png?raw=true 'Result')
 
<br/>

 만일 컨슈머에서 에러가 발생하면 별도로 처리를 할 수 있도록 로그를 남겨둘 수도 있다.
 
 ![Result](https://github.com/jihunparkme/blog/blob/main/img/concurrency/kafka-redis-failed-event.png?raw=true 'Result')
