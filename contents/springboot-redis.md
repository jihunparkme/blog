# Spring Boot + Redis

Spring Boot 에 Redis 를 적용하면서 알게된 내용들을 정리해보자.

**Redis 특징**

- Collection(List, Set, Sorted Set, Hash) 지원 
- Race condition 방지
	- Redis 는 Single Thread 로 Atomic 보장
- persistence 지원
	- 서버가 종료되더라도 데이터 리로드 가능

[Spring Data Redis](https://docs.spring.io/spring-data/data-redis/docs/current/reference/html/#redis:setup)

## Ready
**build.gradle**
- Redis 는 Key-Value 형식을 갖는 자료구조
- Spring Data Redis 는 `RedisTemplate` , `Redis Repository` 를 사용하는 두 가지 접근 방식 제공
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'it.ozimov:embedded-redis:0.7.2' # 테스트 용도로 내장 서버 Redis 환경 구성
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

- 객체 기반으로 Redis에 적재

### Config

**RedisConfig.java**
- Redis Repository 사용을 위한 Configuration
```java
@Getter
@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories // Redis Repository 활성화
public class RedisConfig {

    @Value("${spring.cache.redis.host}")
    private String host;

    @Value("${spring.cache.redis.port}")
    private int port;

    /**
     * 내장 혹은 외부의 Redis를 연결
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        return new LettuceConnectionFactory(host, port);
    }

		/**
		 * RedisConnection에서 넘겨준 byte 값 객체 직렬화
		 */
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
	- `timeToLive` : 유효시간(sec), default : -1L)
- Redis에 저장되는 key 형태: `keyspace:@id`
	- key 는 두 개가 생성되는데, `keyspace`와 `keyspace:@id` 형태로 생성
	- id를 null로 저장할 경우 랜덤값으로 설정

```java
@Getter
@RedisHash(value = "result", timeToLive = 3600) // Redis Repository 사용을 위한 
@AllArgsConstructor
@NoArgsConstructor
public class Result {

    @Id
    private String id;
    @Indexed // 필드 값으로 데이터를 찾을 수 있도록 설정 (findByAccessToken)
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
- JPA Repository와 유사하게 JpaRepository 상속
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
## RedisTemplate 사용

- 자료구조 기반으로 Redis에 적재
- Serialize, Deserialize 로 String 을 사용하는 `StringRedisTemplate` 사용
- Spring Data Redis 에서 제공하는 opsForXXX 메서드를 통해 쉬운 Serialize/Deserialize 가능
	- `opsForValue`: Strings Interface
	- `opsForList`:	List Interface
	- `opsForSet`:	Set Interface
	- `opsForZSet`:	ZSet Interface
	- `opsForHash`:	Hash Interface

```java
class RedisTemplateTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

	/*
	 * Strings: opsForValue
	 */
    @Test
    public void string_test() {
        String key = "key01";

        ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
        stringStringValueOperations.set(key, "1");

        final String result01 = stringStringValueOperations.get(key);
        Assertions.assertThat(result01).isEqualTo("1");

        stringStringValueOperations.increment(key);

        final String result02 = stringStringValueOperations.get(key);
        Assertions.assertThat(result02).isEqualTo("2");
    }

	/*
	 * List: opsForList
	 */
    @Test
    public void list_test() {
        String key = "key01";

        ListOperations<String, String> stringStringListOperations = redisTemplate.opsForList();
        stringStringListOperations.rightPush(key, "H");
        stringStringListOperations.rightPush(key, "i");
        stringStringListOperations.rightPushAll(key, " ", "a", "a", "r", "o", "n");

        final String indexOfFirst = stringStringListOperations.index(key, 1);
        Assertions.assertThat(indexOfFirst).isEqualTo("i");

        final Long size = stringStringListOperations.size(key);
        Assertions.assertThat(size).isEqualTo(8);

        final List<String> resultString = stringStringListOperations.range(key, 0, 7);
        Assertions.assertThat(resultString).isEqualTo(Arrays.asList(new String[]{"H", "i", " ", "a", "a", "r", "o", "n"}));
    }

	/*
	 * Set: opsForSet
	 */
    @Test
    public void set_test() {
        String key = "key01";

        SetOperations<String, String> stringStringSetOperations = redisTemplate.opsForSet();
        stringStringSetOperations.add(key, "H");
        stringStringSetOperations.add(key, "i");

        final Set<String> members = stringStringSetOperations.members(key);
        Assertions.assertThat(members.toArray()).isEqualTo(new String[]{"i", "H"});

        final Long size = stringStringSetOperations.size(key);
        Assertions.assertThat(size).isEqualTo(2);

        StringBuffer sb = new StringBuffer();
        final Cursor<String> cursor = stringStringSetOperations.scan(key, ScanOptions.scanOptions().match("*").count(3).build());
        while(cursor.hasNext()) {
            sb.append(cursor.next());
        }
        Assertions.assertThat(sb.toString()).isEqualTo("iH");
    }

	/*
	 * Set: opsForZSet
	 */
    @Test
    public void sorted_set_test() {
        String key = "key01";

        ZSetOperations<String, String> stringStringZSetOperations = redisTemplate.opsForZSet();
        stringStringZSetOperations.add(key, "H", 1);
        stringStringZSetOperations.add(key, "i", 5);
        stringStringZSetOperations.add(key, "~", 10);
        stringStringZSetOperations.add(key, "!", 15);

        final Set<String> range = stringStringZSetOperations.range(key, 0, 3);
        Assertions.assertThat(range.toArray()).isEqualTo(new String[]{"H", "i", "~", "!"});

        final Long size = stringStringZSetOperations.size(key);
        Assertions.assertThat(size).isEqualTo(4);

        final Set<String> rangeByScore = stringStringZSetOperations.rangeByScore(key, 0, 15);
        Assertions.assertThat(rangeByScore.toArray()).isEqualTo(new String[]{"H", "i", "~", "!"});
    }

	/*
	 * Set: opsForHash
	 */
    @Test
    public void hash_test() {
        String key = "key01";

        HashOperations<String, Object, Object> stringObjectObjectHashOperations = redisTemplate.opsForHash();
        stringObjectObjectHashOperations.put(key, "Hi01", "apple");
        stringObjectObjectHashOperations.put(key, "Hi02", "banana");
        stringObjectObjectHashOperations.put(key, "Hi03", "orange");

        final Object get = stringObjectObjectHashOperations.get(key, "Hi01");
        Assertions.assertThat(get).isEqualTo("apple");

        final Map<Object, Object> entries = stringObjectObjectHashOperations.entries(key);
        Assertions.assertThat(entries.get("Hi02")).isEqualTo("banana");

        final Long size = stringObjectObjectHashOperations.size(key);
        Assertions.assertThat(size).isEqualTo(3);
    }
}
```

[Cache와 Redis](https://sabarada.tistory.com/103)

[Redis 기본 명령어](https://sabarada.tistory.com/104)

[Spring Data Redis / RedisTemplate](https://sabarada.tistory.com/105)

[Spring Data Redis / Repository](https://sabarada.tistory.com/106)

[Spring - Redis 연동하기](https://backtony.github.io/spring/redis/2021-08-29-spring-redis-1/)