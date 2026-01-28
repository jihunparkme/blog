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









