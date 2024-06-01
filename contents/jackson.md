# jackson Serialized/Deserialized

Jackson 직렬화/역직렬화에 대하여 알아보려고 합니다.

## Field

먼저 필드의 `접근 제어자`와 `Jackson`의 관계는 어떠할까요?!

> ℹ️ Jackson 은 public 필드만 기본적으로 직렬화하고 있습니다.

```java
@Test
void jackson_and_access_level_test() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    JacksonAndAccessLevelTest dtoObject = new JacksonAndAccessLevelTest();
    String dtoAsString = mapper.writeValueAsString(dtoObject);

    System.out.println(dtoAsString);
    assertThat(dtoAsString, not(containsString("privateString")));
    assertThat(dtoAsString, not(containsString("defaultString")));
    assertThat(dtoAsString, not(containsString("protectedString")));
    assertThat(dtoAsString, containsString("publicString"));
}

private class JacksonAndAccessLevelTest {
    private String privateString;
    String defaultString;
    protected String protectedString;
    public String publicString;
}
```

결과를 보면 public 상태인 publicString 필드만 직렬화가 된 것을 확인할 수 있습니다.

## Getter

> ℹ️ private 필드에 Getter 메서드가 제공된다면 직렬화/역직렬화가 가능해 집니다.

getter 메서드가 있으면 해당 필드는 속성으로 간주됩니다.

```java
@Test
void jackson_and_getter_test() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Serialized
     */
    JacksonAndGetterTest dtoObject = new JacksonAndGetterTest();
    String dtoAsString = mapper.writeValueAsString(dtoObject);

    // {"privateString":null,"publicString":null}
    assertThat(dtoAsString, containsString("privateString"));
    assertThat(dtoAsString, not(containsString("defaultString")));
    assertThat(dtoAsString, not(containsString("protectedString")));
    assertThat(dtoAsString, containsString("publicString"));

    /**
     * Deserialized
     */
    String jsonAsString = "{\"privateString\":\"privateString\"}";
    dtoObject = mapper.readValue(jsonAsString, JacksonAndGetterTest.class);

    Assertions.assertEquals("privateString", dtoObject.getPrivateString());
}

private static class JacksonAndGetterTest {
    private String privateString;
    String defaultString;
    protected String protectedString;
    public String publicString;

    public String getPrivateString() {
        return privateString;
    }
}
```

결과를 보면 privateString 필드는 private 이지만, 직렬화/역직렬화에 모두 포함된 것을 확인할 수 있습니다.

## Setter

> ℹ️ Setter 는 private 필드만 역직렬화 가능하게 합니다.

`getter` 메서드는 **private 필드를 직렬화/역직렬화** 가능하게 하지만,<br/>
반면, `setter` 메서드는 **비공개 필드를 역직렬화만** 가능하게 합니다.

```java
 @Test
void jackson_and_setter_test() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Serialized
     */
    JacksonAndSetterTest dtoObject = new JacksonAndSetterTest();
    String dtoAsString = mapper.writeValueAsString(dtoObject);

    System.out.println(dtoAsString); // {"publicString":null}
    assertThat(dtoAsString, not(containsString("privateString")));
    assertThat(dtoAsString, not(containsString("defaultString")));
    assertThat(dtoAsString, not(containsString("protectedString")));
    assertThat(dtoAsString, containsString("publicString"));

    /**
     * Deserialized
     */
    String jsonAsString = "{\"privateString\":\"privateString\", \"defaultString\":\"defaultString\", \"protectedString\":\"protectedString\", \"publicString\":\"publicString\"}";
    dtoObject = mapper.readValue(jsonAsString, JacksonAndSetterTest.class);

    Assertions.assertEquals("privateString", dtoObject.privateString);
    Assertions.assertEquals("defaultString", dtoObject.defaultString);
    Assertions.assertEquals("protectedString", dtoObject.protectedString);
    Assertions.assertEquals("publicString", dtoObject.publicString);
}

@Setter
private static class JacksonAndSetterTest {
    private String privateString;
    String defaultString;
    protected String protectedString;
    public String publicString;
}
```

결과를 보면 예상했듯이 public 상태인 publicString 필드만 직렬화가 된 것을 확인할 수 있습니다.<br/>
반면, setter 메서드를 통해 역직렬화는 모든 필드가 가능하게 되었습니다.

setter 메서드는 필드를 `역직렬화만 할 수 있어`야 하고 `직렬화는 할 수는 없도록` 해야 합니다.

## setVisibility

소스 코드를 직접 수정할 수 없는 경우, Jackson 이 외부에서 비공개 필드를 처리하는 방식을 구성할 수 있습니다.

모든 멤버 필드를 직렬화/역직렬화 가능하도록 구성해 보겠습니다.

```java
@Test
void setVisibility_to_serialize_all_member_fields() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Serialized
     */
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    JacksonAndAccessLevelTest dtoObject = new JacksonAndAccessLevelTest();
    String dtoAsString = mapper.writeValueAsString(dtoObject);

    System.out.println(dtoAsString); // {"privateString":null,"defaultString":null,"protectedString":null,"publicString":null}
    assertThat(dtoAsString, containsString("privateString"));
    assertThat(dtoAsString, containsString("defaultString"));
    assertThat(dtoAsString, containsString("protectedString"));
    assertThat(dtoAsString, containsString("publicString"));

    /**
     * Deserialized
     */
    String jsonAsString = "{\"privateString\":\"privateString\", \"defaultString\":\"defaultString\", \"protectedString\":\"protectedString\", \"publicString\":\"publicString\"}";
    dtoObject = mapper.readValue(jsonAsString, JacksonAndAccessLevelTest.class);
    dtoAsString = mapper.writeValueAsString(dtoObject);

    System.out.println(dtoAsString); // {"privateString":"privateString","defaultString":"defaultString","protectedString":"protectedString","publicString":"publicString"}
    assertEquals("privateString", dtoObject.privateString);
    assertEquals("defaultString", dtoObject.defaultString);
    assertEquals("protectedString", dtoObject.protectedString);
    assertEquals("publicString", dtoObject.publicString);
}
```

결과를 보면, private, default, protected, public 모두 직렬화/역직렬화에 성공한 것을 볼 수 있습니다.

### ObjectMapper.setVisibility

`ObjectMapper.setVisibility` 는 객체(field/method)의 접근 유형 가시성을 제어하는 설정입니다.

.

**mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);**

- `PropertyAccessor.FIELD`: 객체의 모든 멤버 필드
- `JsonAutoDetect.Visibility.ANY`: 모든 멤버 필드(private, default, protected, public)를 감지하여 직렬화/역직렬화 허용

**mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);**

- `PropertyAccessor.GETTER`: 객체의 getter 메서드
- `JsonAutoDetect.Visibility.NONE`: 어떤 getter 메서드도 자동 감지되지 않음(접근 한정자에 관계없이 모두 무시)
- 클래스에 public getter 메서드가 존재하더라도 JSON 직렬화/역직렬화 과정에서 무시
  - getter 메서드를 통한 속성의 직렬화를 완전히 비활성화하여, 필드가 직접 접근되거나 다른 방식으로 처리되어야 함을 의미

**mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);**

- `PropertyAccessor.IS_GETTER`: boolean 속성의 getter 메서드
- `JsonAutoDetect.Visibility.NONE`: 어떤 getter 메서드도 자동 감지되지 않음(접근 한정자에 관계없이 모두 무시)
- GETTER 설정과 유사하게, is 메서드는 자동 감지되지 않으며 직렬화/역직렬화에 미포함

## JsonIgnore / JsonProperty

때로는 직렬화/역직렬화 중 하나만 가능하도록 해야 할 때가 있습니다.

직렬화는 막고 역직렬화만 가능하도록 구성해 보겠습니다.

```java
@Test
public void JsonIgnore_JsonProperty_test() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Serialized
     */
    User userObject = new User();
    userObject.setPassword("1234");

    String userAsString = mapper.writeValueAsString(userObject);

    System.out.println(userAsString); // {"email":null}
    assertThat(userAsString, not(containsString("password")));
    assertThat(userAsString, not(containsString("1234")));

    /**
     * Deserialized
     */
    String jsonAsString = "{\"email\":\"aaron@gmail.com\", \"password\":\"1234\"}";

    userObject = mapper.readValue(jsonAsString, User.class);

    assertEquals("aaron@gmail.com", userObject.getEmail());
    assertEquals("1234", userObject.getPassword());
}

private static class User {

    private String email;
    private String password;

    @JsonIgnore // 직렬화 무시
    public String getPassword() {
        return password;
    }

    @JsonProperty // 역직렬화 허용
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
}
```

직렬화 결과를 보면 password 필드는 직렬화되지 않고, email 필드만 직렬화된 것을 볼 수 있습니다.<br/>
반면, 역직렬화는 두 필드 모두 적용되었습니다.

## Reference

- <https://www.baeldung.com/jackson-field-serializable-deserializable-or-not>