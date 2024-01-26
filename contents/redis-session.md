# Redis로 Session 관리하기

사이드 프로젝트에서 JWT를 쿠키에 저장하는 방식으로 인가를 구현하게 되었습니다.
<br/>

처음에 각각 명확한 장/단점이 존재하는 쿠키 방식과 세션 방식 중 고민을 많이 했었는데, JWT 정보가 클라이언트 측에 저장되다 보니 쿠키에 저장된 JWT 정보만 탈취하면 너무나도 쉽게 계정을 도용해서 접속할 수 있을 것 같다는 생각이 들었습니다.
<br/>

사용자가 얼마나 될지 모르겠지만, 그래도 사용자에게 로그인에 대한 찝찝함을 제공하지 않으려면 그래도 안전한 세션 방식을 활용하는 것이 좋을 것 같아서 세션 방식으로 다시 적용하게 되었습니다.
(찾다 보니 쿠키, 세션의 장점을 모두 활용하는 방식으로 함께 적용한다고도 합니다.)
<br/>

.
<br/>

세션 저장소는 `in-memory data store`(redis)를 선택하게 되었습니다.
<br/>

Redis는 세션의 단점인 `서버 부하`(많은 사용자가 동시에 접속하는 경우)와 `확장 어려움`(서버가 여러 대로 확장되는 경우 세션 데이터의 일관성 유지 필요)을 어느 정도 해결해 줄 수 있다고 생각하여 적용하게 되었습니다.

## Redis in Docker

`Amazon ElastiCache for Redis`를 사용할 수도 있지만 자원의 한계로 인해.. Docker 컨테이너로 Redis를 구성해 보려고 합니다.

.
<br/>

**Pull redis image**

```shell
docker pull redis
```

.
<br/>

**Run redis container**

```shell
docker run -itd --name redis -p 6379:6379 --restart=always redis
```

`-itd`

- i: t 옵션과 같이 사용. 표준입력 활성화. 컨테이너와 연결되어있지 않더라도 표준입력 유지
- t: i 옵션과 같이 사용. TTY 모드로 사용하며 bash 사용을 위해 반드시 필요
- d: 컨테이너를 백그라운드로 실행. 실행시킨 뒤 docker ps 명령어로 컨테이너 실행 확인 가능

`--name`

- 해당 컨테이너의 이름 설정
- 이름을 설정해 놓으면 컨테이너 id 외에도 해당 이름으로 컨테이너 설정 가능

`-p 6379:6379`

- 컨테이너 포트를 호스트와 연결
- 컨테이너 외부와 통신할 6379 포트와 컨테이너 내부적으로 사용할 6379 포트 설정

`--restart=always`
- 도커 실행 시 재시작 정책 설정

.
<br/>

## Spring Boot Configuration

**Updating Dependencies**
<br/>

`spring-session-data-redis`
- 스프링 세션으로 Redis를 사용하기 위해 종속성을 추가해야 합니다.
- 스프링 부트는 스프링 세션 모듈에 대한 종속성 관리를 제공하므로 종속성 버전을 명시적으로 선언할 필요가 없습니다.

`spring-boot-starter-data-redis`
- 스프링 부트에서 Redis를 사용하기 위해 종송성을 추가합니다.
- 마찬가지로 스프링 부트가 종속성 관리를 제공하므로 종속성 버전을 명시적으로 선언할 필요가 없습니다.

```groovy
implementation 'org.springframework.session:spring-session-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

.
<br/>

**Spring Boot Configuration**
<br/>

redis가 지원하는 스프링 세션을 설정합니다.

`spring.session.store-type=redis` 설정으로 스프링 부트는 `@EnableRedisHttpSession`를 수동으로 추가하는 것과 동일한 구성을 적용합니다.
- 즉 해당 설정을 추가해 주면 Spring Session으로 Redis를 사용하기 위한 어노테이션 설정이 불필요합니다.

```yml
spring:
  session:
    store-type: redis # Session store type.
```

위 설정으로 `Filter`를 구현한 `springSessionRepositoryFilter`의 이름을 가진 스프링 빈이 자동으로 생성됩니다.
- `Filter`는 스프링 세션에서 지원할 `HttpSession` 구현을 대체하는 역할을 담당합니다.
- 아래와 같이 세션에 대한 추가 사용자 설정도 가능합니다.

```yml
server.servlet.session.timeout= 3600 # Session timeout. If a duration suffix is not specified, seconds is used.
spring.session.redis.flush-mode=on_save # Sessions flush mode.
spring.session.redis.namespace=spring:session # Namespace for keys used to store sessions.
```

.
<br/>

**Configuring the Redis Connection**
<br/>

스프링 부트는 기본적으로 `6379` 포트의 `localhost`에 있는 Redis Server에 Spring Session을 연결하는 `RedisConnectionFactory`를 자동으로 생성합니다. 
- 개발 환경에서는 아래 설정을 생략할 수 있지만, 상용 환경에서는 Redis 서버를 가리키도록 구성을 업데이트해야 합니다. 

```yml
spring:
  data:
    redis:
      host: 0.0.0.0 # Redis server host.
      password: # Login password of the redis server.
      port: 6379 # Redis server port.
```

.
<br/>

## RedisSessionRepository and RedisIndexedSessionRepository

스프링 세션으로 redis를 사용할 때는 `RedisSessionRepository`와 `RedisIndexedSessionRepository` 중 하나를 선택해야 합니다.
- 두 Repository 모두 세션 데이터를 redis에 저장하는 Session Repository Interface를 구현한 것이지만 세션 인덱싱과 쿼리를 처리하는 방식이 다릅니다.

.
<br/>

`RedisSessionRepository`
- **별도의 인덱싱 없이** 세션 데이터를 Redis에 저장하는 기본 구현
- 세션 속성을 저장하기 위해 **단순한 키-값 구조**를 사용
- 각 세션에는 고유한 세션 ID가 할당되고, 세션 데이터는 해당 ID와 연관된 Redis 키로 저장
- 세션을 검색해야 할 때, 저장소는 관련 세션 데이터를 가져오기 위해 **세션 ID를 사용하여 Redis에 쿼리**
- 인덱싱이 없기 때문에 세션 ID 이외의 **속성이나 기준에 기반하여 세션을 쿼리하는 것은 비효율적**

`RedisIndexedSessionRepository`
- Redis에 저장된 세션에 대한 **인덱싱 기능을 제공**하는 확장된 구현
- 속성 또는 기준에 기반하여 **효율적으로 세션을 쿼리**하기 위해 Redis에 추가 데이터 구조를 도입
- RedisSessionRespository에서 사용하는 키-값 구조 외에도 **빠른 조회가 가능하도록 추가 인덱스를 유지**
  - ex) 사용자 ID 또는 마지막 액세스 시간과 같은 세션 속성에 기반하여 인덱스 생성 가능
  - 이러한 인덱스는 특정 기준에 기반하여 효율적인 세션 쿼리를 가능하게 하여 성능을 향상시키고 고급 세션 관리 가능
  - 변경된 속성을 추적하고 해당 속성만 업데이트
  - 그 외에도 세션 만료 및 삭제도 지원..
- 세션 라이프사이클에 따라 일종의 처리를 수행할 수도 있습니다.
  - [Listening to Session Events](https://docs.spring.io/spring-session/reference/configuration/redis.html#listening-session-events)
- 특정 사용자의 모든 세션을 검색할 수도 있습니다.
  - [Finding All Sessions of a Specific User](https://docs.spring.io/spring-session/reference/configuration/redis.html#finding-all-user-sessions)

.
<br/>

**Configuring the RedisSessionRepository**
<br/>

스프링 부트 사용 시 `RedisSessionRepository`는 기본 구현입니다.
- 이를 명시적으로 설명하려면 속성을 설정할 수 있습니다.

```yml
spring:
  session:
    redis:
      repository-type: default
```

`@EnableRedisHttpSession`를 선언하여 `RedisSessionRepository`를 구성할 수도 있습니다.
<br/>

```java
@Configuration
@EnableRedisHttpSession
public class SessionConfig {
    // ...
}
```

.
<br/>

**Configuring the RedisIndexedSessionRepository**
<br/>

`RedisIndexedSessionRepository`를 사용하려면 아래와 같이 속성을 설정합니다.
<br/>

```yml
spring:
  session:
    redis:
      repository-type: indexed
```

`@EnableRedisIndexedHttpSession` 주석을 사용하여 `RedisIndexedSessionRepository`를 구성할 수도 있습니다.

.

## application.yml

적용한 전체 속성은 아래와 같습니다.
<br/>

```yml
spring:
  session:
    store-type: redis
    redis:
      repository-type: indexed  
  data: # 생략 가능
    redis:
      host: localhost
      port: 6379

server:
  servlet:
    session:
      timeout: 3600
```


## RedisConfig

Redis 관련 설정을 구성합니다.

`@EnableRedisRepositories`를 선언하여 RedisRepositories를 활성화합니다.
<br/>

```java
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

        // redis-cli 을 통한 데이터 조회 시 알아볼 수 있는 형태로 변환하기 위해 key-value Serializer 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        return redisTemplate;
    }
}
```

## Check Session in Redis

Spring Session으로 Redis를 사용하기 위한 설명을 모두 마쳤다면, 로그인 후 세션을 저장하고 Redis에 저장된 세션 정보를 확인해 보겠습니다.
- 세션 인덱스 설정이 필요할 경우 `PRINCIPAL_NAME_INDEX_NAME`에 username으로 사용할 필드를 저장해 주시면 됩니다.
- A session index that contains the current principal name (i.e. username)

**create a session**

```java
httpSession.setAttribute("member", SessionMember.from(member));
httpSession.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, email);
```

**Execute redis**

```shell
docker exec -it --user root 'redis' /bin/bash
```

.
<br/>

**redis-cli 접속**
<br/>

redis 컨테이너 내부에 접속하여 redis-cli 접속
<br/>

```shell
redis-cli
```

.

### Create a Session

**Keys**
<br/>

```shell
keys *
```

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/redis/keys.png" width="100%"></center>
<br/>

로그인 후 저장된 세션을 보면 4개의 key가 저장됩니다.

(1) `spring:session:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:113835272441423113034` (Set)
- username 으로 세션을 가져올 수 있도록 저장되는 인덱스

(2) `spring:session:sessions:expires:{sesssionId}` (String)
- 스프링 세션 만료 key

(3) `spring:session:sessions:{sesssionId}` (hash)
- 스프링 세션 데이터

(4) `spring:session:expirations:{expireTime}` (Set)
- 스프링 세션의 만료시간

.
<br/>

세션 만료 시간은 `Session.getMaxInactiveInterval()`을 기반으로 각 세션과 연결됩니다. 
- 스프링 세션은 Redis의 삭제 및 만료된 [keyspace notifications](http://redis.io/topics/notifications)에 의존하여 `SessionDeletedEvent`, `SessionExpiredEvent` 를 각각 실행합니다.
- 만료는 세션 데이터를 더 이상 사용할 수 없음을 의미하므로 세션 키 자체에서 직접 추적되지 않고, 대신 세션 만료 키(`spring:session:sessions:expires`)가 사용됩니다.
- 세션 만료 키(`spring:session:sessions:expires`)가 삭제되거나 만료되면 keyspace notifications이 실제 세션의 조회를 트리거하고 `SessionDestroyedEvent`가 발생합니다.

.
<br/>

**Hash Keys**
<br/>

```shell
hkeys {key}
```

`spring:session:sessions:$sesssionId` Key를 조회해 보면 세션에 담긴 정보를 확인할 수 있습니다.
<br/>

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/redis/sessions.png" width="100%"></center>

- `creationTime`: 세션 생성시간
- `lastAccessedTime`: 마지막 세션 조회 시간
- `sessionAttr`: 세션에 저장한 데이터
- `maxInactiveInterval`: 세션 만료시간(sec)

.
<br/>

**check hash values**
<br/>

`hgetall` 명령어로 해시 밸류를 전부 조회할 수도 있습니다.
<br/>

```shell
hgetall {key}
```

### Session Expired

만료 시간은 테스트를 위해 60초로 설정해 두었습니다.
<br/>

```yml
server:
  servlet:
    session:
      timeout: 60
```

**세션 만료 전**
<br/>

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/redis/01.png" width="100%"></center>

.
<br/>

**세션 만료 후(60초 후)**
<br/>

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/redis/02.png" width="100%"></center>

60초가 경과하고 세션이 만료되어야 하는데 `spring:session:sessions:expires:{sesssionId}`, `spring:session:expirations:{expireTime}` 키는 삭제되지만  
`spring:session:sessions:{sesssionId}`는 남아있는 것을 확인할 수 있습니다.
- 세션은 남아있지만 세션 안에 있는 사용자 정보는 삭제됩니다.
<br/>

.
<br/>

그 이유는 [Storage Details](https://docs.spring.io/spring-session/reference/api.html#api-redisindexedsessionrepository-storage)에서 찾아볼 수 있었습니다.
<br/>

```text
Note that the expiration that is set to five minutes after the session actually expires. This is necessary so that the value of the session can be accessed when the session expires. An expiration is set on the session itself five minutes after it actually expires to ensure that it is cleaned up, but only after we perform any necessary processing.
```

실제 세션이 만료되는 시점은 세션 만료 시점(세션 생성 60초 후)에 세션 값에 엑세스를 위해, `만료 설정 시간(60초) + 5분` 뒤에 실제 만료되는 것을 확인할 수 있습니다.
- 세션 세부 데이터가 세션 만료시간(1분)에 삭제되는 순간에도 필요하기 때문에 5분의 시간이 더 주어지게 됩니다.

만료시간을 60초로 지정하였으므로 `spring:session:sessions:expires`에는 60초로 설정되고, `spring:session:sessions`에는 5분을 더한 6분이 만료시간으로 설정됩니다. 


.
<br/>

그렇게 세션이 생성되고 6분 뒤(세션 만료 설정 1분 + redis 기본 설정 5분)에 세션이 사라지는 것을 확인할 수 있습니다.
<br/>

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/redis/03.png" width="40%"></center>

## Reference

글 작성에 참고한 글들.

- [[Docs] Spring Session - Spring Boot](https://docs.spring.io/spring-session/reference/guides/boot-redis.html)
- [[Docs] Connecting to Redis](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/#data.nosql.redis.connecting)
- [[Docs] Spring Session](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/#web.spring-session)
- [[Docs] Redis Configurations](https://docs.spring.io/spring-session/reference/configuration/redis.html)
- [[Docs] Storage Details](https://docs.spring.io/spring-session/reference/api.html#api-redisindexedsessionrepository-storage)
- [Redis내부에 session 저장/삭제](https://velog.io/@sileeee/Redis%EB%82%B4%EB%B6%80%EC%97%90-session-%EC%A0%80%EC%9E%A5%EC%82%AD%EC%A0%9C)

나중에 참고해 보면 좋을 글들.

- [Redis Master Slave 구성하기](https://velog.io/@ililil9482/Redis-Master-Slave-%EA%B5%AC%EC%84%B1)
- [Spring Boot로 ElastiCache 간단한 실습해보기](https://devlog-wjdrbs96.tistory.com/314)