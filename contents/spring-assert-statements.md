# Spring Assert Statements

`package org.springframework.util;`

인수를 검증하고 예외를 발생시키는 유용한 클래스

## Assert Class

Spring Assert 특징

- Assert 메서드는 정적
- 예상되는 인수를 가정하고, 조건이 충족되지 않으면 런타임 예외를 발생
- 첫 번째 매개 변수는 검증을 위한 인수 또는 확인할 논리적 조건
- 두 번째 매개 변수는 유효성 검사에 실패할 경우 표시되는 예외 메시지

## Example

```java
public class Car {
    private String state = "stop";

    /**
     * speed > 0 조건을 충족하지 않을 경우 런타임 예외 발생
     */
    public void drive(int speed) {
        Assert.isTrue(speed > 0, "speed must be positive");
        this.state = "drive";
        // ...
    }
}
```

`Assert.isTrue(speed > 0, "speed must be positive");` 코드를 풀어쓴다면 아래와 같이 작성할 수 있다.

```java
if (!(speed > 0)) {
    throw new IllegalArgumentException("speed must be positive");
}
```

## Assertions

[Class Assert](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/Assert.html)

### Logical Assertions

**`isTrue`**

- Assert a boolean expression, throwing an IllegalArgumentException if the expression evaluates to false.

**`state`**

- Assert a boolean expression, throwing an IllegalStateException if the expression evaluates to false.

  ```java
  public void fuel() {
      Assert.state(this.state.equals("stop"), "car must be stopped");
      // ...
  }
  ```

### Object and Type Assertions

**`notNull`**

- Assert that an object is not null.

  ```java
  public void сhangeOil(String oil) {
    Assert.notNull(oil, "oil mustn't be null");
    // ...
  }
  ```

**`isNull`**

- Assert that an object is null.

  ```java
  public void replaceBattery(CarBattery carBattery) {
      Assert.isNull(
        carBattery.getCharge(), 
        "to replace battery the charge must be null");
      // ...
  }
  ```

**`isInstanceOf`**

- Assert that the provided object is an instance of the provided class.

  ```java
  public void сhangeEngine(Engine engine) {
      Assert.isInstanceOf(ToyotaEngine.class, engine);
      // ...
  }
  ```

**`isAssignable`**

- Assert that superType.isAssignableFrom(subType) is true.

  ```java
  public void repairEngine(Engine engine) {
      Assert.isAssignable(Engine.class, ToyotaEngine.class);
      // ...
  }
  ```

###  Text Assertions

**`hasLength`**

- Assert that the given String is not empty; that is, it must not be null and not the empty String.

  ```java
  public void startWithHasLength(String key) {
      Assert.hasLength(key, "key must not be null and must not the empty");
      // ...
  }
  ```

**`hasText`**

- Assert that the given String contains valid text content; that is, it must not be null and must contain at least one non-whitespace character.

  ```java
  public void startWithHasText(String key) {
      Assert.hasText(
        key, 
        "key must not be null and must contain at least one non-whitespace  character");
      // ...
  }
  ```

**`doesNotContain`**

- Assert that the given text does not contain the given substring.

  ```java
  public void startWithNotContain(String key) {
      Assert.doesNotContain(key, "123", "key mustn't contain 123");
      // ...
  }
  ```

### Collection and Map Assertions

**`notEmpty() for Collections`**

- Assert that a collection contains elements; that is, it must not be null and must contain at least one element.

  ```java
  public void repair(Collection<String> repairParts) {
      Assert.notEmpty(
        repairParts, 
        "collection of repairParts mustn't be empty");
      // ...
  }
  ```

**`notEmpty() for Maps`**

- Assert that a Map contains entries; that is, it must not be null and must contain at least one entry.

  ```java
  public void repair(Map<String, String> repairParts) {
      Assert.notEmpty(
        repairParts, 
        "map of repairParts mustn't be empty");
      // ...
  }
  ```

### Array Assertions

**`notEmpty() for Arrays`**

- Assert that an array contains elements; that is, it must not be null and must contain at least one element.
  
  ```java
  public void repair(String[] repairParts) {
      Assert.notEmpty(
        repairParts, 
        "array of repairParts mustn't be empty");
      // ...
  }
  ```

**`noNullElements`**

- Assert that an array contains no null elements.

  ```java
  public void repairWithNoNull(String[] repairParts) {
      Assert.noNullElements(
        repairParts, 
        "array of repairParts mustn't contain null elements");
      // ...
  }
  ```

## Reference

[Spring Assert Statements](https://www.baeldung.com/spring-assert)

[Class Assert](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/Assert.html)