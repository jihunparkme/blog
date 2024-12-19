# Oauth2 Sign in

# OAuth 2.0 Login(Kakao, Google, Apple)

## Kakao

카카오 로그인은 [카카오 API 문서]((https://developers.kakao.com/docs/latest/ko/kakaologin/common))에서 상세하게 설명되어 있지만, 적용 전에 연동하는 부분만 간략하게 정리해 보려고 합니다.

[카카오 로그인](https://developers.kakao.com/docs/latest/ko/kakaologin/prerequisite#kakao-login) 서비스 사용을 위해 [카카오 개발자 페이지](https://developers.kakao.com/)에서 애플리케이션을 먼저 등록합니다.
- (1) **내 어플리케이션 메뉴**로 이동
- (2) **애플리케이션 추가**하기 선택
- (3) 앱 아이콘, 앱 이름, 사업자명 입력
- (4) `내 애플리케이션` > `앱 설정` > `앱 키`에서 REST API 키를 사용할 예정
- (5) `내 애플리케이션` > `제품 설정` > `카카오 로그인`
  - 활성화 설정 **ON**
  - 필요 시 OpenID Connect 활성화 설정 **ON**
  - 카카오 로그인 진행 시 **Redirect URI** 등록
    - ex. `http://localhost:8080/login/oauth2/code/kakao`
- (6) `내 애플리케이션` > `제품 설정` > `카카오 로그인` > `보안`
  - Client Secret 코드 생성 및 활성 설정
- (7) `내 애플리케이션` > `앱 설정` > `플랫폼` > `Web 플랫폼 등록`
  - ex. `http://localhost:8080`
- (7) `내 애플리케이션` > `제품 설정` > `카카오 로그인` > `동의항목`
  - 로그인 시 가져올 정보 선택(일부 항목은 검수)

.




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

> [REST API 테스트 도구](https://developers.kakao.com/tool/rest-api/open/post/v1-user-logout)


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


```json
{
  "web": {
    "client_id": "123456678123-9shapubp16vmidk9mee67w4nxc4n06o94.apps.googleusercontent.com",
    "project_id": "aaron-123123",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    "client_secret": "ABCDEF-BV4vlK0-e3exFnAt4dovkeOiVir3",
    "redirect_uris": [
      "http://localhost:8080/login/oauth2/code/google"
    ],
    "javascript_origins": [
      "http://localhost:8080"
    ]
  }
}
```


.

- JWT 적용은 인프런 강의를 참고
  - [Spring Boot JWT Tutorial](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-jwt/dashboard)
- [Spring Security 소셜 로그인 로직 구현하기](https://inkyu-yoon.github.io/docs/Language/SpringBoot/OauthLogin)
- <https://velog.io/@gkrry2723/SpringBoot-OAuth2-%ED%99%9C%EC%9A%A9%ED%95%B4%EC%84%9C-Google-login-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B02-%EC%8A%A4%ED%94%84%EB%A7%81-%EC%8B%9C%ED%81%90%EB%A6%AC%ED%8B%B0-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0>
- <https://zkdlu.tistory.com/12>
- <https://whatistudy.tistory.com/entry/%EC%8B%A4%EC%8A%B5-%EC%8A%A4%ED%94%84%EB%A7%81-%EC%8B%9C%ED%81%90%EB%A6%AC%ED%8B%B0-OAuth2-Login-2>
- [[Spring Boot] OAuth 2.0 로그인 (카카오, 네이버, 페이스북 로그인)](https://chb2005.tistory.com/183#6.2.%20OAuth2UserInfo%20(interface))


- [🍪 프론트에서 안전하게 로그인 처리하기](https://velog.io/@yaytomato/%ED%94%84%EB%A1%A0%ED%8A%B8%EC%97%90%EC%84%9C-%EC%95%88%EC%A0%84%ED%95%98%EA%B2%8C-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%EC%B2%98%EB%A6%AC%ED%95%98%EA%B8%B0)

.

**Reference**
- [Using OAuth 2.0 to Access Google APIs](https://developers.google.com/identity/protocols/oauth2)
- [Setting up OAuth 2.0](https://support.google.com/cloud/answer/6158849?hl=ko)




## Apple




https://appleid.apple.com/auth/authorize?scope=name%20email&scope=name%20email&response_mode=form_post&response_type=code&client_id=net.dongsik.REON&state=tTN_CXov4I0KQ_jh3UJdV-jZYvIyqQsUJyPYdtItbUI%3D&redirect_uri=https://www.reonai.shop/login/oauth2/code/apple



https://appleid.apple.com/auth/authorize?client_id=com.reonaicoffee.services&redirect_uri=https%3A%2F%2Fwww.reonaicoffee.com%2Flogin%2Foauth2%2Fapple%2Fredirect&nonce=20R22E-0O4-0N4&response_type=code+id_token&scope=name+email&response_mode=form_post 



/**
 * state: null
 * code: c972c3853257040a4991f0ce1efabd445.0.rryus.EyPtDHR9_y1wD6Ev9lSbnQ
 * idToken: eyJraWQiOiJCaDZIN3JIVm1iIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoiY29tLnJlb25haWNvZmZlZS5zZXJ2aWNlcyIsImV4cCI6MTcwOTQ0OTAzNywiaWF0IjoxNzA5MzYyNjM3LCJzdWIiOiIwMDE4NDIuZmFkMGIzYzk1NTg0NDQ1MGFhNjM1OGYwNGE0ZjQyNDMuMTIyOCIsIm5vbmNlIjoiMjBCMjBELTBTOC0xSzgiLCJjX2hhc2giOiJHZ2h0R0h5SzlkaUdldVdpQWNMMFpBIiwiZW1haWwiOiJwbGs0NjIzQG5hdmVyLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdXRoX3RpbWUiOjE3MDkzNjI2MzcsIm5vbmNlX3N1cHBvcnRlZCI6dHJ1ZX0.ejM54W1fX24yukRmuTSohdtbL2uO_ieW--zgJML9PaiiaeEq9GfLwmY82K-IcIQxP3jEd_XKk7ZJ_pnhlS0N7U2vrxeA8CzsSPBzOtg6bFNqU1x5ILUP81-dRPsLBT4awFzfUZMZoEy4N6Dp3pK6NyFDtpxYyMwr_dhsjngu7Zdv08nv-nIuVcQuWnq7a4b-yRWBsWh4uiQANUBInVcNiMMxV_pk4xYcmYRJB3bcVQk7vbEz7aIaO2ObxOiGrkpXm1gQ5ei1BhnWfuKHS4dJTebg36UfvfoU_Y-czTineo4kER3Qx49YiL4r8FiCygYJDwRt_Bs8AmWNakCrT999Tg
 * user: null
 */




- https://whitepaek.tistory.com/60
- https://whitepaek.tistory.com/61








https://shxrecord.tistory.com/289
https://tyrannocoding.tistory.com/66



https://mingg123.tistory.com/262
https://devcheon.tistory.com/98
https://velog.io/@ddonghyeo_/Spring-%EC%95%A0%ED%94%8C-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0
https://hwannny.tistory.com/71
https://2bmw3.tistory.com/23
  - https://shxrecord.tistory.com/289
  - https://2bmw3.tistory.com/22



- 참고.
  - [OAuth 2.0 로그인 (카카오, 네이버, 페이스북 로그인)](https://chb2005.tistory.com/183#6.3.%20GoogleUserInfo)
  - [OAuth 2.0 로그인 (구글 로그인)](https://chb2005.tistory.com/182)










## Dependency

```groovy
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
```


DevSecurityConfig 따라가면서..

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .userDetailsService(userDetailsService)
            .csrf(AbstractHttpConfigurer::disable) // token 을 사용하므로 csrf disable

            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptionHandling -> exceptionHandling
                    .accessDeniedHandler(jwtAccessDeniedHandler)
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )

            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                    .requestMatchers(
                            new AntPathRequestMatcher("/"),
                            new AntPathRequestMatcher("/css/**"),
                            new AntPathRequestMatcher("/img/**"),
                            new AntPathRequestMatcher("/js/**"),
                            new AntPathRequestMatcher("/vendor/**"),

                            new AntPathRequestMatcher("/profile"),
                            new AntPathRequestMatcher("/management/actuator/health"),

                            new AntPathRequestMatcher("/login/**"),
                            new AntPathRequestMatcher("/member/**"),
                            new AntPathRequestMatcher("/record/**")
                    ).permitAll()
                    .requestMatchers(
                            new AntPathRequestMatcher("/admin/**"),
                            new AntPathRequestMatcher("/management/actuator/**")
                    ).hasAuthority(Role.ADMIN.name())
                    .requestMatchers(PathRequest.toH2Console()).permitAll()
                    .anyRequest().authenticated()
            )

            .formLogin(formLoginConfigurer -> formLoginConfigurer
                    .loginPage("/login")
                    .successForwardUrl("/"))

            .logout((logout) ->
                    logout.deleteCookies(AuthConst.ACCESS_TOKEN)
                            .invalidateHttpSession(false)
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/")
            )

            .oauth2Login(oauth2 -> // oauth2 로그인 기은 설정
                    oauth2.userInfoEndpoint(userInfo -> // oauth2 로그인 성공 이후 사용자 정보 조회 설정
                            userInfo.userService(customOauth2UserService)) // 사용자 정보 조회 이후 기능
                            .successHandler(oauth2SuccessHandler)
                            .failureHandler(oauth2FailureHandler))

            .sessionManagement(sessionManagement -> // 세션 미사용 설정
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .headers(headers -> // h2 console 사용을 위한 설정
                    headers.frameOptions(options ->
                            options.sameOrigin()
                    )
            )

            .apply(new JwtSecurityConfig(tokenProvider));

    return http.build();
}
```





