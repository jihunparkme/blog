# Spring Error Handling

[Custom Error Pages Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-web-applications.spring-mvc.error-handling)

- Spring Boot 는 Error Handling 과정에서 먼저 Custom Error Page 를 찾고, 없을 경우 White label Error Page를 보여준다.
- Spring Document를 참고하면 Custom Error Pages 를 쉽게 적용할 수 있다.

# Add Error Pages

- 특정 상태 코드에 대한 사용자 정의 HTML 오류 페이지를 표시하려면 /error 디렉터리에 파일을 추가하자.
- 오류 페이지는 정적(static) HTML거나 템플릿(templates)을 사용하여 작성할 수 있다.
- 파일 이름은 `정확한 상태 코드` 또는 `영상 시리즈 마스크`

**static HTML file**

- map `404`

```properties
src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- public/
             +- error/
             |   +- 404.html
             +- <other public assets>
```

**template (Free marker)**

- map all `5xx`

```properties
src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- templates/
             +- error/
             |   +- 5xx.ftlh
             +- <other templates>
```

# View

**resources/static/error/404.html**

- 정적 HTML을 사용하는 방법

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Page Not Found</title>
  </head>
  <body>
    Page Not Found
  </body>
</html>
```

**templates/error/4xx.html**

- templates를 사용하면 오류 속성을 활용할 수 있음
  - timestamp : 오류 발생 시각
  - status : HTTP Status Code
  - error : 오류 발생 이유
  - exception : 예외 클래스 이름
  - message : 예외 메시지
  - errors : BindingResult 예외로 발생한 모든 오류
  - trace : 예외 스택 트레이스
  - path : 오류가 발생했을때 요청한 URL 경로

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
  <head>
    <meta charset="UTF-8" />
    <title>Page Not Found</title>
  </head>
  <body>
    <h2>Sorry! Page not found.</h2>
    <h3 th:text="@{${error} + ' (' + ${path} + ')'}"></h3>

    <h3 class="h1" th:text="${'path: ' + path}"></h3>
    <!-- 400 -->
    <h3 class="h1" th:text="${'error: ' + error}"></h3>
    <!-- Bad Request -->
    <h3 class="h1" th:text="${'status: ' + status}"></h3>
    <!-- /gallery/d -->
  </body>
</html>
```

# Result

![structure](https://raw.githubusercontent.com/jihunparkme/blog/main/img/error1.jpg 'structure')

![Result1](https://raw.githubusercontent.com/jihunparkme/blog/main/img/error2.jpg 'Result1')

![Result2](https://raw.githubusercontent.com/jihunparkme/blog/main/img/error3.jpg 'Result2')

# Reference

> [HTTP Status Codes](https://httpstatuses.com/)
>
> [https://goddaehee.tistory.com/214](https://goddaehee.tistory.com/214)

# Project Code

[Github](https://github.com/jihunparkme/blog/tree/main/projects/Spring_Error_Handling/demo)
