# Introduction to AssertJ

dependencies

```gradle
dependencies {
    testImplementation "org.junit.jupiter:junit-jupiter"
    testImplementation "org.assertj:assertj-core"
}
```

single static import를 통해 test class를 쉽게 추가할 수 있다.

```java
import static org.assertj.core.api.Assertions.*;
```

참고로, assertThat() 메서드만으로는 아무것도 증명할 수 없다. 증명의 대상을 설정하는 정도로 생각하면 좋을 것 같다.

 ```java
 assertThat(anything);
 ```

## Object

- `isEqualTo()` 메서드는 객체의 참조를 비교한다.
- 따라서, 객체의 필드를 비교하려면 `usingRecursiveComparison()` 메서드를 활용하자.
- 그 밖에도 다양한 메서드를 제공하고 있다. [AbstractObjectAssert](https://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractObjectAssert.html)를 참고해보자.

```java
public class Dog { 
    private String name; 
    private Float weight;
    
    // standard getters and setters
}

@Test
void objectTest() {
    Dog fido = new Dog("Fido", 5.25F);
    Dog fidosClone = new Dog("Fido", 5.25F);

    assertThat(fido).isNotSameAs(fidosClone);
    assertThat(fido).usingRecursiveComparison().isEqualTo(fidosClone);
}
```

## Boolean

- Boolean은 생각보다 간단하다.

```java
@Test
void booleanTest() {
    assertTrue("".isEmpty());
}
```

## Iterable/Array

- contains() : 요소 포함 여부 확인
- isNotEmpty() : Collection이 비어 있는지 확인
- startsWith() : 주어진 문자로 시작하는지 확인
- 그 밖에도 다양한 메서드를 제공하고 있다. [AbstractIterableAssert](https://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractIterableAssert.html)를 참고해보자.

```java
@Test
void arrayTest() {
    List<String> list = Arrays.asList("1", "2", "3");
    assertThat(list).contains("1");
    assertThat(list).isNotEmpty();
    assertThat(list).startsWith("1");
}
```

- 둘 이상의 assertion을 연결하여 확인할 경우

```java
@Test
void arrayTest2() {
    List<String> list = Arrays.asList("1", "2", "3");
    assertThat(list)
            .isNotEmpty()
            .contains("1")
            .doesNotContainNull()
            .containsSequence("2", "3");
}
```

## Character

- isNotEqualTo() : 문자 비교
- inUnicode() : 해당 문자가 유니코드 테이블에 있는지 확인
- isGreaterThanOrEqualTo() : 특정 문자보다 같거나 큰지 확인
- isLowerCase() : 소문자인지 확인
- 그 밖에도 다양한 메서드를 제공하고 있다. [AbstractCharacterAssert](https://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractCharacterAssert.html)를 참고해보자.

```java
@Test
void characterTest() {
    assertThat("s")
            .isNotEqualTo("b")
            .inUnicode()
            .isGreaterThanOrEqualTo("c")
            .isLowerCase();
}
```

## Class

- 클래스 타입은 모통 필드, 클래스 타입, 애노테이션 등을 확인할 때 사용
  - isInterface() : 특정 클래스가 인터페이스인지 확인
  - isAssignableFrom() : 특정 클래스가 다른 클래스에 할당 가능한지 확인
  - 그밖에 다양한 메서드는 [AbstractClassAssert](https://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractClassAssert.html)를 참고해보자.


```java
@Test
void classTest() {
    assertThat(Runnable.class).isInterface();
    assertThat(Exception.class).isAssignableFrom(NoSuchElementException.class);
}
```

## File

- 파일 타입은 보통 파일의 존재 여부, 디렉터리/파일 여부, 콘텐츠 포함 여부 등을 확인할 때 사용
  - exists() : 파일 존재 확인
  - isFile() : 파일 여부 확인
  - canRead() : 읽기 가능 여부
  - canWrite(); : 쓰기 가능 여부
  - 그밖에 다양한 메서드는 [AbstractFileAssert](https://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractFileAssert.html)를 참고해보자.

```java
@Test
void fileTest() {
    assertThat(new File("/Users/aaron/Desktop/capture/test.jpeg"))
            .exists()
            .isFile()
            .canRead()
            .canWrite();
}
```

## Double/Float/Integer

- withPrecision() 메서드를 활용하여 주어진 정밀도에 따라 두 값이 같은지 확인
- 그밖에 다양한 메서드는 [AbstractDoubleAssert](https://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractDoubleAssert.html)를 참고해보자.

```java
@Test
void doubleTest() {
    assertThat(5.1).isEqualTo(5, withPrecision(1d));
}
```

## Map

- isNotEmpty() : Map이 비어있는지 확인
- containsKey() : 특정 Key 포함 여부 확인
- doesNotContainKeys() : 특정 Key 미포함 여부 확인
- contains(entry()) : 특정 entry 포함 여부 확인
- 그밖에 다양한 메서드는 [AbstractMapAssert](https://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractMapAssert.html)를 참고해보자.

```java
@Test
void mapTest() {
    HashMap<Integer, String> map = new HashMap<>();
    map.put(1, "a");
    map.put(2, "b");
    map.put(3, "c");

    assertThat(map)
            .isNotEmpty()
            .containsKey(2)
            .doesNotContainKeys(10)
            .contains(entry(1, "a"));
}
```

## Throwable

- Throwable 타입은 주로 예외 메시지, stacktraces, thrown 여부 등을 확인할 때 사용
  - hasNoCause() : thrown 여부 확인
  - hasMessageEndingWith() : 에러 메시지가 특정 문자로 끝나는지 확인
  - hasMessageContaining("exception") : 에러 메시지에 특정 문자열이 포함되는지 확인
  - 그밖에 다양한 메서드는 [AbstractThrowableAssert](https://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractThrowableAssert.html)를 참고해보자.

```java
@Test
void throwableTest() {
    Exception ex = new NullPointerException("This is Null Point exception");
    assertThat(ex)
            .hasNoCause()
            .hasMessageEndingWith("n")
            .hasMessageContaining("exception");
}
```

## Describing

- 동적으로 싸용자 지정 설명 만들기

```java
@Test
void describingTest() {
    Dog puppy = new Dog("Puppy", 5.25F);

    assertThat(puppy.getWeight())
            .as("%s's weight should be equal to 5.25", puppy.getName())
            .isEqualTo(5.25F);
}
```

## Exception

- Exception 테스트를 위해 assertThatThrownBy(), assertThatExceptionOfType() 혹은 별도 제공 Exception 메서드를 사용할 수 있다.
- 자주 발생하는 Exception은 별도의 메서드 제공
  - assertThatNullPointerException
  - assertThatIllegalArgumentException
  - assertThatIllegalStateException
  - assertThatIOException

```java
@Test
void exceptionTest() {

    //assertThatThrownBy
    assertThatThrownBy(() -> {
        "abc".charAt(3);
    }).isInstanceOf(IndexOutOfBoundsException.class)
            .hasMessageContaining("String index out of range: 3");

    //assertThatExceptionOfType
    assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> {"abc".charAt(3);})
            .withMessage("String index out of range: %s", "3")
            .withMessageContaining("index out of")
            .withNoCause();

    //assertThatIOException
    assertThatIOException().isThrownBy(() -> { throw new IOException("boom!"); })
        .withMessage("%s!", "boom")
        .withMessageContaining("boom")
        .withNoCause();

    //doesNotThrowAnyException
    assertThatCode(() -> {
        "abc".charAt(2);
    }).doesNotThrowAnyException();
}
```

### Reference

- [Introduction to AssertJ](https://www.baeldung.com/introduction-to-assertj)
- [AssertJ Core features highlight](https://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html)
