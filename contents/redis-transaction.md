# Redis Transactions

프로젝트에 Redis 를 적용하면서 Redis Transaction 을 잘 모르고 사용했다가,

Spring Transaction 과 함께 데이터가 롤백되어 버리는 상황을 맞이하고 Redis Transaction 에 대해 알아보게 되었다.

.

Redis 는 **싱글 스레드 기반**으로 데이터 처리

- 반면, 이벤트 루프(Event Loop)라는 Redis 동작 원리로 여러 클라이언트 요청을 동시에 응답하는 **동시성 보유**
  - 유저 레벨에서는 싱글 스레드로 동작하지만, 커널 I/O 레벨에서는 스레드 풀 이용
- 따라서, 동시성 문제에 대한 처리가 필요

Redis 동시성 처리를 위한 트랜잭션 방법은 `SessionCallback 인터페이스를 구현`하는 방법과 `@Transactional 을 사용`하는 방법이 존재.

## @Transactional

@Transactional 어노테이션 사용을 위해 PlatformTransactionManager Bean 등록하기

- @EnableTransactionManagement 적용
- EnableTransactionSupport true 설정
- JDBC DataSourceTransactionManager 또는 JPA JpaTransactionManager 사용

**application.yml**

```yml
spring:  
  redis:
    host: localhost
    port: 6379
```

**config.java**

```java
@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
@EnableTransactionManagement // <=
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true); // <=

        return redisTemplate;
    }

    @Bean
    public PlatformTransactionManager transactionManager() { // <=
        return new JpaTransactionManager(); // <=
    }
}
```

- JDBC DataSourceTransactionManager 사용 시

```java
@Bean
public PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(dataSource());
}

@Bean
@ConfigurationProperties(prefix = "spring.datasource")
public DataSource dataSource() {
    return DataSourceBuilder.create().build();
}
```

## RedisTemplate

RedisTemplate 을 활용한 트랜잭션 적용

- 여러 명령을 하나로 묶어서 처리할 경우 SessionCallback 인터페이스를 통해 직접적으로 Redis 명령어를 사용하여 트랜잭션 경계를 설정
  - `MULTI` : Redis 트랜잭션 시작 커맨드
    - MULTI 커맨드로 트랜잭션을 시작하면 이후에 입력되는 커맨드를 바로 실행하지 않고 Queue에 적재
  - `EXEC` : Queue에 쌓여있는 커맨드를 일괄적으로 실행 (트랜잭션 종료)
    - RDBMS commit과 유사
  - `DISCARD` : Queue에 쌓여있는 커맨드를 일괄적으로 폐기
    - RDBMS Rollback과 유사
  - `WATCH, UNWATCH` : Lock을 담당하는 커맨드
    - 낙관적 락(Optimistic Lock) 기반
    - WATCH 명령어 사용 시 이후 UNWATCH 전까지는 한 번의 EXEC 또는 Trasaction이 아닌 다른 커맨드만 허용
    - MULTI, EXEC 커맨드만으로 동시성 문제의 트랜잭션의 고립성을 보장할 수 없으므로 사용
    - WATCH로 인하여 예외 발생 시 트랜잭션의 Queue에 쌓여있는 커맨드들을 폐기하는 DISCARD 명령어 등을 통해 처리 가능

**MULTI, EXEC**

multi, exec 커맨드를 활용한 기본적인 트랜잭션

```java
@Test
void multi_and_exec() throws Exception {
    String key = "test:multi_and_exec";
    redisTemplate.execute(new SessionCallback() {
        public Object execute(RedisOperations operations) throws DataAccessException {
            // start transaction
            operations.multi();
            operations.opsForHash().put(key, "user1", "1");
            operations.opsForHash().put(key, "user1", "2");

            // end transaction
            return operations.exec();
        }
    });

    final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
    assertThat(hashOperations.get(key, "user1")).isEqualTo("2");
}
```

**Exception**

예외가 발생하게 되면 트랜잭션으로 인해 입력이 동작하지 않음

```java
@Test
void exception() throws Exception {
    String key = "test:exception";
    try {
        redisTemplate.execute(new SessionCallback() {
            public Object execute(RedisOperations operations) throws DataAccessException {
                // start transaction
                operations.multi();
                operations.opsForHash().put(key, "user1", "1");
                operations.opsForHash().put(key, "user1", "2");

                if (true) {
                    throw new RuntimeException("exception");
                }

                // end transaction
                return operations.exec();
            }
        });
    } catch (Exception e) {
        log.info("exception : {}", e.getMessage(), e);
    }

    final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
    assertThat(hashOperations.get(key, "user1")).isEqualTo(null);
}
```

**WATCH/UNWATCH**

watch 커맨드 이후 입력은 반영되지만, multi~exec 커맨드 사이 트랜잭션에서는 예외가 발생했기 때문에 반영되지 않음

- watch / unwatch 를 통해 key 변경 감지
- watch 를 선언한 key 는 exec 실행 즉시 unwatch 상태로 변경
  - 직접 unwacth 선언 시 watch 가 선언된 모든 key 반환
  - 각각의 key 별로 unwatch 선언은 불가
- 특정 key 가 watch 를 선언하였다면, 트랜잭션 외부에서 변경이 감지될 경우 해당 key 는 트랜잭션 내부에서의 변경을 허용하지 않음
- unwatch 선언된 이후에는 트랜잭션 외부에서 key 가 변경되더라도 해당 key 는 트랜잭션 내부에서 변경 가능

아래 예제에서는 외부 트랜잭션을 생성할 수 없어서 multi~exec 사이 변경사항은 반영되지 않는 부분만 확인

```java
@Test
void watch() throws Exception {
    String key = "test:watch";
    assertThatThrownBy(() -> redisTemplate.execute(new SessionCallback<List<Object>>() {
        @Override
        public List<Object> execute(RedisOperations operations) throws DataAccessException {
            try {
                // optimistic locking
                operations.watch(key);
                operations.opsForHash().put(key, "user1", "1");

                // start transaction
                operations.multi();
                operations.opsForHash().put(key, "user1", "2");

                throw new RuntimeException("exception");
            } catch (Exception e) {
                log.info("exception : {}", e.getMessage(), e);
                // stop transaction
                operations.discard();
            }

            // end transaction
            return operations.exec();
        }
    })).isInstanceOf(RuntimeException.class);

    final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
    assertThat(hashOperations.get(key, "user1")).isEqualTo("1");
}
```

## Reference

[Redis Transactions Ref.](https://docs.spring.io/spring-data/data-redis/docs/current/reference/html/#tx)

[Redis 동시성 처리를 위한 Transaction 사용](https://wildeveloperetrain.tistory.com/137)

[Redis Transaction](https://minholee93.tistory.com/entry/Redis-Transaction)