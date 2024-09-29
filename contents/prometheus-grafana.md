# Prometheus & Grafana

[GitBook Spring Boot](https://jihunparkme.gitbook.io/docs/lecture/spring-boot#undefined-9) 정리 글에서 모니터링에 대한 부분을 재정리한 내용입니다.

> 서비스를 운영하며 어디에 어떤 문제가 발생했는지 사전 대응하고, 실제 문제 발생 시에도 원인을 빠르게 파악하고 대처하기 위해
>
> 애플리케이션의 CPU, Memory, Connection, Request 같은 수 많은 지표들을 확인하는 것이 필요

## Spring Actuator

[Production-ready Features](https://docs.spring.io/spring-boot/reference/actuator/index.html)

애플리케이션이 살아있는지, 로그 정보는 정상 설정 되었는지, 커넥션 풀은 얼마나 사용되고 있는지 등 확인

- 지표(metric): CPU 사용량
- 추적(trace): 이슈 코드 추적
- 감사(auditing): 고객 로그인, 로그아웃 이력 추적
- 모니터링: 시스템 상태

**Dependency**

```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

http://localhost:8080/actuator 로 확인 가능

- 애플리케이션 상태 정보: http://localhost:8080/actuator/health
- 각 엔드포인트는 `/actuator/{endpoints}` 형식으로 접근
- 더 많은 기능을 제공받기 위해 엔드포인트 노출 설정 가능(모든 엔드포인트를 웹에 노출)
- 엔드포인트는 shutdown 제외하고 대부분 기본으로 활성화
- 특정 엔드포인트 활성화 시 `management.endpoint.{endpoints}.enabled=true`

```yml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

## Micrometer

[Micrometer Documentation](https://micrometer.io/docs)

> 모니터링을 위한 수 많은 툴이 제공되고 있는데, 각 툴마다 전달 방식이 다른 것들을 추상화한 라이브러리가 `Micrometer`

- `Micrometer`는 `application metric facade`라고 불리는데, 애플리케이션의 메트릭(측정 지표)을 Micrometer가 정한 표준 방법으로 모아서 제공(추상화된 Micrometer로 구현체를 쉽게 갈아끼울 수 있음)
- spring boot actuator는 Micrometer를 기본 내장
- 개발자는 Micrometer가 정한 표준 방법으로 메트릭(측정 지표) 전달
  - 사용하는 모니터링 툴에 맞는 구현체 선택
  - 이후 모니터링 툴이 변경되어도 해당 구현체만 변경
  - 애플리케이션 코드는 모니터링 툴이 변경되어도 그대로 유지

```bash
├── CPU / JVM / CON ...
│   └── Micrometer 표준 측정 방식 # (1. micrometer 표준 방식에 맞추어 설정)
│       └── Micrometer JMX 구현체 # (2. micrometer JMX에 맞도록 변환해서 전달)
│           └── JMX 모니터링 툴
│       └── Micrometer Prometheus 구현체 # (2. Prometheus에 맞도록 변환해서 전달)
│           └── Prometheus 모니터링 툴
└── 
```

## Metric

[Supported Metrics and Meters](https://docs.spring.io/spring-boot/reference/actuator/metrics.html#actuator.metrics.supported)

> Spring Boot는 다양한 기술에 대한 자동 Meters 등록을 제공
>
> 대부분의 상황에서 기본적으로 제공되는 실용적인 메트릭은 모니터링 시스템에 게시할 수 있다.

제공되는 기능들
- JVM Metrics
- System Metrics
- Application Startup Metrics
- Logger Metrics
- Task Execution and Scheduling Metrics
- JMS Metrics
- Spring MVC Metrics
- Spring WebFlux Metrics
- Jersey Server Metrics
- HTTP Client Metrics
- Tomcat Metrics
- Cache Metrics
- Spring Batch Metrics
- Spring GraphQL Metrics
- DataSource Metrics
- Hibernate Metrics
- Spring Data Repository Metrics
- RabbitMQ Metrics
- Spring Integration Metrics
- Kafka Metrics
- MongoDB Metrics
- Jetty Metrics
- @Timed Annotation Support
- Redis Metrics

## Prometheus & Grafana

**Prometheus**
- Metric을 지속해서 수집하고 DB에 저장하는 역할
- [Prometheus Docs](https://prometheus.io/docs/introduction/overview/)

**Grafana**
- Prometheus에 있는 데이터를 불러서 데이터를 그래프로 보여주는 툴
- 다양한 그래프를 제공하고, Prometheus를 포함한 다양한 데이터소스 지원

```bash
├── CPU / JVM / CON ...
│   └── Micrometer 표준 측정 방식 # (1. micrometer 표준 방식에 맞추어 Metric 측정)
│       └── Micrometer Prometheus 구현체
│           # (2. Prometheus는 Metric 지속 수집하여 구현체로 전달)
│           └── Prometheus # (3. Prometheus DB에 Metric 저장)
│           # (Prometheus를 통해 Metric 조회)
│           └── Grafana # (4. Grafana를 통한 조회) 
└── 
```

- (1). spring boot actuator, Micrometer를 사용하면 수 많은 `Metric`이 자동 생성
  - `Micrometer Prometheus 구현체`는 Prometheus가 읽을 수 있는 포멧으로 Metric을 생성
- (2). `Prometheus`는 이렇게 만들어진 Metric을 지속해서 수집
- (3). `Prometheus`는 수집한 Metric을 내부 DB에 저장
- (4). 사용자는 `Grafana` 대시보드 툴을 통해 그래프로 편리하게 Metric을 조회(필요한 데이터는 Prometheus를 통해 조회)

### Prometheus

