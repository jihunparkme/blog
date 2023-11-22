# Spring MVC Custom Validation

일반적으로, 사용자 입력 검증이 필요할 경우 Spring MVC는 미리 정의된 검증자를 제공한다. 

하지만, 좀 더 특정한 유형의 입력을 검증해야 할 경우 사용자 정의 검증 로직을 자체적으로 생성할 수 있다.

.

## Dependency

Spring Boot를 사용한다면 `spring-boot-starter-web` 라이브러리에서 `hibernate-validator`을 의존하고 있으므로 추가할 필요는 없다.
- [org.hibernate:hibernate-validator](https://central.sonatype.com/artifact/org.hibernate/hibernate-validator/8.0.1.Final/overview)

.

# Custom Validation 

들어가기 전에 검증 로직 구현을 위해 필요한 [ConstraintValidator](https://docs.oracle.com/javaee%2F7%2Fapi%2F%2F/javax/validation/ConstraintValidator.html) 인터페이스를 살짝 확인해 보자.

- 주어진 `객체 유형 T`에 대하여 주어진 `제약 조건 A`를 검증하는 논리를 정의할 수 있다.
- 구현을 위해 아래와 같은 제한 사항을 준수해야 한다.
  - T는 매개 변수가 지정되지 않은 유형이어야 한다.
  - T의 일반 매개 변수는 무제한 와일드카드 유형이어야 한다.

```java
public interface ConstraintValidator<A extends Annotation, T> {

    /**
     * isValid 호출을 대비하여 validator 초기화
     * - 주어진 제약 조건 선언에 대한 제약 조건 주석을 파라미터로 전달.
     * - 검증을 위해 해당 인스턴스를 사용하기 전에 호출되는 것을 보장.
     */
	default void initialize(A constraintAnnotation) {}
    
    /**
     * 유효성 검사 로직 구현
     * - 값의 상태를 변경해서는 안됨.
     * - 동시에 접근 가능하며, 구현에 의해 스레드 안전성이 보장되어야 힘.
     */
	boolean isValid(T value, ConstraintValidatorContext context);
}
```

.

## Custom Validation Annotation

전화번호 유형을 검증하는 Custom Validation Annotation 생성

- `@Constraint` : 유효성 검사를 담당하는 ConstraintValidator 구현 클래스 명시
- message() default 값은 검증 오류 발생 시 사용자에게 표시되는 디폴트 오류 메시지

.

ContactNumberConstraint.java

```java
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@Constraint(validatedBy = ContactNumberValidator.class) 
@Target({METHOD, FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContactNumberConstraint {
    String message() default "Invalid phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

## Creating a Validator

유효성 검사 클래스

- ConstraintValidator 인터페이스 및 `isValid` 메서드 구현
  - 지정된 개체에 대해 지정된 제약 조건의 유효성을 검사하는 논리를 정의
- ConstraintValidator 인터페이스를 확인해 보면 기본적으로 많은 검증자를 제공
  - 쉬운 예로 AbstractEmailValidator, EmailValidator를 참고해 보자

```java
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ContactNumberValidator implements ConstraintValidator<ContactNumberConstraint, String> {
    
    @Override
    public void initialize(ContactNumberConstraint contactNumber) {
    }

    @Override
    public boolean isValid(String contactField, ConstraintValidatorContext context) {
        return contactField != null && contactField.matches("^01([0|1]?)-([0-9]{4})-([0-9]{4})$")
                && (contactField.length() > 8) && (contactField.length() < 14);
    }
}
```

## Applying Validation Annotation

Phone.java

- 필드에 생성한 @ContactNumberConstraint 선언

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Phone {
    @ContactNumberConstraint
    private String phone;
}
```

.

ValidatedPhoneController.java

- Validator 검증에 실패할 경우 BindingResult 객체에 검증 에러 정보가 담기게 된다.

```java
@Slf4j
@RestController
public class ValidatedPhoneController {

    @PostMapping("/validate-contact-number")
    public BasicResponse<String> submitForm(@Valid @RequestBody Phone phone, BindingResult result) {
        log.info("phone: {}", phone.getPhone());

        if(result.hasErrors()) {
            List<ObjectError> allErrors = result.getAllErrors();
            if (!CollectionUtils.isEmpty(allErrors)) {
                return BasicResponse.clientError(allErrors.get(0).getDefaultMessage());
            }
            return BasicResponse.clientError("FAIL");
        }

        return BasicResponse.success("SUCCESS");
    }
}
```

.

BasicResponse.java

```java
@Getter
@NoArgsConstructor
public class BasicResponse<T> {

    private T data;
    private HttpStatus status;
    private int code;
    private String message;

    public BasicResponse(T body) {
        this.data = body;
    }

    private BasicResponse(final HttpStatus statusCode, final String message) {
        this.status = statusCode;
        this.code = statusCode.value();
        this.message = message;
    }

    private BasicResponse(final HttpStatus statusCode, final T data) {
        this.status = statusCode;
        this.code = statusCode.value();
        this.data = data;
    }

    public static <T> BasicResponse<T> success(final T data) {
        return new BasicResponse<>(HttpStatus.OK, data);
    }

    public static <T> BasicResponse<T> serverError(final String message) {
        return new BasicResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static <T> BasicResponse<T> clientError(final String message) {
        return new BasicResponse<>(HttpStatus.BAD_REQUEST, message);
    }
}
```

## Request

정상 요청

```http
### Request
POST http://localhost:8080/validate-contact-number/
accept: */*
Content-Type: application/json

{
  "phone" : "010-1234-1234"
}

### Response
{
  "data": "SUCCESS",
  "status": "OK",
  "code": 200,
  "message": null
}
```

검증 오류

```http
### Request
POST http://localhost:8080/validate-contact-number/
accept: */*
Content-Type: application/json

{
  "phone" : "010-12341234"
}

### Response
{
  "data": null,
  "status": "BAD_REQUEST",
  "code": 400,
  "message": "Invalid phone number"
}
```

.

## TEST

```java
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidatedPhoneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void validate_contact_number_should_be_return_400() throws Exception {
        Phone request = Phone.builder()
                .phone("123")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                        post("/validate-contact-number")
                                .content(new ObjectMapper().writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        BasicResponse basicResponse =
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), BasicResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, basicResponse.getStatus());
        assertEquals("Invalid phone number", basicResponse.getMessage());
    }

    @Test
    public void validate_contact_number_should_be_return_400_2() throws Exception {
        Phone request = Phone.builder()
                .phone("01012341234")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                        post("/validate-contact-number")
                                .content(new ObjectMapper().writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        BasicResponse basicResponse =
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), BasicResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, basicResponse.getStatus());
        assertEquals("Invalid phone number", basicResponse.getMessage());
    }

    @Test
    public void validate_contact_number_should_be_return_200() throws Exception {
        Phone request = Phone.builder()
                .phone("010-1234-1234")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                        post("/validate-contact-number")
                                .content(new ObjectMapper().writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        BasicResponse basicResponse =
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), BasicResponse.class);
        assertEquals("SUCCESS", basicResponse.getData());
    }
}
```

.

# Custom Class Level Validation

클래스 레벨에서 사용 가능한 사용자 정의 유효성 검사기를 정의하여 클래스의 하나 이상의 속성을 검증

.

## Creating the Annotation

클래스에 여러 FieldValueMatch를 정의하기 위한 List 하위 인터페이스 선언

```java
@Constraint(validatedBy = FieldsValueMatchValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsValueMatch {

    String message() default "Fields values don't match!";

    String field();

    String fieldMatch();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        FieldsValueMatch[] value();
    }
}
```

.

##  Creating the Validator

```java
public class FieldsValueMatchValidator implements ConstraintValidator<FieldsValueMatch, Object> {

    private String field;
    private String fieldMatch;

    // FieldsValueMatch 선언 시 작성된 field, fieldMatch 필드값 세팅
    public void initialize(FieldsValueMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.fieldMatch = constraintAnnotation.fieldMatch();
    }

    // FieldsValueMatch가 선언된 객체에서 필드값 가져오기
    // 클래스의 특정 두 필드가 일치하는 값을 갖는지 확인
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object fieldValue = new BeanWrapperImpl(value).getPropertyValue(field);
        Object fieldMatchValue = new BeanWrapperImpl(value).getPropertyValue(fieldMatch);

        if (fieldValue != null) {
            return fieldValue.equals(fieldMatchValue);
        } else {
            return fieldMatchValue == null;
        }
    }
}
```

.

## Applying the Annotation

- field/fieldMatch 값 비교
- email(field), verifyEmail(fieldMatch)
- password(field), verifyPassword(fieldMatch)

```java
@FieldsValueMatch.List({
        @FieldsValueMatch(
                field = "password",
                fieldMatch = "verifyPassword",
                message = "Passwords do not match!"
        ),
        @FieldsValueMatch(
                field = "email",
                fieldMatch = "verifyEmail",
                message = "Email addresses do not match!"
        )
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserForm {
    private String email;
    private String verifyEmail;
    private String password;
    private String verifyPassword;
}


...

@Slf4j
@RestController
public class ValidatedVerifyController {

    @GetMapping("/user-form-validate")
    public BasicResponse<String> submitForm(@Valid @RequestBody UserForm form, BindingResult result) {
        if(result.hasErrors()) {
            List<ObjectError> allErrors = result.getAllErrors();
            if (!CollectionUtils.isEmpty(allErrors)) {
                return BasicResponse.clientError(allErrors.get(0).getDefaultMessage());
            }
            return BasicResponse.clientError("FAIL");
        }

        return BasicResponse.success("SUCCESS");
    }
}
```

.

## Request

정상 요청

```http
### Request
GET http://localhost:8080/user-form-validate
accept: */*
Content-Type: application/json

{
    "email" : "aaron@gmail.com",
    "verifyEmail" : "aaron@gmail.com",
    "password" : "pass",
    "verifyPassword" : "pass"
}

### Response
{
    "data": "SUCCESS",
    "status": "OK",
    "code": 200,
    "message": null
}
```

검증 오류

```http
### Request
POST http://localhost:8080/validate-contact-number/
accept: */*
Content-Type: application/json

{
    "email" : "aaron@gmail.com",
    "verifyEmail" : "aaron@gmail.com",
    "password" : "pass",
    "verifyPassword" : "passxx"
}

### Response
{
    "data": null,
    "status": "BAD_REQUEST",
    "code": 400,
    "message": "Passwords do not match!"
}
```

.

## TEST

```java
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidatedVerifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void validate_verify_email_and_password_should_be_return_200() throws Exception {
        UserForm request = UserForm.builder()
                .email("aaron@gmail.com")
                .verifyEmail("aaron@gmail.com")
                .password("pass")
                .verifyPassword("pass")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                        get("/user-form-validate")
                                .content(new ObjectMapper().writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        BasicResponse basicResponse =
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), BasicResponse.class);
        assertEquals("SUCCESS", basicResponse.getData());
    }

    @Test
    public void validate_verify_email_and_password_should_be_return_400() throws Exception {
        UserForm request = UserForm.builder()
                .email("aaron@gmail.com")
                .verifyEmail("aaron@gmail.com")
                .password("pass")
                .verifyPassword("passxx")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                        get("/user-form-validate")
                                .content(new ObjectMapper().writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        BasicResponse basicResponse =
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), BasicResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, basicResponse.getStatus());
        assertEquals("Passwords do not match!", basicResponse.getMessage());
    }

    @Test
    public void validate_verify_email_and_password_should_be_return_400_2() throws Exception {
        UserForm request = UserForm.builder()
                .email("aaron@gmail.com")
                .verifyEmail("aaron@gma.com")
                .password("pass")
                .verifyPassword("pass")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                        get("/user-form-validate")
                                .content(new ObjectMapper().writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        BasicResponse basicResponse =
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), BasicResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, basicResponse.getStatus());
        assertEquals("Email addresses do not match!", basicResponse.getMessage());
    }
}
```

.

---

.

**Reference**

[Spring MVC Custom Validation](https://www.baeldung.com/spring-mvc-custom-validator)