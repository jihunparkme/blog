# SpringBoot Security

[Spring Security Docs](https://docs.spring.io/spring-security/site/docs/5.4.6/reference/html5/)

[Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture#_web_security)

## Dependency

- springBoot Security 사용을 위한 의존성

- thymeleaf에서 security 사용을 위한 의존성

```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity5'
```

## Config

Spring Security 관련

`*/config/SecurityConfig.java`

```java
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // Spring Security 설정 클래스로 등록
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private MemberService memberService;

    /**
     * 비밀번호 암호화를 위한 Bean
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security 설정
     * @param web FilterChainProxy 생성 필터
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception
    {
        // Spring Security가 인증을 무시할 경로 설정
        web.ignoring().antMatchers("/css/**", "/img/**", "/js/**", "/lib/**", "/vendor/**");
    }

    /**
     * Security 설정
     * @param http HTTP 요청에 대한 보안 구성
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // 페이지 권한 설정
                .antMatchers("/info").hasRole("MEMBER") // MEMBER, ADMIN만 접근 허용
                .antMatchers("/admin").hasRole("ADMIN") // ADMIN만 접근 허용
                .antMatchers("/**").permitAll() // 그외 모든 경로에 대해서는 권한 없이 접근 허용
                // .anyRequest().authenticated() // 나머지 요청들은 권한의 종류에 상관 없이 권한이 있어야 접근 가능
            .and() // 로그인 설정
                .formLogin()
                    .loginPage("/user/login") // Custom login form 사용
                    .failureUrl("/login-error") // 로그인 실패 시 이동
                    .defaultSuccessUrl("/") // 로그인 성공 시 redirect 이동
            .and() // 로그아웃 설정
                .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // 로그아웃 시 URL 재정의
                    .logoutSuccessUrl("/") // 로그아웃 성공 시 redirect 이동
                    .invalidateHttpSession(true) // HTTP Session 초기화
                    .deleteCookies("JSESSIONID") // 특정 쿠키 제거
            .and()
                // 403 예외처리 핸들링
                .exceptionHandling().accessDeniedPage("/denied");
    }

    /**
     * Spring Security 인증
     * AuthenticationManagerBuilder를 사용하여 AuthenticationManager 생성
     * @param auth
     * @throws Exception
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }
}
```

## Controller

`*/login/controller/MemberController.java`

```java
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class MemberController {
    private MemberService memberService;

    /**
     * 로그인 페이지 이동
     * @return
     */
    @GetMapping("/user/login")
    public String dispLogin() {
        return "login/login";
    }

    /**
     * 로그인 에러
     * @param model
     * @return
     */
    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);

        return "/login/login";
    }

    /**
     * 회원가입 페이지 이동
     * @return
     */
    @GetMapping("/signup")
    public String dispSignup() {
        return "login/signup";
    }

    /**
     * 회원가입 처리
     * @param memberDto
     * @return
     */
    @PostMapping("/signup")
    public String execSignup(MemberDto memberDto) {
        memberService.joinUser(memberDto);

        return "redirect:/user/login";
    }

    /**
     * 접근 거부 페이지 이동
     * @return
     */
    @GetMapping("/denied")
    public String dispDenied() {
        return "login/denied";
    }

    /**
     * 내 정보 페이지 이동
     * @return
     */
    @GetMapping("/info")
    public String dispMyInfo() {
        return "login/myinfo";
    }

    /**
     * Admin 페이지 이동
     * @return
     */
    @GetMapping("/admin")
    public String dispAdmin() {
        return "login/admin";
    }
}

```

## Entity

`*/login/domain/Member.java`

```java
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "member")
public class Member implements UserDetails {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "auth")
    private String auth;

    @Builder
    public Member(String email, String password, String auth) {
        this.email = email;
        this.password = password;
        this.auth = auth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> roles = new HashSet<>();
        for (String role : auth.split(",")) {
            roles.add(new SimpleGrantedAuthority(role));
        }
        return roles;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

```

## Role

권한 Class

`*/login/domain/Role.java`

```java
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    ADMIN("ROLE_ADMIN"),
    MEMBER("ROLE_MEMBER");

    private String value;
}
```

## Dto

`*/login/dto/MemberDto.java`

```java
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MemberDto {
    private String email;
    private String password;
    private String auth;

    public Member toEntity(){
        return Member.builder()
                .email(email)
                .password(password)
                .auth(auth)
                .build();
    }

    @Builder
    public MemberDto(String email, String password, String auth) {
        this.email = email;
        this.password = password;
        this.auth = auth;
    }
}
```

## Repository

`*/login/repository/MemberRepository.java`

```java
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String userEmail);
}

```

## Service

`*/login/service/MemberService.java`

```java
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {
    private MemberRepository memberRepository;

    /**
     * 회원가입 처리
     * @param memberDto
     * @return
     */
    @Transactional
    public Long joinUser(MemberDto memberDto) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // 비밀번호 암호화 처리
        memberDto.setPassword(passwordEncoder.encode(memberDto.getPassword()));

        return memberRepository.save(memberDto.toEntity()).getId();
    }

    /**
     * 상세 정보 조회
     * Security 지정 서비스이므로 필수 구현
     * @param email
     * @return 사용자의 계정정보와 권한을 갖는 UserDetails 인터페이스 반환
     * @throws UsernameNotFoundException
     */
    @Override
    public Member loadUserByUsername(String email) throws UsernameNotFoundException {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException((email)));
    }
}

```

---

## View

`src/main/resources/templates/index.html`

```html
<!DOCTYPE html>
<html
  lang="ko"
  xmlns:th="http://www.thymeleaf.org"
  xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity5"
>
  <head>
    <meta charset="UTF-8" />
    <title>Main</title>
  </head>
  <body>
    <h1>This is Main Page.</h1>
    <hr />
    <div sec:authorize="isAuthenticated()">
      <span sec:authentication="name"></span>님 환영합니다.
    </div>
    <!-- 익명의 사용자 -->
    <a sec:authorize="isAnonymous()" th:href="@{/user/login}">로그인</a>
    <a sec:authorize="isAnonymous()" th:href="@{/signup}">회원가입</a>

    <!-- 인증된 사용자 -->
    <a sec:authorize="isAuthenticated()" th:href="@{/logout}">로그아웃</a>

    <!-- 특정 권한의 사용자 -->
    <a sec:authorize="hasRole('ROLE_MEMBER')" th:href="@{/info}">내정보</a>
    <a sec:authorize="hasRole('ROLE_ADMIN')" th:href="@{/admin}">어드민</a>
  </body>
</html>
```

`src/main/resources/templates/login/login.html`

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.w3.org/1999/xhtml">
  <head>
    <meta charset="UTF-8" />
    <title>Login</title>
  </head>
  <body>
    <h1>This is Login Page.</h1>
    <hr />
    <!-- Security config의 loginPage("url")와 action url과 동일하게 작성-->
    <form action="/user/login" method="post">
      <!-- Spring Security가 적용되면 POST 방식으로 보내는 모든 데이터는 csrf 토큰 값이 필요 -->
      <input
        type="hidden"
        th:name="${_csrf.parameterName}"
        th:value="${_csrf.token}"
      />
      <p th:if="${loginError}" class="error">Wrong user or password</p>
      <!-- 로그인 시 아이디의 name 애트리뷰트 값은 username -->
      <!-- 파라미터명을 변경하고 싶을 경우 config class formlogin()에서 .usernameParameter("") 명시 -->
      <input type="text" name="username" placeholder="이메일 입력해주세요" />
      <input type="password" name="password" placeholder="비밀번호" />
      <button type="submit">로그인</button>
    </form>
  </body>
</html>
```

`src/main/resources/templates/login/signup.html`

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.w3.org/1999/xhtml">
  <head>
    <meta charset="UTF-8" />
    <title>Signup</title>
  </head>
  <body>
    <h1>This is Signup Page.</h1>
    <hr />

    <form th:action="@{/signup}" method="post">
      <input type="text" name="email" placeholder="이메일 입력해주세요" />
      <input type="password" name="password" placeholder="비밀번호" />
      <input type="radio" name="auth" value="ROLE_ADMIN,ROLE_MEMBER" /> admin
      <input type="radio" name="auth" value="ROLE_MEMBER" checked="checked" />
      member <br />
      <button type="submit">가입하기</button>
    </form>
  </body>
</html>
```

`src/main/resources/templates/login/myinfo.html`

```html
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>MyInfo</title>
  </head>
  <body>
    <h1>This is MyInfo Page.</h1>
    <hr />
  </body>
</html>
```

`src/main/resources/templates/login/admin.html`

```html
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>Admin</title>
  </head>
  <body>
    <h1>This is Admin Page.</h1>
    <hr />
  </body>
</html>
```

`src/main/resources/templates/login/denied.html`

```html
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>denied</title>
  </head>
  <body>
    <h1>This is denied Page.</h1>
    <hr />
  </body>
</html>
```

---

### reference

> https://victorydntmd.tistory.com/328
>
> https://shinsunyoung.tistory.com/78
>
> https://goodteacher.tistory.com/269?category=828441
>
> https://xmfpes.github.io/spring/spring-security/
>
> [Thymeleaf + Spring Security integration basics](https://www.thymeleaf.org/doc/articles/springsecurity.html)
>
> [Spring Security 로그인 후 이전 페이지로 이동](http://chomman.github.io/blog/java/spring%20security/programming/spring-security-redirect-previous-after-login/)
>
> [Spring Security Logout](https://www.baeldung.com/spring-security-logout)
