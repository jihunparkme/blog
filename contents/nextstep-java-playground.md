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

> Reference
>
> [AssertJ Core](https://joel-costigliola.github.io/assertj/assertj-core.html)
> 
> [AssertJ Exception Assertions](https://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html#exception-assertion)
> 
> [JUnit 5 Parameterized](https://www.baeldung.com/parameterized-tests-junit-5)
> 
> [Introduction to AssertJ](https://www.baeldung.com/introduction-to-assertj)
