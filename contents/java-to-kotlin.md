# Java to Kotlin

## Kotlin version

먼저 코틀린 설정을 위해 아무 `.java` 파일 우클릭 후 `Convert Java File to Kotlin File` 기능을 사용하면 `OK, Configure Kotlin In the Project`로 코틀린 설정이 가능합니다.

`JDK 21`을 사용할 예정이라서 Kotlin 버전은 `2.1.0`으로 설정하였습니다.

※ [Which versions of Kotlin are compatible with which versions of Java?](https://stackoverflow.com/questions/63989767/which-versions-of-kotlin-are-compatible-with-which-versions-of-java)

<center><img src="https://github.com/jihunparkme/blog/blob/main/img/java-tio-kotlin/kotlin-version.png?raw=true" width="80%"></center>

IDE의 도움으로 kotlin 설정을 마치면 `build.gradle`, `settings.gradle` 파일에 코틀린 설정이 추가됩니다.

**Kotlin 설정 및 gradle.kts로 변환**

䷿AS-IS) build.gradle

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.5'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '21'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // firebase Cloud Firestore
    implementation 'com.google.firebase:firebase-admin:9.2.0'

    // util
    implementation 'org.jsoup:jsoup:1.17.1'
    implementation 'org.apache.commons:commons-lang3'

    // lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // tool
    compileOnly 'org.springframework.boot:spring-boot-devtools'

    // test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testAnnotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.projectlombok:lombok'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

䷾ TO-BE) build.gradle.kts

```kts
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    val kotlinVersion = "2.1.0"
    kotlin("jvm") version kotlinVersion // Kotlin JVM을 사용하는 프로젝트를 위한 플러그인
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" // Kotlin 코드 스타일을 자동으로 검사하고 포맷팅하는 도구
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // data
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Utility libraries
    implementation("org.jsoup:jsoup:1.17.1")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Spring Boot Devtools
    compileOnly("org.springframework.boot:spring-boot-devtools")

    // Testing libraries
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito")
    }
    testImplementation("com.ninja-squad:springmockk:2.0.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.4.2")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

## Minimum Gradle version

코틀린 설정을 완료했다면 Gradle도 버전에 맞게 올려줍시다!

Kotlin version `2.1.0`으로 설정했으니 Gradle version `8.11`로 사용할 계획입니다.

[Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html#kotlin)

<center><img src="https://github.com/jihunparkme/blog/blob/main/img/java-tio-kotlin/embedded-kotlin-version.png?raw=true" width="60%"></center>

gradle version 설정은 `gradle > wrapper > gradle-wrapper.properties`에서 수정할 수 있습니다.

## Java to Kotlin

먼저 `java`에서 적용되던 `lombok`과의 이별을 해야 할 때입니다.

전반적인 전환 순서는 컴파일 오류가 발생하는 lombok을 없애면서 변환하게 되었는데, 대략적으로 아래 순서로 진행하게 되었던 것 같습니다.
- DTO class
- Util class
- Entity class
- Repository class
- Service class
- Controller class
- 테스트 코드도 변환 및 보완하면서 정상동작 확인 (Test 파트 참고)

### lombok 대신 data class

- 컴파일러가 `equals()`, `hashCode()`, `toString()`, `copy()`, `componentN()` 메서드를 자동 생성
- 데이터 클래스에 property 를 선언하는 순간 해당 property 는 `field`, `Getter`, `Setter`, `생성자 파라미터 `역할

䷿ AS-IS) 

```java
@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavePostRequest {
    private String subject;
    private String title;
    private String url;
    private String category;
    private String writer;
    private String date;
    List<String> tags;
    private String createdDt;

    public boolean isLatestDatePost(final String latestPostDate) {
        if (StringUtils.isBlank(this.date) || StringUtils.isBlank(latestPostDate)) {
            return true;
        }

        try {
            final LocalDate latest = LocalDate.parse(latestPostDate, DateUtils.CREATED_FORMATTER);
            final LocalDate date = LocalDate.parse(this.date, DateUtils.CREATED_FORMATTER);
            return date.isAfter(latest);
        } catch (Exception e) {
            log.error("Error parsing the date. date: {}, message: {}", this.date, e.getMessage(), e);
            return false;
        }
    }

    public Post toPost() {
        return Post.builder()
                .subject(this.subject)
                .title(this.title)
                .category(this.category)
                .writer(this.writer)
                .date(this.date)
                .tags(this.tags)
                .url(this.url)
                .shared(false)
                .createdDt(this.createdDt)
                .build();
    }
}
```

䷾ TO-BE)

```kotlin
private val logger = KotlinLogging.logger {}

data class SavePostRequest(
    val subject: String,
    val title: String,
    val url: String,
    val category: String,
    val writer: String,
    val date: String,
    var tags: List<String>,
    val createdDt: String,
) {

    fun isLatestDatePost(latestPostDate: String): Boolean {
        if (date.isBlank() || latestPostDate.isBlank()) return true

        return try {
            val latest = LocalDate.parse(latestPostDate, DateUtils.CREATED_FORMATTER)
            val parsedDate = LocalDate.parse(date, DateUtils.CREATED_FORMATTER)
            parsedDate.isAfter(latest)
        } catch (e: Exception) {
            logger.error { "Error parsing the date. date: $date, message: ${e.message}" }
            false
        }
    }

    fun toPost(): Post = Post(
        subject = this.subject,
        title = this.title,
        category = this.category,
        writer = this.writer,
        date = this.date,
        tags = this.tags,
        url = this.url,
        shared = false,
        createdDt = this.createdDt,
    )
}
```

### Enum

- enum도 마찬가지로 property 선언이 `field`, `Getter`, `Setter`, `생성자 파라미터` 역할을 하게 됩니다.

䷿ AS-IS) 

```java
@AllArgsConstructor
public enum PostSubjects {
    SPRING("Spring"),
    JAVA("Java"),
    ;

    private String value;

    public String value() {
        return value;
    }
}
```

䷾ TO-BE)

```kotlin
enum class PostSubjects(val value: String) {
    SPRING("Spring"),
    JAVA("Java"),
    ;

    companion object {
        fun from(value: String): PostSubjects {
            return entries.firstOrNull { it.value == value} ?: SPRING
        }
    }
}
```

### Controller

- 아래 코드 기준으로는 `@RequiredArgsConstructor` 제외하고는 크게 달라지는 점이 없어 보네요.

䷿ AS-IS) 

```java
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostsService postsService;

    @GetMapping("/spring")
    public ResponseEntity springScroll(
            @RequestParam(value = "categories", required = false) final List<String> categories,
            @RequestParam(value = "page", required = false, defaultValue = "1") final int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") final int size) {

        final PageRequest pageable = PageRequest.of(page, size,
                Sort.by("createdDt").descending().and(Sort.by("date").descending()));
        final Page<Post> releasePage = postsService.findAllRelease(PostSubjects.SPRING, pageable, categories);
        return BasicResponse.ok(releasePage);
    }
    //...
}
```

䷾ TO-BE)

```kotlin
@RestController
@RequestMapping("/posts")
class PostsController(
    private val postsService: PostsService
) {
    @GetMapping("/spring")
    fun springScroll(
        @RequestParam(value = "categories", required = false) categories: List<String>?,
        @RequestParam(value = "page", required = false, defaultValue = "1") page: Int,
        @RequestParam(value = "size", required = false, defaultValue = "10") size: Int
    ): ResponseEntity<*> {
        val pageable = PageRequest.of(
            page, size,
            Sort.by("createdDt").descending().and(Sort.by("date").descending())
        )
        val releasePage = postsService.findAllRelease(PostSubjects.SPRING, pageable, categories)
        return BasicResponse.ok(releasePage)
    }
    //...
}
```

### Service

- kotlin 코드가 확실하게 간결한 것을 확인할 수 있습니다.

䷿ AS-IS) 

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class PostsSchedulerService {

    private final PostsRepository postsRepository;

    @Transactional
    public void insertPost(SavePostRequest savePostRequest) {
        try {
            postsRepository.save(savePostRequest.toPost());
            log.info("add new post. {}", savePostRequest.getTitle());
        } catch (Exception e) {
            log.error("SpringBlogsSchedulerService.insertPost exception", e);
        }
    }

    @Transactional(readOnly = true)
    public Post findLatestPost(final String category) {
        final List<Post> latestPost = postsRepository.findByCategoryOrderByDateDescLimitOne(category);
        if (latestPost.isEmpty()) {
            return Post.EMPTY;
        }

        return latestPost.get(0);
    }

    public boolean isNotExistOracleJavaPosts(final String title) {
        final List<Post> posts = postsRepository.findByTitle(title);
        if (posts.isEmpty()) {
            return true;
        }
        return false;
    }
}
```

䷾ TO-BE)

```kotlin
private val logger = KotlinLogging.logger {}

@Service
class PostsSchedulerService(
    private val postsRepository: PostsRepository,
) {
    @Transactional
    fun insertPost(savePostRequest: SavePostRequest) {
        return try {
            postsRepository.save(savePostRequest.toPost())
            logger.info("add new post. ${savePostRequest.title}")
        } catch (e: java.lang.Exception) {
            logger.error("SpringBlogsSchedulerService.insertPost exception", e)
        }
    }

    @Transactional(readOnly = true)
    fun findLatestPost(category: String): Post =
        postsRepository.findByCategoryOrderByDateDescLimitOne(category).firstOrNull() ?: Post()

    fun isNotExistOracleJavaPosts(title: String): Boolean =
        postsRepository.findByTitle(title).isEmpty()
}
```

### warning

- `Unnecessary non-null assertion (!!) ...` 불필요한 non-null assertion 제거
- Kotlin은 Java로부터 변환될 때 null 허용을 기본으로 하고 있다보니 `?` 키워드를 가급적 모두 제거하고 필요 시 추가하기

## Test

### 단위테스트

✅ `Fixtures`

- 테스트 픽스처(테스트를 위한 전재 조건)를 반환하는 팩토리함수

```kotlin
createPost(title = "post01")
createPost(subject = "subject01")

...

fun createPost(
    id: String = "",
    subject: String = "",
    title: String = "",
    url: String = "",
    category: String = "",
    writer: String = "",
    date: String = "",
    tags: List<String> = emptyList(),
    shared: Boolean = false,
    createdDt: String = "",
): Post {
    return Post(id, subject, title, url, category, writer, date, tags, shared, createdDt)
}
```

✅ `테스트 확장함수`

- 확장함수를 사용하여 값을 더 쉽게 표현
- 확장함수로 검증 코드의 가독성을 향상

👉🏻 `StringSpec`

#### Kotest

✅ `Kotest`

- 코틀린다운 테스트를 위해 Kotest 라이브러리 활용
- Kotlin에서 Kotest가 가장 많이 사용

```kts
testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
```

✅ `StringSpec`

- 테스트를 단순하고 직관적으로 작성 가능
- 각 테스트가 독립적으로 실행
- 주로 `독립적인 단위 테스트`나, `테스트를 선언적`으로 나열하는 데 적합

```kotlin
class SavePostRequestTest : StringSpec({
    "latest post should be return true" {
        val release = SavePostRequest(
            date = "2024-04-30"
        )
        assertSoftly(release) {
            release.isLatestDatePost("2024-04-29").shouldBeTrue()
            release.isLatestDatePost("2024-04-26").shouldBeTrue()
        }
    }

    "not latest post should be return false" {
        val release = SavePostRequest(
            date = "2024-04-30"
        )
        assertSoftly(release) {
            release.isLatestDatePost("2024-04-30").shouldBeFalse()
            release.isLatestDatePost("2024-04-31").shouldBeFalse()
        }
    }
})
```

✅ `BehaviorSpec`

- `BDD`(Behavior-Driven Development) 스타일로 작성된 테스트
- 테스트를 `행동 단위로 그룹화`하며, Given, When, Then의 구조를 따름
- 계층적으로 테스트를 구성할 수 있어 복잡한 시나리오 테스트에 적합

```kotlin
class PostsSchedulerServiceTest : BehaviorSpec({
    val postsRepository = mockk<PostsRepository>()

    val postsSchedulerService = PostsSchedulerService(postsRepository)

    Given("특정 카테고리의 게시물이 존재하는 경우") {
        val post = listOf(createPost(date = "2024-12-25"))

        every { postsRepository.findByCategoryOrderByDateDescLimitOne(any()) } returns post

        When("가장 마지막으로 수집된 게시을 조회하여") {
            Then("게시일을 확인할 수 있다.") {
                val lastPost = postsSchedulerService.findLastPost(JavaBlogsSubject.INSIDE.value)
                lastPost.date shouldBe "2024-12-25"
            }
        }
    }

    Given("특정 카테고리의 게시물이 존재하지 않는 경우") {
        every { postsRepository.findByCategoryOrderByDateDescLimitOne(any()) } returns emptyList()

        When("기본 엔티티를 리턴하여") {
            Then("기본 게시일을 확인할 수 있다.") {
                val lastPost = postsSchedulerService.findLastPost(JavaBlogsSubject.INSIDE.value)
                lastPost.date shouldBe ""
            }
        }
    }
})
```

✅ `ExpectSpec`

- `예상`(expectation) 기반의 테스트
- 테스트 계층을 구성할 수 있고, 명확한 테스트 시나리오와 결과를 나타내는 데 유리
- 기대치를 중심으로 `context`와 `expect` 블록을 사용해 테스트를 구성

```kotlin
```

## ktlint

> Kotlin 코드 스타일을 자동으로 검사하고 포맷팅하는 플러그인

`ktlint`를 제공하는 여러 도구 중 [JLLeitschuh/ktlint-gradle](https://github.com/JLLeitschuh/ktlint-gradle) 이 주로 사용되는 것 같습니다.

```kts
id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
```

[Standard rules](https://pinterest.github.io/ktlint/latest/rules/standard/)을 `enable` 처리할 수 있는데 `pinterest`에서 제공하는 `ktlint` 문서를 참고할 수 있습니다.
- 커스텀한 규칙은 `.editorconfig` 파일에서 설정할 수 있습니다.

**`.editorconfig`**

```yml
root = true

[*]
insert_final_newline = true

[*.{kt,kts}]
# A multiline expression should start on a new line
ktlint_standard_string-template-indent=disabled
ktlint_standard_multiline-expression-wrapping=disabled

# Newline expected before closing parenthesis
# Newline expected before expression body
# First line of body expression fits on same line as function signature
# ...
ktlint_standard_function-signature=disabled

# Parameter should start on a newline
ktlint_standard_parameter-list-wrapping=disabled
```

### Apply IDE

해당 프로젝트에만 적용

```bash
$ ./gradlew ktlintApplyToIdea
```

IntelliJ를 사용하는 모든 프로젝트에 적용

```bash
$ ./gradlew ktlintApplyToIdeaGlobally
```

수동으로 ktlint를 이용하여 컨벤션 체크
- 또는 `Tasks` → `verification` → `ktlintCheck`

```bash
$ ./gradlew clean ktlintCheck
```

Git hook을 통해 ktlint 설정
- 커밋 전에 ktlintCheck 테스트 실행
- 등록한 hook을 삭제하고 싶다면, hook 경로(.git/hooks)에서 `pre-commit` 삭제

```bash
$ mkdir .git/hooks
$ ./gradlew addKtlintCheckGitPreCommitHook
```

## Reference

> [자바 프로젝트 3개 코틀린 점진적 전환기(feat. lombok 됩니다.)](https://tech.kakaopay.com/post/kotlin-migration/)