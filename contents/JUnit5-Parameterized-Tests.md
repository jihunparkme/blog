# JUnit 5 Parameterized Tests

[Guide to JUnit 5 Parameterized Tests](https://www.baeldung.com/parameterized-tests-junit-5)를 통해 Parameterized Test를 학습하며 정리한 글입니다.

.

테스트 코드는 만들었는데 여러 값으로 테스트를 해보고 싶은 적이 있지 않은가?! 😯

코드가 중복되는 건 싫은데..

.

그렇다면 Parameterized Test를 사용해볼 때이다.🕵

Parameterized Test는 서로 다른 인수로 동일한 테스트를 여러 번 실행해볼 수 있는 유용한 기능이다.

일반 테스트와 다른 점은 `@ParameterizedTest`를 사용하는 것과 `@ValueSource`에 인수를 정의해주는 것뿐!

**Dependencies**

- pom.xml

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <version>5.8.2</version>
    <scope>test</scope>
</dependency>
```

- build.gradle

```gradle
testCompile("org.junit.jupiter:junit-jupiter-params:5.8.2")
```

## Parameterized Test

### @ParameterizedTest

- Parameterized Test를 적용하기 위해서 `@ParameterizedTest` 애노테이션만 추가해주면 된다.

### @ValueSource

- @ValueSource에 할당된 인수의 개수만큼 테스트가 반복
- 테스트마다 @ValueSource에 있는 배열에서 하나의 항목을 가져와 매개변수로 전달
- 테스트 가능한 인수 타입
  - short (with the shorts attribute)
  - byte (bytes attribute)
  - int (ints attribute)
  - long (longs attribute)
  - float (floats attribute)
  - double (doubles attribute)
  - char (chars attribute)
  - java.lang.String (strings attribute)
  - java.lang.Class (classes attribute)
- @ValueSource에서 String, Class의 경우 인수로 Null 전달이 불가능

## Numbers

Parameterized Test 인수로 정수를 전달하고 싶다면 `ints` 옵션을 사용하자.

```java
@ParameterizedTest
@ValueSource(ints = {1, 3, 5, -3, 15, Integer.MAX_VALUE})
@DisplayName("모든 숫자가 홀수")
void isOdd_ShouldReturnTrueForOddNumbers(int number) {
    assertTrue(Numbers.isOdd(number));
}
```
![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/numbers.png)


## String

Parameterized Test 인수로 문자열을 전달하고 싶다면 `strings` 옵션을 사용하자.

```java
@ParameterizedTest
@ValueSource(strings = {"", "  "})
@DisplayName("모든 문자열이 공백")
void isBlank_ShouldReturnTrueForNullOrBlankStrings(String input) {
    assertTrue(Strings.isBlank(input));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/string.png)

## Null and Empty Values

인수로 Null이나 Empty 값을 전달하기 위해서는 `@NullSource`, `@EmptySource`, `@NullAndEmptySource`를 사용하자.

- String , Collection, Array에 적용 가능

**Null**

```java
@ParameterizedTest
@NullSource
@DisplayName("입력 값이 Null")
void isBlank_ShouldReturnTrueForNullInputs(String input) {
    assertTrue(Strings.isBlank(input));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/null.png)

.

**Empty**

```java
@ParameterizedTest
@EmptySource
@DisplayName("문자열이 공백")
void isBlank_ShouldReturnTrueForEmptyStrings(String input) {
    assertTrue(Strings.isBlank(input));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/empty.png)

.

**Null and Empty**

```java
@ParameterizedTest
@NullAndEmptySource
@DisplayName("문자열이 Null 혹은 Empty")
void isBlank_ShouldReturnTrueForNullAndEmptyStrings(String input) {
    assertTrue(Strings.isBlank(input));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/null-and-empty.png)

.

**combine**

```java
@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"  ", "\t", "\n"})
void isBlank_ShouldReturnTrueForAllTypesOfBlankStrings(String input) {
    assertTrue(Strings.isBlank(input));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/null-combine.png)

## Enum

Enum 타입을 Parameterized Test의 인수로 전달하고 싶다면 `@EnumSource` 를 사용하자.

```java
@ParameterizedTest
@EnumSource(Month.class)
@DisplayName("모든 Month number는 1과 12 사이")
void getValueForAMonth_IsAlwaysBetweenOneAndTwelve(Month month) {
    int monthNumber = month.getValue();
    assertTrue(monthNumber >= 1 && monthNumber <= 12);
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/enum-1.png)

.

Enum의 일부 값만 인수로 사용할 경우

```java
@ParameterizedTest
@EnumSource(value = Month.class, names = {"APRIL", "JUNE", "SEPTEMBER", "NOVEMBER"})
@DisplayName("4월, 6월, 9얼, 11월은 30일까지 존재")
void someMonths_Are30DaysLong(Month month) {
    final boolean isALeapYear = false;
    assertEquals(30, month.length(isALeapYear));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/enum-2.png)

.

Enum의 일부 값을 제외한 값을 인수로 사용할 경우

- mode = EnumSource.Mode.EXCLUDE 속성을 전달

```java
@ParameterizedTest
@EnumSource(
    value = Month.class,
    names = {"FEBRUARY", "APRIL", "JUNE", "SEPTEMBER", "NOVEMBER"},
    mode = EnumSource.Mode.EXCLUDE)
@DisplayName("2월, 4월, 6월, 9얼, 11월을 제외한 달은 31일까지 존재")
void exceptFourMonths_OthersAre31DaysLong(Month month) {
    final boolean isALeapYear = false;
    assertEquals(31, month.length(isALeapYear));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/enum-3.png)

.

정규식을 name 속성에 전달할 경우

```java
@ParameterizedTest
@EnumSource(value = Month.class, names = ".+BER", mode = EnumSource.Mode.MATCH_ANY)
@DisplayName("BER로 끝나는 달은 SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER")
void fourMonths_AreEndingWithBer(Month month) {
    EnumSet<Month> months =
        EnumSet.of(Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER);
    assertTrue(months.contains(month));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/enum-4.png)

## CSV Literals

입력값과 예상 값을 전달하여 테스트가 필요할 경우  `@CsvSource`를 사용하자.

- 테스트가 반복될 때마다 @CsvSource에서 하나의 배열 항목을 가져와 쉼표로 분할하고 각 배열을 매개변수로 전달

```java
@ParameterizedTest
@CsvSource({"test,TEST", "tEst,TEST", "Java,JAVA"})
@DisplayName("입력값과 예상값을 쉼표로 구분")
void toUpperCase_ShouldGenerateTheExpectedUppercaseValue(String input, String expected) {
    String actualValue = input.toUpperCase();
    assertEquals(expected, actualValue);
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/csv-1.png)

.

*delimiter* 속성을 전달하여 구분 기호 설정 가능

```java
@ParameterizedTest
@CsvSource(value = {"test:test", "tEst:test", "Java:java"}, delimiter = ':')
@DisplayName("입력값과 예상값을 콜론으로 구분")
void toLowerCase_ShouldGenerateTheExpectedLowercaseValue(String input, String expected) {
    String actualValue = input.toLowerCase();
    assertEquals(expected, actualValue);
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/csv-2.png)

## CSV File

`@CsvSource` 대신 실제 CSV 파일을 인수로 전달하려면 `@CsvFileSource`를 사용하자.

- csv 파일 헤더를 무시하려면 numLinesToSkip 옵션을 전달하자
- 필요에 따라 *lineSeparator*, *encoding* 속성도 사용

```java
@ParameterizedTest
@CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
@DisplayName("실제 csv 파일을 인수로 전달")
void toUpperCase_ShouldGenerateTheExpectedUppercaseValueCSVFile(
    String input, String expected) {
    String actualValue = input.toUpperCase();
    assertEquals(expected, actualValue);
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/csv-file.png)

## Method

메서드를 인수로 전달하려면 `@MethodSource`를 사용하자.

- 인수로 전달되는 메서드는 컬렉션 인터페이스를 반환할 수 있다.
- 서로 다른 테스트 클래스 간에 인수를 공유할 경우 유용하게 사용될 것 같다.

```java
@ParameterizedTest
@MethodSource("provideStringsForIsBlank")
@DisplayName("메서드를 인수로 전달")
void isBlank_ShouldReturnTrueForNullOrBlankStrings(String input, boolean expected) {
    assertEquals(expected, Strings.isBlank(input));
}

private static Stream<Arguments> provideStringsForIsBlank() {
    return Stream.of(
        Arguments.of(null, true),
        Arguments.of("", true),
        Arguments.of("  ", true),
        Arguments.of("not blank", false)
    );
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/method-source.png)

.

메서드가 하나의 인수만 전달하는 경우 `@MethodSource` 에 메서드명을 전달을 하지 않아도 된다.

- `@MethodSource`에 메서드명을 전달하지 않으면 JUnit은 테스트 메서드와 이름이 같은 메서드를 탐색

```java
@ParameterizedTest
@MethodSource
@DisplayName("메서드가 하나의 인수만 전달")
void isBlank_ShouldReturnTrueForNullOrBlankStringsOneArgument(String input) {
    assertTrue(Strings.isBlank(input));
}

private static Stream<String> isBlank_ShouldReturnTrueForNullOrBlankStringsOneArgument() {
    return Stream.of(null, "", "  ");
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/method-source-2.png)

.

서로 다른 테스트 클래스 간에 인수를 공유
  - `@MethodSource`에 해당 메서드의 패키지 경로도 모두 작성해주자.

```java
class StringsUnitTest {
    @ParameterizedTest
    @MethodSource("com.aaron.parameterized.StringParams#blankStrings")
    @DisplayName("서로 다른 테스트 클래스 간 인수 공유")
    void isBlank_ShouldReturnTrueForNullOrBlankStringsExternalSource(String input) {
        assertTrue(Strings.isBlank(input));
    }
}

public class StringParams {
    static Stream<String> blankStrings() {
        return Stream.of(null, "", "  ");
    }
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/method-source-3.png)

## Interface

`ArgumentsProvider` Interface를 구현하여 테스트 인수를 전달할 수도 있다.

```java
class BlankStringsArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
          Arguments.of((String) null), 
          Arguments.of(""), 
          Arguments.of("   ") 
        );
    }
}
```

.

구현한 ArgumentsProvider를 테스트 인수로 사용하기 위해 `@ArgumentsSource`에 명시해주자.

```java
@ParameterizedTest
@ArgumentsSource(BlankStringsArgumentsProvider.class)
@DisplayName("ArgumentsProvider Interface 구현")
void isBlank_ShouldReturnTrueForNullOrBlankStringsArgProvider(String input) {
    assertTrue(Strings.isBlank(input));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/interface.png)

## Argument Conversion

### 암시적 변환

JUnit 5는 문자열 인수를 내장된 여러 암시적 변환 컨버터를 활용하여 변환시켜준다.

- *UUID* 
- *Locale*
- *LocalDate*, *LocalTime*, *LocalDateTime*, *Year*, *Month*, etc.
- *File* and *Path*
- *URL* and *URI*
- *Enum* subclasses

```java
@ParameterizedTest
@CsvSource({"APRIL", "JUNE", "SEPTEMBER", "NOVEMBER"})
@DisplayName("인수의 암시적 변환")
void someMonths_Are30DaysLongCsv(Month month) {
    final boolean isALeapYear = false;
    assertEquals(30, month.length(isALeapYear));
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/implicit-conversion.png)

### 명시적 변환

암시적 변환이 불가능한 인수는 `ArgumentConverter` Interface 구현을 통해 명시적으로 변환시킬 수 있다.

```java
class SlashyDateConverter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        if (!(source instanceof String)) {
            throw new IllegalArgumentException("The argument should be a string: " + source);
        }
        
        try {
            String[] parts = ((String) source).split("/");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert", e);
        }
    }
}
```

.

`@ConvertWith`를 추가하여 명시적 변환에 사용할 컨버터를 명시해주자.

```java
@ParameterizedTest
@CsvSource({"2018/12/25,2018", "2019/02/11,2019"})
@DisplayName("yyyy/mm/dd 형식의 문자열을 LocalDate 인스턴스로 변환")
void getYear_ShouldWorkAsExpected(
    @ConvertWith(SlashyDateConverter.class) LocalDate date, int expected) {
    assertEquals(expected, date.getYear());
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/explicit-conversion.png)

##  Argument Accessor

기본적으로 parameterized test에 제공되는 인수는 단일 메서드의 매개변수에 해당한다.

단일 메서드에 여러 매개변수를 전달하고자 한다면 `ArgumentsAccessor`를 사용해보자.

```java
class Person {
    String firstName;
    String middleName;
    String lastName;

    // constructor
    
    public String fullName() {
        if (middleName == null || middleName.trim().isEmpty()) {
            return String.format("%s %s", firstName, lastName);
        }

        return String.format("%s %s %s", firstName, middleName, lastName);
    }
}
```

.

전달된 모든 인수를 `ArgumentsAccessor`의 인스턴스로 캡슐화하고 인덱스로 검색

- `getString(index)` : 특정 인덱스에서 요소를 검색하고 *String*으로 변환
- `get(index)` : 특정 인덱스의 요소를 *Object*로 검색
- `get(index, type)` :  특정 인덱스에서 요소를 검색하고 지정된 *Class Type* 으로 변환

```java
@ParameterizedTest
@CsvSource({"Isaac,,Newton,Isaac Newton", "Charles,Robert,Darwin,Charles Robert Darwin"})
@DisplayName("Argument Accessor text")
void fullName_ShouldGenerateTheExpectedFullName(ArgumentsAccessor argumentsAccessor) {
    String firstName = argumentsAccessor.getString(0);
    String middleName = (String) argumentsAccessor.get(1);
    String lastName = argumentsAccessor.get(2, String.class);
    String expectedFullName = argumentsAccessor.getString(3);

    Person person = new Person(firstName, middleName, lastName);
    assertEquals(expectedFullName, person.fullName());
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/arguments-accessor.png)

## Argument Aggregator

`ArgumentsAccessor` Interface를 구현하여 재사용 가능한 `ArgumentsAccessor` 작성하기

```java
class PersonAggregator implements ArgumentsAggregator {

    @Override
    public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
      throws ArgumentsAggregationException {
        return new Person(
          accessor.getString(1), accessor.getString(2), accessor.getString(3));
    }
}
```

.

`@AggregateWith`에 구현한 `ArgumentsAccessor` 를 명시해주자.

```java
@ParameterizedTest
@CsvSource({"Isaac Newton,Isaac,,Newton", "Charles Robert Darwin,Charles,Robert,Darwin"})
@DisplayName("ArgumentsAccessor 구현")
void fullName_ShouldGenerateTheExpectedFullName(
    String expectedFullName,
    @AggregateWith(PersonAggregator.class) Person person) {

    assertEquals(expectedFullName, person.fullName());
}
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/arguments-accessor-2.png)

## Customizing Display Names

`@ParameterizedTest`의 name 속성을 통해 결과를 더 읽기 쉽게 확인할 수 있다.

```java
@ParameterizedTest(name = "{index} {0} is 30 days long")
@EnumSource(value = Month.class, names = {"APRIL", "JUNE", "SEPTEMBER", "NOVEMBER"})
void someMonths_Are30DaysLong(Month month) {
    final boolean isALeapYear = false;
    assertEquals(30, month.length(isALeapYear));
}
```

- before

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/customizing-display-names-before.png)

- after

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/JUnit5-Parameterized-Tests/customizing-display-names.png)

