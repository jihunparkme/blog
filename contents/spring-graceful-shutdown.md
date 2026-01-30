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

웹 서버가 종료될 때, ThreadPoolTaskExecutor 를 통해 백그라운드에서 실행 중인 작업들은 웹 서버의 graceful 설정만으로는 안전한 종료를 완벽히 보장하기 어려워요. 따라서 Custom Thread Pool을 사용한다면 아래와 같은 명시적 설정이 필요하답니다.

```java
@Bean
fun taskExecutor(): TaskExecutor {
    val executor = ThreadPoolTaskExecutor()
    executor.corePoolSize = 5
    executor.maxPoolSize = 10
    
    // 종료 시 대기 중인 작업들을 완료할 때까지 대기
    executor.setWaitForTasksToCompleteOnShutdown(true)
    // 최대 대기 시간 설정 (lifecycle timeout보다 작거나 같게 설정 권장)
    executor.setAwaitTerminationSeconds(30)

    executor.initialize()
    return executor
}
```

## Shutdown Callbacks

> 종료 콜백을 사용하면 우아하게 애플리케이션 종료를 제어할 수 있습니다.

🎯 **종료 콜백 접근 방식**

Spring은 컴포넌트 수준과 컨텍스트 수준의 종료 콜백을 모두 지원합니다. 다음과 같이 콜백을 생성할 수 있습니다.
- @PreDestroy
- DisposableBean interface
- Bean-destroy method
- Global ServletContextListener













## 주의사항

**SIGTERM vs SIGKILL**
- Graceful Shutdown은 OS의 SIGTERM 신호를 받았을 때 작동
- 만약 kill -9(SIGKILL) 명령어로 프로세스를 즉시 종료하면, Spring이 손쓸 새도 없이 꺼지게 되어 Graceful Shutdown이 작동하지 

**Kubernetes 환경**
- 쿠버네티스에서 운영 중이라면 terminationGracePeriodSeconds 설정이 Spring의 타임아웃 설정보다 충분해야 함
- 컨테이너가 먼저 죽어버리면 Spring의 Graceful Shutdown 설정이 무의미