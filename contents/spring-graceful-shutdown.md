# Spring Graceful Shutdown

## Web Server

[Spring Boot 2.3](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.3-Release-Notes#graceful-shutdown)부터 네 개의 임베디드 웹 서버(Tomcat, Jetty, Undertow, Netty) 모두에 대해 **Graceful Shutdown** 기능을 지원해요.

Graceful Shutdown 활성화를 위해 `server.shutdown` 속성을 `graceful`로 설정하기만 하면 돼요.
- 참고로, [SpringBoot 3.4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes#graceful-shutdown) 버전 부터는 기본적으로 graceful로 설정되어 있어요.

```yml
server:
    shutdown: graceful
```

일부 요청은 정상 종료 단계가 시작되기 직전에 처리될 수 있는데, 이 경우 서버는 지정된 시간까지 작업이 완료되기를 기다려요. 이 유예 기간은 `spring.lifecycle.timeout-per-shutdown-phase` 구성 속성을 사용하여 구성할 수 있어요.

```yml
spring:
    lifecycle:
        timeout-per-shutdown-phase: 1m # default 30s
```

## Thread pools

ThreadPoolTaskExecutor를 사용하는 경우 `WaitForTaskToCompleteOnShutdown` 옵션을 통해 

종료 요청이 수신되면 작업 실행자는 새 작업을 추가할 수 없도록 대기열을 닫고, 현재 실행 중인 작업과 대기 중인 작업이 모두 완료해요.

진행 중인 작업과 대기열에 있는 작업이 완료될 때까지 기다리도록 구성. 최대 대기 시간을 지정.

```java
@Bean
fun taskExecutor(): TaskExecutor {
    val executor = ThreadPoolTaskExecutor()
    executor.corePoolSize = 2
    executor.maxPoolSize = 2
    executor.setAwaitTerminationSeconds(30);
    executor.setWaitForTasksToCompleteOnShutdown(true)
    executor.initialize()
    return executor
}
```

