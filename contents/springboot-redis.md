# Spring Boot + Redis
[Spring Data Redis](https://docs.spring.io/spring-data/data-redis/docs/current/reference/html/#redis:setup)

## Ready
**build.gradle**
- Spring Data Redis 는 `RedisTemplate` , `Redis Repository` 를 사용하는 두 방식 제공
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'it.ozimov:embedded-redis:0.7.2' # 내장 서버로 Redis 환경 구성
```

**application.yaml**
- default: `localhost:6379`
```yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600 # 데이터 유지 시간(sec)
      cache-null-values: true # null 캐싱 여부
      host: localhost
      port: 6379
```


## Repository 사용
Repository 방식은 트랜잭션을 지원하지 않으므로 트랜잭션이 필요할 경우 `RedisTemplate` 사용

### Config

**RedisConfig.java**
- Redis Repository 사용을 위한 Configuration
```java
@Getter
@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.cache.redis.host}")
    private String host;

    @Value("${spring.cache.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<?,?> redisTemplate(){
        RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
```

### Entity
**Person.java**
- Redis 에 저장할 객체 정의
	- `value` : Redis keyspace
	* `timeToLive` : 유효시간(sec), default : -1L)
* Redis에 저장되는 키 값의 형태: `keyspace:id`

```java
@Getter
@RedisHash(value = "result", timeToLive = 3600)
@AllArgsConstructor
@NoArgsConstructor
public class Result {

    @Id
    private String id;
    @Indexed // 필드 값으로 데이터를 찾을 수 있도록 설정
    private String ip;
    private String originalText;
    private String translatedText;

    @Builder
    public Result(String ip, String originalText, String translatedText) {
        this.ip = ip;
        this.originalText = originalText;
        this.translatedText = translatedText;
    }
}

```

### Repository
**PersonRedisRepository.java**
- CrudRepository 상속
```java
public interface ResultRedisRepository extends JpaRepository<Result, String> {
    Optional<List<Result>> findByIp(String ip);
}
```

### Test
```java
@Slf4j
@SpringBootTest
@ActiveProfiles("local")
class ResultRedisRepositoryTest {

    @Autowired
    private ResultRedisRepository redisRepository;

    @AfterEach
    void afterAll() {
        redisRepository.deleteAll();
    }

    @Test
    void save() throws Exception {
        // given
        Result result = Result.builder()
                .ip("127.0.0.1")
                .originalText("안녕하세요.")
                .translatedText("hello")
                .build();

        // when
        Result save = redisRepository.save(result);

        // then
        Result find = redisRepository.findById(save.getId()).get();
        log.info("id: {}", find.getId());
        log.info("original text: {}", find.getOriginalText());
        log.info("translated text: {}", find.getTranslatedText());

        Assertions.assertThat(save.getIp()).isEqualTo(find.getIp());
        Assertions.assertThat(save.getOriginalText()).isEqualTo(find.getOriginalText());
        Assertions.assertThat(save.getTranslatedText()).isEqualTo(find.getTranslatedText());
    }

    @Test
    void save_multi() throws Exception {
        // given
        Result rst1 = Result.builder()
                .ip("127.0.0.1")
                .originalText("안녕하세요.")
                .translatedText("hello")
                .build();

        Result rst2 = Result.builder()
                .ip("127.0.0.1")
                .originalText("반갑습니다.")
                .translatedText("Nice to meet you.")
                .build();

        // when
        redisRepository.save(rst1);
        redisRepository.save(rst2);

        // then
        List<Result> results = redisRepository.findByIp(rst1.getIp()).get();
        Assertions.assertThat(results.size()).isEqualTo(2);
    }
}
```

[Spring Boot 에서 Redis 사용하기 :: 뱀귤 블로그](https://bcp0109.tistory.com/328)
[Spring Boot Redis (Lettuce)를 이용한 간단한 API 제작](https://ozofweird.tistory.com/entry/Spring-Boot-Redis-Lettuce%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EA%B0%84%EB%8B%A8%ED%95%9C-API-%EC%A0%9C%EC%9E%91)