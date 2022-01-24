# Leather-Homepage

신입 백엔드 개발자로 일한 지 어느덧 1년이 지났다. 👨‍💻

부족한 실력이지만 여자친구의 창업 선물로 홈페이지를 만들어 보려고 한다.

(이렇게 "여자친구 홈페이지 만들어주기 프로젝트"가 시작되었다고 한다. 😎)

/

프론트단은 지금 실력으로 상당한 시간이 예상되어.. 시간 단축을 위해 Free Bootstrap Templates 의 힘을 빌렸다..

현재로서는 백엔드 쪽 전문성을 쌓는 게 우선이다 😢

(나중에는 프론트 쪽도 직접 만들어줄게..👀)

/

누군가를 위한, 실제 서비스를 위한 개인 프로젝트는 처음인 만큼 차근차근 잘 정리하면서 만들어 보고자 한다.

새롭게 알고, 공부하게 된 내용을 위주로 작성할 듯싶다.

AWS 배포 부분은 이동욱님의 [스프링 부트와 AWS로 혼자 구현하는 웹 서비스](http://www.kyobobook.co.kr/product/detailViewKor.laf?ejkGb=KOR&mallGb=KOR&barcode=9788965402602) 책을 많이 참고하게 되었다.

기술 스택은 현재 사용 중인 혹은 배워보고 싶은 기술들을 택했다.

- `Back-End` : Kotlin, Spring Boot, Spring MVC, Spring Security
- `Front-End` : Thymeleaf, JavaScript, jQuery, Bootstrap
- `Data` : Spring Data JPA, JPA, QueryDSL
- `Test` : Mockito, Spock
- `DevOps` : MySQL, Jenkins, Nginx, AWS-EC2

/

개발에 필요한 서비스, 기술들은 변경될 수 있지만, 초기 설정을 크게 보면 아래와 같다.

쇼핑몰이 아닌 단순 홈페이지라서 회원 관리는 사실 불필요하지만, 공부 겸 구현을 해보려고 한다.

개발하면서 필요한 기능들을 계속해서 추가해 나아갈 예정이다.

- 회원관리
- 상품관리
- 공지관리
- 후기관리
- 문의하기
- About
- 기타
  - [MySQL 데이터 백업](https://server-talk.tistory.com/30)
  - 댓글, 후기, 문의하기 요청이 들어오면 관리자에게 자동 메일 발송

## build.gradle

프로젝트 설정 부분은 항상 작성하라는 대로만 작성하고 무심코 지나갔었는데, 이번 기회에 살펴보게 되어 다행이다.

**Kotlin Spring**

- aka. Kotlin dsl

```gradle
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVersion = "1.5.10"
	id("org.springframework.boot") version "2.6.2"
	id("io.spring.dependency-management") version "1.0.11.RELEASE" //> spring boot 의존성 관리 플러그인
	kotlin("jvm") version kotlinVersion //> jvm(bytecode)으로 컴파일
	kotlin("plugin.spring") version kotlinVersion //> 클래스를 open으로 기본 설정
	kotlin("plugin.jpa") version kotlinVersion
}

group = "com"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin") //> 매개변수가 없는 생성자가 없더라도 직렬화와 역직렬화를 지원
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") //> 코틀린 필수 기능 제공
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

allOpen{ //지연로딩을 위해 추가 (kotlin final class 와 관련)
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

```

`kotlin("plugin.spring")`

- 코틀린의 클래스는 기본적으로 final 이므로 상속이 불가능
- 하지만, Spring AOP 는 cglib 를 사용할 때 상속을 통해 proxy 패턴을 사용
- 해당 플러그인을 통해 클래스를 open 으로 기본 설정

**Java Spring**

```gradle
plugins { //> 기존 gradle buildscript, apply plugin 을 간편화
	id 'org.springframework.boot' version '2.6.2'
	id 'io.spring.dependency-management' version '1.0.11.RELEASE' //> spring boot 의존성 관리 플러그인
	id 'java'
	id 'eclipse'
	id 'com.example.hello' version '1.2.3' apply false //> 일부 서브 프로젝트에만 적용 시
}

//> 서브 프로젝트에만 적용
subprojects {
	if (name.startsWith("hello")) {
		apply("com.example.hello")
	}
}

group = 'com.hello'
version = '0.0.1-SNAPSHOT-'+new Date().format("yyyyMMddHHmmss")
sourceCompatibility = '11'

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

//> 원격 의존성(library) 저장소
repositories {
	mavenCentral()
	jcenter()
}

//> 의존성 선언 (group:name:)
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

test {
	useJUnitPlatform() //> 단위 테스트 플랫폼 제공
}

```

**dependencies**

- `implementation` : 의존 라이브러리 수정 시 해당 모듈까지만 재빌드
  - 종속된 하위 모듈을 패키지에 포함하지 않음. (ex. spring-boot-starter-web)
- `api` : 의존 라이브러리 수정 시 연관된 모든 모듈 재빌드
  - 종속된 하위 모듈을 모두 패키지에 포함
- `testImplementation` : 테스트 코드를 컴파일하고 실행하는 데 필요한 종속성 (ex. spring-boot-starter-test)
- `compileOnly` : 컴파일 단계에서만 필요한 종속성
  - 컴파일 시에만 빌드하고 결과물에는 포함하지 않음 (ex. lombok)
- `runtimeOnly` : 런타임 단계에서만 필요한 종속성 (ex. h2)
- `annotationProcessor` : annotation 이 선언된 클래스의 경우 annotationProcessor 처리가 필요한 종속성 (ex. lombok)

> [Kotlin Gradle](https://kotlinlang.org/docs/gradle.html#targeting-the-jvm)
>
> [The Java Library Plugin](https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_configurations_graph)

> [[kotlin + Spring] 코틀린 환경에서 Spring Boot 사용하기](https://sabarada.tistory.com/180)

## Test Code

```java
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = HelloController.class,
        excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
)
public class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @WithMockUser(roles="USER")
    @Test
    public void hello가_리턴된다() throws Exception {
        String hello = "hello";

        mvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(hello));
    }

	@WithMockUser(roles="USER")
    @Test
    public void helloDto가_리턴된다() throws Exception {
        String name = "hello";
        int amount = 1000;

        mvc.perform(
                    get("/hello/dto")
                            .param("name", name)
                            .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.amount", is(amount)));
    }
}
```

- `@RunWith(SpringRunner.class)` : 스프링 실행자(SpringRunner)를 실행
  - SpringBootTest 와 Junit 사이의 연결자 역할
- `@WebMvcTest` : Spring MVC 에 집중할 수 있는 어노테이션
  - @Controller, @ControllerAdvice 등을 사용할 수 있지만, @Service, @Component, @Repository 등은 사용할 수 없음
- `@Autowired` : Spring 이 관리하는 Bean 주입
- `private MockMvc mvc` Web API 테스트 시 사용
- `.param` : 요청 파라미터
- `jsonPath` : JSON 응답값을 필드별로 검증 ($ 기준으로 필드명 명시)

## Controller

`Bean 주입 시 생성자로 주입받자.`

- @RequiredArgsConstructor
- 클래스의 의존성 관계가 변경될 때마다 생성자를 수정해야하는 번거로움을 해결

## Entity & Dto

`Entity 클래스와 Controller 에서 사용할 Dto 는 분리해서 사용하자.`

- Entity 클래스는 DB와 맞닿은 핵심 클래스이고 수많은 서비스 클래스나 비즈니스 로직들에 사용되므로 잦은 변경이 일어나지 않도록 하자.
