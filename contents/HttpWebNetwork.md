# HTTP Web Network

영한님의 [모든 개발자를 위한 HTTP 웹 기본 지식](https://www.inflearn.com/course/http-%EC%9B%B9-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC#) 강의
HTTP Web Network 강의 노트

**Blog Note**

- [HTTP Web Network. Basic](https://jihunparkme.github.io/Http-Web-Network_basic/)

- [HTTP Web Network. Method](https://jihunparkme.github.io/Http-Web-Network_method/)

- [HTTP Web Network. Header](https://jihunparkme.github.io/Http-Web-Network_header/)

# Table Of Contents

- [인터넷 네트워크](#인터넷-네트워크)

  - IP(Internet Protocol)
  - TCP, UDP
  - PORT
  - DNS

- [URI와 웹 브라우저 요청 흐름](#URI과-웹-브라우저-요청-흐름)

  - URI
  - 웹 브라우저 요청 흐름

- [HTTP 기본](#HTTP-기본)

  - HTTP
  - 클라이언트 서버 구조
  - Stateful, Stateless
  - 비 연결성(connectionless)
  - HTTP 메시지

- [HTTP 메서드](#HTTP-메서드)

  - HTTP API
  - GET, POST
  - PUT, PATCH, DELETE
  - HTTP 메서드 속성

- [HTTP 메서드 활용](#HTTP-메서드-활용)

  - 클라이언트에서 서버로 데이터 전송
  - HTTP API 설계 예시

- [HTTP 상태코드](#HTTP-상태코드)

  - 2xx (성공)
  - 3xx (리다이렉션)
  - 4xx (클라이언트 오류)
  - 5xx (서버 오류)

- [HTTP 해더 (일반 헤더)](<#HTTP-해더-(일반-헤더)>)
  - 표현
  - 콘텐츠 협상 (Content negotiation)
  - 전송 방식
  - 일반 정보
  - 특별한 정보
  - 인증
  - 쿠키
- [HTTP 해더 (캐시와 조건부 요청)](<#HTTP-해더-(캐시와-조건부-요청)>)
  - 캐시 기본 동작
  - 검증 헤더와 조건부 요청
  - 캐시와 조건부 요청 헤더
  - 프록시 캐시
  - 캐시 무효화

---

# 인터넷 네트워크

message를 서버를 통해 어떻게 전달될까?

## `IP(Internet Protocol)`

- 지정한 IP Address에 데이터 전달
- Packet이라는 통신 단위로 데이터 전달
  - IP Packet 정보 : 출발지 IP, 목적지 IP, 기타.. 전송 데이터
  - 인터넷 노드들끼리 목적지 IP를 향해 Packet을 전달
- Internet Protocol의 한계
  - 비연결성 (대상이 없거나 서비스 불능 상태일 경우)
  - 비신뢰성 (패킷이 사라지거나, 순서가 지켜지지 않을 경우)
- 프로그램 구분

## `TCP, UDP`

- `참고 `> 프로토콜 계층 순서

  - 애플리케이션 계층 (브라우저, 채팅, 게임 등)
    - HTTP, FTP
  - OS
    - 전송 계층 : TCP, UDP
    - 인터넷 계층 : IP
  - 네트워크 인터페이스
    - LAN 드라이버/장비

- `TCP (Transmission Control Protocol)` 특징

  - 연결지향 (3 Way Handshake)
  - 데이터 전달 보증
  - 순서 보장 등..
  - IP의 한계를 해결. 대부분이 사용

- `UDP (User Datagram Protocol)`

  - 기능이 거의 없지만, 추가 설정 가능
  - 단순하고 빠름
  - IP와 동일하지만 PORT, 체크섬 정도 추가

## `PORT`

- TCP/IP 패킷
  - IP로 목적지 서버 찾기
  - PORT로 같은 서버 안에서 프로세스 구분
  - 클라이언트 IP : 100.100.1:1010
  - 서버 IP : 200.200.200:80
- 0 ~ 65535 까지 할당 가능
- 0 ~ 1023 은 잘 알려진 포트이므로, 사용하지 않는 것을 추천

  - FTP : 20, 21
  - TELNET : 23
  - HTTP : 80
  - HTTPS : 443

## `DNS`

- Domain Name System
- `도메인명`(이름)과 `IP`(전화번호)가 저장된 전화번호부

📑

```
복잡한 인터넷 망을 통해 메시지를 보내기 위해서 IP가 필요.
IP만으로는 신뢰가 부족하고 구분이 힘들기 때문에 TCP/UDP가 필요.
같은 IP 안에서 동작하는 애플리케이션을 구분하기 위해 PORT가 필요
IP는 변하기 쉽고 외우기 어렵기 때문에 DNS가 필요
```

---

# URI과 웹 브라우저 요청 흐름

## `URI` (**U**niform **R**esource **I**dentifier)

- 자원을 식별하는 방법
- **URI**는
  - **UR**esource**L**ocator : 리소스가 있는 위치를 지정
    - foo://example.com:8042/over/there?name=ferret#nose
  - **UR**esource**N**ame : 리소스에 이름을 부여
    - urn:example:animal:ferret:nose
  - 또는 둘 다 추가로 분류될 수 있음.
- [RFC Reference](https://www.ietf.org/rfc/rfc3986.txt)
- URL 문법
  - https://www.google.com:433/search?q=hello&hl=ko
    - `https` : 프로토콜 (http, https, ftp 등)
    - `www.google.com` : 호스트명
    - `433` : 포트 번호 (http는 80, https는 443)
    - `/search` : Path (리소스 경로)
    - `q=hello&hl=ko` : query parameter or query string 으로 불림

## 웹 브라우저 요청 흐름

- https://www.google.com:433/search?q=hello&hl=ko
  - www.google.com : DNS 조회 -> 200.200.200.2
    - 443 : HTTPS PORT 생략
  - 웹 브라우저가 HTTP 요청 메시지 생성
    - GET /search?q=hello&hl=ko HTTP/1.1
    - Host: www.google.com
  - socket 라이브러리를 통해 TCP/IP에 전달
  - TCP/IP 패킷 생성, HTTP 메시지 포함
  - ..
  - 응답 메시지

---

# HTTP 기본

## `HTTP`

- **H**yper**T**ext **T**ransfer **P**rotocol
- HTTP에 거의 모든 형태의 데이터를 전송할 수 있음
  - HTML, TEXT, IMAGE, 음성, 영상, 파일, JSON, XML ...
- 특징
  - 클라이언트 서버 구조
  - Stateless, connectionless
  - HTTP 메시지로 통신
  - 단순, 확장성

## 클라이언트 서버 구조

- Request-Response 구조
- 클라이언트는 서버에 request 후 response 대기
- 서버가 request에 대한 결과를 만들어서 응답

## `Stateful, Stateless`

### `Stateful`

- 서버가 클라이언트의 `이전 상태를 보존`
- ex) 점원이 바뀌면 고객의 상태를 알 수 없음 (로그인)
  - 항상 같은 서버와 연결
- 일반적으로 브라우저 쿠키와 서버 세션들을 사용해 상태 유지
- 상태 유지는 <i>최소한</i>만 사용

### `Stateless`

- 서버가 클라이언트의 `이전 상태를 보존 X`
- ex) 점원이 바뀌어도 고객의 상태를 알 수 있음 (소개 페이지)
  - 스케일 아웃(수평 확장)에 유리
- 장: 서버 확장성이 높음, 응답 서버를 쉽게 바꿀 수 있음
- 단: 클라이언트가 필요한 데이터를 지속적으로 전송

## 비 연결성(connectionless)

- Connectionless
  - 서버 연결을 유지하는 모델
    - 서버 자원 소모
  - 서버 연결을 유지하지 않는 모델
    - 최소한의 자원 유지
- HTTP는 기본이 연결을 유지하지 않는 모델
- 서버의 자원을 효율적으로 사용
- HTTP 지속 연결(Persistent Connections)로 TCP/IP의 3 way handshake 시간 소요 해결
- HTTP/2, HTTP/3 에서 최적화

## HTTP 메시지

- HTTP 메시지 구조
  - 시작 라인 (start-line)
  - 헤더 (header)
  - 공백 라인 (empty line, CRLF)
  - message body

### Start-Line

- request-line (`GET` `/search?q=hello&hl=ko` `HTTP/1.1`)
  - HTTP 메서드
    -GET, POST, PUT, DELETE
  - 요청 대상 (absolute-path[?query])
  - HTTP Version
  ```text
  // 요청 메시지
  GET /search?q=hello&hl=ko HTTP/1.1
  Host: www.google.com
  ```
- status-line

  - HTTP 버전
  - HTTP 상태 코드
    - 200, 400, 500
  - 이유 문구

  ```text
  // 응답 메시지
  HTTP/1.1 200 OK
  Content-Type: text/html;charset=UTF-8
  Content-Length: 3423

  <html>
      <body>...</body>
  </html>
  ```

### HTTP Header

- HTTP 전송에 필요한 모든 부가정보
  - message body를 제외하고 필요한 모든 메타 정보

### Message Body

- 실제 전송할 데이터
  - HTML 문서, 이미지, 영상, JSON 등등 (Byte 표현 가능한 모든 데이터)

HTTP 메서드

- GET : 리소스 조회
- POST : 요청 데이터 처리(등록)
- PUT : 리소스 대체, 없으면 생성
- PATCH : 리소스 부분 변경
- DELETE : 리소스 삭제

## API URI 설계

- 좋은 URI 설계는 리소스 식별이 중요
  - 회원 = 리소스
  - **회원** 목록 조회 /members
  - **회원** 조회 /members/{id} `GET`
  - **회원** 등록 /members/{id} `POST`
  - **회원** 수정 /members/{id} `PUT`
  - **회원** 삭제 /members/{id} `DELETE`
- 리소스(회원)와 행위(조회, 등록, 삭제, 변경)를 분리
  - URI는 리소스만 식별

### `GET`

- 리소스 **조회**
- 전달 데이터는 query parameter OR query string 을 통해 전달

### `POST`

- 새 리소스 **생성**(등록)
- 요청 **데이터 처리**
  - 프로세스 처리
  - 컨트롤 URI
- massage body를 통해 서버로 요청 데이터 전달

### `PUT`

- 리소스가 있으면 대체, 없으면 생성 **(덮어쓰기)**
- 클라이언트가 리소스 위치를 알고 URI 지정 (POST와의 차이)
  - `PUT /members/100 HTTP/1.1`
  - `POST /members HTTP/1.1`

### `PATCH`

- 리소스 **부분 변경**
  - `PATCH /members/100 HTTP/1.1`

### `DELETE`

- 리소스 **제거**
  - `DELETE /members/100 HTTP/1.1`

## HTTP 메서드의 속성

[HTTP 속성](https://ko.wikipedia.org/wiki/HTTP#%EC%9A%94%EC%95%BD%ED%91%9C)

- 안전(Safe)
  - 리소스 변경이 일어나지 않는 것 (ex. GET, HEAD ..)
- 멱등(Idempotent)
  - 몇 번을 호출하든 결과는 같다 (GET, PUT, DELETE)
  - 자동 복구 메커니즘에서 활용
- 캐시가능(Cacheable)
  - 응답 결과 리소스를 캐시해서 사용 (GET, HEAD 정도만 캐시로 사용)

---

# HTTP 메서드 활용

## 클라이언트에서 서버로 데이터 전송

- Query Parameter를 통한 데이터 전송
  - GET
  - ex) 정렬 필터(검색)
- Message Body를 통한 데이터 전송
  - POST, PUT, PATCH
  - ex) 회원가입, 상품주문, 리소스 등록, 리소스 변경

**정적 데이터 조회**

- GET
- 이미지, 정적 텍스트 문서
- Query Parameter 없이 리소스 경로로 단순 조회

**동적 데이터 조회**

- GET
- 게시물 검색, 정렬, 필터
- Query Parameter 사용

**HTML FORM을 통한 데이터 전송**

- POST
- 등록, 변경
- Content-Type
  - application/x-www-form-urlencoded
    - form 내용을 Message Body를 통해 전송
    - 전송 데이터를 url encoding 처리
  - multipart/form-data
    - 파일 업로드 같은 바이너리 데이터 전송 시 사용
    - 다른 종류의 여러 파일과 폼 내용을 함께 전송 가능
- HTML Form 전송은 GET, POST만 지원

**HTTP API를 통한 데이터 전송**

- Server to Server 통신
- 모바일 앱 클라이언트
- Ajax 웹 클라이언트
- GET -> Query Parameter로 데이터 전달 후 조회
- POST, PUT, PATCH -> Message Body를 통해 데이터 전송
- Content-Type : application/json

## HTTP API 설계 예시

**POST 기반 등록**

서버가 관리하는 리소스 디렉토리 (Collection)

- 회원 목록 /members -> `GET`
- 회원 등록 /members -> `POST`
- 회원 조회 /members/{id} -> `GET`
- 회원 수정 /members/{id} -> `PATCH`, `PUT`, `POST`
- 회원 삭제 /members/{id} -> `DELETE`

**PUT 기반 등록**

클라이언트가 관리하는 리소스 저장소 (Store)

- 파일 목록 /files -> `GET`
- 파일 조회 /files/{filename} -> `GET`
- 파일 등록 /files/{filename} -> `PUT`
- 파일 삭제 /files/{filename} -> `DELETE`
- 파일 대량 등록 /files -> `POST`

**HTML FORM 사용**

HTML FORM은 GET, POST만 지원하므로 Control URI 사용

- 회원 목록 /members -> `GET`
- 회원 등록 폼 /members/new -> `GET`
- 회원 등록 /members/new -> `POST`
- 회원 조회 /members/{id} -> `GET`
- 회원 수정 폼 /members/{id}/edit -> `GET`
- 회원 수정 /members/{id}/edit -> `POST`
- 회원 삭제 /members/{id}/delete -> `POST`

[REST Resource Naming Guide](https://restfulapi.net/resource-naming/)

- 컬렉션과 문서로 최대한 해결하고 그 후에 컨트롤 URI 사용

---

# HTTP 상태코드

## `2xx` (Successful): 요청 정상 처리

**Code**

- 200 OK
- 201 Created (POST)
- 202 Accepted (batch)
- 204 No Content

## `3xx` (Redirection): 요청을 완료를 위해 추가 행동 필요

**Redirect**

- 웹 브라우저는 3xx 응답의 결과에 Location 헤더가 있으면, Location 위치로 자동 이동
- 영구 리다이렉션 : 특정 리소스의 URI가 영구적으로 이동 (301, 308)
- 일시 리다이렉션 : 일시적인 변경 (302, 303, 307)
  - PRG(Post/Redirect/Get)에 사용 / 새로고침 중복 주문 방지
- 특수 리다이렉션 : 결과 대신 캐시 사용

**Code**

- 300 Multiple Choices (X)
- 301 Moved Permanently
  - 리다이렉트 시 <u>Get</u>으로 변하고, 본문 손실
- 302 Found
  - 리다이렉트 시 <u>GET</u>으로 변하고, 본문 제거
- 303 See Other
  - 리다이렉트 시 <u>GET</u>으로 변경
- 304 Not Modified
  - 클라이언트에게 리소스가 수정되지 않았음을 알려줌 (캐시 재사용)
- 307 Temporary Redirect
  - 리다이렉트 시 메서드와 본문 유지
- 308 Permanent Redirect
  - 리다이렌트 시 <u>POST</u>, 본문 유지

## `4xx` (Client Error)

- 오류의 원인은 클라이언트

**Code**

- 400 Bad Request
  - 클라이언트가 잘못된 요청을 해서 서버가 요청을 처리할 수 없음
- 401 Unauthorized
  - 클라이언트가 해당 리소스에 대한 인증이 필요
  - 인증(Authentication): 로그인
  - 인가(Authorization): 권한
- 403 Forbidden
  - 서버가 요청을 이해했지만 승인을 거부 (접근 권한 제한)
- 404 Not Found
  - 요청 리소스를 찾을 수 없음

## `5xx` (Server Error)

- 서버 문제로 오류 발생

**Code**

- 500 Internal Server Error
  - 서버 내부 문제로 오류 발생 (애매하면 500)
- 503 Service Unavailable
  - 서비스 이용 불가

---

# HTTP 해더 (일반 헤더)

```header
HTTP/1.1 200 OK         -- start line

-- HTTP Header
Content-Type: text/html;charset=UTF-8
Content-Length: 3423
---

-- Message Body
<html>
 <body>...</body>
</html>
---
```

## HTTP 헤더

- `header-field`
  - field-name: field-value
  - HTTP 전송에 필요한 모든 부가정보
    - ex) Mesasage body 내용/크기, 압축, 인증, 서버 정보 등..
  - [표준 헤더](https://en.wikipedia.org/wiki/List_of_HTTP_header_fields)

## 표현

- **표현**은 요청이나 응답에서 전달할 실제 데이터
- **표현 헤더**는 **표현 데이터**를 해석할 수 있는 정보 제공
- 표현 헤더
  - `Content-Type` : 표현 데이터의 형식
    - text/html; charset=utf-8
    - application/json
    - image/png
  - `Content-Encoding` : 표현 데이터의 압축 방식
    - gzip
    - deflate
    - identity
  - `Content-Language` : 표현 데이터의 자연 언어
    - ko
    - en
    - en-US
  - `Content-Length` : 표현 데이터의 길이(Byte)

## Content negotiation

Client가 선호하는 표현 요청 (요청시에만 사용)

[rfc7231 Accept](https://datatracker.ietf.org/doc/html/rfc7231#section-5.3.2)

- Accept : Client가 선호하는 미디어 타입 전달
  - `Accept: text/*, text/plain, text/plain;format=flowed, */*`
- Accept-Charset : Client가 선호하는 문자 인코딩
- Accept-Encoding : Client가 선호하는 압축 인코딩
- Accept-Language : Client가 선호하는 자연 언어
  - `Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7`

## 전송 방식

**단순 전송**

- `Content-Length: 3423`

**압축 전송**

- `Content-Encoding: gzip`

**분할 전송**

- `Transfer-Encoding: chunked`

**범위 전송**

- `Content-Range: bytes 1001-2000 / 2000`

## 일반 정보

`Form`

- User agent email 정보 (요청)

`Referer`

- 이전 웹 페이지 주소 (요청)

`User-Agent`

- User-Agent Application 정보 (요청)
- Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36

`Server`

- 요청을 처리하는 ORIGIN 서버의 소프트웨어 정보 (응답)
- Apache/2.2.22

`Date`

- 메시지가 발생한 날짜와 시간 (응답)

## 특별한 정보

`Host`

- 요청한 호스트의 정보 (도메인)
- 요청에서 필수
- 하나의 IP에 여러 도메인이 적용되었을 경우

`Location`

- 페이지 리다이렉션
- 3xx 응답의 결과에 Location 헤더가 있으면, Location 위치로 자동 이동
- 201 (Created), 3xx (Redirection)

## 인증

`Authorization`

- 클라이언트 인증 정보를 서버에 전달
- Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW

`WWW-Authenticate`

- 리소스 접근시 필요한 인증 방법 정의

## 쿠키

`Set-Cookie`

- 서버에서 클라이언트로 쿠키 전달(응답)

`Cookie`

- 클라이언트가 서버에서 받은 쿠키를 저장하고, HTTP 요청시 서버로 전달

**동작**

1. 로그인
2. 서버는 Set-Cookie에 user 정보를 담아서 응답
3. 웹브라우저 내부 쿠키 저장소에 쿠키(user) 정보 저장
4. 로그인 이후 요청을 보낼 때마다 자동으로 쿠키 저장소를 조회 후 Cookie 헤더를 만들어서 서버에 전송

**사용**

- 사용자 로그인 세션 관리
- 광고 정보 트래킹

**쿠키 정보는 항상 서버에 전송**

- 네트워크 트래픽 추가 유발
- 최소한의 정보만 사용(session id, 인증 token)
- 웹 스토리지(localStorage, sessionStorage)를 사용하여 웹 브라우저 내부에 데이터 저장 가능

**생명주기**

- Set-Cookie
  - `expires`=Sat, 26-Dec-2020 04:39:21 GMT
  - `max-age`=3600 (sec)
  - `domain`=google.com
    - 명시 : 명시 도메인 + 서브 도메인
    - 생략 : 현재 문서 기준 도메인
  - `path`=/
    - 명시 경로 포함 하위 경로
  - `Secure`
    - https인 경우에만 쿠키 전송
  - `HttpOnly`
    - XSS 공격 방지 / JS에서 접근 불가
  - `SameSite`
    - XSRF 공격 방지
    - 요청 도메인과 쿠키 설정 도메인이 같은 경우에만 쿠키 전송
- 세션 쿠키: 만료 날짜를 생략하면 브라우저 종료시 까지만 유지
- 영속 쿠키: 만료 날짜를 입력하면 해당 날짜까지 유지

---

# HTTP 해더 (캐시와 조건부 요청)

## 캐시 기본 동작

**동작**

1. 캐시 유효 시간 설정 -> `cache-control`: max-age=60
2. 응답 결과를 브라우저 캐시에 저장
3. 두 번째 요청 시 캐시를 탐색 후 캐시에서 조회 (네트워크 사용량 감소)
4. 재요청 시 캐시 유효 시간이 초과되었다면 갱신

## 검증 헤더와 조건부 요청 (Last-Modified)

- 캐시 만료후에도 서버에서 데이터를 변경하지 않았다만 저장해 두었던 캐시를 재사용 할 수 있다.

- 초기 요청 시 데이터 최종 수정일을 캐시에 함께 저장 (검증 헤더)
  - `Last-Modified`: Wed, 21 July 2021 07:28:00 GMT
- 캐시 시간 초과 후 재요청 시 데이터 최종 수정일을 헤더에 함께 전달 (조건부 요청)
  - `if-modified-since`: Wed, 21 July 2021 07:28:00 GMT
- 서버에서 데이터가 수정되지 않은게 확인되면 **304 Not Modified** 로 응답
  - HTTP Body는 포함하지 않고 Header 메타 정보만 응답
- 클라이언트는 캐시에 저장되어 있는 데이터 재사용

## 검증 헤더와 조건부 요청 (ETag)

- Entity Tag : Last-Modified의 단점 보완
- 캐시 제어 로직을 서버에서 관리
  - 캐시 데이터는 임의의 고유 버전 혹은 Hash 이름 보유
- 초기 요청 시 ETag를 캐시에 함께 저장 (검증 헤더)
  - `ETag`: "a2jiodwjekjl3"
- 캐시 시간 초과 후 재요청 시 ETag를 헤더에 함께 전달 (조건부 요청)
  - `If-None-Match`: "aaaaaaaaaa"
- 서버에서 데이터가 수정되지 않은게 확인되면 **304 Not Modified** 로 응답
  - HTTP Body는 포함하지 않고 Header 메타 정보만 응답
- 클라이언트는 캐시에 저장되어 있는 데이터 재사용

## 캐시와 조건부 요청 헤더

`Cache-Control` : 캐시 제어

- `max-age` : 캐시 유효 시간 (초)
- `no-cache` : (이터는 캐시해도 되지만), 프록시 캐시가 아닌 항상 원서버에 변경사항 검증 후 사용
- `no-store` : 데이터에 민감한 정보가 있으므로 저장 X
- `must-revalidate` : 캐시 만료 후 최초 조회 시 원 서버에 검증

## 프록시 서버

- 해외 원서버에 있는 데이터를 브라우저에서(private cache)빠르게 이용하기 위해 중간(프록시 캐시 서버, public cache)에서 공용으로 사용하는 캐시 서버

캐시 지시어(directives)

- `Cache-Control: public`
  - 응답이 public 캐시에 저장 가능
- `Cache-Control: private`
  - 응답이 해당 사용자만을 위한 것, private 캐시에 저장(기본값)

## 캐시 무효화

`Cache-Control`: no-cache, no-store, must-revalidate

`Pragma`: no-cache # HTTP 1.0 하위 호환

> Reference
>
> [HTTP 스펙 : RFC 7230~7235](https://datatracker.ietf.org/doc/html/rfc7230)
>
> HTTP 완벽가이드 도서
