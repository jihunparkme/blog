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

# 단위 테스트

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

## Google Java Style Guide

[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html#s4.6-whitespace)