# Spring Graceful Shutdown

**서비스의 품격 있는 퇴장, Spring Graceful Shutdown**

갑작스러운 서버 종료는 처리 중이던 요청의 연결 끊김이나 데이터 유실을 초래할 수 있습니다. 이를 방지하기 위해 Spring Boot는 실행 중인 작업을 안전하게 마무리하고 종료하는 **Graceful Shutdown** 기능을 제공합니다.

# Web Server

**Web Server의 Graceful Shutdown**

[Spring Boot 2.3](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.3-Release-Notes#graceful-shutdown)부터 주요 4대 임베디드 웹 서버(Tomcat, Jetty, Undertow, Netty) 모두에서 **Graceful Shutdown**을 지원합니다.

이 옵션을 활성화하면 서버는 종료 신호(SIGTERM)를 받았을 때 다음과 같이 동작합니다.
1. **신규 요청 차단**: 새로운 네트워크 연결을 더 이상 받지 않습니다.
2. **기존 요청 처리**: 이미 들어와서 처리 중인 요청들이 완료될 때까지 지정된 시간 동안 기다립니다.

### ✅ 설정 방법

`application.yml`에 아래 설정을 추가하여 활성화할 수 있습니다.

```yml
server:
  shutdown: graceful
```

> 💡 **참고**: [Spring Boot 3.4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes#graceful-shutdown)부터는 기본값이 `immediate`(즉시 종료)에서 `graceful`로 변경되었습니다. 최신 버전을 사용 중이라면 별도 설정 없이도 기본적인 우아한 종료가 작동합니다.

### ✅ 종료 유예 기간(Timeout) 설정

서버가 모든 요청을 무한정 기다릴 수는 없습니다. 따라서 특정 시간이 지나면 강제로 프로세스를 종료하도록 유예 기간을 설정해야 합니다.

```yml
spring:
  lifecycle:
    timeout-per-shutdown-phase: 1m # 기본값은 30초
```

이 설정은 웹 서버뿐만 아니라 Spring Context 내의 다른 Bean들이 종료되는 **단계별(Phase) 타임아웃**을 의미합니다. 웹 요청 처리뿐만 아니라 백그라운드 작업 등 애플리케이션의 전반적인 정리 시간을 고려하여 적절한 값을 설정하는 것이 중요합니다.

# Thread Pool (TaskExecutor)

웹 서버의 Graceful Shutdown 설정은 'HTTP 요청'을 처리하는 스레드에 집중되어 있습니다. 따라서 `@Async`나 별도의 비동기 처리를 위해 커스텀하게 생성한 `ThreadPoolTaskExecutor`는 별도의 설정 없이는 작업 도중 즉시 종료될 위험이 있습니다.

비동기 작업까지 안전하게 마무리하려면 아래와 같이 명시적인 설정이 필요합니다.

```kotlin
@Bean
fun taskExecutor(): TaskExecutor {
    val executor = ThreadPoolTaskExecutor()
    executor.corePoolSize = 5
    executor.maxPoolSize = 10
    
    // 애플리케이션 종료 시 큐에 대기 중인 작업들을 모두 완료할 때까지 대기
    executor.setWaitForTasksToCompleteOnShutdown(true)
    
    // 종료를 기다릴 최대 시간 설정
    // spring.lifecycle.timeout-per-shutdown-phase 값보다 작게 설정하는 것을 권장
    executor.setAwaitTerminationSeconds(30)

    executor.initialize()
    return executor
}
```

### ✅ 주요 설정
- **setWaitForTasksToCompleteOnShutdown(true)**: 프로세스 종료 시 해당 Executor가 즉시 멈추지 않고, 현재 실행 중인 작업과 큐에 쌓인 작업들을 처리할 때까지 기다리도록 합니다.
- **setAwaitTerminationSeconds(30)**: 대기 중인 작업들을 처리하기 위해 최대 몇 초간 기다릴지 결정합니다. 이 시간이 지나면 남은 작업과 상관없이 스레드 풀을 강제로 종료합니다.

# Shutdown Callbacks

> 종료 콜백을 사용하면 우아하게 애플리케이션 종료를 제어할 수 있습니다.

🎯 **종료 콜백 접근 방식**

Spring은 컴포넌트 수준과 컨텍스트 수준의 종료 콜백을 모두 지원합니다. 다음과 같이 콜백을 생성할 수 있습니다.
- **@PreDestroy**
- **DisposableBean interface**
- **Bean-destroy method**
- **Global ServletContextListener**

### Using @PreDestroy

빈 초기화 중에 스프링은 `@PreDestroy`로 주석이 달린 모든 빈 메서드를 등록하고 애플리케이션이 종료되면 이를 호출합니다.

```kotlin
@Component
class Bean1 {
    @PreDestroy
    fun destroy() {
        System.out.println("Callback triggered - @PreDestroy.")
    }
}
```










## 주의사항

**SIGTERM vs SIGKILL**
- Graceful Shutdown은 OS의 SIGTERM 신호를 받았을 때 작동
- 만약 kill -9(SIGKILL) 명령어로 프로세스를 즉시 종료하면, Spring이 손쓸 새도 없이 꺼지게 되어 Graceful Shutdown이 작동하지 

**Kubernetes 환경**
- 쿠버네티스에서 운영 중이라면 terminationGracePeriodSeconds 설정이 Spring의 타임아웃 설정보다 충분해야 함
- 컨테이너가 먼저 죽어버리면 Spring의 Graceful Shutdown 설정이 무의미