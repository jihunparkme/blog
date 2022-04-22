# Java Code Conventions

[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) 를 읽어보면서 참고할만한 내용만 간략하게 정리해 보았다. 

.

[Oracle Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-introduction.html) 도 참고해보면 좋을 듯 하다.

다만... 1999-04-20에 작성된 글로 잘 걸러서 보아야할 듯 하다. 😨

.

## Source File Basics

- 소스 파일은 UTF-8로 인코딩하기
- 탭 문자를 들여쓰기에 사용하지 않기

## Source File Structure

- wildcard imports 사용하지 않기
- import statements 줄바꿈하지 않기
- static imports, non-static imports를 분리하여 작성하기
- 중첩 클래스에 static import 사용하지 않기

## Formatting

**중괄호**

- if, else, for, do, while 문의 내용이 비어있거나 한 줄이더라도 중괄호를 사용하기
- 줄 바꿈은 아래의 경우 적용
  - 여는 괄호 후
  - 본문을 종료하는 닫는 괄호 전 (괄호 두 쉼표, 세미클론 등이 있을 경우 제외)

```java
return () -> {
  while (condition()) {
    method();
  }
};

return new MyClass() {
  @Override public void method() {
    if (condition()) {
      try {
        something();
      } catch (ProblemException e) {
        recover();
      }
    } else if (otherCondition()) {
      somethingElse();
    } else {
      lastThing();
    }
    {
      int x = foo();
      frob(x);
    }
  }
};
```

- `빈 블록은 간결`하게 작성하기
  - 단, if/else, try/catch/finally 제외

```java
void doNothing() {}
```

**들여쓰기**

- 새로운 블록이 열릴 때마다 `들여쓰기 두 칸씩` 증가

**한 줄에는 하나의 명령문**

- 각 문장 뒤에는 줄 바꿈

**열 제한**

- 아래를 제외하고 `열 길이가 100을 넘지 않도록` 하기
  - 긴 URL 또는 JSNI 메서드 참조
  - 패키지와 import
  - shell에 복사하여 붙일 수 있는 주석의 명령줄
  - 드물지만.. 매우 긴 식별자

**줄 바꿈**

- 연속된 라인에서 줄 바꿈이 일어날 경우, 들여쓰기는 `최소 +4 공백`
  - 병렬 요소로 시작하는 경우에만 동일한 들여쓰기 수준을 사용하기

**타입 구조**

- 모든 변수(필드/로컬) 선언은 하나의 변수만 선언하기
  - for 루프 헤드는 제외

```java
int a;
int b;
```

- `지역 변수`는 범위를 최소화하기 위해 처음 `사용된 점에 가깝게 선언`하기
- 배열은 블록과 유사한 형식으로 지정

```java
new int[] {           new int[] {
  0, 1, 2, 3            0,
}                       1,
                        2,
new int[] {             3,
  0, 1,               }
  2, 3
}                     new int[]
                          {0, 1, 2, 3}

```

- switch
  - 계속해서 다음 case로 넘어갈 경우 fall through comment 남기기

```java
switch (input) {
  case 1:
  case 2:
    prepareOneOrTwo();
    // fall through
  case 3:
    handleOneTwoOrThree();
    break;
  default:
    handleLargeNumber(input);
}
```

**네이밍**

- 특수 접두사/접미사 사용하지 않기
- 패키지 이름은 소문자와 숫자만 사용하기
- 클래스 이름은 UpperCamelCase로 작성하기 (명사/명사구)
- 메소드 이름은 lowerCamelCase로 작성하기 (동사/동사구)
- 상수 이름은 단일 밑줄로 다음 단어와 구분하기 `UPPER_SNAKE_CASE`

```java
static final int NUMBER = 5;
static final ImmutableList<String> NAMES = ImmutableList.of("Ed", "Ann");
static final Map<String, Integer> AGES = ImmutableMap.of("Ed", 35, "Ann", 32);
static final Joiner COMMA_JOINER = Joiner.on(',');
static final SomeMutableType[] EMPTY_ARRAY = {};
```

- 상수가 아닌 필드 이름은 lowerCamelCase로 작성하기 (명사/명사구)
- 매개변수 이름은 lowerCamelCase로 작성하기
  - 공용 메서드에서 한 문자 이름 피하기
- 지역 변수 이름은 lowerCamelCase로 작성하기
  - 지역 변수를 상수 스타일로 지정하지 않기

# Clean Code

Google Java Style Guide에서 참고할만한 내용이 약간 아쉬워서 [Clean Code](http://www.yes24.com/Product/Goods/11681152) 내용을 추가하였다.

책을 읽은지 어느덧 1년이 지나기도 했고, 초심을 잃고 내 방처럼 코드를 정리하고 있진 않았는지 돌아보는 마음으로..🙄

다들 빗자루 들고 슥삭슥삭할 준비를!🧹

## [네이밍](https://data-make.tistory.com/632)

변수, 함수, 클래스 이름을 `의미 있고`, `발음하기 쉽고`, `검색하기 쉽게` 지어주자.

- 존재 이유, 수행 기능, 사용 방법이 이름에서 드러나도록!
- 주석이 필요하다면 의도를 분명히 드러내지 못 한 것!

## [함수](https://data-make.tistory.com/633)

함수는 `짧고`, `한 가지 작업만 수행하고`, `서술적 이름`으로 만들자.

- 함수는 한 가지를 해야 한다. 그 한 가지를 잘 해야 한다. 그 한 가지만을 해야 한다.
- 함수 이름만으로 어떤 로직이 수행되는지 이해할 수 있어야 한다.
- 이상적인 인수 개수는 0 -> 1 -> 2개..

## [주석](https://data-make.tistory.com/634)

나쁜 코드에 주석을 달지 말고, 새로 짜자.

- 부정확한 주석은 아예 없는 것만 못하다.
- 코드로 의도를 표현하자.
- 다만 주석을 달아야 한다면 충분한 시간을 들여 최고의 주석을 달아보자.

## [포매팅](https://data-make.tistory.com/635)

코드 형식은 의사소통의 일환이다.

- 오늘 구현한 코드의 가독성은 앞으로 바뀔 코드의 품질에 지대한 영향을 미친다!
- 신문 기사를 작업하는 마음으로 코드를 작성하자.
- 밀접한 코드는 서로 가까이 위치시키고, 빈 행으로 개념을 분리시키자.

## [객체와 자료구조](https://data-make.tistory.com/636)

자료를 세세하게 공개하기 보다는 추상적인 개념으로 표현해보자.

- 객체 
  - 동작을 공개하고 자료를 숨김
  - 기존 동작을 변경하지 않으면서 `새 객체 타입을 추가하기 쉬움`
  - 반면, `기존 객체에 새 동작을 추가하기 어려움`
- 자료 구조
  - 별다른 동작 없이 자료 노출
  - 기존 자료 구조에 `새 동작 추가가 쉬움`
  - 반면, `기존 함수에 새 자료 구조를 추가하기 어려움`

## [오류 처리](https://data-make.tistory.com/637)

오류 처리 코드로 인해 프로그램 논리를 이해하기 어려워진다면 깨끗한 코드라 부르기 어렵다.

- 오류 코드보다 예외를 사용하자.
- try-catch-finally 문으로 시작하자.
- 예외에 의미를 제공하자 (ex.실패한 연산 이름과 실패 유형).
- NULL을 반환하거나 전달하지 말자.

## [경계(외부 API)](https://data-make.tistory.com/638)

Map, List 같은 경계 인터페이스를 이용할 때는 이를 이용하는 클래스나 클래스 계열 밖으로 노출되지 않도록 주의하기

- Class 안에서 객체 유형을 관리하고 변환하자.

  ```java
  public class Sensors {
      private Map sensors = new HashMap();
  
      public Sensor getById(String id) {
          return (Sensor) sensors.get(id);
      }
      // ..
  }
  ```

- 경계에 위치하는 코드는 깔끔히 분리하고, 외부 패키지를 호출하는 코드를 가능한 줄여 경계를 관리하자.

  

## [단위 테스트](https://data-make.tistory.com/640)

실제 코드를 작성하기 전에 단위 테스트를 작성하는 습관을 들여보자.

**테스트는 유연성, 유지보수성, 재사용성을 재공한다**

- `실패하는 단위 테스트를 작성할 때까지` 실제 코드를 작성하지 않는다.
- 컴파일은 실패하지 않으면서 `실행이 실패하는 정도로만 단위 테스트를 작성`한다.
- 현재 `실패하는 테스트를 통과할 정도로만 실제 코드를 작성`한다.
- 하지만, 실제 코드와 맞먹을 정도의 방대한 테스트 코드는 심각한 관리 문제를 유발하기도 한다는 것..

+

- 깨끗한 테스트 코드를 만들려면 `가독성`이 실제 코드보다 중요하다.
- 개념 당 assert 문 수를 최소로 줄이기
- 테스트 함수 하나에 개념 하나만 테스트하기

+

- F.I.R.S.T (깨끗한 테스트의 다섯 가지 규칙)
  -  Fast
    - 테스트는 `빨라야` 한다.
  - Independent
    - 각 테스트는 `독립적`이고 실행 순서에 무관하게 동작해야 한다.
  - Repeatable
    - 테스트는 어떤 환경에서도 `반복 가능`해야 한다.
  - Self-Validating
    - 테스트는 `bool 값으로 결과`(성공 아니면 실패)를 내야 한다. 
  - Timely
    - 테스트는 `적시에` 작성해야 한다.
    - 단위 테스트는 테스트하려는 실제 코드를 구현하기 직전에 구현한다.

## [클래스](https://data-make.tistory.com/641)

큰 클래스 몇 개가 아니라 작은 클래스 여럿으로 이뤄진 시스템이 더 바람직하다.

새 기능을 수정하거나 기존 기능을 변경할 때 건드릴 코드가 최소인 시스템 구조가 바람직하다.

- 클래스 이름은 해당 클래스 책임을 기술하기
- 단일 책임 원칙 `SRP`
  - 클래스는 책임이 작게 만들기
- 응집도
  - 각 클래스 메서드가 클래스 인스턴스 변수를 하나 이상 사용하도록 하여 응집도 높이기

## [시스템](https://data-make.tistory.com/644)

소프트웨어 시스템은 준비 과정과 런타임 로직으로 분리하기

관심사를 적절히 분리해 관리하면서 소프트웨어 아키텍처를 점진적으로 발전시키기

## [창발성](https://data-make.tistory.com/645)

## [동시성](https://data-make.tistory.com/646)

## [점진적인 개선](https://data-make.tistory.com/647)

