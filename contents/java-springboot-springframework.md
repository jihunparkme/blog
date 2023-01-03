# Java / Spring Framework / Spring Boot Release

## Java

### Java 10

**[Local Variable Type Inference](https://openjdk.org/projects/amber/guides/lvti-style-guide)** 

```java
// Choose variable names that provide useful information.
// Before
List<Customer> x = dbconn.executeQuery(query);
// After
var custList = dbconn.executeQuery(query); 
```

```java
// Consider var when the initializer provides sufficient information to the reader.
// Before
ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 // After
var outputStream = new ByteArrayOutputStream();

 // Before
BufferedReader reader = Files.newBufferedReader(...);
List<String> stringList = List.of("a", "b", "c");
 // After
var reader = Files.newBufferedReader(...);
var stringList = List.of("a", "b", "c");
```

```java
// Take care when using var with literals.
// Before
try (InputStream is = socket.getInputStream();
     InputStreamReader isr = new InputStreamReader(is, charsetName);
     BufferedReader buf = new BufferedReader(isr)) {
    return buf.readLine();
}
// After
try (var inputStream = socket.getInputStream();
     var reader = new InputStreamReader(inputStream, charsetName);
     var bufReader = new BufferedReader(reader)) {
    return bufReader.readLine();
}
```

```java
// Don’t worry too much about “programming to the interface” with local variables.
// Before
List<String> list = new ArrayList<>();
// After
var list = new ArrayList<String>();
```

### Java 14

[Switch Expressions](https://docs.oracle.com/en/java/javase/17/language/switch-expressions.html)

```java
// case L ->" Labels
Day day = Day.WEDNESDAY;    
System.out.println(
    switch (day) {
        case MONDAY, FRIDAY, SUNDAY -> 6;
        case TUESDAY                -> 7;
        case THURSDAY, SATURDAY     -> 8;
        case WEDNESDAY              -> 9;
        default -> throw new IllegalStateException("Invalid day: " + day);
    }
);    
```

```java
// “Case L” & `yield` 
int numLetters = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> {
        System.out.println(6);
        yield 6;
    }
    case TUESDAY -> {
        System.out.println(7);
        yield 7;
    }
    case THURSDAY, SATURDAY -> {
        System.out.println(8);
        yield 8;
    }
    case WEDNESDAY -> {
        System.out.println(9);
        yield 9;
    }
    default -> {
        throw new IllegalStateException("Invalid day: " + day);
    }
}; 
// ...
```

### Java 15

[Text Blocks](https://docs.oracle.com/en/java/javase/14/text-blocks/index.html)

```java
// Using a literal string
String dqName = "Pat Q. Smith";
// Using a text block
String tbName = """
                Pat Q. Smith""";

dqName.equals(tbName)    // true
dqName == tbName         // true
```

### Java 16

[Record Classes](https://docs.oracle.com/en/java/javase/17/language/records.html)

불변 데이터 운반에 집중된 클래스
- 접근자 / 생성자 / equals / hashCode / toString 자동 생성
- 정적 필드와 초기화, 정적 메서드 사용 가능 ->  단, 인스턴스 초기화는 불가능
- Jackson 직렬화 지원
- class casting 불필요


```java
// 정식 생성자
record Rectangle(double length, double width) {
    public Rectangle(double length, double width) {
        if (length <= 0 || width <= 0) {
            throw new java.lang.IllegalArgumentException(
                String.format("Invalid dimensions: %f, %f", length, width));
        }
        this.length = length;
        this.width = width;
    }
}

// 압축 생성자
record Rectangle(double length, double width) {
    public Rectangle {
        if (length <= 0 || width <= 0) {
            throw new java.lang.IllegalArgumentException(
                String.format("Invalid dimensions: %f, %f", length, width));
        }
    }
}
```

### Java 17

[Sealed Classes](https://docs.oracle.com/en/java/javase/17/language/sealed-classes-and-interfaces.html)

- 봉인된 클래스로 사용할 수 있는 클래스를 지정
	- `final` :  더 이상 연장 불가
	- `sealed` : 허가된 서브클래스에 대하여 확장만 가능
	* `non-sealed`: 알 수 없는 하위 클래스로 확장 가능

```java
public sealed class Shape
    permits Circle, Square, Rectangle {
    // 허용 클래스 정의
}

public final class Circle extends Shape {
    public float radius;
}

// 비표준 클래스
public non-sealed class Square extends Shape {
   public double side;
}  

public sealed class Rectangle extends Shape 
    permits FilledRectangle {
    public double length, width;
}

public final class FilledRectangle extends Rectangle {
    public int red, green, blue;
}

```

## Spring Framework 6.0 

[What's New in Spring Framework 6.x · spring-projects/spring-framework Wiki · GitHub](https://github.com/spring-projects/spring-framework/wiki/What%27s-New-in-Spring-Framework-6.x/)

* Java EE 8 -> Jakarta EE 9 변경 
* 네임스페이스(namespace) 변경: **javax.* -> jakarta.***
* AOP(Ahead-Of-Time) 엔진 도입 
	* Spring Native 이미지(Graal VM image) 빌드 
	* 빌드 단계에서 정적분석을 통해서 최적화 작업 수행 
* Micrometer 관측 지원 강화 


## Spring Boot 3.0

https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes

최소 요구사항

* Gradle 7.3 
* Java 17 
* Kotlin 1.6 
* Jakarta EE 9 
* Spring Framework 6 
* 미사용 라이브러리 및 지원 중단 

## Reference

**Java**

- [Java Language Changes](https://docs.oracle.com/en/java/javase/17/language/java-language-changes.html#GUID-6459681C-6881-45D8-B0DB-395D1BD6DB9B)

**Spring Framework**

- [Releases](https://github.com/spring-projects/spring-framework/releases)

**Spring Boot**

- [Releases](https://github.com/spring-projects/spring-boot/releases)