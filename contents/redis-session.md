# Redis로 Session 관리하기

사이드 프로젝트에서 JWT를 쿠키에 저장하는 방식으로 인가를 구현하게 되었습니다.

처음에 각각 명확한 장/단점이 존재하는 쿠키 방식과 세션 방식 중 고민을 많이 했었는데, JWT 정보가 클라이언트 측에 저장되다보니 쿠키에 저장된 JWT 정보만 있으면 너무나도 쉽게 계정을 도용해서 접속할 수 있을 것 같다는 생각이 들었습니다.

사용자가 얼마나 될지 모르겠지만, 그래도 사용자에게 로그인에 대한 찝찝함을 제공하지 않으려면 그래도 안전한 세션 방식을 활용하는 것이 좋을 것 같아서 세션 방식으로 다시 적용하게 되었습니다. (찾다보니 쿠키, 세션의 장점을 모두 활용하는 방식으로 함께 적용한다고도 합니다.)

세션 저장소는 `in-memory data store`(redis)를 선택하게 되었습니다.

Redis는 세션의 단점인 `서버 부하`(많은 사용자가 동시에 접속하는 경우)와 `확장 어려움`(서버가 여러 대로 확장되는 경우 세션 데이터의 일관성 유지 필요)을 어느정도 해결해 줄 수 있다고 생각하여 적용하게 되었습니다.

## Redis in Docker

`Amazon ElastiCache for Redis`를 사용할 수도 있지만 자원의 한계로 인해.. 

Docker 컨테이너로 Redis를 구성해 보려고 합니다.

.

**Pull redis image**

```shell
docker pull redis
```

.

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

## Spring Boot Configuration

[Spring Session - Spring Boot](https://docs.spring.io/spring-session/reference/guides/boot-redis.html)

**Updating Dependencies**

`spring-session-data-redis`
- 스프링 세션으로 Redis를 사용하기 위해 종속성을 추가해야 합니다.
- 스프링 부트는 스프링 세션 모듈에 대한 종속성 관리를 제공하므로 종속성 버전을 명시적으로 선언할 필요가 없습니다.

`spring-boot-starter-data-redis`
- 스프링 부트에서 Redis를 사용하기 위해 종송성을 추가합니다.

```groovy
implementation 'org.springframework.session:spring-session-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

.

**Spring Boot Configuration**

redis가 지원하는 스프링 세션을 설정합니다.

`spring.session.store-type=redis` 설정으로 스프링 부트는 `@EnableRedisHttpSession`를 수동으로 추가하는 것과 동일한 구성을 적용합니다.
- 즉 해당 설정을 해주면 Spring Session으로 Redis를 사용하기 위한 어노테이션 설정이 불필요합니다.

```yml
spring:
  session:
    store-type: redis
```

이것은 `Filter`를 구현한 `springSessionRepositoryFilter`의 이름을 가진 스프링 빈을 생성합니다.
- `Filter`는 스프링 세션에서 지원할 `HttpSession` 구현을 대체하는 역할을 담당합니다.
- 아래와 같이 추가 사용자 지정도 가능합니다.

```yml
server:
  servlet:
    session:
      timeout: 3600 # Session timeout. If a duration suffix is not specified, seconds is used.  
      redis:
        flush-mode: on_save # Sessions flush mode.
        namespace: spring:session # Namespace for keys used to store sessions.
```

.

**Configuring the Redis Connection**

스프링 부트는 기본 포트인 6379 포트의 로컬 호스트에 있는 Redis Server에 Spring Session을 연결하는 `RedisConnectionFactory`를 자동으로 생성합니다. 
- 상용 환경에서는 Redis 서버를 가리키도록 구성을 업데이트해야 합니다. 

```yml
spring:
  data:
    redis:
      host: localhost # Redis server host.
      password: # Login password of the redis server.
      port: 6379 # Redis server port.
```

[Connecting to Redis](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/#data.nosql.redis.connecting)

.

## RedisSessionRepository and RedisIndexedSessionRepository

스프링 세션으로 redis를 사용할 때는 `RedisSessionRepository`와 `RedisIndexedSessionRepository` 중 하나를 선택해야 합니다.
- 두 Repository 모두 세션 데이터를 redis에 저장하는 Session Repository Interface를 구현한 것이지만 세션 인덱싱과 쿼리를 처리하는 방식이 다릅니다.


`RedisSessionRepository`
- 별도의 인덱싱 없이 세션 데이터를 Redis에 저장하는 기본 구현
- 세션 속성을 저장하기 위해 단순한 키-값 구조를 사용
- 각 세션에는 고유한 세션 ID가 할당되고, 세션 데이터는 해당 ID와 연관된 Redis 키로 저장
- 세션을 검색해야 할 때, 저장소는 관련 세션 데이터를 가져오기 위해 세션 ID를 사용하여 Redis에 쿼리
- 인덱싱이 없기 때문에 세션 ID 이외의 속성이나 기준에 기반하여 세션을 쿼리하는 것은 비효율적

`RedisIndexedSessionRepository`
- Redis에 저장된 세션에 대한 인덱싱 기능을 제공하는 확장된 구현
- 속성 또는 기준에 기반하여 효율적으로 세션을 쿼리하기 위해 Redis에 추가 데이터 구조를 도입
- RedisSessionRespository에서 사용하는 키-값 구조 외에도 빠른 조회가 가능하도록 추가 인덱스를 유지
  - ex. 사용자 ID 또는 마지막 액세스 시간과 같은 세션 속성에 기반하여 인덱스 생성 가능
  - 이러한 인덱스는 특정 기준에 기반하여 효율적인 세션 쿼리를 가능하게 하여 성능을 향상시키고 고급 세션 관리 가능
  - 그 외에도 세션 만료 및 삭제도 지원
- 세션 라이프사이클에 따라 일종의 처리를 수행할 수도 있습니다.
  - [Listening to Session Events](https://docs.spring.io/spring-session/reference/configuration/redis.html#listening-session-events)
- 특정 사용자의 모든 세션을 검색할 수도 있습니다.
  - [Finding All Sessions of a Specific User](https://docs.spring.io/spring-session/reference/configuration/redis.html#finding-all-user-sessions)

.

**Configuring the RedisSessionRepository**

스프링 부트 사용 시 `RedisSessionRepository`는 기본 구현입니다.
- 이를 명시적으로 설명하려면 속성을 설정할 수 있습니다.

```yml
spring:
  session:
    redis:
      repository-type: default
```

`@EnableRedisHttpSession`를 선언하여 `RedisSessionRepository`를 구성할 수 있습니다.

```java
@Configuration
@EnableRedisHttpSession
public class SessionConfig {
    // ...
}
```

.

**Configuring the RedisIndexedSessionRepository**

`RedisIndexedSessionRepository`를 사용하려면 아래와 같이 속성을 설정합니다.

```yml
spring:
  session:
    redis:
      repository-type: indexed
```

`@EnableRedisIndexedHttpSession` 주석을 사용하여 `RedisIndexedSessionRepository`를 구성할 수 있습니다.

.

## application.yml

적용한 전체 속성은 아래와 같습니다.

```yml
spring:
  session:
    store-type: redis
    redis:
      repository-type: indexed
  servlet:
    session:
      timeout: 3600
  data:
    redis:
      host: localhost
      port: 6379
```


## RedisConfig

Redis 관련 설정을 구성합니다.

`@EnableRedisRepositories`를 선언하여 RedisRepositories를 활성화합니다.

```java
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /**
     * LettuceConnectionFactory 제공을 위해 Lettuce를 이용합니다.
     */
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

```java
httpSession.setAttribute("member", SessionMember.from(member));
```

**Execute redis**

```shell
docker exec -it --user root 'redis' /bin/bash
```

.

**redis-cli 접속**

redis 컨테이너 내부에 접속하여 redis-cli 접속

```shell
redis-cli
```

.

### Keys

```shell
keys *
```

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/redis/keys.png" width="100%"></center>

로그인 세션을 보면 4개의 key가 저장됩니다.

(1) `spring:session:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:113835272441423113034` (Set)
- username 으로 세션을 가져올 수 있도록 저장되는 인덱스

(2) `spring:session:sessions:expires:$sesssionId` (String)
- 스프링 세션의 만료시간을 관리하는 key

(3) `spring:session:sessions:$sesssionId` (hash)
- 생성된 스프링 세션 데이터

(4) `spring:session:expirations:$expireTime` (Set)
- 스프링 세션의 만료시간

.

### Hash Keys

```shell
hkeys {key}
```

`spring:session:sessions:$sesssionId` Key를 조회해 보면 세션에 담긴 정보를 확인할 수 있습니다.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/redis/sessions.png" width="100%"></center>

.

**check hash values**

`hgetall` 명령어로 해시 밸류를 전부 조회할 수 있습니다.

```shell
hgetall {key}
```


> 여기서 로그아웃을 해도 세션 정보와 만료시간이 남아있게 됩니다.
>
> 로그인 정보와 세션의 생명주기가 완전히 같지 않기 때문에 세션에 로그인 정보를 담고 있습니다.

즉, 코드상으로 만료시간을 60초로 지정하면 spring:session:sessions:expires에는 60초로 설정되고, spring:session:sessions에는 5분을 더한 6분이 만료시간으로 설정됩니다. 그 이유는 세션 세부 데이터가 세션 만료시간(1분)에 삭제되는 순간에도 필요하기 때문에 5분의 시간이 더 주어지는 것입니다.


redis session 삭제 시점

https://velog.io/@sileeee/Redis%EB%82%B4%EB%B6%80%EC%97%90-session-%EC%A0%80%EC%9E%A5%EC%82%AD%EC%A0%9C








redis 위키 확인 Luttuce


https://velog.io/@readnthink/Redis%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%B4-Session%EC%A0%95%EB%B3%B4%EB%A5%BC-%EA%B3%B5%EC%9C%A0%ED%95%98%EC%9E%90


https://zzang9ha.tistory.com/442

https://zuminternet.github.io/spring-session/

https://escapefromcoding.tistory.com/702




## Reference

- [Spring Session](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/#web.spring-session)
- [Redis Configurations](https://docs.spring.io/spring-session/reference/configuration/redis.html)
- [Redis Master Slave 구성하기](https://velog.io/@ililil9482/Redis-Master-Slave-%EA%B5%AC%EC%84%B1)



[Spring Boot로 ElastiCache 간단한 실습해보기](https://devlog-wjdrbs96.tistory.com/314)

