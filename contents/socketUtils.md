# SocketUtils.findAvailableTcpPort() BindException: 주소가 이미 사용 중입니다

Jenkins `Build periodically Schedule` 기능을 활용하여 스프링 배치를 주기적으로 빌드해 주고 있다.

.

그러던 어느 날..🌅

Jenkins 에 새로운 배치 아이템이 추가될수록 아래 에러가 종종 발생하기 시작했다.

```shell
org.springframework.boot.web.server.WebServerException: Unable to start embedded Tomcat server
...
Caused by: java.net.BindException: 주소가 이미 사용 중입니다
```

.

로그를 보아하니.. 포트는 랜덤으로 할당되고 있는 것 같아서 관련 코드를 확인해 보았다.

`SocketUtils.findAvailableTcpPort` 를 사용해서 batch Job 마다 사용가능한 랜덤 포트를 할당해주고 있었다.

Application.java

```java
public static void main(String[] args) {
    setRandomPort(50000, 60000);
    System.exit(SpringApplication.exit(SpringApplication.run(Application.class, args)));
}

public static void setRandomPort(int minPort, int maxPort) {
    try {
        String userDefinedPort = System.getProperty("server.port", System.getenv("SERVER_PORT"));
        if (StringUtils.isEmpty(userDefinedPort)) {
            int port = SocketUtils.findAvailableTcpPort(minPort, maxPort);
            System.setProperty("server.port", String.valueOf(port));
            log.info("Random Server Port is set to {}.", port);
        }
    } catch (IllegalStateException e) {
        log.error("No port available in range {} - {}. Default embedded server configuration will be used.", minPort, maxPort);
    }
}
```

50000 ~ 60000 범위에서 포트를 랜덤 하게 사용할 수 있는데 어떻게 포트가 겹칠 수 있는 것이지❓❗️

.

## SocketUtils

[SocketUtils](https://docs.spring.io/spring-framework/docs/5.3.24/javadoc-api/org/springframework/util/SocketUtils.html) 클래스를 먼저 살펴보자.

해당 클래스에 대한 설명이다.

```text
SocketUtils 는 주로 사용 가능한 랜덤 포트에서 외부 서버를 시작하는 통합 테스트 작성을 지원하기 위해 스프링 프레임워크 4.0에 도입되었다.

그러나 이 유틸리티들은 특정 포트의 후속 가용성에 대해 보장하지 않으므로 신뢰할 수 없다.

SocketUtils 를 사용하여 서버에 사용 가능한 로컬 포트를 찾기 보다는 서버가 선택하거나 운영 체제가 할당한 랜덤 포트에서 시작하는 서버의 능력에 의존하는 것이 좋다. 해당 서버와 상호 작용하려면 서버가 현재 사용 중인 포트를 쿼리해야 한다.
```

- `이 유틸리티들은 특정 포트의 후속 가용성에 대해 보장하지 않으므로 신뢰할 수 없다.`
- `서버가 선택하거나 운영 체제가 할당한 랜덤 포트에서 시작하는 서버의 능력에 의존하는 것이 좋다.` 
- 라는 말을 보면..

분명 SocketUtils 를 사용하여 서버에 사용 가능한 로컬 포트를 찾는 방법은 신뢰할 수 없다는 것을 알 수 있다.

그러다 보니 아래와 같이 스프링 6.0 에서는 Deprecated 예정이다.

```text
as of Spring Framework 5.3.16, to be removed in 6.0
```

[Deprecate SocketUtils](https://github.com/spring-projects/spring-framework/issues/28052)

.

spring-boot 프로젝트에 올라온 이슈를 보면 대략적으로 아래와 같은데 이러한 이유 때문에 SocketUtils 클래스가 Deprecated 된 것을 알 수 있다.

- [Stop using SocketUtils.findAvailableTcpPort() wherever possible](https://github.com/spring-projects/spring-boot/issues/9382)

```text
SocketUtils.findAvailableTcpPort() 는 포트가 이미 사용 중이기 때문에 다소 신뢰성이 떨어져 간헐적인 테스트 실패로 이어진다. 
빈 포트를 얻기 위해 AvailableTcpPort 를 찾은 소켓이 실제로 포트를 해제하지 않아서 이런 일이 발생하는지, 아니면 다른 문제가 발생해서 같은 포트를 잡을 수 있는지 모르지만, 어느 쪽이든 결과는 동일하다.
...
우리는 소켓을 여는 모든 것에 0 포트를 전달한 다음, OS 가 할당하는 포트를 사용해야 한다. 
이것은 예를 들어 메인 코드인 TunnelClient 를 약간 변경해야 하지만, 빌드의 안정성을 개선하는 것은 가치가 있다고 생각한다.
```

## 결론

톰켓에 포트를 0 으로 할당하여 OS 가 할당하는 포트를 사용하는 방법도 있지만,

무엇보다 스프링 배치는 배치 모니터링을 위해 톰캣을 사용한다고 하는데, 톰캣을 사용하지 않으면 포트 문제를 생각할 필요가 없었다.

.

아래와 같은 설정으로 간단하게 웹서버 없이 스프링 부트를 실행시킬 수 있도록 할 수 있다.

[application-properties.core.spring.main.web-application-type](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.core.spring.main.web-application-type)
- [WebApplicationType](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/WebApplicationType.html)

```yml
spring:
  main:
    web-application-type: none
```

 .

 실제 로그를 확인해 보면 기존 상태에서는 tomcat 을 실행 시키고 있지만 `web-application-type: NONE` 설정 이후 tomcat 을 싫행시키고 있지 않다.

 ```text
 Tomcat initialized with port(s): 52303 (http)
 ...
 Tomcat started on port(s): 52303 (http)
 ```