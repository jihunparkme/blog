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

# 스트림즈 DSL

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