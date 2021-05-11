## HTML

- JSP

```jsp
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

```jsp
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
```

- Thymeleaf

```html
<html
  lang="ko"
  xmlns:th="http://www.thymeleaf.org"
  xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity5"
></html>
```

---

## Include

- JSP

```jsp
<%@ include file="/WEB-INF/views/common/head.jsp"%>
```

- Thymeleaf

```html
<div th:replace="/common/head :: head"></div>
```

---

## Link

- JSP

```jsp
<link href="assets/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet" />
```

- Thymeleaf

```html
<link th:href="@{/vendor/bootstrap/css/bootstrap.min.css}" rel="stylesheet" />
```

---

## Script

- JSP

```jsp
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

```jsp

```

- Thymeleaf

```html

```
