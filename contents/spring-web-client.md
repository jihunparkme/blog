# Spring WebClient

Spring WebClient 먼저 알아 보고, 

WebClient vs. RestTemplate 를 간략하게 비교해 보자.

.

## Spring 5 WebClient

[Baeldung - Spring 5 WebClient](https://www.baeldung.com/spring-5-webclient) 내용 정리 ✏️

.

`Web Client`: 웹 요청을 수행하기 위한 주요 진입점을 나타내는 인터페이스

- Spring Web Reactive 모듈의 일부로 개발되었으며, 기존의 RestTemplate 대체
- HTTP/1.1 프로토콜에서 동작하는 반응형 `non-blocking solution`
- non-blocking 클라이언트이고 spring-webflux 라이브러리에 속하지만 `동기/비동기` 작업을 모두 지원

.

**`Dependencies`**

[spring-boot-starter-webflux](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-webflux/overview)

```groovy
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

.

## Working with the WebClient

### Creating a WebClient

**`Default WebClient Instance`**

1) `기본 설정`의 Web Client Instance 생성하기

```java
WebClient client = WebClient.create();
```

2) `기본 URI`로 Web Client Instance 생성하기

```java
WebClient client = WebClient.create("http://localhost:8080");
```

3) `DefaultWebClientBuilder` 클래스를 사용하여 Web Client Instance 생성하기

```java
WebClient client = WebClient.builder()
                        .baseUrl("http://localhost:8080")
                        .defaultCookie("cookieKey", "cookieValue")
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) 
                        .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
                        .build();
```

.

**`WebClient Instance with Timeouts`**

default HTTP timeouts: 30 sec.

HttpClient 인스턴스를 생성하고 WebClient가 이를 사용하도록 구성
- **ChannelOption.CONNECT_TIMEOUT_MILLIS** 옵션을 통한 HTTP 연결 시간 제한 설정
- **ReadTimeoutHandler**, **WriteTimeoutHandler**를 각각 사용하여 읽기/쓰기 타임아웃 설정
- **responseTimeout** 지시를 사용하여 응답 시간 초과 구성

```java
HttpClient httpClient = HttpClient.create()
                          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                          .responseTimeout(Duration.ofMillis(5000))
                          .doOnConnected(conn -> 
                            conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                              .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

WebClient client = WebClient.builder()
                      .clientConnector(new ReactorClientHttpConnector(httpClient))
                      .build();
```

여기서 타임아웃은 HTTP 연결, 읽기/쓰기 또는 응답 타임아웃이 아닌 Mono/Flux Publisher의 신호 타임아웃

.

### Preparing a Request

**Define the `Method`**

```java
// 1) 요청 HttpMethod 정의하기
UriSpec<RequestBodySpec> uriSpec = client.method(HttpMethod.POST);

// 2) shortcut methods 호출하기
UriSpec<RequestBodySpec> uriSpec = client.post();
```

.

**Define the `URL`**

```java
// 1) uri API 설정하기
RequestBodySpec bodySpec = uriSpec.uri("/resource");

// 2) UriBuilder 활용하기
RequestBodySpec bodySpec = uriSpec.uri(
                              uriBuilder -> uriBuilder.pathSegment("/resource").build());

// 3) java.net.URL instance 활용하기
RequestBodySpec bodySpec = uriSpec.uri(URI.create("/resource"));
```

.

**Define the `Body`**

```java
// 1) bodyValue를 활용하기
RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue("data");

// 2) body method에 Publisher 표시하기
RequestHeadersSpec<?> headersSpec = bodySpec.body(
                                      Mono.just(new Foo("name")), Foo.class);

// 3) BodyInserters utility 클래스 활용하기
RequestHeadersSpec<?> headersSpec = bodySpec.body(
                                      BodyInserters.fromValue("data"));

// 4) reactor 인스턴스를 사용하는 경우 BodyInserters#fromPublisher 활용하기
RequestHeadersSpec headersSpec = bodySpec.body(
                                      BodyInserters.fromPublisher(Mono.just("data")),
                                      String.class);


// 5) multipart requests 요청이 필요한 경우
LinkedMultiValueMap map = new LinkedMultiValueMap();
map.add("key1", "value1");
map.add("key2", "value2");
RequestHeadersSpec<?> headersSpec = bodySpec.body(
                                      BodyInserters.fromMultipartData(map));
```

.

**Define the `Headers`**

클라이언트를 인스턴스화할 때 이미 실정된 값에 추가

```java
ResponseSpec responseSpec = headersSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                              .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                              .acceptCharset(StandardCharsets.UTF_8)
                              .ifNoneMatch("*")
                              .ifModifiedSince(ZonedDateTime.now())
                              .retrieve();
```

.

### Getting a Response

`exchangeToMono`, `exchangeToFlux`, `retrieve method` 활용

exchangeToMono, exchangeToFlux 메소드 사용 시 클라이언트 응답의 상태 및 헤더와 함께 액세스 가능

```java
Mono<String> response = headersSpec.exchangeToMono(response -> {
                          if (response.statusCode().equals(HttpStatus.OK)) {
                              return response.bodyToMono(String.class);
                          } else if (response.statusCode().is4xxClientError()) {
                              return Mono.just("Error response");
                          } else {
                              return response.createException()
                                .flatMap(Mono::error);
                          }
                        });
```

retrieve 메소드 사용 시 간결한 작성 가능
- 상태 코드가 4xx, 5xx인 경우 WebClientException을 발생시키는 bodyToMono 주의

```java
Mono<String> response = headersSpec.retrieve()
                            .bodyToMono(String.class);
```

## TEST with the WebTestClient

`WebTestClient`는 WebFlux 서버 엔드포인트 테스트를 위한 주요 진입점
- WebClient와 아주 유사한 API를 가짐
- 대부분의 작업을 테스트 컨텍스트를 제공하는 데 중점을 두고 내부 WebClient 인스턴스에 위임
- DefaultWebTestClient 클래스는 단일 인터페이스 구현체
- 테스트를 위한 클라이언트는 실제 서버에 바인딩되거나 특정 컨트롤러 또는 기능과 함께 작동 가능

.

## Binding to

**`Server`**

- 실행 중인 서버에 대한 실제 요청과 통합 테스트를 위해 `bindToServer` 활용

```java
WebTestClient testClient = WebTestClient
                                .bindToServer()
                                .baseUrl("http://localhost:8080")
                                .build();
```

.

**`Router`**

- 특정 라우터 기능을 `bindToRouterFunction` 메서드에 전달하여 테스트 가능

```java
RouterFunction function = RouterFunctions.route(
  RequestPredicates.GET("/resource"),
  request -> ServerResponse.ok().build()
);

WebTestClient
  .bindToRouterFunction(function)
  .build().get().uri("/resource")
  .exchange()
  .expectStatus().isOk()
  .expectBody().isEmpty();

```

.

**`Web Handler`**

- WebHandler 인스턴스를 사용하는 `bindToWebHandler` 메서드에서도 동일한 동작 수행 가능

```java
WebHandler handler = exchange -> Mono.empty();
WebTestClient.bindToWebHandler(handler).build();
```

.

**`Application Context`**

- `bindToApplicationContext` 메소드를 사용할 때 흥미로운 상황이 발생
  - ApplicationContext를 사용하고 컨트롤러 빈 및 @EnableWebFlux 구성에 대한 컨텍스트를 분석

```java
@Autowired
private ApplicationContext context;

WebTestClient testClient = WebTestClient
                              .bindToApplicationContext(context)
                              .build();
```

.

**`Controller`**

`bindToController` 메소드를 활용하여 더 간결한 방식으로 테스트
- 테스트하고자 하는 컨트롤러 배열 제공하기

```java
@Autowired
private Controller controller;

WebTestClient testClient = WebTestClient
                            .bindToController(controller)
                            .build();
```

.

## Making a Request

WebTestClient 개체 생성 체인의 모든 작업이 WebClient와 유사

```java
WebTestClient
  .bindToServer()
    .baseUrl("http://localhost:8080")
    .build()
    .post()
    .uri("/resource")
  .exchange()
    .expectStatus().isCreated()
    .expectHeader().valueEquals("Content-Type", "application/json")
    .expectBody().jsonPath("field").isEqualTo("value");
```

.

> [Spring 5 WebClient](https://www.baeldung.com/spring-5-webclient)

.

# Spring WebClient vs. RestTemplate

[Baeldung - Spring WebClient vs. RestTemplate](https://www.baeldung.com/spring-webclient-resttemplate) 내용 정리 ✏️

.

Spring 웹 클라이언트 구현 중 두 가지인

[Spring WebClient](https://www.baeldung.com/spring-5-webclient), [RestTemplate](https://www.baeldung.com/rest-template)

를 비교해 보자.

.

## Blocking vs Non-Blocking Client

web applications에서 다른 서비스에 HTTP 요청을 하기 위해 web client tool이 필요하다.

.

**`RestTemplate Blocking Client`**

- 스프링은 오랜시간 RestTemplate을 web client 추상화를 제공
- thread-per-request model에 기반한 `Java Servlet API` 사용
  - web client가 응답을 받을 때까지 스레드가 차단(`동기식`)
  - Blocking 코드의 문제는 각각의 스레드가 어느 정도의 메모리 및 CPU 사이클을 소모(`Blocking Client`)
- 처리가 오래 걸리는 서비스에 많은 요청이 들어올 경우 기다리는 요청이 쌓이게 되면서, 스레드 풀이 소진되거나 사용 가능한 메모리를 모두 차지하게 될 수 있음
  - CPU 컨텍스트(스레드) 전환이 잦아 성능 저하 발생 가능

.

**`WebClient Non-Blocking Client`**

- WebClient는 Spring Reactive Framework에서 제공하는 `비동기`식 `Non-Blocking` 솔루션 사용
- RestTemplate가 각 호출에 대한 caller 스레드를 사용하는 반면
  - WebClient는 각 호출에 대한 "tasks" 생성 후, 그 뒤에서 Reactive Framework는 이러한 "tasks"를 대기열에 올려놓고 적절한 응답이 있을 때만 실행
- Reactive Framework는 `이벤트 기반 아키텍처`를 사용
  - Reactive Streams API를 통해 `비동기` 로직을 구성할 수 있는 수단 제공
  - Reactive 접근 방식은 동기/차단 방식에 비해 더 적은 스레드와 시스템 리소스를 사용하면서 더 많은 로직 처리 가능
- WebClient는 `Spring WebFlux` 라이브러리의 일부
  - 반응형(Mono, Flux)을 가진 functional, fluent API 를 사용하여 클라이언트 코드를 함수형으로 작성 가능

.

**`Conclusion`**

**RestTemplate**
- Spring 3.0 부터 지원
- RESTful 형식을 지원
- 멀티 스레드 방식
- Blocking I/O기반의 동기 방식 API(비동기 방식도 지원)
  - Java Servlet API 사용으로 블로킹, 동기 특성
  - 응답이 돌아오는 동안 스레드가 계속 사용
- Spring 4.0에서 비동기 문제를 해결하고자 AsyncRestTemplate이 등장했으나, 현재 deprecated 됨
- 경우에 따라 Non-Blocking 방식이 Blocking 방식에 비해 훨씬 적은 시스템 리소스를 사용하기 때문에 이 경우 WebClient가 적합

```java
@GetMapping("/blocking")
public List<Tweet> getBlocking() {
    log.info("Starting BLOCKING Controller!");
    final String uri = getSlowServiceUri();

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<List<Tweet>> response = restTemplate.exchange(
                        uri, 
                        HttpMethod.GET, 
                        null,
                        new ParameterizedTypeReference<List<Tweet>>(){});

    List<Tweet> result = response.getBody();
    result.forEach(tweet -> log.info(tweet.toString()));
    log.info("Exiting BLOCKING Controller!");
    return result;
}

...

// Starting BLOCKING Controller!
// ---> doSometing
// Exiting BLOCKING Controller!
```

**WebClient**
- Spring 5.0 부터 지원
- 싱글 스레드 방식
- Non-Blocking I/O기반의 비동기 방식 API
  - 응답이 돌아올 때까지 기다리는 동안 실행 중인 스레드를 블로킹하지 않고, 응답이 준비될 때에만 알림이 생성
- Reactor 기반의 Functional API (Mono, Flux)

```java
@GetMapping(value = "/non-blocking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Tweet> getNonBlocking() {
    log.info("Starting NON-BLOCKING Controller!");
    Flux<Tweet> tweetFlux = WebClient.create()
      .get()
      .uri(getSlowServiceUri())
      .retrieve()
      .bodyToFlux(Tweet.class);

    tweetFlux.subscribe(tweet -> log.info(tweet.toString()));
    log.info("Exiting NON-BLOCKING Controller!");
    return tweetFlux;
}

...

// Starting NON-BLOCKING Controller!
// Exiting NON-BLOCKING Controller!
// ---> doSometing
```

.

> [Spring WebClient vs. RestTemplate](https://www.baeldung.com/spring-webclient-resttemplate)
>
> [WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
>
> [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
>
> [The Guide to RestTemplate](https://www.baeldung.com/rest-template)# Spring WebClient

Spring WebClient 먼저 알아 보고, 

WebClient vs. RestTemplate 를 간략하게 비교해 보자.

.

## Spring 5 WebClient

[Baeldung - Spring 5 WebClient](https://www.baeldung.com/spring-5-webclient) 내용 정리 ✏️

.

`Web Client`: 웹 요청을 수행하기 위한 주요 진입점을 나타내는 인터페이스

- Spring Web Reactive 모듈의 일부로 개발되었으며, 기존의 RestTemplate 대체
- HTTP/1.1 프로토콜에서 동작하는 반응형 `non-blocking solution`
- non-blocking 클라이언트이고 spring-webflux 라이브러리에 속하지만 `동기/비동기` 작업을 모두 지원

.

**`Dependencies`**

[spring-boot-starter-webflux](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-webflux/overview)

```groovy
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

.

## Working with the WebClient

### Creating a WebClient

**`Default WebClient Instance`**

1) `기본 설정`의 Web Client Instance 생성하기

```java
WebClient client = WebClient.create();
```

2) `기본 URI`로 Web Client Instance 생성하기

```java
WebClient client = WebClient.create("http://localhost:8080");
```

3) `DefaultWebClientBuilder` 클래스를 사용하여 Web Client Instance 생성하기

```java
WebClient client = WebClient.builder()
                        .baseUrl("http://localhost:8080")
                        .defaultCookie("cookieKey", "cookieValue")
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) 
                        .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
                        .build();
```

.

**`WebClient Instance with Timeouts`**

default HTTP timeouts: 30 sec.

HttpClient 인스턴스를 생성하고 WebClient가 이를 사용하도록 구성
- **ChannelOption.CONNECT_TIMEOUT_MILLIS** 옵션을 통한 HTTP 연결 시간 제한 설정
- **ReadTimeoutHandler**, **WriteTimeoutHandler**를 각각 사용하여 읽기/쓰기 타임아웃 설정
- **responseTimeout** 지시를 사용하여 응답 시간 초과 구성

```java
HttpClient httpClient = HttpClient.create()
                          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                          .responseTimeout(Duration.ofMillis(5000))
                          .doOnConnected(conn -> 
                            conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                              .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

WebClient client = WebClient.builder()
                      .clientConnector(new ReactorClientHttpConnector(httpClient))
                      .build();
```

여기서 타임아웃은 HTTP 연결, 읽기/쓰기 또는 응답 타임아웃이 아닌 Mono/Flux Publisher의 신호 타임아웃

.

### Preparing a Request

**Define the `Method`**

```java
// 1) 요청 HttpMethod 정의하기
UriSpec<RequestBodySpec> uriSpec = client.method(HttpMethod.POST);

// 2) shortcut methods 호출하기
UriSpec<RequestBodySpec> uriSpec = client.post();
```

.

**Define the `URL`**

```java
// 1) uri API 설정하기
RequestBodySpec bodySpec = uriSpec.uri("/resource");

// 2) UriBuilder 활용하기
RequestBodySpec bodySpec = uriSpec.uri(
                              uriBuilder -> uriBuilder.pathSegment("/resource").build());

// 3) java.net.URL instance 활용하기
RequestBodySpec bodySpec = uriSpec.uri(URI.create("/resource"));
```

.

**Define the `Body`**

```java
// 1) bodyValue를 활용하기
RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue("data");

// 2) body method에 Publisher 표시하기
RequestHeadersSpec<?> headersSpec = bodySpec.body(
                                      Mono.just(new Foo("name")), Foo.class);

// 3) BodyInserters utility 클래스 활용하기
RequestHeadersSpec<?> headersSpec = bodySpec.body(
                                      BodyInserters.fromValue("data"));

// 4) reactor 인스턴스를 사용하는 경우 BodyInserters#fromPublisher 활용하기
RequestHeadersSpec headersSpec = bodySpec.body(
                                      BodyInserters.fromPublisher(Mono.just("data")),
                                      String.class);


// 5) multipart requests 요청이 필요한 경우
LinkedMultiValueMap map = new LinkedMultiValueMap();
map.add("key1", "value1");
map.add("key2", "value2");
RequestHeadersSpec<?> headersSpec = bodySpec.body(
                                      BodyInserters.fromMultipartData(map));
```

.

**Define the `Headers`**

클라이언트를 인스턴스화할 때 이미 실정된 값에 추가

```java
ResponseSpec responseSpec = headersSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                              .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                              .acceptCharset(StandardCharsets.UTF_8)
                              .ifNoneMatch("*")
                              .ifModifiedSince(ZonedDateTime.now())
                              .retrieve();
```

.

### Getting a Response

`exchangeToMono`, `exchangeToFlux`, `retrieve method` 활용

exchangeToMono, exchangeToFlux 메소드 사용 시 클라이언트 응답의 상태 및 헤더와 함께 액세스 가능

```java
Mono<String> response = headersSpec.exchangeToMono(response -> {
                          if (response.statusCode().equals(HttpStatus.OK)) {
                              return response.bodyToMono(String.class);
                          } else if (response.statusCode().is4xxClientError()) {
                              return Mono.just("Error response");
                          } else {
                              return response.createException()
                                .flatMap(Mono::error);
                          }
                        });
```

retrieve 메소드 사용 시 간결한 작성 가능
- 상태 코드가 4xx, 5xx인 경우 WebClientException을 발생시키는 bodyToMono 주의

```java
Mono<String> response = headersSpec.retrieve()
                            .bodyToMono(String.class);
```

## TEST with the WebTestClient

`WebTestClient`는 WebFlux 서버 엔드포인트 테스트를 위한 주요 진입점
- WebClient와 아주 유사한 API를 가짐
- 대부분의 작업을 테스트 컨텍스트를 제공하는 데 중점을 두고 내부 WebClient 인스턴스에 위임
- DefaultWebTestClient 클래스는 단일 인터페이스 구현체
- 테스트를 위한 클라이언트는 실제 서버에 바인딩되거나 특정 컨트롤러 또는 기능과 함께 작동 가능

.

## Binding to

**`Server`**

- 실행 중인 서버에 대한 실제 요청과 통합 테스트를 위해 `bindToServer` 활용

```java
WebTestClient testClient = WebTestClient
                                .bindToServer()
                                .baseUrl("http://localhost:8080")
                                .build();
```

.

**`Router`**

- 특정 라우터 기능을 `bindToRouterFunction` 메서드에 전달하여 테스트 가능

```java
RouterFunction function = RouterFunctions.route(
  RequestPredicates.GET("/resource"),
  request -> ServerResponse.ok().build()
);

WebTestClient
  .bindToRouterFunction(function)
  .build().get().uri("/resource")
  .exchange()
  .expectStatus().isOk()
  .expectBody().isEmpty();

```

.

**`Web Handler`**

- WebHandler 인스턴스를 사용하는 `bindToWebHandler` 메서드에서도 동일한 동작 수행 가능

```java
WebHandler handler = exchange -> Mono.empty();
WebTestClient.bindToWebHandler(handler).build();
```

.

**`Application Context`**

- `bindToApplicationContext` 메소드를 사용할 때 흥미로운 상황이 발생
  - ApplicationContext를 사용하고 컨트롤러 빈 및 @EnableWebFlux 구성에 대한 컨텍스트를 분석

```java
@Autowired
private ApplicationContext context;

WebTestClient testClient = WebTestClient
                              .bindToApplicationContext(context)
                              .build();
```

.

**`Controller`**

`bindToController` 메소드를 활용하여 더 간결한 방식으로 테스트
- 테스트하고자 하는 컨트롤러 배열 제공하기

```java
@Autowired
private Controller controller;

WebTestClient testClient = WebTestClient
                            .bindToController(controller)
                            .build();
```

.

## Making a Request

WebTestClient 개체 생성 체인의 모든 작업이 WebClient와 유사

```java
WebTestClient
  .bindToServer()
    .baseUrl("http://localhost:8080")
    .build()
    .post()
    .uri("/resource")
  .exchange()
    .expectStatus().isCreated()
    .expectHeader().valueEquals("Content-Type", "application/json")
    .expectBody().jsonPath("field").isEqualTo("value");
```

.

> [Spring 5 WebClient](https://www.baeldung.com/spring-5-webclient)

.

# Spring WebClient vs. RestTemplate

[Baeldung - Spring WebClient vs. RestTemplate](https://www.baeldung.com/spring-webclient-resttemplate) 내용 정리 ✏️

.

Spring 웹 클라이언트 구현 중 두 가지인

[Spring WebClient](https://www.baeldung.com/spring-5-webclient), [RestTemplate](https://www.baeldung.com/rest-template)

를 비교해 보자.

.

## Blocking vs Non-Blocking Client

web applications에서 다른 서비스에 HTTP 요청을 하기 위해 web client tool이 필요하다.

.

**`RestTemplate Blocking Client`**

- 스프링은 오랜시간 RestTemplate을 web client 추상화를 제공
- thread-per-request model에 기반한 `Java Servlet API` 사용
  - web client가 응답을 받을 때까지 스레드가 차단(`동기식`)
  - Blocking 코드의 문제는 각각의 스레드가 어느 정도의 메모리 및 CPU 사이클을 소모(`Blocking Client`)
- 처리가 오래 걸리는 서비스에 많은 요청이 들어올 경우 기다리는 요청이 쌓이게 되면서, 스레드 풀이 소진되거나 사용 가능한 메모리를 모두 차지하게 될 수 있음
  - CPU 컨텍스트(스레드) 전환이 잦아 성능 저하 발생 가능

.

**`WebClient Non-Blocking Client`**

- WebClient는 Spring Reactive Framework에서 제공하는 `비동기`식 `Non-Blocking` 솔루션 사용
- RestTemplate가 각 호출에 대한 caller 스레드를 사용하는 반면
  - WebClient는 각 호출에 대한 "tasks" 생성 후, 그 뒤에서 Reactive Framework는 이러한 "tasks"를 대기열에 올려놓고 적절한 응답이 있을 때만 실행
- Reactive Framework는 `이벤트 기반 아키텍처`를 사용
  - Reactive Streams API를 통해 `비동기` 로직을 구성할 수 있는 수단 제공
  - Reactive 접근 방식은 동기/차단 방식에 비해 더 적은 스레드와 시스템 리소스를 사용하면서 더 많은 로직 처리 가능
- WebClient는 `Spring WebFlux` 라이브러리의 일부
  - 반응형(Mono, Flux)을 가진 functional, fluent API 를 사용하여 클라이언트 코드를 함수형으로 작성 가능

.

**`Conclusion`**

**RestTemplate**
- Spring 3.0 부터 지원
- RESTful 형식을 지원
- 멀티 스레드 방식
- Blocking I/O기반의 동기 방식 API(비동기 방식도 지원)
  - Java Servlet API 사용으로 블로킹, 동기 특성
  - 응답이 돌아오는 동안 스레드가 계속 사용
- Spring 4.0에서 비동기 문제를 해결하고자 AsyncRestTemplate이 등장했으나, 현재 deprecated 됨
- 경우에 따라 Non-Blocking 방식이 Blocking 방식에 비해 훨씬 적은 시스템 리소스를 사용하기 때문에 이 경우 WebClient가 적합

```java
@GetMapping("/blocking")
public List<Tweet> getBlocking() {
    log.info("Starting BLOCKING Controller!");
    final String uri = getSlowServiceUri();

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<List<Tweet>> response = restTemplate.exchange(
                        uri, 
                        HttpMethod.GET, 
                        null,
                        new ParameterizedTypeReference<List<Tweet>>(){});

    List<Tweet> result = response.getBody();
    result.forEach(tweet -> log.info(tweet.toString()));
    log.info("Exiting BLOCKING Controller!");
    return result;
}

...

// Starting BLOCKING Controller!
// ---> doSometing
// Exiting BLOCKING Controller!
```

**WebClient**
- Spring 5.0 부터 지원
- 싱글 스레드 방식
- Non-Blocking I/O기반의 비동기 방식 API
  - 응답이 돌아올 때까지 기다리는 동안 실행 중인 스레드를 블로킹하지 않고, 응답이 준비될 때에만 알림이 생성
- Reactor 기반의 Functional API (Mono, Flux)

```java
@GetMapping(value = "/non-blocking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Tweet> getNonBlocking() {
    log.info("Starting NON-BLOCKING Controller!");
    Flux<Tweet> tweetFlux = WebClient.create()
      .get()
      .uri(getSlowServiceUri())
      .retrieve()
      .bodyToFlux(Tweet.class);

    tweetFlux.subscribe(tweet -> log.info(tweet.toString()));
    log.info("Exiting NON-BLOCKING Controller!");
    return tweetFlux;
}

...

// Starting NON-BLOCKING Controller!
// Exiting NON-BLOCKING Controller!
// ---> doSometing
```

.

> [Spring WebClient vs. RestTemplate](https://www.baeldung.com/spring-webclient-resttemplate)
>
> [WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
>
> [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
>
> [The Guide to RestTemplate](https://www.baeldung.com/rest-template)