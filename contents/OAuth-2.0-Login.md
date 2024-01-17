# Sign in with Apple

# OAuth 2.0 Login(Kakao, Google, Apple)

## Kakao

[카카오 개발자 페이지](https://developers.kakao.com/)에서 애플리케이션을 먼저 등록합니다.
- (1) 내 어플리케이션 메뉴로 이동
- (2) 애플리케이션 추가하기 선택
- (3) 앱 아이콘, 앱 이름, 사업자명 입력
- (4) 앱 키에서 REST API 키를 사용할 예정
- (5) 카카오 로그인 - 활성화 설명 ON
  - 카카오 로그인 진행 시 Redirect URI 등록
  - ex. http://localhost:8080/login/oauth2/kakao


보안 -> Client Secret 생성 후 코드도 저장 + 활성화
플랫폼 -> Web 플랫폼 등록 -> http://localhost:8080 설정
동의항목 -> 가져올 정보값 선택 (일부 항목은 검수를 해야 필수 동의 설정 가능)
닉네임, 이메일만 선택하고 진행해 봄

.

[카카오 API 문서]((https://developers.kakao.com/docs/latest/ko/kakaologin/common))에서 상세하게 설명되어 있지만, 적용 전에 연동하는 부분만 간략하게 정리해 보려고 합니다.

<figure>
    <img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/oauth-login/kakaologin_sequence.png">    
    <figcaption>https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#before-you-begin-process</figcaption>
</figure>

.

**Step 1. 인가 코드 받기**

`GET https://kauth.kakao.com/oauth/authorize`

.

**Step 2. 토큰 받기**

.

**Step 3. 사용자 로그인 처리**



```json
{
    id=2632890179, 
    connected_at=2023-01-22T08:17:54Z, 
    properties = {nickname=안창범}, 
    kakao_account = {
        profile_nickname_needs_agreement=false, 
        profile={nickname=안창범}, 
        has_email=true, 
        email_needs_agreement=false, 
        is_email_valid=true, 
        is_email_verified=true, 
        email=chb2005@naver.com
    }
}
```



## Google

[console.developers.google.com](https://console.developers.google.com/)에서 프로젝트를 생성합니다.
- (1) **사용자 인증 정보 메뉴**
- (2) **사용자 인증 정보 -> 사용자 인증 정보 만들기 -> OAuth 클라이언트 ID -> OAuth 동의 화면 구성**
  - OAuth 동의 화면
  - 범위
  - 테스트 사용자
  - 요약
- (3) **사용자 인증 정보 만들기 -> OAuth 클라이언트 ID 만들기**
  - 애플리케이션 유형
  - 이름
  - 승인된 자바스크립트 원본: `http://localhost:8080`
  - 승인된 리디렉션 URI: `http://localhost:8080/login/oauth2/code/google`







https://inkyu-yoon.github.io/docs/Language/SpringBoot/OauthLogin#7-oauth2successhandler-%EC%84%A4%EC%A0%95









.

**Reference**
- [Using OAuth 2.0 to Access Google APIs](https://developers.google.com/identity/protocols/oauth2)
- [Setting up OAuth 2.0](https://support.google.com/cloud/answer/6158849?hl=ko)



## Apple


- apple
  - https://shxrecord.tistory.com/289
  - https://2bmw3.tistory.com/22



- 참고.
  - [OAuth 2.0 로그인 (카카오, 네이버, 페이스북 로그인)](https://chb2005.tistory.com/183#6.3.%20GoogleUserInfo)
  - [OAuth 2.0 로그인 (구글 로그인)](https://chb2005.tistory.com/182)