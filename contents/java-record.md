# Record

`record` 클래스를 잘 알고 사용하기 위해 java14에 출시한 `record` 클래스를 알아보려고 합니다.

## Intro

Record 클래스는 데이터를 저장하는 데 사용되는 DTO(Data Transfer Object)나 VO(Value Object) 같은 간단한 데이터를 운반하기 위한 불변 클래스를 정의할 때 간결하고 유용하게 사용할 수 있습니다.
<br/>

record 클래스는 데이터를 저장하는데 사용되는 DTO(Data Transfer Object)라고 불리는 클래스를 API 요청, 쿼리 결과 등의 데이터 이동에 사용하곤 합니다.
<br/>

DTO는 대부분 불변성을 가지고 있는데, 불변 클래스로 만들기 위해 적지 않은 노력이 필요합니다.
- 각 데이터에 private, final 키워드 선언
- 각 필드에 대한 getter
- 각 필드를 파라미터로 갖는 public 생성자
- 모든 필드가 일치할 때 동일한 클래스의 객체에 대하여 true 를 반환하는 equals 메서드
- 모든 필드가 일치할 때 동일한 값을 반환하는 hashCode 메서드
- 클래스 이름, 각 필드의 이름과 값을 포함하는 toString 메서드

DTO 로 사용되는 Person 클래스를 살펴보겠습니다.
- 다행히도 lombok 덕분에 getter, toString, equals, hashCode 메서드는 애노테이션만으로 선언할 수 있기도 합니다.
<br/>

```java
public class Person {

    private final String name;
    private final String address;

    public Person(String name, String address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Person)) {
            return false;
        } else {
            Person other = (Person) obj;
            return Objects.equals(name, other.name)
              && Objects.equals(address, other.address);
        }
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", address=" + address + "]";
    }

    // standard getters
}
```

lombok 덕분에 코드가 간결하게 유지될 수 있긴 하지만 DTO 클래스 생성을 위해 필드 생성, 애노테이션 선언 등의 작업이 반복되는 것은 막을 수 없습니다.
<br/>

또한, 단순히 두 필드를 갖는 클래스에 여러 선언이 추가되면서 클래스의 목적이 모호해지게 됩니다.

## Record Class

JDK 14 이후 DTO 생성을 위해 반복되는 작업을 레코드로 대체할 수 있게 되었습니다.
- 단순하게 필드 유형과 이름만 있으면 되는 불변 데이터 클래스입니다.

Java compiler 는 record 선언 시 아래 항목들을 생성합니다.
- Field
  - private
  - final
  - public constructor
- Method
  - getter
  - toString
  - equals
  - hashCode

기존 Person DTO 클래스를 record 키워드를 사용하여 변경하면 아래와 같이 단순하게 생성할 수 있습니다.
<br/>

```java
public record Person (String name, String address) {}
```

record 클래스에 static field, static method .. 등을 추가해 볼 수도 있습니다.
<br/>

```java
@Builder
record Person(String firstName, String secondName, String address) {
    // include static variables
    private static final String UNKNOWN_ADDRESS = "Unknown";

    Person {
        Assert.hasText(firstName, "firstName must not be empty");
        Assert.hasText(secondName, "secondName must not be empty");
        Assert.hasText(address, "address must not be empty");
    }

    Person(String firstName, String secondName) {
        this(firstName, secondName, UNKNOWN_ADDRESS);
    }

    // include static methods
    public static Person getDefaultPerson() {
        return Person.builder()
                .firstName("Aaron")
                .secondName("Park")
                .address("Korea Seoul")
                .build();
    }

    String generateFullName() {
        return this.firstName + " " + this.secondName;
    }
}
```

## Bytecode of Record

위에서 생성한 Record 를 바이트 코드로 확인해 보겠습니다.
- 별도로 추가한 `@Builder` 관련 코드는 생략하도록 하겠습니다.

```java
// Record 클래스는 내부적으로 이미 Record 클래스를 상속받고 있으므로 다른 클래스를 상속받을 수 없습니다.
final class com/example/example/record/Person extends java/lang/Record {

  // ...

  // 필드는 기본적으로 private final 로 변환됩니다.
  private final Ljava/lang/String; firstName
  private final Ljava/lang/String; secondName
  private final Ljava/lang/String; address
  private final static Ljava/lang/String; UNKNOWN_ADDRESS = "Unknown"

  // public constructor 가 자동으로 생성된 것을 확인할 수 있습니다.
  <init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    // parameter  firstName
    // parameter  secondName
    // parameter  address

  // toString 메서드가 자동으로 생성된 것을 확인할 수 있습니다.
  public final toString()Ljava/lang/String;
   
  // hashCode 메서드가 자동으로 생성된 것을 확인할 수 있습니다.
  public final hashCode()I
   
  // equals 메서드가 자동으로 생성된 것을 확인할 수 있습니다.
  public final equals(Ljava/lang/Object;)Z

  // getter 메서드가 자동으로 생성된 것을 확인할 수 있습니다.
  public firstName()Ljava/lang/String;
  public secondName()Ljava/lang/String;
  public address()Ljava/lang/String;

  // ...
}
```

## Test

record 를 사용하면 기존 DTO 클래스와 동일하게 메서드들을 사용할 수 있습니다. 테스트 코드로 확인해 보겠습니다.
<br/>

```java
public class RecordTest {

    private static final String FIRST_NAME = "Aaron";
    private static final String SECOND_NAME = "Park";
    private static final String ADDRESS = "Korea Seoul";

    @Test
    void record_constructor_and_getter_test() {
        final Person person = Person.getDefaultPerson();

        Assertions.assertEquals(FIRST_NAME, person.firstName());
        Assertions.assertEquals(SECOND_NAME, person.secondName());
        Assertions.assertEquals(ADDRESS, person.address());
    }

    @Test
    void record_equals_and_hashCode_test() {
        final Person person1 = Person.getDefaultPerson();
        final Person person2 = Person.getDefaultPerson();

        Assertions.assertTrue(person1.equals(person2));
        Assertions.assertEquals(person1.hashCode(), person2.hashCode());
    }

    @Test
    void record_toString_test() {
        final Person person = Person.getDefaultPerson();

        Assertions.assertEquals("Person[firstName=Aaron, secondName=Park, address=Korea Seoul]", person.toString());
    }

    @Test
    void record_method_test() {
        final Person person = Person.getDefaultPerson();

        final String result = person.generateFullName();

        Assertions.assertEquals("Aaron Park", result);
    }
}
```

## pros and cons

장점.
- 간결성: 데이터를 운반하는 불변 클래스를 간결하게 정의
- 불변성: 모든 필드는 기본적으로 final
- 자동 생성 메서드: equals(), hashCode(), toString(), getter() 메서드가 자동으로 생성
- 명확한 의도: 데이터 운반 목적으로 설계되어 클래스의 목적이 명확

단점.
- 유연성 부족: 상속 불가. 다형성을 활용한 설계에는 부적합
- 불변성 제한: 필드가 모두 final 이므로 상태를 변경할 수 있는 객체를 다룰 때는 사용하기 어려움
- 커스터마이징 제한: 자동 생성된 메서드의 동작을 변경하거나 추가적인 로직을 구현하려면, 별도의 메서드 정의가 필요

## Reference
- [Java 14 Record Keyword](https://www.baeldung.com/java-record-keyword)
- [java17 Record Classes](https://docs.oracle.com/en/java/javase/17/language/records.html#GUID-6699E26F-4A9B-4393-A08B-1E47D4B2D263)
- [java14 ](https://docs.oracle.com/en/java/javase/14/language/records.html#GUID-6699E26F-4A9B-4393-A08B-1E47D4B2D263)