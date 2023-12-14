# 슬랙봇으로 개발 생산성 높이기 with Slack RTM API

대부분의 회사에서 상용 데이터베이스(이하 DB)에 접근하려면 보안을 위해 몇 단계의 절차를 거치게 됩니다.<br/>
보통 아래와 같은 단계를 거쳐서 DB에 접근하고 계실 것으로 생각됩니다.<br/> 
**가상 데스크톱 원격 접속(로그인)** ➡ **DB 접근제어 솔루션 실행(로그인)** ➡ **DB 관리 툴 실행** ➡ **DB 연결**

개발을 하면서 때때로 간단한 프로퍼티를 조회하기 위해 매번 **번거로운 상용 DB 접근 절차**를 거치게 되는데요.<br/>
일정 시간이 지나면 보안상 세션이 빠르게 풀리다 보니 **다시 동일한 절차를 반복**적으로 거치게 되면서 **비효율적인 상황을 감지**하게 되었습니다.

**개발 생산성을 높이기 위해** 프로퍼티를 조회하는 절차를 효율적으로 개선해 보자는 생각을 시작으로 **공통 코드 조회 슬랙봇(WhatIsThisCode)**이 만들어지게 되었고, 이번 글에서는 그 생성 과정을 공유해 드리고자 합니다.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image01.png" width="300" height="300"></center>

## 들어가기

슬랙봇을 만드는 방법은 정말 다양했습니다.<br/>
그중에서도 저에게는 아래와 같은 조건을 만족하는 방법이 필요했습니다.
- **API를 외부(Slack)에 오픈하지 않고 슬랙봇을 동작**시킬 수 있는가?
- 구현 환경에서 **방화벽 이슈**가 발생하지 않는가?
- 슬랙봇이 **실시간으로 이벤트를 수신**하고 메시지를 보낼 수 있는가?

다행히도 슬랙은 이러한 조건을 만족하며 슬랙봇을 만들 수 있는 `Real Time Messaging API`를 제공해 주고 있었습니다.

[Real Time Messaging](https://slack.dev/node-slack-sdk/rtm-api)은 줄여서 `RTM` 으로도 불리고, 이름처럼 실시간으로 이벤트를 수신하고 메시지를 보낼 수 있는 **WebSocket 기반의 API**입니다.<br/>
`RTM`은 [API 문서](https://slack.dev/node-slack-sdk/rtm-api)를 보면 최신 스코프 앱에서는 `RTM`을 더 이상 사용할 수 없지만, 방화벽과 같은 **구현 환경 제한**으로 인해 사용이 필요할 경우 **레거시 스코프 앱**으로 생성하여 사용하도록 안내하고 있습니다. (추가로, `RTM` 사용을 위해 **최신 스코프 앱으로 업데이트하지 말 것**을 안내하고 있습니다.)

> 참고. 스코프란 슬랙앱이 작동하는 데 필요한 권한 범위

만일 구현 환경에 제한이 없다면 [Events API](https://slack.dev/node-slack-sdk/events-api) 또는 [Web API](https://slack.dev/node-slack-sdk/web-api) 사용을 권장하고 있습니다.

```text
Note: RTM isn’t available for modern scoped apps anymore. We recommend using the Events API and Web API instead. 
If you need to use RTM (possibly due to corporate firewall limitations), you can do so by creating a legacy scoped app. 
If you have an existing RTM app, do not update its scopes as it will be updated to a modern scoped app and stop working with RTM.
```

구현 환경에 대한 조건을 만족하는 선에서 안전하게 슬랙봇을 만들어야 했기 때문에 **`Real Time Messaging API`를 활용**하여 **`Python` 언어**로 슬랙봇을 만들기로 결정하게 되었습니다.

---

## 슬랙 앱 생성하기

Slack App을 생성할 때 기본적으로 최신 스코프 앱([Create New App](https://api.slack.com/apps))으로 생성하게 됩니다.<br/>
**`Real Time Messaging API` 적용**을 위해서는 최신 스코프 앱을 생성하는 것이 아닌 **클래식 앱([Create Classic App](https://api.slack.com/apps?new_classic_app=1))으로 생성**해 주어야 합니다.

`Python Slack SDK`의 [RTM Client](https://slack.dev/python-slack-sdk/real_time_messaging.html#real-time-messaging-rtm) 문서를 보면 아래와 같이 RTM API 사용을 위한 `classic Slack app` 생성과 관련된 내용을 확인할 수 있습니다.

```text
Configuring the RTM API

Events using the RTM API must use a classic Slack app (with a plain bot scope).

If you already have a classic Slack app, you can use those credentials. 
If you don’t and need to use the RTM API, you can create a classic Slack app. You can learn more in the API documentation.

Also, even if the Slack app configuration pages encourage you to upgrade to the newer permission model, don’t upgrade it and keep using the “classic” bot permission.
```

- RTM API를 사용하는 이벤트는 `classic Slack App`을 사용해야 한다.
- 이미 `classic Slack App`이 있다면 그 자격 증명을 사용할 수 있고, [create a classic Slack app](https://api.slack.com/apps?new_classic_app=1)으로 새로 생성할 수 있다.

.

**Create a Classic Slack App**

[Create a Slack App (Classic)](https://api.slack.com/apps?new_classic_app=1) 으로 접근하면 **레거시 스코프 앱 생성을 위한 팝업**이 나타나게 됩니다.<br/>
앱 이름을 지어주고, 슬랙 워크스페이스를 선택합니다.<br/>
해당 팝업에서 Slack App을 생성하게 되면 들어가기에서 말씀드린 **레거시 스코프 앱으로 생성**됩니다.<br/>

![Create a Classic Slack App](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image02.png)

.

**Add Bot function**

사용자가 Slack App과 메시지를 교환할 수 있도록 **봇 기능을 추가**합니다.<br/>
피노키오가 말하고 움직일 수 있도록 생명을 불어넣어 준 요정이 되어볼 수 있는 기회입니다!

![출처: https://blog.naver.com/sisujjang/221587402692](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image04.jpg)

`Settings` ➡ `Basic Information` ➡ `Building Apps for Slack` ➡ `Add features and functionality` 경로로 이동해서 `Bots` 버튼을 눌러줍니다.

![Add Bots function](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image03.png)

.

**Add a legacy bot user**

`Bots` 버튼을 누르면 `Features` ➡ `App Home` 탭으로 이동하게 되는데, 레거시 스코프를 이용하기 위해<br/>
`First, add a legacy bot user` 영역에 있는 `Add Legacy Bot User` 버튼을 눌러줍니다.

![Add a legacy bot user](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image05.png)

.

**Add App Display Name**

봇 이름을 멋지게 지어주고, 기본 사용자 이름도 작성해 줍니다.

![Add App Display Name](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image06.png)

`Add` 버튼을 누르면 생성한 Slack App이 레거시 봇 유저를 사용하고 있다는 문구와 함께 봇 정보가 보이게 됩니다.<br/>
스코프를 업데이트하라는 안내도 나와 있지만 들어가기에서 언급하였듯이 **RTM 사용을 위해 최신 스코프 앱으로 업데이트하지 않는 것**을 권장하고 있습니다.

![App](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image07.png)

.

**Scopes**

생성한 봇의 스코프를 확인하고 싶을 경우 `Features` ➡ `OAuth & Permissions` ➡ `Scopes` 탭에서 확인할 수 있습니다.

![Scopes](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image08.png)

봇 기능을 이미 추가했기 때문에 bot Scope가 자동으로 적용되었습니다.<br/>
bot Scope에는 **기본적으로 RTM API에 필요한 스코프들이 포함**되어 있습니다.<br/>
개발을 진행하면서 필요한 스코프가 생긴다면 [Permission scopes](https://api.slack.com/scopes)를 참고하여 추가할 수 있습니다.

.

**Messages Tab**

`Features` ➡ `App Home` ➡ `Show Tabs` ➡ `Messages Tab`

Messages Tab이 활성화되어야 슬랙앱에서 보내는 메시지가 슬랙 메시지 탭에 표시될 수 있습니다.<br/>
추가로 **사용자도 메시지 탭을 통해 메시지를 보낼 수 있도록 허용**해 줍니다.<br/>
☑ Allow users to send Slash commands and messages from the messages tab

![Messages Tab](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image09.png)

비활성화가 되어 있다면 아래 안내 문구가 노출되는데,

```text
The Message tab requires read/write scopes such as im:history, im:write, and/or im:read

Direct messages your app sends will show in this tab.
```

스코프에 `im:history`, `im:write`, `im:read`가 포함되어 있다면 자동으로 활성화 상태가 됩니다.<br/>
봇 기능을 추가하고 자동으로 적용된 `bot` 스코프에는 **기본적으로 RTM API에 필요한 스코프들이 포함**되어 있습니다.

처음에 슬랙봇에게 메시지를 보낼 수 없는 상태가 지속되어 찾게 된 글인데 참고차 링크를 남겨두겠습니다.<br/>
[Sending messages to this app has been turned off.](https://github.com/MicrosoftDocs/bot-docs/issues/2077#issuecomment-843960695)

.

**Install your app**

마지막으로 워크스페이스에 지금까지 만든 슬랙앱을 인스톨하는 단계입니다.<br/>
앱을 슬랙 워크스페이스에 인스톨하면 **앱을 인증할 수 있는 토큰이 자동으로 생성**됩니다.

`Settings` ➡ `Basic Information` ➡ `Building Apps for Slack` ➡ `Install your app` 에서 인스톨할 수 있습니다.

![Install your app](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image10.png)

인스톨이 완료되면 `Settings` ➡ `Basic Information` ➡ `App Credentials` 에서 **Credentials 정보를 확인**할 수 있습니다.

![App Credentials](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image11.png)

앱의 Credentials 정보와 토큰 정보는 **외부에 노출되지 않도록 주의**해야 합니다.

.

**OAuth Token**

`Features` ➡ `OAuth & Permissions` ➡ `OAuth Tokens for Your Workspace`

슬랙앱 인스톨이 완료되면 자동으로 두 종류의 `OAuth Token`이 생성됩니다.
- User OAuth Token
- Bot User OAuth Token

**`RTMClient module`**(RTM API와 상호작용하기 위한 Python module)에는 두 토큰 중 **`Bot User OAuth Token`**이 사용될 예정입니다.

---

## 동작 방식

`WhatIsThisCode` 슬랙봇을 살짝 보여드리면 **특정 공통 코드를 슬랙봇에게 요청하면 해당 공통 코드에 대한 정보들을 응답**해 주고 있습니다.

![](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image13.png)

`WhatIsThisCode` 슬랙봇의 대략적인 동작 방식은 아래와 같습니다.

> 1) 사용자가 슬랙봇에게 조회하고 싶은 **공통 코드를 요청**
> 
> 2) 슬랙봇은 RTMClient module을 통해 사용자로부터 요청이 들어온 것을 인식하고, **입력값을 공통 코드 조회 API에 전달**
> 
> 3) API는 공통 코드 정보를 조회하여 **슬랙봇에게 결과 응답**
> 
> 4) 슬랙봇은 API 응답 데이터를 정제한 후 RTMClient module의 chat_postMessage 메서드를 통해 **사용자에게 결과 응답**
> 
> 5) 사용자는 번거로운 상용 DB 접근 절차 없이 **빠르고 간편하게 공통 코드 정보 조회**

다음으로, 슬랙봇이 사용자의 요청에 똑똑하게 응답할 수 있도록 조회용 API를 만들어 보겠습니다.

### Create Rest API For Slack Bot

문자열을 응답하는 간단한 API를 만들어서 테스트해 보겠습니다.

**Controller**

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/slack-bot")
public class SlackBotController {

    private final SlackBotService slackBotService;

    @GetMapping(value = "/do-something/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BasicResponse doSomething(@PathVariable("key") String key) {
        String response = slackBotService.doSomething(key);
        return BasicResponse.ok(List.of(response));
    }
}
```

- 사용자가 슬랙봇에게 `AB123` 라는 공통 코드를 입력하면 슬랙봇은 `http://localhost:8080/slack-bot/do-something/AB123` 형태로 API 요청을 하게 되고, SlackBotController에서 해당 API를 처리하게 됩니다.
- PathVariable로 선언된 key 파라미터에 입력값(AB123)이 바인딩되고 서비스 레이어로 조회를 요청합니다.

.

**Service**

```java
@Service
public class SlackBotServiceImpl implements SlackBotService {

    @Override
    public String doSomething(String key) {
        // do something..
        return "Nice to meet you. My name is Pinocchio";
    }
}
```

- doSomething 메소드에서는 인프라스트럭처 영역과 연결하여 DB, 메시징 시스템 같은 외부 시스템과 연동하여 처리할 수 있습니다.
- `WhatIsThisCode` 슬랙봇의 경우 요청으로 받은 공통 코드를 DB로 조회하지만 여기서는 간단하게 문자열을 응답해 주도록 하겠습니다.

.

**Response Entity**

```java
@Getter
@Builder
public class BasicResponse {
    private HttpStatus httpStatus;
    private String message;
    private Integer count;
    private List<?> result;

    public static BasicResponse ok(List<?> result) {
        return BasicResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("SUCCESS")
                .count(result.size())
                .result(result)
                .build();
    }
}
```

- 응답으로 사용될 객체는 List 타입의 조회 결과와 나머지 필요한 정보들을 담고 있습니다.
- JSON 형태로 응답할 경우 아래와 같은 형태로 결과를 얻을 수 있습니다.

```json
{
    "httpStatus": "OK",
    "message": "SUCCESS",
    "count": 1,
    "result": [
        "Nice to meet you. My name is Pinocchio"
    ]
}
```

슬랙봇을 똑똑하게 만들어줄 API를 만들었으니 이제 슬랙봇이 사용자의 요청에도 응답할 수 있도록 생명을 불어넣어 보겠습니다.

### Connecting to the RTM API on Python

**Install Slack SDK package**

먼저 RTM API 활용을 위한 `RTM(Real Time Messaging) Client` 사용을 위해 **`Python Slack SDK` 설치가 필요**합니다.<br/> 
`pip install` 명령으로 `slack-sdk` 를 설치합니다.<br/>
참고로 [Python Slack SDK](https://slack.dev/python-slack-sdk/index.htmlhttps://slack.dev/python-slack-sdk/index.html) 문서를 확인해 보면 `slack_sdk` 패키지는 `Python 3.6` 이상을 지원하고, `PyPI`로 설치하는 것을 권장하고 있습니다.

```shell
pip install slack_sdk
```

.

**Connecting to the RTM API**

본격적으로 RTM API를 활용하여 슬랙봇을 만들어보겠습니다.
- 샘플용으로 단순하게 구현하였고, 코드에 대한 설명은 코드와 함께 보실 수 있도록 주석으로 남기게 되었습니다.

**/src/main/bbot-example.py**

```python
import json
from time import sleep

import requests
from slack_sdk.rtm_v2 import RTMClient


# 슬랙앱 인스톨 후 생성된 Bot User OAuth Token으로 RTMClient module과 연결합니다.
# 프록시를 적용할 경우 proxy 옵션을 사용할 수 있습니다.
BOT_USER_OAUTH_TOKEN = 'xxx'
rtm = RTMClient(token=BOT_USER_OAUTH_TOKEN, proxy="http://127.0.0.1:1234")


def send_message(client: RTMClient, event: dict, msg: str):
    channel_id = event['channel']
    user = event['user']

    # event dictionary에 담긴 channel로 결과를 응답해 줍니다. 
    client.web_client.chat_postMessage(
        channel=channel_id,
        text=f"Hi <@{user}>\n" + msg,
    )


def do_something(key: str):
    # 사용자가 입력한 값으로 API를 요청합니다. 
    url = f"http://localhost:8080/slack-bot/do-something/{key}"
    r = requests.get(url)
    return r.json()['result'][0]


@rtm.on("message")
def handle(client: RTMClient, event: dict):
    # event dictionary에는 type, channel, text, user.. 등 요청에 대한 정보가 담겨져 있습니다.
    # text 키에 사용자의 입력값이 담기게 됩니다.
    request = event['text']
    msg = do_something(request)
    send_message(client, event, msg)


def main():
    # WebSocket 연결과 RTM 서버와의 연결
    rtm.connect()


if __name__ == "__main__":
    main()
    # 슬랙봇이 종료되지 않고 동작을 유지할 수 있도록 무한 루프로 고정합니다.
    while True:
        sleep(1)
```

- [RTM Client](https://slack.dev/python-slack-sdk/real_time_messaging.html#real-time-messaging-rtm) 문서에서 샘플 코드를 참고할 수 있습니다.
- 이제 `python3 ./src/main/bot-example.py` 명령으로 슬랙봇에게 생명을 넣어줄 수 있습니다.
- **Slack 앱 탭** ➡ **앱 추가** ➡ **생성한 앱 검색** 후 슬랙봇에게 메시지를 보내면 아래와 같이 응답을 받아볼 수 있습니다.
  - 샘플로 단순히 문자열을 응답하도록 구현하였지만, 조회/추가/수정과 같이 원하는 형태의 API와 연동시켜 준다면 말을 잘 듣는 슬랙봇을 만들 수 있습니다.
  - 이후에 **맞춤법 검사 슬랙봇(`SpellChecker`)**도 만들었다는 사실은 안 비밀..🤫

![](https://raw.githubusercontent.com/jihunparkme/blog/main/img/increase-work-efficiency-with-slackbot/image12.png)

참고로, 채널이 아닌 스레드로 메시지를 응답하고 싶을 경우 아래와 같이 event dictionary에 담긴 **스레드 정보를 활용**할 수 있습니다. 

```python
channel_id = event['channel']
thread_ts = event['ts']

client.web_client.chat_postMessage(
    channel=channel_id,
    text=msg,
    thread_ts=thread_ts
)
```

---

## 마무리

피노키오의 요정처럼 슬랙봇에게 생명을 불어넣어 주는 기분으로 만들다 보니 재미있고 유익한 시간이었습니다.<br/>
또한, 사내 여러 개발자분께서 공통 코드 조회 슬랙봇(`WhatIsThisCode`)을 사용해 주고 계시다는 소식을 듣고 뿌듯함을 느끼게 되었습니다.<br/>

마지막으로, 읽으시면서 궁금하신 사항이나 개선 사항이 있다면 언제든 아래 코멘트 부탁드립니다.<br/>
글을 읽어주신 모든 분께 감사드립니다. 🙇🏻‍

## Reference

- [RTM Client](https://slack.dev/python-slack-sdk/real_time_messaging.html#real-time-messaging-rtm)
- [Real Time Messaging API](https://slack.dev/node-slack-sdk/rtm-api)
- [Legacy: Real Time Messaging API](https://api.slack.com/legacy/rtm)
- [공통 코드 조회 슬랙봇 이미지](https://www.freepik.com/)


