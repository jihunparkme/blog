# HTTP Web Network

영한님의 [모든 개발자를 위한 HTTP 웹 기본 지식](#https://www.inflearn.com/course/http-%EC%9B%B9-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC#) 강의
HTTP Web Network 강의 노트

# Table Of Contents

1. [인터넷 네트워크](#인터넷-네트워크)
2. [URI와 웹 브라우저 요청 흐름](#URI과-웹-브라우저-요청-흐름)
3. [HTTP 기본](#HTTP-기본)
4. [HTTP 메서드](#HTTP-메서드)
5. [HTTP 메서드 활용](#HTTP-메서드-활용)
6. [HTTP 상태코드](#HTTP-상태코드)
7. [HTTP 해더 (일반 헤더)](<#HTTP-해더-(일반-헤더)>)
8. [HTTP 해더 (캐시와 조건부 요청)](<#HTTP-해더-(캐시와-조건부-요청)>)

---

## 인터넷 네트워크

message를 서버를 통해 어떻게 전달될까?

1.`IP(Internet Protocol)`

- 지정한 IP Address에 데이터 전달
- Packet이라는 통신 단위로 데이터 전달
  - IP Packet 정보 : 출발지 IP, 목적지 IP, 기타.. 전송 데이터
  - 인터넷 노드들끼리 목적지 IP를 향해 Packet을 전달
- Internet Protocol의 한계
  - 비연결성 (대상이 없거나 서비스 불능 상태일 경우)
  - 비신뢰성 (패킷이 사라지거나, 순서가 지켜지지 않을 경우)
- 프로그램 구분

  2.`TCP, UDP`

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

    3.`PORT`

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

    4.`DNS`

- Domain Name System
- `도메인명`(이름)과 `IP`(전화번호)가 저장된 전화번호부

📑

```
복잡한 인터넷 망을 통해 메시지를 보내기 위해서 IP가 필요.
IP만으로는 신뢰가 부족하고 구분이 힘들기 때문에 TCP/UDP가 필요.
같은 IP 안에서 동작하는 애플리케이션을 구분하기 위해 PORT가 필요
IP는 변하기 쉽고 외우기 어렵기 때문에 DNS가 필요
```

## URI과 웹 브라우저 요청 흐름

1.URI (**U**niform **R**esource **I**dentifier)

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

## HTTP 기본

## HTTP 메서드

## HTTP 메서드 활용

## HTTP 상태코드

## HTTP 해더 (일반 헤더)

## HTTP 해더 (캐시와 조건부 요청)
