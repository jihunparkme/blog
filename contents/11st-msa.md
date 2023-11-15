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

### Circuit Breaker

`Circuit Breaker`

- **일정 시간** 동안 **일정 개수 이상**의 호출이 발생한 경우, **일정 비율** 이상의 에러가 발생한다면 Circuit Open(호출 차단)
  - 장애 전파 차단 효과 제공
- **일정 시간 경과** 후에 단 한 개의 요청에 대해서 호출을 허용하며(Half Open), 이 호출이 성공하면 Circuit Close(호출 허용)

```text
hystrix.command.<commandKey>

metrics.rollingStats.timeInMilliseconds (오류 감시 시간) : default. 10초
circuitBreaker.requestVolumeThreshold (감시 시간 내 요청 수) : default. 20개
circuitBreaker.errorThresholdPercentage (요청 대비 오류율) : default. 50%
circuitBreaker.sleepWindowInMilliseconds (Circuit Open 시간) : default. 5초

"10초간 20개 이상의 호출이 발생한 경우 50% 이상의 에러가 발생하면 5초간 Circuit Open"
```

.

`Circuit Breaker 단위`

Hystrix Circuit Breaker 는 한 프로세스 내에서 주어진 CommandKey 단위로 통계를 내고 동작
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

### Fallback

`Fallback`

Fallback으로 지정된 메소드는 아래의 경우 원본 메소드 대신 실행

- Circuit Open(호출 차단)
- Any Exception (HystrixBadRequestException 제외)
  - HystrixBadRequestException :
  - Client Error로 fallback을 실행하지 않고, Circuit Open 을 위한 통계 집계에서 제외
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

- 잘못된 사용으로 비즈니스 로직의 에러나 장애 상황이 감춰지게 될 수 있음
- 올바른 모니터 도구 사용 필요

### Tread Isolation

### Timeout


.

**Hystrix Command 호출 시 벌어지는 일**

- Circuit Breaker
  - 메소드 실행 결과 성공 혹은 실패(Exception) 발생 여부를 인스턴스 단위로 기록하고 통계
  - 통계에 따라 Circuit Open 여부 결정
- Fallback
  - 실패(Exception)한 경우 사용자가 제공한 메소드를 대신 실행
- Tread Isolation
  - 해당 메소드를 인터럽트하여 대신 실행
- Timeout
  - 특정시간동안 메소드가 종료되지 않은 경우 Exception 발생


## Eureka

## Ribbon

## Zuul