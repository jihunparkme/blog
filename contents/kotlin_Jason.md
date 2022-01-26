# Kotlin

토이 프로젝트에 Kotlin 언어를 적용해볼 예정이다.

하지만.. Kotlin에 대해 아는게 0.1도 없기에.. 작년 Jason 님이 우아한 테크 세미나에서 발표하신 내용을 기반으로 간략하게 정리 후 시작해보려고 한다.

> [어디 가서 코프링 매우 알은체하기! : 9월 우아한 테크 세미나](https://www.youtube.com/watch?v=ewBri47JWII&list=WL&index=9&t=6029s)

이 발표를 보고 코프링 매우 알은체하며 프로젝트를 진행해야지 🤠

## Basic

```kotlin
class Person(val name: String, val age: Int = 1) {
    var name: String? = null
}
```

- `val` : 파라미터 읽기 전용
- `val age: Int = 1` : 파라미터 기본 인자
- `var` : 변경 가능 프로퍼티
- `String?` : null 이 될 수 있는 타입

## Item 1. 표준 라이브러리

`코틀린 표준 라이브러리를 익히고 사용하기`

코틀린은 읽기 전용 컬렉션과 변경 가능한 컬렉션을 구별해 제공

- AS-IS

  ```kotlin
  import java.util.Random
  import java.util.concurrent.ThreadLocalRandom

  Random.nextInt()
  ThreadLocalRandom.current().nextInt()
  ```

- TO-BE

  ```kotlin
  import kotlin.random.Random

  Random.nextInt() // thread safe
  ```

## Item 2. 역컴파일

`자바로 역컴파일하는 습관 들이기`

Kotlin to Java

- Tool > Kotlin > Show Kotlin Bytecode > Decompile

Java to Kotlin

- Convert Java File to Kotlin File

## Item 3. 데이터 클래스

`롬복 대신 데이터 클래스 사용하기`

- Annotation processor 는 코틀린 컴파일 이후 동작하므로 롬복에서 생성된 자바 코드는 코틀린 코드에서 접근할 수 없다.

  ```
  *.kt + *.java
      => Kotlin Compiler
          => *.class
              + *.java
                  => Annotation processor
                      => *.class
  ```

- Data Class 를 사용하면 컴파일러가 `equals()`, `hashCode()`, `toString()`, `copy()` 등을 자동 생성
- Data class 에 property 를 선언하는 순간 해당 property 는 field, Getter,
  Setter, 생성자 파라미터 역할을 하게 됨.

  ```kotlin
  val person = Person(name = "Aaron", age = 29)
  val person2 = person.copy(age = 25)

  data class Person(val name: String, val age: Int)
  ```

## Item 4. 지연 초기화

`필드 주입이 필요하면 지연 초기화를 사용하자`

- field 를 통한 의존성 주입의 경우
- `lateinit` 변경자를 붙이면 property를 나중에 초기화할 수 있음
  - 지연 초기화 property는 항상 var
  ```kotlin
  @Autowired
  private lateinit var objectMapper: ObjectMapper
  ```

### jackson

- jackson-module-kotlin 은 매개변수가 없는 생성자가 없더라도 직렬화와 역직렬화를 지원

  - 코틀린은 매개변수가 없는 생성자를 만들기 위해 생성자의 모든 매개변수에 기본 인자가 필요

    ```kotlin
    // Unit Test 의 경우 직접 호출 필요
    val mapper1 = jacksonObjectMapper()
    val mapper2 = ObjectMapper().registerKotlinModule()
    ```

    ```properties
    dependencies {
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    }
    ```

## Item 5. 변경 가능성 제한

`변경 가능성을 제한하자.`

- 가급적 val 로 선언하고 필요 시 var 로 변경하자.
- 생성자 바인딩을 사용하려면 `@EnableConfigurationProperties` 또는 `@ConfigurationPropertiesScan` 을 사용하자.

  ```kotlin
  @ConfigurationProperties("application")
  @ConstructorBinding
  data class ApplicationProperties(val url: String)

  @ConfigurationPropertiesScan
  @SpringBootApplication
  class Application
  ```

- private property and backing property

  - 공개 API 와 구현 세부 사항 프로퍼티로 나눌 경우

  ```kotlin
  @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], orphanRemoval = true)
  @JoinColumn(name = "session_id", nullable = false)
  private val _students: MutableSet<Student> = students.toMutableSet() //backing property
  val student: Set<Student>
      get() = _students
  ```

## Item 6. 엔티티

`엔티티에 데이터 클래스 사용 피하기`

- 양방향 연관 관계의 경우 toString(), hashcode() 로 무한 순환 참조가 발생하게 된다.

## Item 7. 사용자 지정 getter

`사용자 지정 getter를 사용하자.`

- JPA 에 의해 인스턴스화 될 때, 초기화 블록이 호출되지 않으므로 영속화하지 않는 필드는 사용자 지정 getter를 사용하자.

  - 영속화하지 않을 목적이었지만, 인스턴스화를 할 경우 null 을 허용하지 않았음에도 불구하고 null 이 들어가게 됨.

  - 따라서, 사용자 지정 getter 를 정의해서 프로퍼티에 접근할 때마다 호출되도록 하자.

- AS-IS
  ```kotlin
  @Transient
  val fixed: Boolean = startDate.until(endDate).year < 1
  ```
- TO-BE
  ```kotlin
  val fixed: Boolean
    get() = startDate.until(endDate).year < 1
  ```
  - backing property(private)가 없으므로 @Transient 를 사용하지 않아도 된다. (일종의 메서드로 생각하자.)

## Item 8. Null 타입 제거

`Null이 될 수 있는 타입은 빠르게 제거하자.`

- Null이 될 수 있는 타입을 사용하면 Null 검사가 필요하다.
- 0 또는 빈 문자열로 초기화해서 Null이 될 수 있는 타입을 제거하자.

  ```kotlin
  interface ArticleRepository : CrudRepository<Article, Long> {
      fun findBySlug(slug: String): Article?
      fun findAllByOrderByAddedAtDesc(): Iterable<Atricle>
  }

  interface UserRepository : CrudRepository<User, Long> {
      fun findByLogin(login: String): User?
  }
  ```

  - Optional 보다 Nullable 한 타입을 사용해서 불필요한 java import 를 줄이자.

- 확장 함수를 사용할 경우

  ```kotlin
  fun MemberRepository.getById(id: Long): Member {
    if (id == 0L) {
        return Term.SINGLE
    }
    return findByIdOrNull(id) ?: throw NoSuchElementException("존재하지 않는 아이디 입니다. id: $id")
  }

  interface MemberRepository : JpaRepository<Member, Long>
  ```
