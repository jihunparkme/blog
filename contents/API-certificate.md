# 인트라넷 PC에 인증서 추가하기

인트라넷 PC 에서 API 를 활용하다보면 SSL 인증서 문제와 자주 마주치게 된다..🤔

`"unable to find valid certification path to requested target"`

이 경우, API 페이지에 등록된 Root CA 인증서를 PC 의 jre 에 등록해 주면 된다.

인증서를 빨리 등록하고, API 를 활용해보자 !

(참고로 필자는 [한국어기초사전 API](https://krdict.korean.go.kr/openApi/openApiInfo) 를 예로 작성하였다.)

## ROOT CA 인증서 확인

**API ROOT 페이지에서 Root CA 인증서 확인**

Chorme 기준 사이트 정보 보기 버튼은 URL 입력칸 왼쪽에 위치

- 이 사이트는 보안 연결(HTTPS)이 사용되었습니다.

    <img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/api-certification/1.png" width="50%">

- 인증서가 유요함

    <img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/api-certification/2.png" width="50%">

- 인증경로 -> Root CA -> 인증서 보기

    <img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/api-certification/3.png" width="50%">

## 인증서 내보내기

**Root CA 인증서 내보내기**

인증서 창의 인증 경로 탭에서 인증서 보기를 클릭하면 새로운 인증서 창이 열린다.

- 자세히 -> 파일에 복사

    <img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/api-certification/4.png" width="50%">

- DER로 인코딩된 바이너리

    <img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/api-certification/5.png" width="50%">

- 파일 저장 경로

    <img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/api-certification/6.png" width="50%">

- `.cer` 확장자의 인증서가 저장되었다.

    <img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/api-certification/7.png" width="20%">

## 인증서 등록

- 저장한 Root CA 인증서를 등록해보자.

1\. 관리자 권한으로 CMD 실행

2\. jre\bin 디렉토리로 이동

```console
cd C:\Program Files\java-1.8.0\jre\bin
```

3\. 인증서 등록

```console
keytool -importcert -alias your-alias -keystore "C:\Program Files\java-1.8.0\jre\lib\security\cacerts" -storepass changeit -file C:\Users\Cristoval\Downloads\krdictCert.cer
```

<img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/api-certification/8.png" width="40%">

## Reference

https://stackoverflow.com/a/59569901
