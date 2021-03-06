# Internetπ

- [backend-loadmap](https://github.com/kamranahmedse/developer-roadmap/blob/master/translations/korean/img/backend.png) Part 01. Internet

---

## μΈν°λ·μ μλ μλ¦¬π

- μΈν°λ·μ κ°μ₯ κΈ°λ³Έμ μΈ κ²μ, `μ»΄ν¨ν°`λ€μ΄ μλ‘ `ν΅μ  κ°λ₯ν κ±°λν λ€νΈμν¬`λΌλ κ²
- μ€κ°μ `λΌμ°ν°`κ° μμΉνμ¬ μ»΄ν¨ν°μ λΌμ°ν° νΉμ μ»΄ν¨ν° μ¬μ΄μμ ν΅μ μ μ λ¬
  - μ»΄ν¨ν° <-> λΌμ°ν° <-> μ»΄ν¨ν°
- `λͺ¨λ`μ΄λΌλ μ₯λΉλ₯Ό νμ©νμ¬ _λ€νΈμν¬μ μ λ³΄λ₯Ό μ ν μμ€μμ μ²λ¦¬ν  μ μλ μ λ³΄λ‘ λ°κΎΈμ΄_ μ΄λ κ³³μ μλ λΌμ°ν°μ ν΅μ μ΄ κ°λ₯νλλ‘ ν¨
  - μ»΄ν¨ν° <-> λΌμ°ν° <-> λͺ¨λ <-> λΌμ°ν° <-> μ»΄ν¨ν°
- λ€νΈμν¬λ₯Ό *μΈν°λ· μλΉμ€ μ κ³΅ μμ²΄μ (Internet Service Provider, `ISP`)μ μ°κ²°*νμ¬ λ€λ₯Έ ISPμ ν΅μ ν  μ μλλ‘ ν¨
  - μ»΄ν¨ν° <-> λΌμ°ν° <-> λͺ¨λ <-> ISP1 <-> ... <-> ISP2 <-> λͺ¨λ <-> λΌμ°ν° <-> μ»΄ν¨ν°
- Reference

> [How does the Internet work?](https://developer.mozilla.org/ko/docs/Learn/Common_questions/How_does_the_Internet_work)
>
> [How the Web works](https://developer.mozilla.org/ko/docs/Learn/Getting_started_with_the_web/How_the_Web_works)
>
> [How Does the Internet Work?](https://web.stanford.edu/class/msande91si/www-spr04/readings/week1/InternetWhitepaper.htm)

## HTTP?π

- **H**yper **T**ext **T**ransfer **P**rotocol
- HTML λ¬Έμμ κ°μ λ¦¬μμ€λ€μ κ°μ Έμ¬ μ μλλ‘ ν΄μ£Όλ νλ‘ν μ½
- WWW(World Wide Web): μΉ ν΄λΌμ΄μΈνΈμ μλ² κ°μ ν΅μ 
- ν΄λΌμ΄μΈνΈ μ»΄ν¨ν°μ μΉ μλ² κ°μ ν΅μ μ **HTTP μμ²­** μ λ³΄λ΄κ³  **HTTP μλ΅μ** μμ  νμ¬ μν
- Reference

> [An overview of HTTP](https://developer.mozilla.org/ko/docs/Web/HTTP/Overview)
>
> [What is HTTP?](https://www.w3schools.com/whatis/whatis_http.asp)

## λΈλΌμ°μ μ κ·Έ μλ μλ¦¬π

- λΈλΌμ°μ μ μ£Όμ κΈ°λ₯μ μ¬μ©μκ° μ νν μμμ μλ²μ μμ²­νκ³  λΈλΌμ°μ μ νμνλ κ²
- Reference

> [λΈλΌμ°μ λ μ΄λ»κ² λμνλκ°?](https://d2.naver.com/helloworld/59361)
>
> [How Browsers Work: Behind the scenes of modern web browsers](https://www.html5rocks.com/en/tutorials/internals/howbrowserswork/)
>
> [How does web browsers work?](https://medium.com/@monica1109/how-does-web-browsers-work-c95ad628a509)
>
> [how browsers work](https://developer.mozilla.org/en-US/docs/Web/Performance/How_browsers_work)

## DNS(Domain Name System)μ κ·Έ μλ μλ¦¬π

- νΈμ€νΈμ λλ©μΈ μ΄λ¦μ νΈμ€νΈμ λ€νΈμν¬ μ£Όμλ‘ λ°κΎΈκ±°λ κ·Έ λ°λμ λ³νμ μνν  μ μλλ‘ νκΈ° μν΄ κ°λ°
- μλ μλ¦¬
  1.  μΉ λΈλΌμ°μ μ `www.naver.com`μ μλ ₯νλ©΄ λ¨Όμ  Local DNSμκ² `www.naver.com`μ΄λΌλ hostnameμ λν IP μ£Όμλ₯Ό μ§μνμ¬ Local DNSμ μμΌλ©΄ λ€λ₯Έ DNS name μλ² μ λ³΄λ₯Ό λ°μ(Root DNS μ λ³΄ μ λ¬ λ°μ)
  2.  Root DNS μλ²μ `www.naver.com` μ§μ
  3.  Root DNS μλ²λ‘ λΆν° com λλ©μΈμ κ΄λ¦¬νλ TLD (Top-Level Domain) μ΄λ¦ μλ² μ λ³΄ μ λ¬ λ°μ
  4.  TLDμ `www.naver.com` μ§μ
  5.  TLDμμ `name.com` κ΄λ¦¬νλ DNS μ λ³΄ μ λ¬
  6.  `naver.com` λλ©μΈμ κ΄λ¦¬νλ DNS μλ²μ `www.naver.com` νΈμ€νΈλ€μμ λν IP μ£Όμ μ§μ
  7.  Local DNS μλ²μκ² "μ! `www.naver.com`μ λν IP μ£Όμλ 222.122.195.6 μλ΅
  8.  Local DNSλ `www.naver.com`μ λν IP μ£Όμλ₯Ό μΊμ±μ νκ³  IP μ£Όμ μ λ³΄ μ λ¬
- Reference

> [DNSμ μλμλ¦¬](https://velog.io/@goban/DNS%EC%99%80-%EC%9E%91%EB%8F%99%EC%9B%90%EB%A6%AC)
>
> [How the Domain Name System (DNS) Works](https://www.verisign.com/en_US/website-presence/online/how-dns-works/index.xhtml)
>
> [How Domain Name Servers Work](https://computer.howstuffworks.com/dns.htm)

## λλ©μΈ μ΄λ¦?π

- `IP` : μΈν°λ·μ μ°κ²°λμ΄ μλ μ₯μΉ(μ»΄ν¨ν°, μ€λ§νΈν°, νλΈλ¦Ώ, μλ² λ±λ±)λ€μ κ°κ°μ μ₯μΉλ₯Ό μλ³ν  μ μλ μ£Όμλ₯Ό κ°μ§κ³  μλλ° μ΄λ₯Ό `ip`λΌκ³  νλ€. μ) 115.68.24.88, 192.168.0.1
- `λλ©μΈ` : ipλ μ¬λμ΄ μ΄ν΄νκ³  κΈ°μ΅νκΈ° μ΄λ ΅κΈ° λλ¬Έμ μ΄λ₯Ό μν΄μ κ° ipμ μ΄λ¦μ λΆμ¬ν  μ μκ² νλλ°, μ΄κ²μ `λλ©μΈ`μ΄λΌκ³  νλ€. μ) google.com -> 172.217.161.174
- `λλ©μΈμ κ΅¬μ±μμ`
  - opentutorials.org  
    opentutorials : μ»΄ν¨ν°μ μ΄λ¦  
    org : μ΅μμ λλ©μΈ - λΉμλ¦¬λ¨μ²΄
  - daum.co.kr  
    daum : μ»΄ν¨ν°μ μ΄λ¦  
    co : κ΅­κ° ννμ μ΅μμ λλ©μΈμ μλ―Έ  
    kr : λνλ―Όκ΅­μ NICμμ κ΄λ¦¬νλ λλ©μΈμ μλ―Έ
- Reference

> [λλ©μΈμ΄λ?](https://opentutorials.org/course/228/1450)
>
> [What is a domain name?](https://developer.mozilla.org/ko/docs/Learn/Common_questions/What_is_a_domain_name)

## νΈμ€ν?π

- μΈν°λ· νΈμ€ν μλΉμ€μ μΌμ’μΌλ‘ κ°μΈκ³Ό λ¨μ²΄κ° WWWμ ν΅νμ¬ μΉμ¬μ΄νΈλ₯Ό μ κ³΅νλ κ²
- μΉ νΈμ€νΈλ μΈν°λ· μ°κ²°μ μ κ³΅ν λΏ μλλΌ, μΌλ°μ μΌλ‘ λ°μ΄ν° μΌν°μμ ν΄λΌμ΄μΈνΈ μ΄μ©μ λν μλ λλ μμ νλ μλ²μ κ³΅κ°μ μ κ³΅νλ νμ¬λ₯Ό κ°λ¦¬ν¨λ€
- Reference

> [What is Web Hosting?](https://www.website.com/beginnerguide/webhosting/6/1/what-is-web-hosting?.ws&source=SC)
>
> [What is Web Hosting?](https://www.namecheap.com/hosting/what-is-web-hosting-definition/)
