# Using JWT(JSON Web Tokens) in Java

JWT 를 업무에 사용해야 하는 일이 생겼는데, 알고는 써야지 생각을 해서..

JWT 생성과 복호화과정을 간략하게 살펴보려고 한다.

## Token Authentication

- 토큰 인증은 서버 측 세션 ID에서 해결할 수 없는 문제 해결을 위해 개발
  - 서버 로드 감소, 권한 관리 간소화 등의 장점
- 기존 인증과 마찬가지로 사용자는 확인 가능한 자격 증명을 세션 ID 대신 토큰 세트로 제공
- 토큰은 사이트 간 요청 위조(CSRF) 보호 체계, OAuth 2.0 상호 작용, 세션 ID 및 인증 표현(쿠키 내)을 포함한 다양한 응용 프로그램 제공

## JWT

- JSON 형식 웹 토큰
- URL에 안전하고, 인코딩되고, 암호화된 문자열로 다양한 응용 프로그램에서 토큰으로 사용 가능
- 마침표(.)로 구분된 세 개의 정보(`Header`, `Payload`, `Signature`)가 base64 URL로 인코딩되어 URL에서 안전하게 사용 가능
  
```text
Header: 
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.

Payload: eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.

Signature: 
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### Header

base64를 사용하여 헤더를 디코딩하면 아래 JSON 문자열 획득

```json
{
  "typ": "JWT",
  "alg": "HS256"
}
```

### Payload

base64를 사용하여 페이로드를 디코딩하면 아래 JSON 문자열 획득

```json
{
  "iss": "issuer",
  "sub": "subject",
  "name": "Aaron Park",
  "scope": "common",
  "iat": 1682489134,
  "exp": 1690265134
}
```

페이로드 안에는 값을 가진 다양한 키(클레임)들이 있는데, JWT 사양에는 7가지의 기본 클레임이 존재(원하는 클레임 추가 가능)
- iss | Issuer
- sub | Subject
- aud | Audience
- exp | Expiration
- nbf | Not Before
- iat | Issued At
- jti | JWT ID

## Dependency

JWT 암/복호화 작업을 위해 아래 의존성이 필요

```grooby
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
```

## Generate a JWT Token

아래 순서로 JWT 토큰 생성

1.토큰의 내부 클레임 정의
2.JWT의 암호 서명
3.JWT Compact Serialization 규칙에 따른 URL-safe 문자열에 대한 JWT 압축

빌드 결과로 특정 시그니처 알고리즘으로 서명되고, Base64로 인코딩된 세 섹션(`Header`, `Payload`, `Signature`)의 문자열 획득

```java
@Test
void generate_jwt_token() throws Exception {
    String key = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";
    SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));
    Instant now = new Date().toInstant();

    String jws = Jwts.builder()
            // 2. Signature
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .setHeaderParam("typ", "JWT")
            // 1. Payload claim
            .setIssuer("issuer")
            .setSubject("subject")
            .claim("name", "Aaron Park")
            .claim("scope", "common")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(90, ChronoUnit.DAYS)))
            // 3. JWT compact
            .compact();

    log.info("jws: {}", jws);
}
```

## Decode a JWT Token

```java
@Test
void decode_jwt_token() throws Exception {
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiJzdWJqZWN0IiwibmFtZSI6IkFhcm9uIFBhcmsiLCJzY29wZSI6ImNvbW1vbiIsImlhdCI6MTY4MjQ4OTEzNCwiZXhwIjoxNjkwMjY1MTM0fQ.DTgVJWwKGlh1Qn2j5SuzPQjoMAvbxrKztl4YNcgYI7w";
    
    // 토큰을 각 섹션(Header, Payload, Signature)으로 분할
    String[] chunks = token.split("\\.");
    Base64.Decoder decoder = Base64.getUrlDecoder();

    String header = new String(decoder.decode(chunks[0]));
    String payload = new String(decoder.decode(chunks[1]));

    Assertions.assertEquals("{\"typ\":\"JWT\",\"alg\":\"HS256\"}", header);
    Assertions.assertEquals("{\"iss\":\"issuer\",\"sub\":\"subject\",\"name\":\"Aaron Park\",\"scope\":\"common\",\"iat\":1682489134,\"exp\":1690265134}", payload);
}
```

long 타입으로 표현된 날짜를 Date 타입으로 변환

```java
@Test
void jwt_payload_exp_to_date() throws Exception {
    long expiredDateLong = 1690265134;
    Date expireDate = Date.from(Instant.ofEpochSecond(expiredDateLong));
    String expireDateStr = new SimpleDateFormat("yyyy/MM/dd").format(expireDate);

    Assertions.assertEquals("Tue Jul 25 15:05:34 KST 2023", expireDate.toString());
    Assertions.assertEquals("2023/07/25", expireDateStr);

    LocalDate localDate = Instant.ofEpochSecond(expiredDateLong).atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate expire7DaysBefore = localDate.minusDays(7);

    Assertions.assertEquals("2023-07-25", localDate.toString());
    Assertions.assertEquals("2023-07-18", expire7DaysBefore.toString());
}
```

## Verifying JWT

시그니쳐 섹션을 사용하여 헤더와 페이로드의 무결성을 확인하여 변경되지 않았는지 확인

```java
@Test
void verifying_jwt() throws Exception {
    final String key = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";
    final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiJzdWJqZWN0IiwibmFtZSI6IkFhcm9uIFBhcmsiLCJzY29wZSI6ImNvbW1vbiIsImlhdCI6MTY4MjQ4OTEzNCwiZXhwIjoxNjkwMjY1MTM0fQ.DTgVJWwKGlh1Qn2j5SuzPQjoMAvbxrKztl4YNcgYI7w";

    SignatureAlgorithm sa = SignatureAlgorithm.HS256;
    SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), sa.getJcaName());

    String[] chunks = token.split("\\.");
    String tokenWithoutSignature = chunks[0] + "." + chunks[1];
    String signature = chunks[2];

    DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);
    Assertions.assertTrue(validator.isValid(tokenWithoutSignature, signature));
}
```

## Code

전체 코드

```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Slf4j
public class JWTTokenTest {

    @Test
    void generate_jwt_token() throws Exception {
        String key = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";
        SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes("UTF-8"));
        Instant now = new Date().toInstant();

        String jws = Jwts.builder()
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .setHeaderParam("typ", "JWT")
                .setIssuer("issuer")
                .setSubject("subject")
                .claim("name", "Aaron Park")
                .claim("scope", "common")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(90, ChronoUnit.DAYS)))
                .compact();

        log.info("jws: {}", jws);
    }

    @Test
    void decode_jwt_token() throws Exception {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiJzdWJqZWN0IiwibmFtZSI6IkFhcm9uIFBhcmsiLCJzY29wZSI6ImNvbW1vbiIsImlhdCI6MTY4MjQ4OTEzNCwiZXhwIjoxNjkwMjY1MTM0fQ.DTgVJWwKGlh1Qn2j5SuzPQjoMAvbxrKztl4YNcgYI7w";
        String[] chunks = token.split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));

        Assertions.assertEquals("{\"typ\":\"JWT\",\"alg\":\"HS256\"}", header);
        Assertions.assertEquals("{\"iss\":\"issuer\",\"sub\":\"subject\",\"name\":\"Aaron Park\",\"scope\":\"common\",\"iat\":1682489134,\"exp\":1690265134}", payload);
    }

    @Test
    void jwt_payload_exp_to_date() throws Exception {
        long expiredDateLong = 1690265134;
        Date expireDate = Date.from(Instant.ofEpochSecond(expiredDateLong));
        String expireDateStr = new SimpleDateFormat("yyyy/MM/dd").format(expireDate);

        Assertions.assertEquals("Tue Jul 25 15:05:34 KST 2023", expireDate.toString());
        Assertions.assertEquals("2023/07/25", expireDateStr);

        LocalDate localDate = Instant.ofEpochSecond(expiredDateLong).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate expire7DaysBefore = localDate.minusDays(7);

        Assertions.assertEquals("2023-07-25", localDate.toString());
        Assertions.assertEquals("2023-07-18", expire7DaysBefore.toString());
    }

    @Test
    void verifying_jwt() throws Exception {
        final String key = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";
        final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiJzdWJqZWN0IiwibmFtZSI6IkFhcm9uIFBhcmsiLCJzY29wZSI6ImNvbW1vbiIsImlhdCI6MTY4MjQ4OTEzNCwiZXhwIjoxNjkwMjY1MTM0fQ.DTgVJWwKGlh1Qn2j5SuzPQjoMAvbxrKztl4YNcgYI7w";

        SignatureAlgorithm sa = SignatureAlgorithm.HS256;
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), sa.getJcaName());

        String[] chunks = token.split("\\.");
        String tokenWithoutSignature = chunks[0] + "." + chunks[1];
        String signature = chunks[2];

        DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);
        Assertions.assertTrue(validator.isValid(tokenWithoutSignature, signature));
    }
}
```

## Reference

[JWT Decode Test](https://jwt.io/)

[Supercharge Java Authentication with JSON Web Tokens](https://www.baeldung.com/java-json-web-tokens-jjwt)

[Decode a JWT Token in Java](https://www.baeldung.com/java-jwt-token-decode)