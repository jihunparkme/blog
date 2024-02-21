# Content type 'application/octet-stream' not supported for bodyType

## Intro

`WebClient`를 사용하여 아래와 같은 방식으로 API 통신을 하던 중 마주한 예외를 탐구해 보게 되었습니다.
<br/>

```java
Response response = webClient.mutate()
    .baseUrl(baseUrl).build()
    .post().uri(uri)
    .bodyValue(requestBody)
    .headers(httpHeaders -> httpHeaders.setAll(headers))
    .retrieve()
    .onStatus(HttpStatus::isError, res -> {
        return Mono.error(new ResponseStatusException(res.statusCode()));
    })
    .bodyToMono(Response.class)
    .block();
```

`.bodyToMono()` 메서드에서 응답 결과를 타겟 클래스(Response.java)로 디코딩하는 과정에서 아래와 같은 예외를 마주하였습니다.
<br/>

```shell
org.springframework.web.reactive.function.UnsupportedMediaTypeException: Content type 'application/octet-stream' not supported for bodyType={elementClass}
```

참고로, `application/octet-stream`은 파일을 전송할 때 사용하는 MIME(Multipurpose Internet Mail Extensions) 타입 중 하나입니다.
- 특정 파일이 어떤 종류인지 정확히 지정하지 않고, 8비트로 구성된 바이너리 데이터의 스트림을 나타냅니다.
- 주로 알려지지 않은 파일 형식을 다룰 때 사용되며, 이메일 첨부 파일이나 파일 다운로드 등에서 볼 수 있습니다.

## Attempt

서칭을 통해 관련 예외를 확인해 본 결과 클라이언트 입장에서 시도해 볼 수 있는 해결책은 단 한 가지였습니다.

**'Content-Type' 헤더 설정하기**
- 'Content-Type'이 `application/octet-stream`로 설정되어 있다면 `application/json`으로 변경하기

다만.. 이미 미디어 타입은 'application/json'로 적용 중이었습니다.🤔
(그 외에는 서버 측 해결책으로 컨트롤러에서 MediaType 설정, 예외 핸들링.. 등의 내용뿐이었다.)

...
<br/>

그러던 중.. Postman을 통해 테스트해 본 결과 요청 헤더에 `'Content-Type':'application/json'` 설정이 되어있음에도 응답 헤더에 `Content-Type` 속성이 없다는 것을 알게 되었습니다.
<br/>

그렇다면.. 응답 헤더에 `Content-Type` 속성이 비어있을 경우 디폴트로 `application/octet-stream` 로 세팅되도록 어디선가 적용하고 있을 것이라고 생각하였습니다.

## Cause

spring-webflux: 5.3.18


### WebClient

먼저 오늘의 주인공인 `WebClient` 인터페이스의 bodyToMono() 메서드를 따라가 보겠습니다.
<br/>

```java
<T> Mono<T> bodyToMono(Class<T> elementClass);
```

구현체로 이동해 보면 `WebClient`를 구현하고 있는 `DefaultWebClient`를 만날 수 있답니다.

### DefaultWebClient

```java
@Override
public <T> Mono<T> bodyToMono(ParameterizedTypeReference<T> elementTypeRef) {
    Assert.notNull(elementTypeRef, "ParameterizedTypeReference must not be null");
    return this.responseMono.flatMap(response ->
            handleBodyMono(response, response.bodyToMono(elementTypeRef)));
}
```

여기서 this.responseMono 필드는 `Mono<ClientResponse>` 타입이고, `ClientResponse`는 인터페이스입니다.
<br/>

ClientResponse 인터페이스의 구현체는 `ClientResponseWrapper`, `DefaultClientResponse`가 있는데 별다른 설정이 없으면 `DefaultClientResponse`로 적용됩니다.
<br/>

그렇다면 DefaultClientResponse.bodyToMono() 메서드로 이동해 보겠습니다.


### DefaultClientResponse

```java
@Override
public <T> Mono<T> bodyToMono(ParameterizedTypeReference<T> elementTypeRef) {
    return body(BodyExtractors.toMono(elementTypeRef));
}
```

`DefaultClientResponse`의 bodyToMono() 메서드는 `BodyExtractors` 클래스의 toMono() 메서드를 호출하여 Responsebody를 Mono로 변환하고 있는 것을 볼 수 있습니다.

BodyExtractors.toMono() 메서드를 계속 따라가 보겠습니다.

### BodyExtractors

```java
public static <T> BodyExtractor<Mono<T>, ReactiveHttpInputMessage> toMono(Class<? extends T> elementClass) {
    return toMono(ResolvableType.forClass(elementClass));
}

...

private static <T> BodyExtractor<Mono<T>, ReactiveHttpInputMessage> toMono(ResolvableType elementType) {
    return (inputMessage, context) ->
            readWithMessageReaders(inputMessage, context, elementType,
                    (HttpMessageReader<T> reader) -> readToMono(inputMessage, context, elementType, reader),
                    ex -> Mono.from(unsupportedErrorHandler(inputMessage, ex)),
                    skipBodyAsMono(inputMessage));
}
```

`BodyExtractors`의 toMono() 메서드를 계속 따라가 보면 readWithMessageReaders() 메서드를 만날 수 있습니다.

```java
private static <T, S extends Publisher<T>> S readWithMessageReaders(
        ReactiveHttpInputMessage message, BodyExtractor.Context context, ResolvableType elementType,
        Function<HttpMessageReader<T>, S> readerFunction,
        Function<UnsupportedMediaTypeException, S> errorFunction,
        Supplier<S> emptySupplier) {

    ...

    MediaType contentType = Optional.ofNullable(message.getHeaders().getContentType())
            .orElse(MediaType.APPLICATION_OCTET_STREAM);

    ...
}
```

readWithMessageReaders() 메서드에서 contentType 값을 설정해 주는 부분을 보면 `HttpMessage`의 헤더 값에 `Content-Type`이 없다면 `MediaType.APPLICATION_OCTET_STREAM`으로 세팅하고 있는 것을 볼 수 있습니다.

이어서 return 쪽도 살펴보겠습니다.

```java
...

return context.messageReaders().stream()
        .filter(reader -> reader.canRead(elementType, contentType))
        .findFirst()
        .map(BodyExtractors::<T>cast)
        .map(readerFunction)
        .orElseGet(() -> {
            List<MediaType> mediaTypes = context.messageReaders().stream()
                    .flatMap(reader -> reader.getReadableMediaTypes(elementType).stream())
                    .collect(Collectors.toList());
            return errorFunction.apply(
                    new UnsupportedMediaTypeException(contentType, mediaTypes, elementType));
        });
```

`messageReaders`에서 `contentType`(application/octet-stream)으로 bodyToMono() 메서드로 전달된 `elementType`(Response.class)을 읽을 수 있는 `HttpMessageReader`를 찾고, readToMono 메서드를 호출(readerFunction)하고 있습니다.

여기서, 전달된 `elementType`(Response.class)는 `application/json` 으로만 읽을 수 있으므로 적합한 `HttpMessageReader`를 찾지 못하고 orElseGet() 메서드로 빠지게 됩니다.

그렇게 `UnsupportedMediaTypeException`을 전달하게 되면서 아래 로그를 마주할 수 있었습니다.

```shell
org.springframework.web.reactive.function.UnsupportedMediaTypeException: Content type 'application/octet-stream' not supported for bodyType={elementClass}
```

## Solution

API 요청이 실패하거나 응답을 받지 못하는 상황이 아니라 `application/octet-stream` 타입으로 `Response.class`를 읽을 수 있는 `HttpMessageReader`가 없어서 발생하는 현상으로 판단을 하게 되었고,<br/>
단순하게 `Response.class` 대신 `String.class` 를 `application/octet-stream` 타입으로 읽고, Gson.fromJson() 메서드를 이용해서 jsonString을 Object로 변환해 주는 방법을 적용하게 되었습니다.

```java
String response = webClient.mutate()
    .baseUrl(baseUrl).build()
    .post().uri(uri)
    .bodyValue(requestBody)
    .headers(httpHeaders -> httpHeaders.setAll(headers))
    .retrieve()
    .onStatus(HttpStatus::isError, res -> {
        return Mono.error(new ResponseStatusException(res.statusCode()));
    })
    .bodyToMono(String.class) // Response.class -> String.class
    .block();

// JsonString to Object
Response responseObject = new Gson().fromJson(response, Response.class);
return responseObject;
```

---

**Reference**

<https://zorba91.tistory.com/350>