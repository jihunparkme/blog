## HTML

- JSP

```html
<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <!DOCTYPE html>
<html></html>
```

- Thymeleaf

```java
<!DOCTYPE html> <html lang="ko" xmlns:th="http://www.thymeleaf.org">
```

---

## Security

- JSP

```html
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<sec:authorize access="isAnonymous()">
  This content is only shown to anonymous users.
</sec:authorize>

<sec:authorize access="isAuthenticated()">
  This content is only shown to authenticated users.
</sec:authorize>
```

- Thymeleaf

```html
<html
  lang="ko"
  xmlns:th="http://www.thymeleaf.org"
  xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity5"
></html>

<div sec:authorize="isAuthenticated()">
  This content is only shown to authenticated users.
</div>
<div sec:authorize="hasRole('ROLE_ADMIN')">
  This content is only shown to administrators.
</div>
<div sec:authorize="hasRole('ROLE_USER')">
  This content is only shown to users.
</div>
```

---

## Include

- JSP

```html
<%@ include file="/WEB-INF/views/common/head.jsp"%>
```

- Thymeleaf

```html
<div th:replace="/common/head :: head"></div>
```

---

## Link

- JSP

```html
<link href="assets/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet" />
```

- Thymeleaf

```html
<link th:href="@{/vendor/bootstrap/css/bootstrap.min.css}" rel="stylesheet" />
```

---

## Script

- JSP

```html
<script src="assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
```

- Thymeleaf

```html
<script
  type="text/javascript"
  th:src="@{/vendor/bootstrap/js/bootstrap.bundle.min.js}"
></script>
```

## ing

- JSP

```html

```

- Thymeleaf

```html

```
