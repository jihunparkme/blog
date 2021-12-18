# [HTTP Spring 통신 흐름 과정] HTTP Request 부터 HTTP Response 까지의 여정

요즘 여행 가는 것도 힘든데.. HTTP 타고 여행이나 가보자!

먼저 `HTTP` 는 `인터넷에서 데이터를 주고받을 수 있는 프로토콜`을 의미한다.

웹 브라우저에 URL 을 입력한 후 결과 페이지가 보이기까지 어떠한 코스들을 거치는지 구경해 보자.

자리가 얼마 남지 않았다는데.. 빨리 탑승해 보자 !!

참고로 목적지는 `@RequestMapping` 에 해당하는 `Controller Method` 이고, Service 와 Repository 등 응답 로직을 거쳐 요청 처리가 완료되면 복귀할 예정이다.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/http/1.png" width="90%"></center>

---

## URL 입력

우리가 갈 목적지의 주소는 `google.com` 이다.

웹 브라우저에 google.com 을 입력해 보자.

<img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/http/2.jpg">

---

## DNS

`google.com` 행 HTTP에 탑승하긴 했는데.. 정확히 목적지가 어디지?

`-73.990494, 40.7569545`.. 위도와 경도 정보로는 도저히 찾아갈 수가 없겠는데..?

- `DNS`(Domain Name System) 의 작동 원리를 살펴보기 전에 먼저 가까운 DNS Server를 통해 Hostname의 IP주소를 알아보자.

```shell
C:\>ping google.com

Ping google.com [172.217.161.78] 32바이트 데이터 사용:
172.217.161.78의 응답: 바이트=32 시간=34ms TTL=112
172.217.161.78의 응답: 바이트=32 시간=34ms TTL=112
172.217.161.78의 응답: 바이트=32 시간=34ms TTL=112
```

Google 의 실제 IP는 `172.217.161.78` 이다. (IP 주소로도 접속해 보자.)

- 이 IP 주소는 국적과 지역에 따라 다 다르게 나타날 것이다.

IP 주소(172.217.161.78)를 외우고 다니기는 힘들기 때문에 쉽게 기억할 수 있는 Domain Name을 사용하는 것이다.

> IP 를 "위도와 경도(-73.990494, 40.7569545)", Domain Name 을 "미국"으로 생각해 볼 수도 있다.

**DNS 작동 원리**

1. 웹 브라우저에 `google.com`을 입력
2. `Local DNS`에게 Hostname(google.com)에 대한 IP 주소 요청
3. Local DNS 에 IP 주소가 없다면 다른 DNS Name Server(Root DNS) 정보를 응답
4. `Root DNS` 서버에게 Hostname 에 대한 IP 주소를 요청
5. Root DNS 서버는 `.com` 도메인을 관리하는 TLD(Top-Level Domain) Name Serever 정보 응답
6. `TLD` 에게 Hostname 에 대한 IP 주소를 요청
7. TLD 는 Hostname 을 관리하는 DNS Server 정보 응답
8. google.com 도메인을 관리하는 `DNS Server`에게 Hostname에 대한 IP 주소를 요청
9. `Local DNS Server` 는 Hostname 에 대한 IP 주소(172.217.161.78) 응답
10. Local DNS Server 는 Hostname에 대한 IP 주소를 캐싱하고 IP 주소 정보로 HTTP 요청

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/http/3.png" width="100%"></center>

**Reference**

[Internet과 작동 원리 (HTTP, Browser, DNS, Domain, Hosting](https://data-make.tistory.com/665)

[DNS와 작동원리](https://velog.io/@goban/DNS%EC%99%80-%EC%9E%91%EB%8F%99%EC%9B%90%EB%A6%AC)

---

## HTTP 요청

정확한 목적지를 알아냈으니 이동해보자!

- `google.com`로 요청했지만, 실제로는 DNS 서버를 통해 알아낸 IP주소 `172.217.161.78` 와 입력한 URL 정보가 함께 요청으로 전달된다.
- URL 정보와 전달받은 IP 주소는 HTTP Protocol을 사용하여 `HTTP Request Message` 생성
- HTTP 요청 Message 는 TCP Protocol 을 사용하여 인터넷을 거쳐 해당 IP 주소의 컴퓨터 Web Server 로 전송
  - 컴퓨터 네트워크에서 데이터는 패킷이라는 작은 단위로 전달

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/http/4.png" width="80%"></center>

### Internet

- 인터넷은 내 컴퓨터로 시작해 Router, Modem, ISP1(Internet Service Provider), ... , ISP2, Modem, Router 를 거쳐 해당 IP 주소의 컴퓨터 Web Server 로 도착하게 된다.
  - [Router](https://ko.wikipedia.org/wiki/%EB%9D%BC%EC%9A%B0%ED%84%B0) : 컴퓨터 네트워크 간에 데이터 패킷을 전송하는 네트워크 장치
  - [Model](https://ko.wikipedia.org/wiki/%EB%AA%A8%EB%8E%80) : 디지털 정보 전달을 위해 신호를 변조하여 송신하고 수신측에서 원래의 신호로 복구하기 위해 복조하는 장치
  - [ISP](https://namu.wiki/w/ISP) : 개인이나 기업체에게 인터넷 접속 서비스, 웹사이트 구축 및 웹호스팅 서비스 등을 제공하는 인터넷 서비스 제공 업체
- Web Server 로 도착한 HTTP 요청 Message 는 HTTP Protocol을 사용하여 URL 정보로 변환

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/http/5.png" height="1000"></center>

**Reference**

[HTTP 개요](https://developer.mozilla.org/ko/docs/Web/HTTP/Overview)

[웹의 동작 원리](http://tcpschool.com/webbasic/works)

---

## Web Server

목적지에 들어가기 전 Server 에 도착했다.

Web Server 는 요청 URL 정보를 확인하고, 필요한 요청이 여기서 처리되면 돌아가도 된다. 여기서 처리할 수 없는 요청이라면 WAS 로 이동해야 한다. 이왕 온거 계속 가보자!

**동작**

- Web Server는 HTTP 요청을 받고 바로 컨텐츠를 응답하거나, WAS 에 요청을 전달
- WAS 에 요청이 전달되고, WAS에서 처리된 요청이 있다면 해당 컨텐츠를 응답

**기능**

- HTTP 요청이 들어오면 요청을 서비스하는 담당
  - 정적 컨텐츠
    - WAS를 거치지 않고 바로 컨텐츠 제공
  - 동적 컨텐츠
    - 동적 컨텐츠 제공을 위해 WAS 에 요청 전달

ex) Apache Server, Nginx, IIS 등

---

## WAS

Web Server 에서 처리할 수 없는 요청이 있어서 WAS 로 왔다. (사실은 여행을 더 하고 싶어서..)

여기서 목적지까지 이동하기 위해 요청에 맞는 doGet(), doPost() 라는 자동차 혹은 기차를 이용해야 한다.

**동작**

- Web Server 로부터 받은 요청과 관련된 Servlet 을 메모리에 로딩
- `web.xml` 을 참조하여 해당 Servlet 에 대한 Thread 생성 (Thread Pool 활용)
- HttpServletRequest와 HttpServletResponse 객체를 생성하여 생성된 Servlet에 전달
  - Thread는 Servlet의 service() 호출
  - service() 는 요청에 맞는 doGet() or doPost() 호출
- doGet() or doPost() 는 인자에 맞게 생성된 적절한 동적 컨텐츠를 Response 객체에 담아 WAS에 전달
- WAS는 Response 객체를 HttpResponse 형태로 바꾸어 Web Server에 전달
- 생성된 Thread를 종료하고, HttpServletRequest와 HttpServletResponse 객체를 제거

**기능**

- WAS(Web Application Server) 는 Web Server와는 다르게 DB조회 등 다양한 로직 처리를 요구하는 동적인 리소스를 제공
  - HTTP를 통해 Application 수행
  - aka. Web Container, Servlet Container
  - JSP, Servlet 구동 환경 제공

ex) Tomcat, JBoss, Jeus, Web Sphere 등

**Reference**

[Web Server와 WAS의 차이와 웹 서비스 구조](https://gmlwjd9405.github.io/2018/10/27/webserver-vs-was.html)

---

## Servlet Filter

목적지까지 이동하는 길에 여러 인증 절차가 필요하다. 여권을 미리 준비해두자!

- Servlet 이 지원하는 수문장
- 사용자 인증이나 로깅 등과 같은 웹 공통 관심사를 처리

```java
public interface Filter {
	//필터 초기화 메서드 (서블릿 컨테이너가 생성될 때 호출)
    public default void init(FilterConfig filterConfig) throws ServletException {}
	//Client의 요청이 올 때 마다 호출 (필터의 로직 구현)
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException;
	//필터 종료 메서드 (서블릿 컨테이너가 종료될 때 호출)
    public default void destroy() {}
}
```

---

## Dispatcher Servlet

인증을 마치고 이제 거의 목적지에 도달한 것 같다.

하지만 경로가 너무 많아서 길을 찾기가 어렵다.. 여기서 목적지까지 가는 길을 안내 받아보자.

- `doGet() or doPost() (WAS 에서 요청에 맞게 호출된 메서드)`를 통해 전달된 요청을 확인해서 적합한 Controller 에 위임해주는 Front Controller

---

## Spring Interceptor

이제 목적지 입구에 도착했다 !!

목적지에 입장을 하기 위해 예매한 표를 보여주자.

- Servlet Filter 와 동일하게 웹 공통 관심사를 처리
- Spring MVC 구조에 특화된 필터 기능 제공

```java
public interface HandlerInterceptor {
	//Controller 호출 전 (Handler Adapter 호출 전) - return true 시 다음으로 진행, false 시 끝
    default boolean preHandle(HttpServletRequest request,  HttpServletResponse response, Object handler) throws Exception {}
	//Controller 호출 후 (Handler Adapter 호출 후) - Controller에서 예외 발생 시 호출되지 않음.
    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {}
	//HTTP 요청 종료 후 (View rendering 후) - 예외 여부에 관계없이 호출
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {}
}
```

---

## Spring MVC

입구를 통과했으니 조금만 더 가면 목적지에 도착하겠다!

Handler 를 Controller 라고 생각하자.

### HandlerMapping

- HandlerMapping 을 통해 요청 URL에 매핑되는 `Handler 조회`
  - @RequestMapping 기반 Controller 일 경우 `RequestMappingHandlerMapping `
  - Spring Bean 기반 Controller 일 경우 `BeanNameUrlHandlerMapping `

### HandlerAdapter

- 조회한 Handler 를 실행할 수 있는 `Handler Adapter 조회`
  - @RequestMapping 기반 Controller 일 경우 `RequestMappingHandlerAdapter `
  - HttpRequestHandler 처리의 경우 `HttpRequestHandlerAdapter`
  - Spring Bean 기반 Controller 일 경우 `SimpleControllerHandlerAdapter `
- 앞서 조회한 `Handler Adapter 를 실행`하면 Handler Adapter 가 실제 `Handler(Controller) 실행`
  - 드디어 목적지에 도달했다. 😆 실컷 구경하고 오자! 😎
- Handler Adapter 는 Handler(Controller) 가 반환하는 정보를 `ModelAndView로 변환해서 반환`

### ViewResolver

- viewResolver 를 찾고 실행
  - Bena 이름으로 등록된 View 의 경우 `BeanNameViewResolver `
  - Bena 이름으로 등록된 View 가 없을 경우 `InternalResourceViewResolver `

**View**

- viewResolver는 View의 논리 이름을 물리 이름으로 바꾸고, 렌더링 역할을 담당하는` View 객체 반환`
  - `BeanNameViewResolver ` : `BeanNameView` 반환
  - `InternalResourceViewResolver ` : `InternalResourceView ` 반환

**View Rendering**

- View 객체를 통해 View Rendering
  - view.render()

**Reference**

https://jihunparkme.github.io/Spring-MVC-Part1-MVC/

---

## HTTP Response

이제 여행을 마치고 돌아갈 시간이다.

왔던 길과 반대로 Spring Interceptor, Dispatcher Servlet, Servlet Filter, WAS, Web Server 을 거쳐 돌아가면 된다.

...

...

집에 도착하니 선물이 있다!! 🎁

google 페이지 캡쳐
