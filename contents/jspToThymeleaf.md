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

## href

- JSP

```html
<link href="assets/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet" />
```

- Thymeleaf

```html
<link th:href="@{/vendor/bootstrap/css/bootstrap.min.css}" rel="stylesheet" />
```

---

## script

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

## onclick

- JSP

```html
<button onclick="location.href='addForm.html'" type="button">등록</button>
```

- Thymeleaf

```html
<button th:onclick="|location.href='@{/basic/items/add}'|" type="button">
  등록
</button>
```

- th:href="@{/basic/items/{itemId}(itemId=${item.id})}"

- th:href="@{/basic/items/{itemId}(itemId=${item.id},
  query='test')}"

  - http://localhost:8080/basic/items/1?query=test

- th:href="@{|/basic/items/${item.id}|}"

## for

- JSP

```html
<c:forEach items="${items}" var="item">
  <tr>
    <td>
      <a href="/basic/items/" + ${itemId}> <c:out value="${item.id}" /></a>
    </td>
    <td><a href="/basic/items/" + ${itemId}"> <c:out value="${item.itemName}" /></a></td>
    <td>${item.price}</td>
    <td>${item.quantity}</td>
  </tr>
</c:forEach>
```

- Thymeleaf

```html
<tr th:each="item : ${items}">
  <td>
    <a
      th:href="@{/basic/items/{itemId} (itemId=${item.id})}"
      th:text="${item.id}"
      >회원id</a
    >
  </td>
  <td>
    <a th:href="@{|/basic/items/${item.id}|}" th:text="${item.itemName}"
      >상품명</a
    >
  </td>
  <td th:text="${item.price}">10000</td>
  <td th:text="${item.quantity}">10</td>
</tr>
```

## input value

- JSP

```html
<input
  type="text"
  id="itemId"
  name="itemId"
  class="form-control"
  value="1"
  readonly
/>
```

- Thymeleaf

```html
<input
  type="text"
  id="itemId"
  name="itemId"
  class="form-control"
  value="1"
  th:value="${item.id}"
  readonly
/>
```

## ing

- JSP

```html

```

- Thymeleaf

```html

```
