# jQuery Selectric⚡

자주 사용하는 jQuery Selectric 코드 정리.

a.k.a. 자주 사용하는 = 매번 찾기 귀찮은(?)

**Reference**

-   [https://selectric.js.org/](https://selectric.js.org/){:target="\_blank"}
-   [http://selectric.js.org/demo.html](http://selectric.js.org/demo.html){:target="\_blank"}

## Include

```html
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- ... -->
<link rel="stylesheet" type="text/css" href="<c:url value='/css/selectric.css'/>" />
<script src="<c:url value='/js/plugin/jquery.selectric.min.js'/>"></script>
```

## Initialize

```javascript
$(function() {
  $('select').selectric();
});
```

### 동적 생성

-   HTML

```html
<select id="selectricEx" name="selectricEx">
    <option value="">- 선택 -</option>
</select>
```

-   JavaScript

```javascript
$.each(result.list, function(item) {
    var listItem = result.list[item];
    $('#selectricEx').append('<option value="' + listItem.value + '">' + listItem.title + '</option>');
});
```

## Get selected option value

```javascript
// get selected option value
$("#selectricEx").val();

// get change value
$('#selectricEx').selectric().on('change', function() {
    console.log($(this).val());
});

// get before value and change value
$("#selectricEx").on("selectric-before-open", function() {
    console.log(this.value); // selectric을 열기 전 value
}).on("change", function() {
    console.log(this.value); // selectric을 열고 변경 후 value
});
```

## Set value

```javascript
// init option value
$('#selectricEx').prop('selectedIndex', 0).selectric('refresh');

// value에 해당하는 option으로 setting
$('#selectricEx').val(result.value).selectric('refresh');
```