# Prometheus & Grafana

[GitBook Spring Boot](https://jihunparkme.gitbook.io/docs/lecture/spring-boot#undefined-9) 정리 글에서 모니터링에 대한 부분을 재정리한 내용입니다.

> 서비스를 운영하며 어디에 어떤 문제가 발생했는지 사전 대응하고, 실제 문제 발생 시에도 원인을 빠르게 파악하고 대처하기 위해
>
> 애플리케이션의 CPU, Memory, Connection, Request 같은 수 많은 지표들을 확인하는 것이 필요

## Spring Actuator

> [Production-ready Features](https://docs.spring.io/spring-boot/reference/actuator/index.html)

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

### Endpoints

> [Endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html#actuator.endpoints)

**`/actuator`**
- /actuator/`beans`: 스프링 컨테이너에 등록된 스트링 빈 목록
- /actuator/`caches`: /actuator/caches/{cache}:  
- /actuator/`health`: 애플리케이션 문제를 빠르게 인지(전체 상태, db, mongo, redis, diskspace, ping 등 확인 가능)
  - /actuator/health/{*path}: 
  - health 컴포넌트 중 하나라도 문제가 있으면 전체 상태는 DOWN
  - health 정보를 더 자세히 보기 위한 옵션 `management.endpoint.health.show-details=always`
  - 간략히 보기 위한 옵션 `management.endpoint.health.show-components=always`
  - [Auto-configured HealthIndicators](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health.auto-configured-health-indicators)
  - [Writing Custom HealthIndicators](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health.writing-custom-health-indicators)
- /actuator/`info`: 애플리케이션 기본 정보 (default 비활성화)
  - `management.info.<id>.enabled=true`
  - java : 자바 런타임 정보
  - os : OS 정보
  - env : Environment 에서 info. 로 시작하는 정보
  - build : 빌드 정보 (META-INF/build-info.properties 파일 필요)
    ```groovy
    // build.gradle 에 아래 코드를 추가하면 자동으로 빌드 정보 파일 생성
    springBoot {
      buildInfo()
    }
    ```
  - git : git 정보 (git.properties 파일 필요)
    ```groovy
    // git.properties plugin 추가
    id "com.gorylenko.gradle-git-properties" version "2.4.1"
    ```
  - [Writing Custom InfoContributors](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.info.writing-custom-info-contributors)
  - [info endpoints sample](https://github.com/jihunparkme/Inflearn-Spring-Boot/commit/d079267ec76a83406061158a6bfef60eb7d7fb2c)
- /actuator/`conditions`: condition을 통해 빈 등록 시 평가 조건과 일치하거나 일치하지 않는 이유 표시
- /actuator/`configprops`: @ConfigurationProperties 목록
- - /actuator/configprops/{prefix}: 
- /actuator/`env`: Environment 정보
  - /actuator/env/{toMatch}: 
- /actuator/`loggers`: 로깅 관련 정보 확인. 실시간 변경
  - 특정 패키지에 로그 레벨 설정(default. INFO)
    ```properties
    logging.level.hello.controller: debug
    ```
  - 특정 로거 이름 기준으로 조회. /actuator/`loggers/{name}`
    - /actuator/loggers/hello.controller
  - 애플리케이션을 다시 시작하지 않고, (메모리에) 실시간으로 로그 레벨 변경
    ```json
    POST http://localhost:8080/actuator/loggers/hello.controller

    {
      "configuredLevel": "TRACE"
    }
    ```
- /actuator/`heapdump`: 
- /actuator/`threaddump`: 쓰레드 덤프 정보
- /actuator/`metrics`: : 애플리케이션의 메트릭 정보
  - /actuator/metrics/{requiredMetricName}
    ```text
    /actuator/metrics/jvm.memory.used : JVM 메모리 사용량
    --- availableTags
    /actuator/metrics/jvm.memory.used?tag=area:heap

    /actuator/metrics/http.server.requests : HTTP 요청수
    --- availableTags
    /actuator/metrics/http.server.requests?tag=uri:/log
    /actuator/metrics/http.server.requests?tag=uri:/log&tag=status:200
    ```
- /actuator/`scheduledtasks`: 
- /actuator/`mappings`: @RequestMapping 정보 목록

**`/httpexchanges`**
- HTTP 호출 응답 정보. HttpExchangeRepository 구현 빈 등록 필요
- 최대 100개의 HTTP 요청 제공(최대 요청 초과 시 과거 요청을 삭제
- setCapacity() 로 최대 요청수를 변경 가능
- 단순하고 제한이 많은 기능이므로 개발 단계에서만 주로 사용하고, 실제 운영 서비스에서는 모니터링 툴이나 핀포인트, Zipkin 같은 다른 기술 사용 추천

**`/shutdown`**
- 애플리케이션 종료. 기본으로 비활성화

.

외부망을 통해 접근이 필요하다면 `/actuator` 경로에 서블릿 필터, 스프링 인터셉터, 스프링 시큐티리를 통해 인증된 사용자만 접근 가능하도록 설정 필요

```yml
management:
  server:
    port: 1234 # 보안을 위해 내부망에서만 사용 가능하도록 포트 설정
  endpoints:
    info:
      enabled: true # info 엔드포인트 활성화(애플리케이션에 대한 정보(빌드, 메타데이터 등))
    health:
      enabled: true # health 엔드포인트 활성화(애플리케이션의 상태)
    jmx:
      exposure:
        exclude: "*" # JMX(Java Management Extensions) 노출을 전부 제외
    web:
      exposure:
        include: info, health # 웹 인터페이스를 통해 info, health 엔드포인트를 노출하도록 설정
      base-path: /abcdefg/actuator # 엔드포인트 기본 경로 설정
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

