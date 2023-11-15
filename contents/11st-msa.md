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

## Netflix OSS

오픈소스를 위한 소스보다 실제 운영에서 사용하는 코드들로 검증된 솔루션

### Hystrix

**Netflix 가 만든 Fault Tolerance(장애 허용 시스템) Library**

- 장애 전파 방지 & Resilience(회복 탄력성)
- 기능 관점 주요 4가지 기능
  - Circuit Breaker
  - Fallback
  - Tread Isolation
  - Timeout

.

**Hystrix 적용**

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

### Eureka

### Ribbon

### Zuul