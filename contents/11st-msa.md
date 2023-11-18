# 11ST MSA

[11번가 Spring Cloud 기반 MSA로의 전환 - 지난 1년간의 이야기](https://www.youtube.com/watch?v=J-VP0WFEQsY) 영상을 정리한 글이다.

.

입사 전에 이미 본 영상이긴 하지만(당시에는 이해가 되지 않았던 부분이 많았다는 사실..),

입사 후 1년 반이 지난 지금은 입사 전보다는 이해가 잘 될 것이라는 기대를 안고..

지금으로부터는 약 5년 전 영상이지만 회사 기술을 다시 한 번 복기하고자 새로운 마음으로 정리를 해보려고 한다. 💪🏼

## Before

MSA 도입 전의 모습.

- 초대형 거대 Monolithic System
- 낙후된 S/W stack
- 거대한 라인의 공통 모듈

.

이로 인해..

- 많은 개발팀의 코드를 한 번에 배포
  - 한 명의 실수로 모든 코드를 롤백
- 다른 팀의 코드에 영향 범위를 알 수 없으므로 버전업의 어려움
- 거대한 공통 코드로 인한 메모리 부족으로 IDE에 띄우기 버거운 개발 환경

.

Vine Project 의 탄생.
- 점진적 MSA 전환 프로젝트
- 새로운 애플리케이션은 독립된 API 서버로 개발
- 레거시와 함께 운영
- 위 과정을 반복

.

Hybrid MSA

- 업무 도메인 별로 서버 분리
- 레거시 코드에서는 새로운 API 서버들을 호출
- 기존 코드와 새로운 API 호출은 DB Flag 를 통해 Wsitchable 하도록

.

# Netflix OSS

[Netflix OSS](https://github.com/Netflix)

오픈소스를 위한 소스보다 실제 운영에서 사용하는 코드들로 검증된 솔루션

.

## Hystrix

[Netflix/Hystrix](https://github.com/Netflix/Hystrix)

Netflix 가 만든 Fault Tolerance(장애 허용 시스템) Library
- 장애 전파 방지 & Resilience(회복 탄력성)

.

**Hystrix 적용 예**

- Hystrix Annotation 사용
  - Hystrix Javanica, Spring Cloud Netflix 에 포함

```java
@HystrixCommand
public String anyMethodWithExternalDependency() {
    // REST API 로 다른 서버 호출
}

...

public class SampleCommand extends HystrixCommand<String> {
    @Override
    protected String run() {
        // REST API 로 다른 서버 호출
    }
}
```

.

**기능 관점 주요 4가지 기능**
- `Circuit Breaker`
- `Fallback`
- `Isolation`
- `Timeout`

.

**Hystrix Command 호출 시 벌어지는 일**

- `Circuit Breaker`
  - 메소드 실행 결과 성공 혹은 실패(Exception) 발생 여부를 인스턴스 단위로 기록하고 통계
  - 통계에 따라 Circuit Open 여부 결정
- `Fallback`
  - 실패(Exception)한 경우 사용자가 제공한 메소드를 대신 실행
- `Tread Isolation`
  - 해당 메소드를 인터럽트하여 대신 실행
- `Timeout`
  - 특정시간동안 메소드가 종료되지 않은 경우 Exception 발생

.

### Circuit Breaker

**Circuit Breaker**

- **일정 시간** 동안 **일정 개수 이상**의 호출이 발생한 경우, **일정 비율** 이상의 에러가 발생한다면 `Circuit Open`(호출 차단)
  - 장애 전파 차단 효과 제공
- **일정 시간 경과** 후에 단 한 개의 요청에 대해서 호출을 허용하며(Half Open), 이 호출이 성공하면 `Circuit Close`(호출 허용)

```text
hystrix.command.<commandKey>

metrics.rollingStats.timeInMilliseconds (오류 감시 시간) : default. 10초
circuitBreaker.requestVolumeThreshold (감시 시간 내 요청 수) : default. 20개
circuitBreaker.errorThresholdPercentage (요청 대비 오류율) : default. 50%
circuitBreaker.sleepWindowInMilliseconds (Circuit Open 시간) : default. 5초

"10초간 20개 이상의 호출이 발생한 경우 50% 이상의 에러가 발생하면 5초간 Circuit Open"
```

.

**Circuit Breaker 단위**

Hystrix Circuit Breaker 는 `한 프로세스 내`에서 주어진 `CommandKey 단위로` 통계를 내고 동작
- Circuit Breaker 는 CommandKey 단위로 생성
- Circuit Breaker 단위 설정이 가장 중요한 부분(너무 작아도, 너무 커도 좋지 않음)
- 너무 작은 단위일 경우
  - 10초간 20개 이상의 호출이 발생하지 않을 가능성이 높으므로 장애가 발생하더라도 모르고 지나갈 수 있음.
- 너무 큰 단위일 경우
  - 전혀 무관한 기능의 오동작으로 다른 메소드도 호출 차단이 되어버림.

```java
@HystrixCommand(commandKey = "ExtDep1")
public String anyMethodWithExternalDependency1() {
    // 추천 서버 호출 - 상품 추천 #1
}

@HystrixCommand(commandKey = "ExtDep1")
public String anyMethodWithExternalDependency2() {
    // 추천 서버 호출 - 상품 추천 #2
}
```

.

### Fallback

**Fallback**

Fallback으로 지정된 메소드는 아래의 경우 `원본 메소드 대신` 실행

- Circuit Open(호출 차단)
- Any Exception (HystrixBadRequestException 제외)
  - HystrixBadRequestException :
  - Client Error로 fallback을 실행하지 않고, Circuit Open 을 위한 통계 집계에서 제외
  - 만일 Client Error를 다른 Exception으로 던질경우 Circuit Breaker 통계에 집계되어 Client 잘못으로 Circuit Open 및 Fallback 실행으로 오류 인지의 어려움 발생
- Semaphore / ThreadPool Rejection
- Timeout

```java
@HystrixCommand(commandKey = "ExtDep1", fallbackMethod="recommendFallback")
public String anyMethodWithExternalDependency1() {
    // 추천 서버 호출
}

public String recommendFallback() {
    return "미리 준비된 상품 목록";
}
```

잘못된 사용으로 비즈니스 로직의 에러나 장애 상황이 감춰지게 될 수 있으므로 올바른 모니터 도구 사용 필요

.

### Isolation

두 가지 Isolation 방식을 Circuit Breaker 별로 지정 가능

`Thread` (default.)

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/thread-isolation.png?raw=true 'Result')

- Circuit Breaker 별로 사용할 TreadPool 지정(ThreadPoolKey)
- Circuit Breaker: Thread Pool = N : 1 관계 가능
- 최대 개수 초과 시 Thread Pool Rejection 발생 -> Fallback 실행
- Command를 호출한 Thread가 아닌 Thread Pool에서 메소드 실행
  - Semaphore 방식의 단점을 해결
- 실제 메소드 실행은 다른 Thread에서 실행되므로 Thread Local 사용 시 주의 필요

`Semaphore`

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/semaphore-isolation.png?raw=true 'Result')

- Circuit Breaker 한 개당 한 개의 Semaphore 생성
- Semaphore 별로 최대 동시 요청 개수 지정
- 최대 요청 개수 초과 시 Semaphore Rejection 발생 -> Fallback 실행
- Command를 호출한 Client Thread에서 메소드 실행
- 단점) Timeout이 제 시간에 발생하지 못함 -> Client Thread를 중단시킬 수 없으므로..

.

### Timeout

**Timeout**

Hystrix에서는 Circuit Breaker(CommandKey) 단위로 Timeout 설정 가능

```text
hystrix.command.<commandKey>

execution.isolation.thread.timeoutinmilliseconds : default. 1초
```

.

**주의사항**

- Semaphore Isolation인 경우 제 시간에 Timeout이 발생하지 않는 경우가 대부분
- Default 값이 1초로 매우 짧음

.

## Ribbon

[Netflix/ribbon](https://github.com/Netflix/ribbon)

Netflix가 만든 Software Load Balancer를 내장한 RPC(REST) Library
- Client `Load Balancer` with HTTP Client
- API Caller 쪽에 Load Balancer 내장
- 다수의 서버 목록을 애플리케이션 단에서 Load Balancing 하여 호출

```text
Caller Server | Ribbon | -> Server1
              | Ribbon | -> Server2
              | Ribbon | -> Server3
              | Ribbon | -> Server4
```
.

**Ribbon in Spring Cloud**

- Spring Cloud 에서는 Ribbon Client를 사용자가 직접 사용하지 않음
- 옵션이나 설정으로 접하고 직접 Ribbon Client 를 호출해서 사용하지 않음
- Spring Colud의 HTTP 통신이 필요한 요소에 내장되어 있음
  - `Zuul API Gateway`
  - `RestTEmplate`(@LoadBalanced) -> 서버 주소는 호출할 서버군의 이름을 작성
  - `Spring Could Feign`(선언적 Http Client)

참고. API Gateway

```text
MSA 환경에서 API Gateway 필요성

- Single Endpoint 제공
  - API를 사용할 Client들은 API Gateway 주소만 인지
- API 공통 로직 구현
  - Logging, Authentication, Authorization
- Traffic Control
  - API Quota, Throttling
```

.

**기존 Load Balancer 와 다른 점**

- Ribbon은 대부분의 동작이 Programmable 하다.
- Spring Cloud 에서는 아래와 같은 BeanType 으로 Ribbon Client 마다 모든 동작을 코딩 가능
  - [IRule](https://javadoc.io/static/com.netflix.ribbon/ribbon-loadbalancer/2.6.7/com/netflix/loadbalancer/IRule.html) : 주어진 서버 목록에서 어떤 서버를 선택할 것인가.
    - [IRule.java](https://github.com/Netflix/ribbon/blob/master/ribbon-loadbalancer/src/main/java/com/netflix/loadbalancer/IRule.java)
  - [IPing](https://javadoc.io/static/com.netflix.ribbon/ribbon-loadbalancer/2.6.7/com/netflix/loadbalancer/IPing.html) : 각 서버가 살아있는지 검사.
    - [IPing.java](https://github.com/Netflix/ribbon/blob/master/ribbon-loadbalancer/src/main/java/com/netflix/loadbalancer/IPing.java)
  - [ServerList\<Server\>](https://javadoc.io/static/com.netflix.ribbon/ribbon-loadbalancer/2.6.7/com/netflix/loadbalancer/ServerList.html) : 대상 서버 목록 제공.
    - [ServerList.java](https://github.com/Netflix/ribbon/blob/master/ribbon-loadbalancer/src/main/java/com/netflix/loadbalancer/ServerList.java)
  - [ServerListFilter\<Server\>](https://javadoc.io/static/com.netflix.ribbon/ribbon-loadbalancer/2.6.7/com/netflix/loadbalancer/ServerListFilter.html) : 원하는 특성을 가진 후보 서버 목록을 구성하거나 동적으로 획득하여 필터링
    - [ServerListFilter.java](https://github.com/Netflix/ribbon/blob/master/ribbon-loadbalancer/src/main/java/com/netflix/loadbalancer/ServerListFilter.java)
  - [ServerListUpdater](https://javadoc.io/static/com.netflix.ribbon/ribbon-loadbalancer/2.6.7/com/netflix/loadbalancer/ServerListUpdater.html) : 동적 서버 목록 업데이트를 수행하는 다양한 방법에 사용하기 위한 전략
    - [ServerListUpdater.java](https://github.com/Netflix/ribbon/blob/master/ribbon-loadbalancer/src/main/java/com/netflix/loadbalancer/ServerListUpdater.java)
  - IClientConfig
    - [IClientConfig.java](https://github.com/Netflix/ribbon/blob/master/ribbon-core/src/main/java/com/netflix/client/config/IClientConfig.java)
  - [ILoadBalancer](https://javadoc.io/static/com.netflix.ribbon/ribbon-loadbalancer/2.6.7/com/netflix/loadbalancer/ILoadBalancer.html) : 로드 밸런스에 대한 작업을 정의
    - [ILoadBalancer.java](https://github.com/Netflix/ribbon/blob/master/ribbon-loadbalancer/src/main/java/com/netflix/loadbalancer/ILoadBalancer.java)

.

## Eureka

[Netflix/eureka](https://github.com/Netflix/eureka)

Netflix 가 만든 Dynamic Service Discovery

- 등록 : 서버가 자신의 서비스 이름(종류)과 IP 주소, 포트를 등록
- 조회 : 서비스 이름(종류)를 가지고 서버 목록을 조회
  - Ribbon 이 서버 목록을 가져오는 데 사용

.

**Eureke Client 를 탑재할 경우 Spring Application Life Cycle 과 함께 동작**
- 서버 시작 시 Eureka 서버에 자동으로 자신의 서버군 이름과 상태(UP) 등록
  - Eureka 상에 등록된 서버군 이름은 `spring.application.name`
- 주기적인 HeartBeat 으로 Eureka Server 에 자신이 살아 있음을 알림
- 서버 종료 시 Eureka 서버에 자신의 상태(DOWN)를 변경 혹은 목록에서 삭제

.

**Eureka + Ribbon in Spring Cloud**

- Eureka 는 Ribbon 과 결합하여 동작
- 서버에 Eureka Client 와 Ribbon Client 가 함께 설정되면 Spring Cloud 는 다음의 Ribbon Bean 을 대체
  - ServerList<Server>
    - 기본: ConfigurationBasedServerList
    - 변경: `DiscoveryEnabledNIWSServerList`
  - IPing
    - 기본: DummyPing
    - 변경: `NIWSDiscoveryPing`

서버의 목록을 설정으로 명시하는 대신 `Eureka` 를 통해서 `Look Up` 해오는 구현
- Infra 도움 없이 서버 증설과 축소가 간단해 질 수 있음.

> [Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix#learn)

.

## Zuul

[Netflix/zuul](https://github.com/Netflix/zuul)

Sprng Cloud Zuul 는 API Routing 을 Hystrix, Ribbon, Eureka 를 통해 수형
- Spring Cloud 와 가장 잘 통합되어 있는 API Gateway

.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/spring-cloud-zuul.png?raw=true 'Result')

- 각 서버군으로의 모든 호출이 Hystrix Command 로 감싸서 동작
  - Hystrix Command 의 네 가지 장점을 보유
- Hystrix Command 안에는 Ribbon Client 존재
- Ribbon Client 는 Eureka Client 를 통해 실제 서버 목록을 얻어서 호출
- API Gateway 입장에서 어느 서버한테 트래픽을 줘야 하는지 endpoint 를 관리하는 일을 Eureka, Ribbon 에 의해 기본적으로 자동화
- 운영 서비스군에 장애가 있더라도 Gateway 가 죽지 않을 수 있는 최후의 보루가 생김
  - Hystrix 의 Circuit Breaker, Isolation 으로 운영 서비스의 지연이 Zuul 자체를 죽이지 않도록 격리

API 요청들은 각각 Hystrix Command 를 통해 실행되며, 각 API 의 Routing 은 Ribbon, Eureka 의 조합으로 수행

.

**Hystrix Isolation in Spring Cloud Zuul**

- Spring Cloud Zuul 에서 Hystrix Isolation 는 Semephore Isolation 을 기본으로
- Hystrix 의 기본은 Thread Isolation
  - Hystrix Isolation 은 Semaphore/Threa 두 가지 모드 존재
  - Semaphore 는 Circuit Breaker 와 1:1 
  - ThreadPool 은 별도 부여된 ThreadPoolKey 단위로 생성

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/zuul-isolation.png?raw=true 'Result')

- Spring Cloud Zuul 의 기본 설정으로는 Semaphore Isolation
  - 특정 API 군의 장애(지연) 등이 발생하여도 Zuul 자체의 장애로 이어지지 않음
  - 하지만, Semaphore Isolation 사용으로 API Gateway 입장에서 중요한 timeout 기능을 잃게 됨..
  - 품질을 알 수 없는 대단위의 API 서버들이 존재할 때, 한 종류의 API 서버때문에 Zuul 이 영향을 받을 가능성 존재

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/spring-cloud-zuul-thread-pool.png?raw=true 'Result')

- Spring Cloud Zuul 에서의 Thread Isolation 사용
  - Hystrix Timeout 을 통해 특정 서버군의 장애 시에도 Zuul 의 Contaier Work Thread 원활한 반환


```yml
zuul:
  ribbon-isolation-strategy: threa
  threadPool:
    useSeparateThreadPools: true
    threadPoolKeyPrefix: zuulgw
```

.

**Server to Server Call in MSA**

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/server-to-server-call.png?raw=true 'Result')

- Ribbon + Eureka 조합으로 API 간 Peer to Peer 호출
  - Ribbon, Eureka 의 도움으로 받아 Discovery 기반으로 직접 Load Balancing 하여 호출
- Spring Cloud 에서는 다음의 Ribbon + Eureka 기반의 Http 호출 방법 제공
  - `@LoadBalanced RestTemplate`
    - RestTeamplte 이 Ribbon + Eureka 기능을 갖도록 하는 애노테이션
    - RestTeamplte 이 Bean 으로 선언된 것만 적용 가능
  - `Spring Cloud Feign`

.

### Spring Cloud Feign

Declarative Http Client

- Java Interface + Spring MVC Annotation 선언으로 Http 호출이 가능한 Spring Bean 자동 생성
- OpenFeign 기반의 Spring Cloud 확장
- Hystrix + Ribbon + Eureka 와 연동
- Interface 와 @FeignClient 로 interface 타입의 Spring Bean 자동 생성

Ribbon + Eureka 없이 사용

```java
@FeignClient(name = "product", url = "http://localhost:8080")
public interface FeignProductREmoteService {
    @RequestMApping(path = "products/{productId}")
    String getProductInfo(@PathVariable("productId") String productId)
}
```

Ribbon + Eureka 연동
- url 생략 시 Ribbon, Eureka 연동
- Eureka 에서 product 라는 서버 목록을 받아서 메소드 호출 시 알아서 로드 밸런스 수행

```java
@FeignClient(name = "product")
public interface FeignProductREmoteService {
    @RequestMApping(path = "products/{productId}")
    String getProductInfo(@PathVariable("productId") String productId)
}
```

.

**Hystrix 연동**

- Hystrix 를 Classpath 에 넣기
- 메소드 하나하나가 Circuit Breaker 기능에 의해 동작
  - 특정 메소드가 에러가 많다면 호출 차단

```yml
feign:
  hystrix:
    enabled: true
```

.

**Spring Cloud Feign 사용 시 주의**

- Circuit Breaker 단위와 Thread Pool 의 단위가 메소드 단위로 분할되므로 커스텀한 설정을 통해 비슷한 유형의 메소드들은 같은 Circuit Breaker 를 사용하도록 해야 함.

.

**Spring Cloud Feign with Hystrix, Ribbon, Eureka**

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/spring-cloud-feign.png?raw=true 'Result')

- 호출하는 모든 메소드는 Hystrix Command 로 실행
  - Circuit Breaker, Timeout, Isolation, Fallback 적용
- 호출할 서버는 Eureka 를 통해 얻고, Ribbon 으로 Load Balancing 되어 호출

.

# Story

## 장애 시나리오

**`특정 API 서버의 인스턴스가 한 개 DOWN 된 경우`**

**Eureka** : `Heartbeat` 송신이 중단 됨으로 일정 시간 후 목록에서 사라짐
- 서버가 하나 죽었을 때 실제 Caller 까지 서버 목록에서 사라지려면 내부적으로 네 단계를 거치게 됨
- 네 단계 각각 조정할 수 있는 파라미터 옵션이 존재하므로 환경에 맞게 시간을 줄여서 사용 권장(기본값은 상당히 크게 설정)

**Ribbon** : IOException 발생 시 다른 인스턴스로 `Retry`
- 회복 탄력성(Resilience)에 대한 다양한 기능 존재. 대표적으로 Retry
- Hystrix 와 무관하게 IOException 발생 시 어느 서버를 어떻게 몇 번 찌를지 설정 가능

**Hystrix** : Circuit 은 오픈되지 않음(Error = 33%)\
- 서버 3대 중 1대가 장애나면 33%
- Fallback, Timeout 은 동작

.

**`특정 API 가 비정상(지연, 에러) 동작하는 경우`**

**Hystrix** : 해당 API 를 호출하는 Circuit Breaker Open(호출 차단)
- 어느 서버를 찔러도 비정상 동작하므로 에러 비율이 50% 초과
- Fallback, Timeout 도 동작

.

## with Spring Cloud

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/11st-spring-cloud.png?raw=true 'Result')

- 모든 MSA Platform 내의 서버는 Eureka Client 를 탑재
- API Server 들간의 호출도 Spring Cloud Feign 을 통해 Hystrix + Ribbon + Eureka 조합으로 호출

.

**Spring Cloud Config**

- Git 기반의 Config 관리
- 플랫폼 내의 모든 서버들은 Config Client 탑재
- 서버 시작 시 Config 서버가 제공하는 Config 들이 PropertySource 로 등록
- Git Repository 내의 Config 파일들은 다양하게 구성 가능(옵션을 통해 우선순위 조정 가능)
  - 전체 서버 공통 Config
  - 서버군용 Config
  - 특정 서버용 Config

.

**MSA 운영 환경을 위한 전용 모니터링 도구들**

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/11st-spring-cloud-monitoring.png?raw=true 'Result')

- Zipkin, Turbine, Spring Boot Admin

.

**분산 트레이싱에서 서버간 트레이스 정보의 전달**

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/11st-msa/distributed-tracing.png?raw=true 'Result')

- 서버간의 트레이스 정보의 전달은 사용 프로토콜의 헤더를 통해 전달 필요
  - 특정 서버의 endpoint 에서 UUID, tracing 정보 생성 후 HTTP Header 에 계속 들고 다니면서 끝까지 연동
- 이 정보들을 적절한 로깅으로 남기면 특정 UUID 로 서버간 호출된 내용들을 확인 가능
- 다만, 다양한 라이브러리에 의한 Thread 변경으로 Trace 정보의 전달이 어려움
  - 단순한 Thread Local 에 저장하는 방식을 사용하기 어려움
  - Hystrix, RxJava, @Async ..
- 이 문제를 `Spring Cloud Sleuth` 에서 해결

.

**Spring Cloud Sleuth**

- Spring Cloud 에서 제공하는 Distributed Tracing 솔루션
- 대부분의 내외부 호출 구간에서 Trace 정보를 생성 및 전달
- `로그`에 남기거나 수집 서버(`Zipkin`)에 전송하여 검색/시각화
- Spring CLoud Sleuth 가 지원되는 Component
  - Zuul, Servlet, RestTemplate, Hystrix, Feign, RxJava ..
  - 앞단 endpoint 에서 context tracing 정보를 헤더에서 꺼내기 위한 기능
  - 뒷단으로 어떠한 로직을 보낼 때 앞단에서 받은 정보들을 그대로 헤더에 담아서 전달하기 위한 기능
  - 안에서 Thread Change 발생 시 정보들을 옮겨 담는 기능..
  - 추적 정보를 읽지 않고 서버의 입구부터 서버 바깥까지 전달하는 기능을 제공

Spring Cloud Sleuth 사용(dependency) 시 애플리케이션 로그에 Trace Id(UUID)가 함께 출력

```shell
2023-11-17 17:00:00.000 DEBUG [vine-zuul, 123d4fab3k5dd9d3, 192d8b0b78903d09, true]
2023-11-17 17:00:00.000 DEBUG [vine-zuul, 123d4fab3k5dd9d3, 123d4fab3k5dd9d3, true]
```

- Spring Cloud Sleuth 를 적용한 모든 서버들의 애플리케이션 로그가 위처럼 바뀌게 됨
  - [`서버 이름`, `request UUID`, `UUID 에 속한 단위 ID`, `Sampling 여부`]
  - Trace ID(request UUID) : 하나의 Request 에 대해 서버 전체를 걸쳐 동일한 UUID
- 특정 라이브러리를 사용하지 않았다면 서버간의 tracing 정보들이 이미 옮겨 담아서 전달되고 있음
- 로그 수집 / 검색 시스템이 있다면 동일 요청에 해당하는 전체 서버의 로그 분석 가능

.

**Spring Cloud Sleuth with Zipkin**

수집한 로그를 시각화해주는 Twitter 의 Zipkin
- [Distributed Systems Tracing with Zipkin](https://blog.twitter.com/engineering/en_us/a/2012/distributed-systems-tracing-with-zipkin)
- DB 호출 구간은 표현이 안되므로 Spring AOP 를 사용하여 Sleuth API 로 Trace 정보를 직접 생성

.

**Hystrix Monitoring with Netflix Turbine**

- [Netflix/Turbine](https://github.com/Netflix/Turbine)
- 실시간 확인만 가능하고 지난 오류는 확인이 어려운 아쉬움
- InfluxDB 에 일주일치 Hystrix Metrics 를 보관하여 Grafana 를 통해 Dashboard 구성

.

**Spring Boot Admin**

- Eureka 에 등록되어 있는 모든 서버 정보를 표시/제어
- Spring Boot Actuator 호출
- Vine Admin
  - 운영 시 필요한 추가 기능 구현
  - Eureka Server 상태 변경
  - Zuul 내부 Eureka/Ribbon 상태 확인 및 강제 변경



