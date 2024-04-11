# Spring 공통 예외 처리(ControllerAdvice, ExceptionHandler)

`@ControllerAdvice`, `@ExceptionHandler` 는 애플리케이션의 모든 컨트롤러에서 발생할 수 있는 예외를 한 곳에서 관리하기 위해 설계된 기능입니다.<br/>예외 발생 시 ResponseEntity 또는 JSON/XML 형태로 클라이언트에게 에러 정보를 제공할 수 있고, 특정 컨트롤러, 패키지에 대한 예외만 처리할 수도 있습니다.

.
<br/>

아래 코드를 보면 bindingResult 를 검증하는 코드와 try-catch 로 예외를 핸들링하는 것을 볼 수 있습니다.<br/>모든 컨트롤러에 아래와 같은 형태로 구현을 하게 되면 컨트롤러에 코드가 거대해지고 코드를 읽기도 어려워질 것 같죠?!<br/>제가 그랬었답니다..😅

```java
@PutMapping("/example")
public ResponseEntity example(@RequestBody final Request request, final BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        if (!CollectionUtils.isEmpty(allErrors)) {
            return BasicResponse.clientError(allErrors.get(0).getDefaultMessage());
        }
        return BasicResponse.clientError(FAIL.message());
    }

    try {
        final Response response = service.method(request);
        return BasicResponse.ok(response);
    } catch (IllegalArgumentException e) {
        return BasicResponse.clientError(e.getMessage());
    } catch (Exception e) {
        return BasicResponse.internalServerError("Failed to process. Please try again.");
    }
}
```

여기에 `@ControllerAdvice`, `@ExceptionHandler` 을 적용하면 어떻게 될까요?<br/>
예외를 한 곳에서 관리하게 되어 컨트롤러에서 예외를 핸들링하는 코드가 불필요해졌고, 중복되는 코드도 사라지게 되었답니다.<br/>전과 비교하면 코드 다이어트에 성공한 모습을 볼 수 있습니다.

```java
@PutMapping("/example")
public ResponseEntity example(@RequestBody final Request request) {
    final Response response = service.method(request);
    return BasicResponse.ok(response);
}
```

이제 그렇다면 본격적으로 `@ControllerAdvice`, `@ExceptionHandler` 를 알아보려고 합니다.

## ControllerAdvice

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ControllerAdvice { ... }

...

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ControllerAdvice
@ResponseBody
public @interface RestControllerAdvice { ... }
```

일반적으로 `@ExceptionHandler`, `@InitBinder`, `@ModelAttribute` 메서드는 `@Controller` 선언된 클래스(또는 클래스 계층) 내에 적용됩니다.<br/>이러한 메서드를 여러 컨트롤러에 걸쳐 보다 전역적으로 적용하려면 `@ControllerAdvice` 또는 `@RestControllerAdvice`를 사용할 수 있습니다.
<br/>

`@ControllerAdvice`는 `@Component` 가 포함되어 있으므로 스프링 빈에 등록됩니다.<br/>`@RestControllerAdvice`는 `@ResponseBody`가 추가된 형태로 `@ExceptionHandler` 메서드가 메시지 변환을 통해 응답 본문으로 렌더링 할 수 있음을 의미합니다.
<br/>

시작 시 `@RequestMapping`, `@ExceptionHandler` 메서드를 위한 인프라 클래스들은 `@ControllerAdvice` 주석이 붙은 스프링 빈을 탐지한 다음 런타임에 해당 메서드를 적용합니다.<br/>`@ControllerAdvice` 에서의 `@ExceptionHandler` 메서드는 `@Controller` 에서의 로컬 메서드 이후 적용됩니다. 반대로 `@ModelAttribute`, `@InitBinder` 메서드는 로컬 메서드 이전에 적용됩니다.

### Example

특정 컨트롤러, 패키지에 대한 예외만 처리하도록 적용할 수도 있습니다.

- RestController 어노테이션이 선언된 모든 컨트롤러 대상으로 적용

    ```java 
    @ControllerAdvice(annotations = RestController.class)
    public class ExampleAdvice1 {}
    ```

- 특정 패키지에 포함된 모든 컨트롤러 대상으로 적용

    ```java
    @ControllerAdvice("org.example.controllers")
    public class ExampleAdvice2 {}
    ```

- 특정 클래스를 할당할 수 있는 모든 컨트롤러 대상으로 적용

    ```java
    @ControllerAdvice(assignableTypes = {ControllerInterface.class, AbstractController.class})
    public class ExampleAdvice3 {}
    ```

## ExceptionHandler

특정 예외를 처리하는 메서드를 정의할 때 사용되는 어노테이션입니다.<br/>`@Controller`, `@RestController` 내부, 또는 `@ControllerAdvice`, `@RestControllerAdvice` 클래스 내부에 사용할 수 있습니다.
<br/>

지정된 예외 타입이 해당 컨트롤러/어드바이스에서 발생하면, 어노테이션에 의해 마킹된 메서드가 호출되는 방식입니다.<br/>각 예외마다 다른 처리 로직을 구현할 수 있고, 예외에 따라 다른 HTTP 상태 코드, 헤더, 본문을 응답으로 반환할 수도 있습니다.

### Example

본문 초반에 코드 다이어트에 성공한 코드를 소개했었는데요.<br/>`@ControllerAdvice`, `@ExceptionHandler` 를 적용하여 어떻게 코드 다이어트에 성공했는지 확인해 보려고 합니다.

```java
@PutMapping("/example")
public ResponseEntity example(@RequestBody final Request request) {
    final Response response = service.method(request);
    return BasicResponse.ok(response);
}
```

.

`@ControllerAdvice`, `@ExceptionHandler` 를 적용하여 예외를 처리하는 코드의 일부입니다.

```java
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class RestControllerExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        if (bindingResult.hasErrors()) {
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            if (!CollectionUtils.isEmpty(allErrors)) {
                return BasicResponse.clientError(allErrors.get(0).getDefaultMessage());
            }
            return BasicResponse.clientError(FAIL.message());
        }
        return BasicResponse.clientError(ex.getMessage());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            // ...
    })
    public ResponseEntity handleBadRequest(Exception ex) {
        return BasicResponse.clientError(ex.getMessage());
    }

    ㅊ
    public ResponseEntity handleException(Exception ex) {
        log.error("Exception. ", ex);
        return BasicResponse.internalServerError("An error occurred while processing the request. Please try again.");
    }

    // ...
}
```

- `@RestControllerAdvice(annotations = RestController.class)`
  - RestController 어노테이션이 선언된 모든 컨트롤러를 대상으로 적용하였습니다.
  - Controller 어노테이션이 선언된 컨트롤러는 에러 페이지를 띄워주어야 할 수도 있으므로 컨트롤러 특성에 따라 별도로 관리할 수 있습니다.
- `@ExceptionHandler(MethodArgumentNotValidException.class)`
  - 데이터 바인딩 유효성 검사에서 실패할 경우 MethodArgumentNotValidException 이 발생하여 해당 메서드를 거치게 됩니다.
- `@ExceptionHandler(IllegalArgumentException.class)`
  - IllegalArgumentException 발생 시 해당 메서드를 거치게 됩니다.
- `@ExceptionHandler(Exception.class)`
  - 그밖에 Exception 발생 시 해당 메서드를 거치게 됩니다.

## Finish

컨트롤러 단에서 try-catch 를 사용하여 예외를 핸들링하는 코드에 대한 질문을 받게 되었고, 예외를 공통으로 관리할 수 있는 보다 좋은 방법이 있을 것 같다는 조언을 듣게 되었습니다. 그렇게 컨트롤러 단에서 try-catch 없이도 예외를 공통으로 처리할 수 있는 방법을 찾아보다가 `@ControllerAdvice`, `@ExceptionHandler` 를 알게 되었습니다.
<br/>

예외를 한 곳에서 관리하게 되면서 컨트롤러에서 핸들링해야 할 요소들이 많이 줄어들게 되었고, try-catch 로 도배되어 있던 컨트롤러 코드를 깔끔하게 정리할 수 있게 되었습니다.
<br/>

코드를 간결하게 작성하여 클래스를 가볍게 유지하기 위해 적용할 수 있는 다양한 방법들을 차근차근 알아가 봐야겠습니다.🤓

## Reference

- [Controller Advice](https://docs.spring.io/spring-framework/reference/web/webflux/controller/ann-advice.html)
- [Annotation Interface RestControllerAdvice](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RestControllerAdvice.html)
- [Annotation Interface ExceptionHandler](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ExceptionHandler.html)
