# Spring Boot 2.4 profile 구동 방식

Spring Boot 2.4 버전이 릴리즈 되면서 application.properties, application.yml 파일 로드 방식에 변화가 있었다.

설정 파일을 단일 파일로 사용할 경우 해당되지 않겠지만, 각 프로필별 속성이 필요할 경우 변화에 대한 인식이 필요하다.

우리는 시간이 없기에.. 관련 Spring blog 내용을 요약해보았다.

.

.

우선 크게 관심 갖어야 할 부분은 아래 항목인 것 같다.

\1. `spring.profiles` -> `spring.config.activate.on-profile`

\2. `spring.profiles.include` -> `spring.profiles.group`

\3. `spring.config.activate.on-profile` 속성이 있는 문서에서 group 사용 불가

- profile 이 적용된 곳에서는 include 가 불가능하다..! 대신 방법은 있다.


## Document Order

- 다중 프로필 YAML 파일이 주어지면, 하위 문서는 상위 문서의 값을 재지정한다.

```yaml
test: "value"
---
test: "overridden-value"
```

## Multi-document Properties Files

- `.properties` 파일도 이제 `.yml` 파일에서 활용했던 다중 프로필 지원이 가능하다.

```properties
test=value
#---
test=overridden-value
```

## Profile Specific Documents

- Spring Boot 2.3에서는 `spring.profiles` 키를 사용하여 프로필 활성화 작업을 수행했지만, Spring Boot 2.4에서는 속성을 `spring.config.activate.on-profile`로 변경되었다.

```properties
test=value		#--> common setting
#---
spring.config.activate.on-profile=dev
test=overridden-value		#--> override setting in specific profile
```

## Profile Activation

- 프로필 활성화
  - 단, 해당 속성은 spring.config.activate.on-profile 과 함께 사용할 수 없다.

```properties
test=value
spring.profiles.active=local
#---
spring.config.activate.on-profile=dev
test=overridden value
#---
spring.config.activate.on-profile=dev
spring.profiles.active=local 				#--> will fail
test=overridden value
```

## Profile Groups

- 하나의  프로필 안에 여러 하위 프로필을 포함시킬 경우 사용된다.

```properties
spring.profiles.group.prod=proddb,prodmq,prodmetrics
```

## Importing Additional Configuration

- 추가 속성이나 파일 가져오기

```properties
application.name=myapp
spring.config.import=developer.properties
```

- prod profile이 활성화된 경우에만 prod.properties를 로드

```properties
...
#---
spring.config.activate.on-profile=prod
spring.config.import=prod.properties
```

## Volume Mounted Configuration Trees

- URL 속성 가져오기
  - 접두사가 없으면 일반 파일 또는 폴더로 간주
  - `configtree:` 접두사를 사용하는 경우 해당 위치에 Kubernetes 스타일의 볼륨 마운트 구성 트리가 있어야 한다는 것을 Spring Boot에게 선언

```properties
spring.config.import=configtree:/etc/config
```

## Cloud Platform Activation

- 특정 클라우드 플랫폼에서 볼륨 마운트 구성 트리(또는 해당 항목의 속성)만 활성화
- `spring.config.activate.on-profile` 속성과 유사한 방식으로 작동하지만 프로파일 이름 대신 CloudPlatform 값을 사용하자.

```properties
spring.config.activate.on-cloud-platform=kubernetes
spring.config.import=configtree:/etc/config
```

## Using Legacy Processing

- Spring Boot 2.4 이전 버전의 설정 방식을 사용할 경우 

```properties
spring.config.use-legacy-processing=true
```

## Example

가장 중요한 Example !!

### 방법 1

- 하나의 `.yml` 파일에서 다중 프로필을 관리할 경우

```yml
server:
  port: 8080
  tomcat:
    uri-encoding: UTF-8
spring:
  profiles:
    active: test # 활성화시킬 프로필
    group: # 포함시킬 하위 프로필명 (application-oauth.yml, application-devdb.yml)
      test: oauth 
      develop: oauth,devdb
      production: oauth,proddb
---
spring:
  config:
    activate:
      on-profile: test
  #생략..
---
spring:
  config:
    activate:
      on-profile: develop
  #생략..
---
spring:
  config:
    activate:
      on-profile: production
  #생략..
```

### 방법 2

- 프로필을 각각의 파일로 분리해서 관리할 경우

***application.yml***

```yaml
server:
  port: 8080
  tomcat:
    uri-encoding: UTF-8
spring:
  profiles:
    active: test # 활성화시킬 프로필
    group: # 포함시킬 하위 프로필명
      test: test,oauth 
      develop: develop,oauth,devdb
      production: production,oauth,proddb
```

***application-test.yml***

```yaml
spring:
  config:
    activate:
      on-profile: test
  #생략..
```

***application-dev.yml***

```yaml
spring:
  config:
    activate:
      on-profile: develop
  #생략..
```

***application-prod.yml***

```yaml
spring:
  config:
    activate:
      on-profile: production
  #생략...
```

**reference** 

> https://spring.io/blog/2020/08/14/config-file-processing-in-spring-boot-2-4

