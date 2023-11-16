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

# Netflix OSS

[Netflix OSS](https://github.com/Netflix)

오픈소스를 위한 소스보다 실제 운영에서 사용하는 코드들로 검증된 솔루션

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

- Circuit Breaker 별로 사용할 TreadPool 지정(ThreadPoolKey)
- Circuit Breaker: Thread Pool = N : 1 관계 가능
- 최대 개수 초과 시 Thread Pool Rejection 발생 -> Fallback 실행
- Command를 호출한 Thread가 아닌 Thread Pool에서 메소드 실행
  - Semaphore 방식의 단점을 해결
- 실제 메소드 실행은 다른 Thread에서 실행되므로 Thread Local 사용 시 주의 필요

`Semaphore`

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
- Client Load Balancer with HTTP Client
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
  - Zuul API Gateway
  - RestTEmplate(@LoadBalanced) -> 서버 주소는 호출할 서버군의 이름을 작성
  - Spring Could Feign (선언전 Http Client)

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

.

## Zuul

[Netflix/zuul](https://github.com/Netflix/zuul)


> [Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix#learn)