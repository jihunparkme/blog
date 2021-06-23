# Internet🌞

-   [backend-loadmap](https://github.com/kamranahmedse/developer-roadmap/blob/master/translations/korean/img/backend.png){:target="\_blank"} Part 01. Internet

---

## 인터넷의 작동 원리🌟

-   인터넷의 가장 기본적인 것은, `컴퓨터`들이 서로 `통신 가능한 거대한 네트워크`라는 것
-   중간에 `라우터`가 위치하여 컴퓨터와 라우터 혹은 컴퓨터 사이에서 통신을 전달
    -   컴퓨터 <-> 라우터 <-> 컴퓨터
-   `모뎀`이라는 장비를 활용하여 _네트워크의 정보를 전화 시설에서 처리할 수 있는 정보로 바꾸어_ 어느 곳에 있는 라우터와 통신이 가능하도록 함
    -   컴퓨터 <-> 라우터 <-> 모뎀 <-> 라우터 <-> 컴퓨터
-   네트워크를 _인터넷 서비스 제공 업체에 (Internet Service Provider, `ISP`)에 연결_하여 다른 ISP와 통신할 수 있도록 함
    -   컴퓨터 <-> 라우터 <-> 모뎀 <-> ISP1 <-> ... <-> ISP2 <-> 모뎀 <-> 라우터 <-> 컴퓨터
-   Reference

> [How does the Internet work?](https://developer.mozilla.org/ko/docs/Learn/Common_questions/How_does_the_Internet_work){:target="\_blank"}
>
> [How the Web works](https://developer.mozilla.org/ko/docs/Learn/Getting_started_with_the_web/How_the_Web_works){:target="\_blank"}
>
> [How Does the Internet Work?](https://web.stanford.edu/class/msande91si/www-spr04/readings/week1/InternetWhitepaper.htm){:target="\_blank"}

## HTTP?🌟

-   **H**yper **T**ext **T**ransfer **P**rotocol
-   HTML 문서와 같은 리소스들을 가져올 수 있도록 해주는 프로토콜
-   WWW(World Wide Web): 웹 클라이언트와 서버 간의 통신
-   클라이언트 컴퓨터와 웹 서버 간의 통신은 **HTTP 요청** 을 보내고 **HTTP 응답을** 수신 하여 수행
-   Reference

> [An overview of HTTP](https://developer.mozilla.org/ko/docs/Web/HTTP/Overview){:target="\_blank"}
>
> [What is HTTP?](https://www.w3schools.com/whatis/whatis_http.asp){:target="\_blank"}

## 브라우저와 그 작동 원리🌟

-   브라우저의 주요 기능은 사용자가 선택한 자원을 서버에 요청하고 브라우저에 표시하는 것
-   Reference

> [브라우저는 어떻게 동작하는가?](https://d2.naver.com/helloworld/59361){:target="\_blank"}
>
> [How Browsers Work: Behind the scenes of modern web browsers](https://www.html5rocks.com/en/tutorials/internals/howbrowserswork/){:target="\_blank"}
>
> [How does web browsers work?](https://medium.com/@monica1109/how-does-web-browsers-work-c95ad628a509){:target="\_blank"}
>
> [how browsers work](https://developer.mozilla.org/en-US/docs/Web/Performance/How_browsers_work){:target="\_blank"}

## DNS(Domain Name System)와 그 작동 원리🌟

-   호스트의 도메인 이름을 호스트의 네트워크 주소로 바꾸거나 그 반대의 변환을 수행할 수 있도록 하기 위해 개발
-   작동 원리
    1.  웹 브라우저에 `www.naver.com`을 입력하면 먼저 Local DNS에게 `www.naver.com`이라는 hostname에 대한 IP 주소를 질의하여 Local DNS에 없으면 다른 DNS name 서버 정보를 받음(Root DNS 정보 전달 받음)
    2.  Root DNS 서버에 `www.naver.com` 질의
    3.  Root DNS 서버로 부터 com 도메인을 관리하는 TLD (Top-Level Domain) 이름 서버 정보 전달 받음
    4.  TLD에 `www.naver.com` 질의
    5.  TLD에서 `name.com` 관리하는 DNS 정보 전달
    6.  `naver.com` 도메인을 관리하는 DNS 서버에 `www.naver.com` 호스트네임에 대한 IP 주소 질의
    7.  Local DNS 서버에게 "응! `www.naver.com`에 대한 IP 주소는 222.122.195.6 응답
    8.  Local DNS는 `www.naver.com`에 대한 IP 주소를 캐싱을 하고 IP 주소 정보 전달
-   Reference

> [DNS와 작동원리](https://velog.io/@goban/DNS%EC%99%80-%EC%9E%91%EB%8F%99%EC%9B%90%EB%A6%AC){:target="\_blank"}
>
> [How the Domain Name System (DNS) Works](https://www.verisign.com/en_US/website-presence/online/how-dns-works/index.xhtml){:target="\_blank"}
>
> [How Domain Name Servers Work](https://computer.howstuffworks.com/dns.htm){:target="\_blank"}

## 도메인 이름?🌟

-   `IP` : 인터넷에 연결되어 있는 장치(컴퓨터, 스마트폰, 타블릿, 서버 등등)들은 각각의 장치를 식별할 수 있는 주소를 가지고 있는데 이를 `ip`라고 한다. 예) 115.68.24.88, 192.168.0.1
-   `도메인` : ip는 사람이 이해하고 기억하기 어렵기 때문에 이를 위해서 각 ip에 이름을 부여할 수 있게 했는데, 이것을 `도메인`이라고 한다. 예) google.com -> 172.217.161.174
-   `도메인의 구성요소`
    -   opentutorials.org  
        opentutorials : 컴퓨터의 이름  
        org : 최상위 도메인 - 비영리단체
    -   daum.co.kr  
        daum : 컴퓨터의 이름  
        co : 국가 형태의 최상위 도메인을 의미  
        kr : 대한민국의 NIC에서 관리하는 도메인을 의미
-   Reference

> [도메인이란?](https://opentutorials.org/course/228/1450){:target="\_blank"}
>
> [What is a domain name?](https://developer.mozilla.org/ko/docs/Learn/Common_questions/What_is_a_domain_name){:target="\_blank"}

## 호스팅?🌟

-   인터넷 호스팅 서비스의 일종으로 개인과 단체가 WWW을 통하여 웹사이트를 제공하는 것
-   웹 호스트는 인터넷 연결을 제공할뿐 아니라, 일반적으로 데이터 센터에서 클라이언트 이용에 대한 임대 또는 소유하는 서버의 공간을 제공하는 회사를 가리킨다
-   Reference

> [What is Web Hosting?](https://www.website.com/beginnerguide/webhosting/6/1/what-is-web-hosting?.ws&source=SC)
>
> [What is Web Hosting?](https://www.namecheap.com/hosting/what-is-web-hosting-definition/)