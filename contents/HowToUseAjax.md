# Ajax

```javascript
$.ajax({
	url : "/save.do",
    // settings
    async : false,
    type : "POST",
	dataType : "json",
	contentType: "application/json",
	data : JSON.stringify(formObject()) 
})
.done(function() {
	alert("저장하였습니다.");
	location.href = "/list.do";
})
```

- **type** : default `get`
  
  - HTTP method
  
- **url** : 요청이 전송되는 URL

- **async** : default `true`
  - 기본적으로 모든 요청은 비동기적으로 전송
  - 동기 요청이 필요한 경우 `false`
  
- **dataType** : `xml`, `html`, `script`, `json`, `jsonp`, `text`

- **processData** : default `true`
  - data 옵션에 객체로 전달된 데이터는 기본 콘텐츠 유형에 맞는 쿼리 문자열로 처리 및 변환
  - DOMDocument 또는 기타 처리되지 않은 데이터를 보내려면 옵션을 `false`로 설정

- **contentType** : default: `application/x-www-form-urlencoded; charset=UTF-8`
  - 서버로 데이터를 보낼 때 content 유형
  - contentType을 명시적으로 ajax에 전달하면 데이터가 전송되지 않더라도 항상 서버로 전송

- **data** :
  - 서버로 보낼 데이터
  - GET 방식의 경우 URL 에 추가

- **context** : 
  - 호출에 사용된 Ajax 설정을 나타내는 객체

	```javascript
    $.ajax({
      url: "test.do",
      context: document.body
    }).done(function() {
      $( this ).addClass( "done" );
    });
	```

- **cache** : default `true`
  
  - `false`로 설정 시 요청된 페이지가 브라우저에 의해 캐시되지 않음
  
- **timeout** : 요청에 대한 제한 시간(ms)

- **username** : HTTP 엑세스 인증 요청에 대한 응답
  
  - XMLHttpRequest와 함께 사용할 사용자 이름
  
- **beforeSend** : 사전 요청 콜백 함수
  
- 사용자 정의 헤더 등을 설정 

<br>

- **jqXHR.done(function (result) { .. });** : 성공 콜백 옵션
- **jqXHR.fail(function (result) { .. });** : 오류 콜백 옵션
- **jqXHR.always(function (result) { .. });** : 
- **jqXHR.then(function (result) { .. });**

<br>

> reference : https://api.jquery.com/jQuery.ajax/

