# Kafka Streams

[아파치 카프카 애플리케이션 프로그래밍 with 자바](https://product.kyobobook.co.kr/detail/S000001842177) 도서 내용을 바탕으로 간략하게 작성되었습니다.

## 카프카 스트림즈

카프카 스트림즈는 토픽에 적재된 데이터를 기반으로 상태기반 또는 비상태기반으로 **실시간 변환하여 다른 토픽에 적재**하는 라이브러리

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/kafka-stremas.png?raw=true 'Result')

스트림즈 애플리케이션은 내부적으로 스레드를 1개 이상 생성할 수 있으며, 스레드는 1개 이상의 태스크를 가짐
- 스트림즈의 `task`는 스트림즈 애플리케이션을 실행하면 생기는 데이터 처리 최소 단위

.

👉🏻 **병렬처리**

카프카 스트림즈는 컨슈머 스레드를 늘리는 방법과 동일하게 병렬처리를 위해 **파티션**과 **스트림즈 스레드**(또는 프로세스) 개수를 늘려 처리량 향상
- 실제 운영 환경에서는 장애가 발생하더라도 안정적으로 운영할 수 있도록 2개 이상의 서버로 구성하여 스트림즈 애플리케이션을 운영

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/parallel-stream.png?raw=true 'Result')

.

👉🏻 **토폴로지**

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

