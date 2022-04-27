# Java PlayGround

NEXTSTEP [자바 플레이그라운드 with TDD, 클린코드](https://edu.nextstep.camp/c/9WPRB0ys/)에서 새롭게 배운 내용들 기록

## 시작

[소트웍스 앤솔러지](http://www.yes24.com/Product/Goods/3290339)에서 말하는 객체 지향 프로그래믕을 잘하기 위한 9가지 원칙

- 한 메서드에서 오직 한 단계의 들여쓰기만 허용하자
- else 예약어를 사용하지 말자.
- 모든 원시 값과 문자열을 포장하자.
- 한 줄에 점을 하나만 찍자.
- 축약과 같이 줄여 사용하지 말자.
- 모든 엔티티를 작게 유지하자.
- 3개 이상의 인스턴스 변수를 가진 클래스를 쓰지 말자.
- 일급 컬렉션을 사용하자.
- getter/setter/프로퍼티를 사용하지 말자.

## 단위 테스트

[JUnit](https://junit.org/junit5/)는 프로덕션 코드를 편리하게 테스트할 수 있도록 도와준다.

- JUnit 5.x
  - `애노테이션`을 활용한 테스트 코드 구현
  - `@Test`, `@BeforeEach`, `@AfterEach`
  - Assertions 클래스의 static assert method를 활용해 테스트 결과 검증

자세한 내용은 [테스트 코드 작성의 기본기](https://data-make.tistory.com/733)를 참고하자.

✅ [Guide to JUnit 5 Parameterized Tests](https://www.baeldung.com/parameterized-tests-junit-5)는 테스트 코드가 어느정도 익숙해지면 분석해야지.

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
- TDD는 프로덕션 코드 작성 전에 테스트 코드를 만들어야 한다.
  - TDD = TFD(Test First Development) + Refactoring
- TDD는 아래와 같은 장점을 제공한다
  - 디버깅 시간 단축
  - 동작하는 문서 역할
  - 변화에 대한 두려움 감소

**TDD Cycle**

  1. `Test fails`
     - 실패하는 테스트 코드를 먼저 작성하기
      <center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/next-step/fail-test-code.png" width="80%"></center>

  2. `Test passes`
     - Compile Error 해결을 위한 Class, Method 생성
     - 테스트 성공을 위한 Method 세부 로직 구현
  3. `Refactor` (production + test)
     - 테스트 코드가 성공했다면 Refactoring & Test
  4. `Repeat`

**TDD 원칙**

- 실패하는 단위 테스트를 작성할 때까지 프로덕션 코드를 작성하지 않기
- 컴파일은 실패하지 않으면서 실행이 실패하는 정도로만 단위 테스트 작성하기
- 현재 실패하는 테스트를 통과할 정도로만 실제 코드 작성하기

**TDD Tip**

- 테스트하기 쉬운 코드를 만들기 위해 `도메인 기반으로 테스트`를 할 수 있도록 `객체 설계를 분리`하자.
- 테스트 코드를 작성하면서 Class, Method를 만들어 나가자.
- 하나의 테스트 케이스를 완성한 후 커밋을 하는 것이 명확하다.
- TDD가 어렵다면 문제를 작은 단위로 쪼개서 구현해보기
- 객체 필드를 사용해서 상태 확인을 하지 말고, `객체에게 메시지를 보내서 상태`를 확인하도록 하자.
- public method를 통해 대부분이 테스트가 가능하므로, 모든 private method를 테스트하지 않아도 된다.

**Java Tip**

- `고정된 값은 상수`로 표현하기

```java
/*
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

/*
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

- 객체 필드를 사용해서 상태 확인을 하지 말고, 객체지향스럽게 `객체에게 메시지를 보내서 상태를 확인`하기

```java
/*
 * Before
 */ 
if (result == BallStatus.STRIKE) {
}

/*
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

- 메서드 추출을 통해 역할을 명확하게 하기
  - 메서드는 `짧고`, `한 가지 작업만 수행하고`, `서술적 이름`으로 만들자.

```java
/* 
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

/* 
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

# Reference

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