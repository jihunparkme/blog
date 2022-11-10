# Spring Batch Test

Spring Batch를 테스트해 보는 시간을 가져보려고 한다.

Spring Batch와 아직 어색어색하니까..😬 조금씩 친해지면서 테스트 관련 내용을 업데이트할 예정이다.🙄

## 준비

배치 테스트를 위해 spring-batch-test 의존성이 필요

```gradle
testImplementation("org.springframework.batch:spring-batch-test")
```

## 통합 테스트

먼저 전체 코드는 아래와 같고,

어노테이션들을 먼저 간략히 살펴보면, 일반 스프링 테스트 코드를 작성하면서 많이 보았던 익숙한 어노테이선들도 있을 것이다.

- `@Slf4j`
  - 로깅을 위해 선언
- `@ExtendWith(SpringExtension.class)`
  - Junit5의 라이프사이클에 Test에서 사용할 기능 확장을 위해 선언
- `@SpringBootTest`
  - Spring Boot에서 제공하는 통합 테스트를 위해 선언
  - main configuration class를 찾고, Spring application context를 시작하기 위해 사용
- `@EnableAutoConfiguration`
  - Spring Application Context의 자동 구성을 활성화하여 필요할 수 있는 Bean을 등록
- `@EnableBatchProcessing`
  - Spring Batch 기능을 활성화하고 @Configuration 클래스에서 배치 작업을 설정하기 위한 기본 구성 제공
- `@ComponentScan("com.batch.config")`
  - Component class들을 자동으로 스캔하여 빈으로 등록
- `@ActiveProfiles("local")`
  - 테스트 클래스를 위한 ApplicationContext를 로드할 때 사용할 활성 빈 정의 프로파일 선언
- `@SpringBatchTest`
  - 스프링 배치 기반 테스트를 위해 선언
  - spring-batch-test 의존성이 추가되면 사용 가능

```java
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        CancelOrderJobConfig.class, CancelOrderService.class
})
@EnableAutoConfiguration
@EnableBatchProcessing
@ComponentScan("com.batch.config")
@ActiveProfiles("local")
@SpringBatchTest
class CancelOrderJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    @SqlGroup({
            @Sql(scripts = {"classpath:/sql/cancel-order-insert.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = {"classpath:/sql/cancel-order-delete.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    public void 주문_취소_테스트() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        log.info("jobExecution = {}", jobExecution);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }
}
```

**Class**

- 프로젝트에 Job이 여러개가 있다면, Bean 충돌 예방 차원에서 테스트가 필요한 특정 Job Class만 빈으로 등록하기 위해 `@SpringBootTest`에 특정 Job Class를 선언해주자.
- `@SpringBootTest`에 특정 Job Class만 Bean으로 등록하였으므로 `@ComponentScan`을 할용해서 "com.batch.config" 패키지에 포함된 설정에 필요한 빈들도 등록을 해주자.
  - `@SpringBootTest`에 CancelOrderService.class가 포함된 이유는 CancelOrderJobConfig에서 해당 Service가 의존성으로 필요하지만 Bean 등록 범위에 포함이 되지 않아서 따로 등록해 주었다.

**Method**

- `JobLauncherTestUtils`은 batch Job 테스트의 핵심으로, batch Job을 테스트 환경에서 실행할 수 있도록 도와주는 클래스이다.
  - launchJob() 호출로 Job을 실행
  - Job의 결과는 JobExecution 클래스로 반환
- `@SqlGroup`은 더미테이터 활용에 여러 `@Sql`을 사용하기 위해 선언
  - `@Sql`은 SQL 스크립트를 실행시키기 위해 사용
  - 테스트 메서드 실행 전/후로 두 개의 스크립트를 실행하도록 선언
  

## Reference

> [Spring Batch Unit Testing](https://docs.spring.io/spring-batch/docs/current/reference/html/testing.html#testing)
>
> [Spring batch 테스트하기](https://multifrontgarden.tistory.com/291)
> 
> [Spring Batch 가이드 - Spring Batch 테스트 코드](https://jojoldu.tistory.com/455)