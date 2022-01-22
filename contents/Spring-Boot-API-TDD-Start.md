# Spring Boot API TDD Start

현재 회사에서는 테스트 코드 문화가 없다 보니 간접적인 경험을 많이 해보고자 관련 영상들을 많이 찾아보고 있다.

최근 [YOUTHCON'21](https://frost-witch-afb.notion.site/YOUTHCON-21-365e94c3df3443e5b1322520a8b1a2ef) 을 보고 손주원님이 발표하신 Spring Boot TDD 발표가 너무 인상 깊었고 많은 도움이 되어 정말 간략하게 내용을 정리해 보았다.

미래의 나 혹은 또 다른 누군가가 이 글을 보고 도움이 되길..

## 인수 테스트

**`인수 테스트` (Acceptance Test)**

- 사용자의 시나리오(요구사항)를 기반으로 수행하는 테스트
- 소프트웨어가 사용자 요구사항을 충족하는지에 대한 테스트가 진행

**`인수 조건` (Acceptance Creteria)**

- 사용자의 요구사항

### `@SpringBootTest`

- 통합 테스트를 위한 @annotation

  - 여러 모듈들이 의도한 대로 협력하는지를 테스트
  - 개발자가 변경할 수 없는 외부 라이브러리까지 묶어서 검증

- 인수 테스트에서는 최대한 많은 자원을 활용하는 것이 좋아 통합 테스트를 진행

### `WebEnvironment`

- 테스트 환경에 웹 환경을 설정하는 속성
- 기본값은 SpringBootTest.WebEnvironment.MOCK
- 실제 서블릿 컨테이너를 띄우지 않고 서블릿 컨테이너를 mocking(모의 객체 주입) 한 것이 실행
  - embedded tomcat 을 띄워서 실제 운영 환경과 동일한 환경 구축

### WebEnvironment.`RANDOM_PORT`

- 테스트 환경에서 PORT 가 충돌하지 않도록 지원

### `@LocalServerPort`

- RANDOM_PORT 사용 시 @LocalServerPort 를 통해 port 확인

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AcceptanceTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 성공() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get("/reviews/1")
        .then()
                .statusCode(HttpStatus.OK.value())
                .assertThat()
                .body("id", equalTo(1))
                .body("content", equalTo("좋았아요"))
                .body("phoneNumber", equalTo("010-1234-1234"));
    }

    @Test
    void 실패() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get("/reviews/1000")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
```

## Controller

- `@SpringBootTest`, `@AutoConfigureMockMvc` 를 사용하여 테스트할 수도 있지만 테스트 환경이 너무 무거워지는 단점이 발생
- 단위테스트로 진행할 경우 @WebMvcTest 활용

### `@WebMvcTest`

- Controller 에 있는 Bean 들만 로드
- 특정 컨트롤러 지정 가능

### `@MockBean`

- 특정 Bean 을 모의 객체로 주입받아 사용

```java
@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    private Long id = 1L;
    private String content = "좋았어요";
    private String phoneNumber = "010-1234-1234";

    @Test
    void 성공() throws Exception {
        //given
        given(reviewService.getById(id))
                .willReturn(new Review(id, content, phoneNumber));

        //when
        ResultActions perform = mockMvc.perform(get("/reviews/" + id));

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("content").value(content))
                .andExpect(jsonPath("phoneNumber").value(phoneNumber));
    }

    @Test
    void 실패() throws Exception {
        //given
        given(reviewService.getById(1000L))
                .willThrow(new ReviewNotFoundException("no review id : " + 1000));

        //when
        ResultActions perform = mockMvc.perform(get("/reviews/" + 1000));

        //then
        perform
                .andExpect(status().isNotFound());
    }
}
```

## Service

- mocking 을 사용하여 데이터베이스에 직접 접근하지 않고 비즈니스 영역에만 집중
- Service 에서는 Spring 통합 테스트가 아닌 단위 테스트로 진행하는 것이 좋음

```java
class ReviewServiceTest {

    private ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private ReviewService reviewService = new ReviewService(reviewRepository);

    private Long id = 1L;
    private String content = "좋았어요";
    private String phoneNumber = "010-1234-1234";

    @Test
    void 성공() {
        //given
        given(reviewRepository.findById(id))
                .willReturn(Optional.of(new Review(id, content, phoneNumber, false)));

        //when
        Review review = reviewService.getById(id);

        //then
        assertThat(review.getId()).isEqualTo(id);
        assertThat(review.getContent()).isEqualTo(content);
        assertThat(review.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    void 실패() {
        //given
        given(reviewRepository.findById(1000L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                //when
                reviewService.getById(1000L))
                //then
                .isInstanceOf(ReviewNotFoundException.class);
    }
}
```

## Repository

### `@DataJpaTest`

- JPA 관련 테스트 설정만 로드

```java
@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void 성공() {
        //given when
        Review review = reviewRepository.findById(1L)
                                      .orElseThrow(RuntimeException::new);

        //then
        assertThat(review.getId()).isEqualTo(1L);
        assertThat(review.getContent()).isEqualTo("재밌어요");
        assertThat(review.getPhoneNumber()).isEqualTo("010-1111-2222");
    }

    @Test
    void 실패(){
        assertThatThrownBy(() ->
                // given when
                reviewRepository.findById(1000L)
                        .orElseThrow(() -> new ReviewNotFoundException("no review id :" + 1000L)))
                // then
                .isInstanceOf(ReviewNotFoundException.class);
    }
}
```

## Reference

[YOUTHCON'21](https://frost-witch-afb.notion.site/YOUTHCON-21-365e94c3df3443e5b1322520a8b1a2ef) 손주원님의 Spring Boot TDD Start

[wenodev/youthcon-spring-boot-tdd-start](https://github.com/wenodev/youthcon-spring-boot-tdd-start/tree/complete)

[단위 테스트 vs 통합 테스트 vs 인수 테스트](https://tecoble.techcourse.co.kr/post/2021-05-25-unit-test-vs-integration-test-vs-acceptance-test/)
