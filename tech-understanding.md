# Tech Understanding

## MSA

<http://guruble.com/마이크로서비스microservice-아키텍처-그것이-뭣이-중헌디/>

<https://martinfowler.com/articles/microservices.html>

## Message Queue

`메시지 지향 미들 웨어(Message Oriented Middleware: MOM)`는 비동기 메시지를 사용하는 다른 응용 프로그램 사이에서의 데이터 송수신을 의미

- MOM을 구현한 시스템이 `메시지 큐(Message Queue: MQ)`

프로그래밍에서 MQ는 프로세스 또는 프로그램 인스턴스가 데이터를 서로 교환할때 사용하는 방법

- 데이터 교환 시 시스템이 관리하는 메세지 큐를 이용하는 것이 특징
- 서로 다른 프로세스나 프로그램 사이에 메시지를 교환할때 `AMQP(Advanced Message Queueing Protocol)`을 이용
  - AMQP : 메세지 지향 미들웨어를 위한 open standard application layer protocol 

서로 다른 부분으로 분리된 대규모 애플리케이션이 메시지 큐를 사용하여 비동기식으로 통신

- 메시지 처리 방법을 활용하여 유지 보수 및 확장이 용이한 시스템을 구축할 수 있음

**장점**

- 비동기(Asynchronous) : Queue에 넣기 때문에 나중에 처리 가능
- 비동조(Decoupling) : 애플리케이션과 분리 가능
- 탄력성(Resilience) : 일부가 실패 시 전체에 영향을 받지 않음
- 과잉(Redundancy): 실패 할 경우 재실행 가능
- 보증(Guarantees): 작업이 처리된 걸 확인 가능
- 확장성(Scalable): 다수의 프로세스들이 큐에 메시지를 보낼 수 있음

MQ는 대용량 데이터를 처리하기 위한 `배치 작업`이나, `채팅 서비스`, `비동기 데이터`를 처리할때 주로 사용

사용자/데이터가 많아지면 요청에 대한 응답을 기다리는 수가 증가하다가 나중에는 대기 시간이 지연되어서 서비스가 정상적으로 되지 못하는 상황이 오게 된다.

그러므로 기존에 분산되어 있던 데이터 처리를 한곳으로 집중하면서 메세지 브로커를 두어서 필요한 프로그램에 작업을 분산 시키는 방법

**사용**

- 다른 프로그램의 API로 부터 데이터 송수신
- 다양한 애플리케이션에서 비동기 통신
- 이메일 발송 및 문서 업로드
- 많은 양의 프로세스 처리

### RabbitMQ

- AMQT 프로토콜을 구현 해놓은 프로그램
- 신뢰성 : 안정성과 성능을 충족할 수 있도록 다양한 기능 제공
- 유연한 라우팅 : Message Queue가 도착하기 전에 라우팅 되며 플러그인을 통해 더 복잡한 라우팅 설정 가능
- 클러스터링 : 로컬네트워크에 있는 여러 RabbitMQ 서버를 논리적으로 클러스터링 할 수 있고 논리적인 브로커도 가능
- 관리 UI로 편리한 관리
- 거의 모든 언어와 운영체제 지원
- 오픈소스이며 상업적 지원

[Spring AMQP](https://spring.io/projects/spring-amqp)

### Apache Kafka

- `대용량의 실시간 로그 처리에 특화`되어 설계
- 기존 범용 메시징 시스템대비 TPS가 매우 우수
  - 단, 특화된 시스템이기 때문에 범용 메시징 시스템에서 제공하는 다양한 기능들은 제공되지 않음
- 분산 시스템을 기본으로 설계
  - 기존 메시징 시스템에 비해 분산 및 복제 구성이 쉬움
- AMQP 프로토콜이나 JMS API를 사용하지 않고 단순한 메시지 헤더를 지닌 TCP기반의 프로토콜을 사용하여 프로토콜에 의한 오버헤드 감소
- 다수의 메시지를 batch형태로 broker에게 한 번에 전달
- 메시지를 기본적으로 메모리에 저장하는 기존 메시징 시스템과는 달리 메시지를 파일 시스템에 저장
  - 별도의 설정을 하지 않아도 데이터의 영속성(durability)이 보장
- 기존 메시징 시스템에서는 처리되지 않고 남아있는 메시지의 수가 많을 수록 시스템의 성능이 크게 감소하였으나, Kafka에서는 메시지를 파일 시스템에 저장하기 때문에 메시지를 많이 쌓아두어도 성능이 크게 감소하지음
  - 많은 메시지를 쌓아둘 수 있기 때문에, 실시간 처리뿐만 아니라 주기적인 batch작업에 사용할 데이터를 쌓아두는 용도로도 사용 가능
- Consumer에 의해 처리된 메시지(acknowledged message)를 곧바로 삭제하는 기존 메시징 시스템과는 달리 처리된 메시지를 삭제하지 않고 파일 시스템에 그대로 두었다가 설정된 수명이 지나면 삭제
  - 처리된 메시지를 일정 기간동안 삭제하지 않기 때문에 메시지 처리 도중 문제가 발생하였거나 처리 로직이 변경되었을 경우 consumer가 메시지를 처음부터 다시 처리(rewind)하도록 할 수 있음
- 기존의 메시징 시스템에서는 broker가 consumer에게 메시지를 push해 주는 방식인데 반해, Kafka는 consumer가 broker로부터 직접 메시지를 가지고 가는 pull 방식으로 동작
  - consumer는 자신의 처리능력만큼의 메시지만 broker로부터 가져오기 때문에 최적의 성능을 낼 수 있음
- 기존의 push 방식의 메시징 시스템에서는 broker가 직접 각 consumer가 어떤 메시지를 처리해야 하는지 계산하고 어떤 메시지를 처리 중인지 트랙킹하였는데, Kafka에서는 consumer가 직접 필요한 메시지를 broker로부터 pull하므로 broker의 consumer와 메시지 관리에 대한 부담이 경감
- 메시지를 pull 방식으로 가져오므로, 메시지를 쌓아두었다가 주기적으로 처리하는 batch consumer의 구현이 가능
- 큐의 기능은 기존의 JMS나 AMQP 기반의 RabbitMQ(데이타 기반 라우팅,페데레이션 기능등)등에 비해서는 많이 부족하지만 대용량 메세지를 지원할 수 있는 것이 가장 큰 특징
  - 특히 분산 환경에서 용량 뿐 아니라, 복사본을 다른 노드에 저장함으로써 노드 장애에 대한 장애 대응성을 가지고 있기 때문에 용량에는 확실하게 강점

[Spring for Apache Kafka](https://spring.io/projects/spring-kafka)

[Kafka Docs](https://kafka.apache.org/081/documentation.html)

[Kafka Wiki](https://cwiki.apache.org/confluence/display/KAFKA/Index)

**Reference**

> <https://kji6252.github.io/2015/12/18/message-quere/>
>
> <https://www.cloudamqp.com/blog/what-is-message-queuing.html>

## Elastic Search

## Mongodb

## Redis

## Hadoop eco system

## Docker

## Kubernetes

---

Kotlin

Memcached (캐시 활용)
