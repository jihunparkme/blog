# Prometheus & Grafana

[GitBook Spring Boot](https://jihunparkme.gitbook.io/docs/lecture/spring-boot#undefined-9) 정리 글에서 모니터링에 대한 부분을 재정리한 내용입니다.

> 서비스를 운영하며 어디에 어떤 문제가 발생했는지 사전 대응하고, 실제 문제 발생 시에도 원인을 빠르게 파악하고 대처하기 위해
>
> 애플리케이션의 CPU, Memory, Connection, Request 같은 수 많은 지표들을 확인하는 것이 필요

## Micrometer

[Micrometer Documentation](https://micrometer.io/docs)

> 모니터링을 위한 수 많은 툴이 제공되고 있는데, 각 툴마다 전달 방식이 다른 것들을 추상화한 라이브러리가 `Micrometer`
- `Micrometer`는 `application metric facade`라고 불리는데, 애플리케이션의 메트릭(측정 지표)을 Micrometer가 정한 표준 방법으로 모아서 제공(추상화된 Micrometer로 구현체를 쉽게 갈아끼울 수 있음)
- spring boot actuator는 Micrometer를 기본 내장
- 개발자는 Micrometer가 정한 표준 방법으로 메트릭(측정 지표) 전달
  - 사용하는 모니터링 툴에 맞는 구현체 선택
  - 이후 모니터링 툴이 변경되어도 해당 구현체만 변경
  - 애플리케이션 코드는 모니터링 툴이 변경되어도 그대로 유지
