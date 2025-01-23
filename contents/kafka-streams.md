# Kafka Streams

[아파치 카프카 애플리케이션 프로그래밍 with 자바](https://product.kyobobook.co.kr/detail/S000001842177) 도서 내용을 바탕으로 간략하게 작성되었습니다.

## 카프카 스트림즈

카프카 스트림즈는 토픽에 적재된 데이터를 기반으로 상태기반 또는 비상태기반으로 **실시간 변환하여 다른 토픽에 적재**하는 라이브러리

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/kafka-stremas.png?raw=true 'Result')

스트림즈 애플리케이션은 내부적으로 스레드를 1개 이상 생성할 수 있으며, 스레드는 1개 이상의 태스크를 가짐
- 스트림즈의 `task`는 스트림즈 애플리케이션을 실행하면 생기는 데이터 처리 최소 단위

카프카 스트림즈는 컨슈머 스레드를 늘리는 방법과 동일하게 병렬처리를 위해 **파티션**과 **스트림즈 스레드**(또는 프로세스) 개수를 늘려 처리량 향상
- 실제 운영 환경에서는 장애가 발생하더라도 안정적으로 운영할 수 있도록 2개 이상의 서버로 구성하여 스트림즈 애플리케이션을 운영

![Result](https://github.com/jihunparkme/blog/blob/main/img/kafka-streams/parallel-stream.png?raw=true 'Result')