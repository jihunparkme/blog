# Papago 번역 API 적용기

Papago 번역 API를 애플리케이션에 적용해 보고자 한다. 👏🏼

.

참고로 Papago 번역 API는 일 허용량이 10,000 글자이다.

궁금하진 않겠지만.. 어떤 애플리케이션인지는.. 비밀🤫

.

네이버는 API를 쉽게 적용할 수 있도록 잘 설명이 되어 있는 레퍼런스를 제공해 주어서 사실 이 글은 무의미할 수도 있다.. 그래도 기록은 해야지..ㅋ_ㅋ..

[Papago 번역 개요](https://developers.naver.com/docs/papago/papago-nmt-overview.md)

.

우선 네이버 개발자 센터에서 애플리케이션을 등록이 필요한데,
애플리케이션을 등록은 매우 간단하다.

## 애플리케이션 등록

1. 애플리케이션 등록
- 네이버 개발자 센터 상단 `Application` -> `애플리케이션 등록` -> `애플리케이션 등록 (API 이용신청)` 페이지
- `애플리케이션 이름`, `사용 API`, `비로그인 오픈 API 서비스 환경` 입력

2. 애플리케이션 등록 확인
- 네이버 개발자 센터 상단 `Application` -> `내 애플리케이션` -> `애플리케이션`에 등록된 Papago 번역 애플리케이션 정보
- **Client ID**/**Client Secret** 확인
  - API 요청 시 HTTP 요청 헤더에 필요하다.

[Papago 번역 API Reference](https://developers.naver.com/docs/papago/papago-nmt-api-reference.md)

## 적용

친절하게 구현 예제까지 제공해 주어서.. 사실 그냥 가져다 쓰면 된다..

Java, PHP, Node.js, Python, C# 별로 예제 코드를 제공해 준다.

[Papago 번역 API 구현 예제](https://developers.naver.com/docs/papago/papago-nmt-example-code.md)

.

Java 코드의 일부를 보자면..

여기서는 `clientId`, `clientSecret` 가 노출되어 있지만,

이 부분은 당연하게 properties 에 설정 후 사용해야 한다.

.

추가로, ObjectMapper를 통해서 String 이 아닌 Object 상태로 리턴받을 수도 있다.

**PapagoApiTest.java**

```java
public class PapagoApiTest {

    @Value("${translator.papago.url}")
    private String url;

    @Value("${translator.papago.client-id}")
    private String clientId;

    @Value("${translator.papago.client-secret}")
    private String clientSecret;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void test() throws Exception {
        String text;
        try {
            text = URLEncoder.encode("안녕하세요. 오늘 기분은 어떻습니까?", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("인코딩 실패", e);
        }

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        String response = post(url, requestHeaders, text);
        TranslatorResponse responseBody = mapper.readValue(response, TranslatorResponse.class);

        assertThat(responseBody.getTranslatedText()).isEqualTo("Hello. How are you today?");
    }
}
```

**TranslatorResponse.java**

```java
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranslatorResponse {
    private Message message;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static  class Message {
        private Result result;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Result {
        private String srcLangType;
        private String tarLangType;
        private String translatedText;
        private String engineType;
        private String pivot;
        private String dict;
        private String tarDict;
    }

    public String getTranslatedText() {
        return getMessage().getResult().getTranslatedText();
    }
}
```

[구글 번역 API](https://cloud.google.com/translate/pricing?hl=ko) 는 월 500,000자 까지 사용이 가능하지만, 이후에는 백만 자당 $20가 부과된다.

구글 번역 API 도 적용은 해봐야G..👋🏼