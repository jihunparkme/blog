# Kafka Streams

[아파치 카프카 애플리케이션 프로그래밍 with 자바](https://product.kyobobook.co.kr/detail/S000001842177) 도서 내용을 바탕으로 간략하게 작성되었습니다.

# 카프카 스트림즈

> 카프카 스트림즈는 토픽에 적재된 데이터를 기반으로 상태기반 또는 비상태기반으로 **실시간 변환하여 다른 토픽에 적재**하는 라이브러리

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/kafka-stremas.png?raw=true 'Result')

스트림즈 애플리케이션은 내부적으로 스레드를 1개 이상 생성할 수 있으며, 스레드는 1개 이상의 태스크를 가짐
- 스트림즈의 `task`는 스트림즈 애플리케이션을 실행하면 생기는 데이터 처리 최소 단위

## 병렬처리

카프카 스트림즈는 컨슈머 스레드를 늘리는 방법과 동일하게 병렬처리를 위해 **파티션**과 **스트림즈 스레드**(또는 프로세스) 개수를 늘려 처리량 향상
- 실제 운영 환경에서는 장애가 발생하더라도 안정적으로 운영할 수 있도록 2개 이상의 서버로 구성하여 스트림즈 애플리케이션을 운영

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/parallel-stream.png?raw=true 'Result')

## 토폴로지

- `processor`: 카프카 스트림즈에서 토폴로지를 이루는 노드
- `stream`: 노드와 노드를 이은 선

스트림은 토픽의 데이터를 뜻하는데 프로듀서와 컨슈머에서 활용했던 레코드와 동일
- 프로세서에서 소스 프로세서, 스트림 프로세서, 싱크 프로세서 세 가지가 존재

소스 프로세스
- 데이터를 처리하기 위해 최초로 선언해야 하는 노드
- 하나 이상의 토픽에서 데이터를 가져오는 역할

스트림 프로세스
- 다른 프로세서가 반환한 데이터를 처리하는 역할
- 변환, 분기처리와 같은 로직이 데이터 처리의 일종

싱크 프로세서
- 데이터를 특정 카프카 토픽으로 저장하는 역할
- 스트림즈로 처리된 데이터의 최종 종착지

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/topology.png?raw=true 'Result')

## 개발 방법

스트림즈 개발 방법은 두 가지를 제공

- Streams Domain Specific Language 
- processor API

👉🏻 스트림즈DSL로 구현하는 데이터 처리 예시
- 메시지 값을 기반으로 토픽 분기 처리
- 지난 10분간 들어온 데이터의 개수 집계
- 토픽과 다른 토픽의 결합으로 새로운 데이터 생성

👉🏻 프로세서 API로 구현하는 데이터 처리 예시
- 메시지 값의 종류에 따라 토픽을 가변적으로 전송
- 일정한 시간 간격으로 데이터 처리

Streams DSL로 개발하는 방법으로만 진행을 해보려고 한다.

# Streams DSL

스트림즈DSL에는 레코드의 흐름을 추상화한 3가지 개념인 `KStream`, `KTable`, `GlobalKTable`

## KStream

> 레코드의 흐름을 표현한 것으로 메시지 키와 메시지 값으로 구성

`KStream`으로 데이터를 조회하면 **토픽에 존재하는(또는 KStream에 존재하는) 모든 레코드**가 출력

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/kstream-example.png?raw=true 'Result')

## KTable

> KTable은 KStream과 다르게 메시지 키를 기준으로 묶어서 사용

`KStream`은 토픽의 모든 레코드를 조회할 수 있지만 `KTable`은 유니크한 메시지 키를 기준으로 가장 최신 레코드를 사용
- `KTable`로 데이터를 조회하면 **메시지 키를 기준으로 가장 최신에 추가된 레코드의 데이터가 출력**

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/ktable.png?raw=true 'Result')

## GlobalKTable

> `GlobalKTable`은 `KTable`과 동일하게 메시지 키를 기준으로 묶어서 사용

- `KTable`로 선언된 토픽은 1개 파티션이 1개 태스크에 할당되어 사용
- `GlobalKTable`로 선언된 토픽은 모든 파티션 데이터가 각 태스크에 할당되어 사용
- 사용 사례로는 `KStream`과 `KTable`이 데이터 조인을 수행할 때
  - KStream 과 KTable 을 조인하려면 반드시 co-partitioning 되어 있어야 함
  - `co-partitioning`: 조인을 하는 2개 데이터의 파티션 개수가 동일하고 파티셔닝 전략을 동일하게 맞추는 작업

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/globalKTable.png?raw=true 'Result')

## 주요 옵션

> Streams DSL 애플리케이션을 실행할 때 설정해야 하는 필수 옵션과 선택 옵션
>
> [Kafka Streams Configs](https://kafka.apache.org/documentation/#streamsconfigs)

**👉🏻 필수 옵션**
- `bootstrap.servers`:
  - 프로듀서가 데이터를 전송할 대상 카프카 클러스터에 속한 브로커의 호스트 이름:포트 1개 이상 작성
  - 2개 이상 브로커 정보를 입력하여 일부 브로커에 이슈가 발생하더라도 접속하는 데에 이슈가 없도록 설정 가능
- `application.id`:
  - 스트림즈 애플리케이션을 구분하기 위한 고유한 아이디 설정
  - 다른 로직을 가진 스트림즈 애플리케이션들은 서로 다른 application.id 값을 가져야 함

**👉🏻 선택 옵션**
- `default.key.serde`:
  - 레코드의 메시지 키를 직렬화, 역직렬화하는 클래스 지정
  - default. 바이트 직렬/역직렬화 클래스. Serdes.ByteArray().getClass().getName()
- `default.value.serde`:
  - 레코드의 메시지 값을 직렬화, 역직렬화하는 클래스를 지정
  - default. 바이트 직렬/역직렬화 클래스. Serdes.ByteArray().getClass().getName()
- `num.stream.threads`:
  - 스트림 프로세싱 실행 시 실행될 스레드 개수 지정(default: 1)
- `state.dir`:
  - rocksDB 저장소가 위치할 디렉토리 지정
  - default. /tmp/kafka-streams

# Streams DSL 주요 기능

## Run Kafka

📄 **docker-compose**

```yml
version: '3'
services:
  # Zookeeper
  zookeeper-1:
    image: confluentinc/cp-zookeeper:latest
    ports:
      - '32181:32181'

    environment:
      ZOOKEEPER_CLIENT_PORT: 32181
      ZOOKEEPER_TICK_TIME: 2000

  # kafka
  kafka-1:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    ports:
      - '9092:9092'

    depends_on:
      - zookeeper-1
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper-1:32181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-1:29092,EXTERNAL://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      # KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_NUM_PARTITIONS: 4

  kafka-ui:
    image: provectuslabs/kafka-ui
    container_name: kafka-ui
    ports:
      - "8989:8080"
    restart: always
    environment:
      - KAFKA_CLUSTERS_0_NAME=local
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka-1:29092
      - KAFKA_CLUSTERS_0_ZOOKEEPER=zookeeper-1:32181
```

📄 **docker-compose command**

```bash
# 백그라운드에서 커맨드 실행
docker-compose -f docker-compose-kafka.yml up -d

# docker-compose에 정의된 모든 서비스 컨테이너를 한 번에 정지
docker-compose -f docker-compose-kafka.yml stop

# docker-compose에 정의된 모든 서비스 컨테이너를 한 번에 시작
docker-compose -f docker-compose-kafka.yml start

# docker-compose에 정의된 모든 서비스 컨테이너를 한 번에 정지/삭제
docker-compose -f docker-compose-kafka.yml down
```

📄 **create topic**

```bash
# 도커 쉘 접속
docker exec -it kafka /bin/bash

# 토픽 생성
/bin/kafka-topics --create \
--bootstrap-server kafka:9092 \
--partitions 3 \
--topic stream_log
```

## strem(), to()

> 특정 토픽을 KStream 형태로 가져오려면 Streams DSL의 `stream()` 메서드를 사용
> 
> KStream 데이터를 특정 토픽으로 저장하려면 Streams DSL의 `to()` 메서드를 사용

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/stream-to.png?raw=true 'Result')

> 📖 **단순하게 소스 프로세서, 싱크 프로세스로 이루어진 토폴로지를 Streams DSL로 구현하는 예제**

📄 **properties**

```gradle
implementation 'org.apache.kafka:kafka-streams:2.5.0'
```

📄 **애플리케이션 실행**

```java
public class SimpleStreamApplication {

    /**
     * 애플리케이션 아이디 값 기준으로 병렬처리 수행
     * - 다른 스트림즈 애플리케이션을 운영한다면 다른 아이디를 사용
     */
    private static String APPLICATION_NAME = "streams-application";
    /** 스트림즈 애플리케이션과 연동할 카프카 클러스터 정보 */
    private static String BOOTSTRAP_SERVERS = "localhost:9092";
    private static String STREAM_LOG = "stream_log";
    private static String STREAM_LOG_COPY = "stream_log_copy";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        /** 스트림 처리를 위해 메시지 키/값의 역직렬화, 직렬화 방식 지정 */
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        /** 스트림 토폴로지를 정의하기 위한 용도 */
        StreamsBuilder builder = new StreamsBuilder();
        /**
         * 최초의 토픽 데이터를 가져오는 소스 프로세서
         * - KStream 생성 -> stream()
         * - KTable 생성 -> table()
         * - GlobalKTable 생성 -> globalTable()
         * */
        KStream<String, String> stream = builder.stream(STREAM_LOG);
        stream.foreach((k, v) -> System.out.println(k + ": " + v));

        /**
         * 싱크 프로세서
         * - 토픽을 담은 KStream 객체를 다른 토픽으로 전송하기 위한 to()
         */
        stream.to(STREAM_LOG_COPY);

        /**
         * StreamsBuilder로 정의한 토폴로이제 대한 정보와 스트림즈 실행을 위한 기본 옵션을 파라미터로 KafkaStreams 인스턴스 생성
         * 토픽(stream_log)의 데이터를 다른 토픽(stream_log_copy)으로 전달
         */
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
```

📄 **프로듀스 및 컨슘으로 확인**
- stream_log 토픽의 데이터를 stream_log_copy 토픽으로 전송
- 데이터 처리를 위해서 스트림 프로세서가 추가

```bash
# data produce
/bin/kafka-console-producer --bootstrap-server kafka:9092 \
--topic stream_log
> hello
> my
> name
> is
> jihun

# data consume (--from-beginning 토픽의 모든 데이터를 확인)
/bin/kafka-console-consumer --bootstrap-server kafka:9092 \
--topic stream_log_copy --from-beginning
hello
my
name
is
jihun
```

## filter()

> 메시지 키/값을 필터링하여 특정 조건에 맞는 데이터를 골라낼 때는 `filter()` 메서드를 사용

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/filter.png?raw=true 'Result')

📄 **애플리케이션 실행**
- KStream 인스턴스를 생성하고 싶지 않다면, fluent interface style을 적용해볼 수 있다.
- `streamLog.filter((key, value) -> value.length() > 5).to(STREAM_LOG_FILTER);`

```java
public class StreamsFilter {

    private static String APPLICATION_NAME = "streams-filter-application";
    private static String BOOTSTRAP_SERVERS = "localhost:9092";
    private static String STREAM_LOG = "stream_log";
    private static String STREAM_LOG_FILTER = "stream_log_filter";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        /** 소스 프로세서 */
        KStream<String, String> streamLog = builder.stream(STREAM_LOG);

        /** 스트림 프로세서 */
        KStream<String, String> filteredStream = streamLog.filter(
                (key, value) -> value.length() > 5);
        filteredStream.foreach((k, v) -> System.out.println(k + ": " + v));

        /** 싱크 프로세서 */
        filteredStream.to(STREAM_LOG_FILTER);

        KafkaStreams streams;
        streams = new KafkaStreams(builder.build(), props);
        streams.start();

    }
}
```

📄 **프로듀스 및 컨슘으로 확인**
- stream_log_filter 토픽에 5글자가 초과된 데이터만 필터링되어 저장

```bash
/bin/kafka-console-producer --bootstrap-server kafka:9092 --topic stream_log
>hello
>streams
>kafka
>world
>monday

/bin/kafka-console-consumer --bootstrap-server kafka:9092 --topic stream_log_filter
streams
monday
```

## KTable, KStream join()

> `KTable`과 `KStream`는 메시지 키를 기준으로 실시간 데이터들을 조인 가능

사용자의 이벤트 데이터를 데이터베이스에 저장하지 않고도 조인하여 스트리밍 처리가 가능
- KTable은 `이름:주소` 데이터를 가지고, KStream은 `이름:주문정보` 데이터를 가지고 있다면,
- 사용자가 주문을 했을 때, 이미 토픽에 저장된 KTable과 조인하여 주문정보와 주소가 조합된 데이터를 새로 생성

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/join.png?raw=true 'Result')

**코파티셔닝**
- `KTable`, `KStream` 조인 시 가장 중요한 것은 코파티셔닝이 되어 있는지 확인하는 것
  - 코파티셔닝 되어있지 않은 상태에서 조인 시 `topologyException` 발생
- KTable로 사용할 토픽과 KStream으로 사용할 토픽을 생성할 때 `동일한 파티션 개수`, `동일한 파티셔닝`을 사용하는 것이 중요

📄 **create topic**

```bash
# 도커 쉘 접속
docker exec -it kafka /bin/bash

# 토픽 생성
# 파티션: 3개, 파티셔닝 전략: 기본
/bin/kafka-topics --create \
--bootstrap-server kafka:9092 \
--partitions 3 \
--topic address

/bin/kafka-topics --create \
--bootstrap-server kafka:9092 \
--partitions 3 \
--topic order

/bin/kafka-topics --create \
--bootstrap-server kafka:9092 \
--partitions 3 \
--topic order_join

# 생성 토픽 확인
/bin/kafka-topics --bootstrap-server kafka:9092 --describe --topic address
```

📄 **properties**

```gradle
implementation 'org.apache.kafka:kafka-streams:3.5.1'
implementation 'org.rocksdb:rocksdbjni:8.1.1' // Apple Silicon 지원 RocksDB
```

📄 **애플리케이션 실행**

```java
public class KStreamJoinKTable {
    private static String APPLICATION_NAME = "order-join-application";
    private static String BOOTSTRAP_SERVERS = "localhost:9092";
    private static String ADDRESS_TABLE = "address";
    private static String ORDER_STREAM = "order";
    private static String ORDER_JOIN_STREAM = "order_join";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        /**
         * 소스 프로세서
         * - address -> table()
         * - order -> stream()
         */
        KTable<String, String> addressTable = builder.table(ADDRESS_TABLE);
        KStream<String, String> orderStream = builder.stream(ORDER_STREAM);

        /** 스트림 프로세서 */
        orderStream.join(
                // join()을 수행할 KTable 인스턴스
                addressTable,
                // KStream, KTable 에서 동일한 메시지 키를 가진 데이터 발견 경우 각각의 메시지 값을 조합해서 만들 데이터 정의
                (order, address) -> {
                    System.out.println(order + " send to " + address);
                    return order + " send to " + address;
                })
                /** 싱크 프로세서 */
                .to(ORDER_JOIN_STREAM);

        KafkaStreams streams;
        streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
```

📄 **프로듀스 및 컨슘으로 확인**
- `KTable`에 존재하는 메시지 키를 기준으로 `KStream`이 데이터를 조인하여 `order_join` 토픽에서는 물품과 주소 데이터가 조인

```bash
# 이름:주소
/bin/kafka-console-producer --bootstrap-server kafka:9092 --topic address --property "parse.key=true" --property "key.separator=:"
>jihun:Seoul
>gildong:Newyork

# 이름:주문
/bin/kafka-console-producer --bootstrap-server kafka:9092 --topic order --property "parse.key=true" --property "key.separator=:"
>gildong:Galaxy
>jihun:iPhone

/bin/kafka-console-consumer --bootstrap-server kafka:9092 --topic order_join --from-beginning
Galaxy send to Newyork
iPhone send to Seoul
```

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/join-result.png?raw=true 'Result')

⚠️ 만일, 사용자 주소가 변경되는 경우
- KTable은 동일한 메시지 키가 들어올 경우 가장 마지막 레코드를 유효한 데이터로 보므로 가장 최근에 바뀐 주소로 조인을 수행

```bash
# 이름:주소
/bin/kafka-console-producer --bootstrap-server kafka:9092 --topic address --property "parse.key=true" --property "key.separator=:"
>jihun:LA

# 이름:주문
/bin/kafka-console-producer --bootstrap-server kafka:9092 --topic order --property "parse.key=true" --property "key.separator=:"
>jihun:G-Wagon

/bin/kafka-console-consumer --bootstrap-server kafka:9092 --topic order_join --from-beginning
G-Wagon send to LA
```