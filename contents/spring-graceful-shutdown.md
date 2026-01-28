# Spring Graceful Shutdown

**서비스의 품격 있는 퇴장, Spring Graceful Shutdown**

갑작스러운 서버 종료는 처리 중이던 요청의 끊김이나 데이터 유실을 초래할 수 있어요. 이를 방지하기 위해 Spring Boot는 실행 중인 작업을 안전하게 마무리하고 종료하는 **Graceful Shutdown** 기능을 제공해요.

## Web Server

**Web Server의 Graceful Shutdown**

[Spring Boot 2.3](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.3-Release-Notes#graceful-shutdown)부터 주요 4대 임베디드 웹 서버(Tomcat, Jetty, Undertow, Netty) 모두에서 **Graceful Shutdown**을 지원하기 시작했어요.

이 옵션을 활성화하면 서버는 종료 신호를 받았을 때 새로운 요청을 거절하고, 기존에 처리 중이던 요청이 완료될 때까지 기다린답니다.

✅ **설정 방법**

application.yml에 아래 설정을 추가하는 것만으로 간단히 활성화할 수 있어요.

```yml
server:
    shutdown: graceful
```

> 💡 참고, [SpringBoot 3.4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes#graceful-shutdown) 부터는 기본값이 `immediate`에서 `graceful`로 변경되었어요.
> 
> 따라서, 최신 버전을 사용 중이라면 별도 설정 없이도 기본적인 우아한 종료가 작동해요.

✅ **종료 유예 기간 설정**

서버가 무한정 대기할 수는 없으므로, 특정 시간이 지나면 강제로 종료되도록 유예 기간을 설정해야 해요.

```yml
spring:
    lifecycle:
        timeout-per-shutdown-phase: 1m # 기본값은 30초
```

이 설정은 웹 서버뿐만 아니라 Spring Context 내의 다른 Bean들이 종료되는 단계별 타임아웃을 의미해요.

## Thread Pool (TaskExecutor)

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

