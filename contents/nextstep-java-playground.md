# Java PlayGround

NEXTSTEP [자바 플레이그라운드 with TDD, 클린 코드](https://edu.nextstep.camp/c/9WPRB0ys/)에서 새롭게 배우고 깨닫게 된 내용들을 기록한 글입니다.

.

**Part01. `단위테스트`, `TDD`**

.

**시작**

[소트웍스 앤솔러지](http://www.yes24.com/Product/Goods/3290339)에서 말하는 객체 지향 프로그래밍을 잘하기 위한 9가지 원칙

- **한 메서드에서 오직 한 단계의 들여쓰기만 허용하자**
- **else 예약어를 사용하지 말자.**
- **모든 원시 값과 문자열을 포장하자.**
- **한 줄에 점을 하나만 찍자.**
- **축약과 같이 줄여 사용하지 말자.**
- **모든 엔티티를 작게 유지하자.**
- **3개 이상의 인스턴스 변수를 가진 클래스를 쓰지 말자.**
- **일급 컬렉션을 사용하자.**
- **getter/setter/프로퍼티를 사용하지 말자.**

## 단위 테스트

[JUnit](https://junit.org/junit5/)는 프로덕션 코드를 편리하게 테스트할 수 있도록 도와준다.

- JUnit 5.x
  - `애노테이션`을 활용한 테스트 코드 구현
  - `@Test`, `@BeforeEach`, `@AfterEach`
  - Assertions 클래스의 static assert method를 활용해 테스트 결과 검증

자세한 내용은 [테스트 코드 작성의 기본기](https://data-make.tistory.com/733)를 참고하자.

파라미터를 활용한 테스트는 [JUnit 5 Parameterized Tests](https://data-make.tistory.com/737)를 참고해보자.

> Reference
>
> [AssertJ Core](https://joel-costigliola.github.io/assertj/assertj-core.html)
> 
> [JUnit 5 Parameterized](https://www.baeldung.com/parameterized-tests-junit-5)
> 
> [Introduction to AssertJ](https://www.baeldung.com/introduction-to-assertj)

## TDD

TDD(`Test-Driven Development`)와 단위 테스트는 다르다.
- 단위 테스트는 프로덕션 코드를 작성하고 만들 수 있지만
- TDD는 `프로덕션 코드 작성 전에 테스트 코드를 만들어`야 한다.
  - TDD = TFD(Test First Development) + Refactoring
- TDD는 아래와 같은 장점을 제공한다
  - 디버깅 시간 단축
  - 동작하는 문서 역할
  - 변화에 대한 두려움 감소

.

**TDD Cycle**

  1. `Test fails`
     - 실패하는 테스트 코드를 먼저 작성하자.
      ![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/next-step/fail-test-code.png)
  2. `Test passes`
     - Compile Error 해결을 위한 Class, Method를 먼저 생성하자.
  3. `Refactor` (production + test)
     - 테스트 코드가 성공했다면 세부 로직을 구현하고 
     - 점진적으로 Refactoring을 수행하며 Test를 진행
  4. `Repeat`
     - 위 과정을 반복하면서 멋진 프로덕션 코드를 만들어가자.

.

**TDD 원칙**

- 실패하는 단위 테스트를 작성할 때까지 프로덕션 코드를 작성하지 않기
- 컴파일은 실패하지 않으면서 실행이 실패하는 정도로만 단위 테스트 작성하기
- 현재 실패하는 테스트를 통과할 정도로만 실제 코드 작성하기

.

**TDD Tip**

- 테스트하기 쉬운 코드를 만들기 위해 `도메인 기반으로 테스트`를 할 수 있도록 `객체 설계를 분리`하자.
- 테스트 코드를 작성하면서 Class, Method를 만들어 나가자.
- 하나의 테스트 케이스를 완성한 후 커밋을 하는 것이 명확하다.
- TDD가 어렵다면 문제를 작은 단위로 쪼개서 구현해보자.
- 객체 필드를 사용해서 상태 확인을 하지 말고, `객체에게 메시지를 보내서 상태`를 확인하도록 하자.
- public method를 통해 대부분이 테스트가 가능하므로, 모든 private method를 테스트하지 않아도 된다.
- 테스트 값은 가능한 `경계값을 사용`하자.
- 도메인 테스트를 할 때 getter 메서드를 사용해서 값을 비교하지 말고 `객체 자체를 비교`해보자.

.

**테스트 가능한 코드 만들기**

TDD는 테스트하기 힘든 코드를 테스트 가능한 구조로 만드는 것이 중요하다!

- 메서드와 클래스를 계속 분리해 보자.
- 레거시 코드를 리펙터링하려면, `기존 메서드 시그니처를 변경하지 않고 테스트 가능한 코드로` 만들어 보자.
  - 그렇게 테스트 코드를 만든 상태에서 점진적으로 리펙터링을 수행하자.

```java
public class Car {
    public void move() {
        if (getRandomNo() >= FORWARD_NUM) {
        }
    }

    // private to protected to be testable (기존 메서드 시그니처는 유지)
    protected int getRandomNo() {
        Random random = new Random();
        return random.nextInt(MAX_BOUND);
    }
}

// Test Code
Car car = new Car("aaron") {
    @Override
    protected int getRandomNo() {
        return 3;
    }
}
```
  - 이후 안정화가 되면 `테스트하기 어려운 코드를 분리`하자.
    - 테스트하기 어려운 Random()을 분리
```java
public void move(int randomNo) { // use parameter
    if (randomNo >= FORWARD_NUM) {
    }
}
```
- 테스트를 위해 인터페이스로 분리하여 의존성을 주입시켜줄 수도 있다.
  - 수정이 자주 발생하는 로직은 인터페이스로 추상화시켜보자.

```java
@FunctionalInterface
public interface MovingStrategy {
    boolean movable();
}

public class RandomMovingStrategy implements MovingStrategy {
    //...
    @Override
    public boolean movable() {
        return getRandomNo() >= FORWARD_NUM;
    }

    protected int getRandomNo() {
        Random random = new Random();
        return random.nextInt(MAX_BOUND);
    }
}

public class Car {
    //...
    public void move(MovingStrategy movingStrategy) { // Dependency Injection
        if (movingStrategy.moveable()) {
        }
    }
}

// Test Code
car.move(() -> true);
``` 

## 원시값과 문자열 포장

원시값을 포장한 객체(하나의 원시값을 가진 객체)

- 클래스를 작게 만들면서 단일 책임 원칙을 잘 지킬 수 있게 된다.
- 값에 대한 범위를 객체가 책임지면서 안전하게 값을 사용할 수 있게 된다.
- 외부에 의해서 값이 변경될 수 없는 불변 객체(Value Object)로 만들어 보자.
  - 변경이 사이드 이펙트를 발생시키지 않고, 한 지점에 국한되어 변경을 가할 수 있어야 잘 설계된 객체지향이라고 할 수 있다.

```java
/**********************
 * 불변 객체 : 객체의 값을 변경하지 않고 새로운 인스턴스를 반환
 * 단점은 GC가 많이 발생하여 성능 저하 이슈가 발생할 가능성이 있다.
 */
public Position move() {
    return new Position(position + 1);
}

/**********************
 * 가변 객체 : 객체의 값을 변경하여 자기 자신을 반환
 */
public Position move() {
    position = position + 1;
    return this; 
}
```

.

원시값 포장하기

**Before**

```java
public Class Car {
    private final String name;
    private int position = 0;
    //...
}
```

**After**

```java
public class Name {
    private final String name;

    public Name(String name) {
        this.name = name;
    }
    //..
}

public class Position {
    private int position;

    public Position(int position) {
        this.position = position;
    }
    //..
}

public Class Car {
    private final Name name;
    private Position position;
    //...
}
```

## 일급 컬렉션

원시값을 포장하는 것과 같이 컬렉션을 포장하는 것을 의미(하나의 컬렉션을 가진 객체)

```java
public class Cars {
    private final List<Car> cars;

    public Cars(List<Car> cars) {
        this.cars = cars;
    }
    //..
}
```

## JAVA

`고정된 값은 상수`로 표현하기

```java
/**********
 * Before
 */
public class BallNumber {
    //...
    public BallNumber(int no) {
        if (no < 0 || no > 9) {
        }
        //...
    }
}

/**********
 * After
 */
public class BallNumber {
    public static final int MIN_NO = 0;
    public static final int MAX_NO = 9;
    //...
    public BallNumber(int no) {
        if (no < MIN_NO || no > MAX_NO) {
            throw new IllegalArgumentException("볼 숫자는 1부터 9사이로 입력해야 합니다.");
        }
        this.no = no;
    }
}
```

.

객체 필드를 사용해서 상태 확인을 하지 말고, 객체지향스럽게 `객체에게 메시지를 보내서 상태를 확인`하기

```java
/**********
 * Before
 */ 
if (result == BallStatus.STRIKE) {
}

/**********
 * After
 */
public enum BallStatus {
    NOTHING, BALL, STRIKE;
    //...
    public boolean isStrike() {
        return this == BallStatus.STRIKE;
    }
}

if (result.isStrike()) {
}
```

.

메서드 추출을 통해 역할을 명확하게 구분하기
- 메서드는 `짧고`, `한 가지 작업만 수행하고`, `서술적 이름`으로 만들자.

```java
/**********
 * Before
 */
private List<Ball> makeBalls(List<Integer> balls) {

    if (balls.size() < BALL_SIZE || balls.size() > BALL_SIZE) {
        throw new IllegalArgumentException("숫자는 세자리로 입력해야 합니다.");
    }

    Set<Integer> set = new HashSet<>();
    for (Integer ball : balls) {
        set.add(ball);
    }

    if (set.size() != BALL_SIZE) {
        throw new IllegalArgumentException("중복되지 않는 숫자를 입력해야 합니다.");
    }

    List<Ball> result = new ArrayList<>();
    for (int i = 0; i < BALL_SIZE; i++) {
        result.add(new Ball(i + 1, new BallNumber(balls.get(i))));
    }

    return result;
}

/**********
 * After
 */
private List<Ball> makeBalls(List<Integer> balls) {
    checkBallSize(balls);
    checkBallDuplication(balls);

    List<Ball> result = new ArrayList<>();
    for (int i = 0; i < BALL_SIZE; i++) {
        result.add(new Ball(i + 1, new BallNumber(balls.get(i))));
    }

    return result;
}

private void checkBallDuplication(List<Integer> balls) {
    Set<Integer> set = new HashSet<>();
    for (Integer ball : balls) {
        set.add(ball);
    }

    if (set.size() != BALL_SIZE) {
        throw new IllegalArgumentException("중복되지 않는 숫자를 입력해야 합니다.");
    }
}

private void checkBallSize(List<Integer> balls) {
    if (balls.size() < BALL_SIZE || balls.size() > BALL_SIZE) {
        throw new IllegalArgumentException("숫자는 세자리로 입력해야 합니다.");
    }
}
```

## Code

- code convention, format 잘 지키기

**상수**

- 상수 제대로 선언하기 (static final, 변수 이름은 대문자)
  - 상수, 클래스 변수, 인스턴스 변수, 생성자 ... 순으로

**New Line**

- New Line은 문맥을 분리하는 부분에 사용
- 해당 메서드가 몇 가지 일을 하고 있는지 파악이 쉬워짐

**Naming**

-  변수가 표현하고 있는 것을 완벽하고 정확하게, 구체적이게 설명하자.
-  Total, Sub, Average,.. 등의 한정자를 사용해야 한다면, 이름의 끝에 위치시키자.
-  유명한 오픈소스 코드를 많이 읽고 좋은 이름을 짓기 위한 연습을 해보자.
- [효과적인 이름 짓기](https://remotty.github.io/blog/2014/03/01/hyogwajeogin-ireumjisgi/), [CODE COMPLETE](http://www.yes24.com/Product/goods/1480040)

**도메인 객체**

- 객체를 객체스럽게 사용하도록 리팩터링 해보자.
- final을 사용해서 값의 변경을 적절하게 막아보자.
- 도메인 클래스에서 setter 보다는 생성자를 활용해서 초기화하자.
  - 상태 데이터도 getter로 사용하지 말고, 메시지를 보내자.
  - getter가 정말 필요하다면 마지막에 추가해보자.
- 객체 인스턴스 변수의 접근 제어자는 private으로 구현하자.
- 인스턴스 변수의 수를 최소화하자.
- 현재 객체의 상태를 보기 위한 로그 메시지 성격이 강하다면 toString()을 통해 구현하자.
- View에서 사용할 데이터라면 getter 메서드를 통해 데이터를 전달하자.

**Test Code**

- Fixture란 테스트를 실행하기 위해 준비해야 할 것들을 의미
  - 각 Test Case에서 공통으로 필요한 인스턴스 변수만 위치시키고, 나머지는 각 Test Case에 로컬 변수로 구현
  - @BeforeEach는 각 Test Case에서 중복으로 사용하는 Fixture만 초기화

## Commit Message Conventions

[Commit Message Conventions](https://gist.github.com/stephenparish/9941e89d80e2bc58a153#message-body)

**Format of the commit message**

```text
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

- type

  ```text
  feat (feature)
  fix (bug fix)
  docs (documentation)
  style (formatting, missing semi colons, …)
  refactor
  test (when adding missing tests)
  chore (maintain)
  ```

- scope
  - 커밋 변경 위치를 지정하는 모든 것
- subject
  - 명령형, 현재 시제 사용
- body
  - 변화에 대한 동기와 이전 코드와의 대조
- footer
  - 주요 변경 사항

**example**

```text
feat($browser): onUrlChange event (popstate/hashchange/polling)

Added new event to $browser:
- forward popstate event if available
- forward hashchange event if popstate not available
- do polling when neither popstate nor hashchange available

Breaks $browser.onHashChange, which was removed (use onUrlChange instead)
```

```text
fix($compile): couple of unit tests for IE9

Older IEs serialize html uppercased, but IE9 does not...
Would be better to expect case insensitive, unfortunately jasmine does
not allow to user regexps for throw expectations.

Closes #392
Breaks foo.bar api, foo.baz should be used instead
```

## Java Code Conventions

[Java Code Conventions](https://data-make.tistory.com/734)
