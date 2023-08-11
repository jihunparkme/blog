# Slack Bot - Real Time Messaging API

슬랙의 `Real Time Messaging` API 를 활용해서 슬랙봇을 만들어 보자.

.

Real Time Messaging 은 줄여서 `RTM` 으로 불리기도 하고,

이름처럼 실시간으로 이벤트를 수신하고 메시지를 보낼 수 있는 WebSocket 기반 API 이다.

그 밖에도 [Events API](https://slack.dev/node-slack-sdk/events-api), [Web API](https://slack.dev/node-slack-sdk/web-api) API 를 제공해 주고 있다.

.

사내에서는 방화벽 문제로 [Real Time Messaging API](https://slack.dev/node-slack-sdk/rtm-api) 를 활용하려고 한다.

Real Time Messaging API 의 간략한 설명을 보면 해당 API 를 더 이상 지원을 하고 있지는 않다.

다만, [Events API](https://slack.dev/node-slack-sdk/events-api), [Web API](https://slack.dev/node-slack-sdk/web-api) 활용에 있어 방화벽 제한이 있을 경우 사용할 것으로 안내하고 있다.

```text
@slack/rtm-api 패키지에는 이벤트를 수신하고 슬랙의 실시간 메시징 API로 간단한 메시지를 보낼 수 있는 간단하고 편리하며 구성 가능한 클라이언트가 포함되어 있습니다. 앱에서 이를 사용하여 지속적인 웹소켓 연결을 통해 슬랙 플랫폼에 계속 연결할 수 있습니다.

참고: RTM은 더 이상 최신 범위의 앱에 사용할 수 없습니다. 대신 이벤트 API 및 웹 API를 사용하는 것이 좋습니다. RTM을 사용해야 하는 경우(회사 방화벽 제한 때문에) 기존 범위의 앱을 생성하여 이를 수행할 수 있습니다. 기존 RTM 앱이 있는 경우 최신 범위의 앱으로 업데이트되므로 범위를 업데이트하지 말고 RTM 작업을 중지하십시오.
```

자세한 설명은 [Real Time Messaging API](https://slack.dev/node-slack-sdk/rtm-api) 를 참고하자.

.

처음에 [Create New App](https://api.slack.com/apps) 으로 Slack App 을 생성해서 Real Time Messaging API 를 적용하려고 하는데 계속 아래와 같은 문구와 함께 `not_allowed_token_type` 에러가 발생했었다.

```ssh
slack_sdk.errors.SlackApiError: The request to the Slack API failed. (url: https://www.slack.com/api/rtm.connect)

The server responded with: {'ok': False, 'error': 'not_allowed_token_type'}
```

그리하여 엄청난 서칭 끝에.. [python-slack-sdk Issues](https://github.com/slackapi/python-slack-sdk/issues/958#issuecomment-780439838) 글을 보게 되었고, RTM API 활용을 위해 Classic App 을 생성해야 한다는 것을 알게 되었다.

.

찾아보니 [Python RTM Client](https://slack.dev/python-slack-sdk/real_time_messaging.html#real-time-messaging-rtm) 에도 아래와 같이 설명이 되어 있다.

```text
Events using the RTM API must use a classic Slack app (with a plain bot scope).

Also, even if the Slack app configuration pages encourage you to upgrade to the newer permission model, don’t upgrade it and keep using the “classic” bot permission
```

.

그렇다면 이제 본격적으로 `Classic App` 을 생성해서 `Real Time Messaging API` 를 적용해 보자.

## Create a Slack App (Classic)

[Create a Slack App (Classic)](https://api.slack.com/apps?new_classic_app=1) 로 접근하면 Classic App 생성 팝업을 통해 Classic App 을 생성할 수 있다.

.

![Result](https://github.com/jihunparkme/blog/blob/main/img/slack-rtm/1.png?raw=true 'Result')

## Add features and functionality

`Basic Informaton` → `Add features and functionality` → `Bots`

.

사용자와 슬랙 앱이 메시지를 주고받을 수 있도록 봇을 추가

.

![Result](https://github.com/jihunparkme/blog/blob/main/img/slack-rtm/6.png?raw=true 'Result')

## Add a legacy bot user

`App Home` → `First, Add Legacy Bot User`

.

`Add Legacy Bot User` 버튼을 누르고

.

![Result](https://github.com/jihunparkme/blog/blob/main/img/slack-rtm/2.png?raw=true 'Result')

.

`Add App Display Name` 설정을 해보자.
.


![Result](https://github.com/jihunparkme/blog/blob/main/img/slack-rtm/3.png?raw=true 'Result')

## OAuth & Permissions

`OAuth & Permissions` → `Scopes`

.

![Result](https://github.com/jihunparkme/blog/blob/main/img/slack-rtm/4.png?raw=true 'Result')

.

Add features and functionality 에서 봇을 추가했다면 bot Scope 가 자동으로 추가되었을 것이다. (아니라면.. 추가해 주세요..)

bot Scope 에는 기본적으로 RTM API 에 필요한 scope 들이 포함되어 있다.

개발을 진행하면서 필요한 scope 가 생기면 추가하면 된다.

참고. [Permission scopes](https://api.slack.com/scopes)


## Message Tab

`App Home` → `Show Tabs` → `Messages Tab`

.

![Result](https://github.com/jihunparkme/blog/blob/main/img/slack-rtm/5.png?raw=true 'Result')

.

Messages Tab 이 활성화되어야 슬랙봇에게 메시지를 보낼 수 있다.

비활성화 시 아래 안내 문구가 노출되는데, 

scopes 에 `im:history`, `im:write`, `im:read` 가 포함되면 자동으로 활성화되는 것 같다.

```text
The Message tab requires read/write scopes such as im:history, im:write, and/or im:read

Direct messages your app sends will show in this tab.
```

.

아래 링크는 처음에 슬랙봇에게 메시지를 보낼 수 없는 상태가 계속되어서 찾게 된 글이다.

[Sending messages to this app has been turned off.](https://github.com/MicrosoftDocs/bot-docs/issues/2077#issuecomment-843960695)

## Install to Workspace

`Basic Information` → `Install your app`

슬랙 API 호출을 위해 필요한 토큰 생성을 위해 앱 설치가 필요하다.

앱 설치가 완료되면 자동으로 OAuth Token 이 생성된다.

- User OAuth Token
- Bot User OAuth Token

.

여기서 우리가 필요한 토큰은 `Bot User OAuth Token` 이다.

`OAuth & Permissions` → `OAuth Tokens for Your Workspace` → `Bot User OAuth Token`

## Apply Slack Real Time Messaging API

여기서는 Python 에 RTM(Real Time Messaging) Client 를 적용해 보려고 한다.

**Installation**

```python
pip install slack-sdk
```

**Connecting to the RTM API**

RTM API 를 활용해서 간단한 챗봇을 만들어 보자.

.

생성한 앱에게 메시지를 보내면 스레드로 '응 안녕'이라는 답변이 달리게 된다.

아래 코드를 활용해 슬랙에 다양한 봇을 추가해 보자.

```python
from time import sleep
from slack_sdk.rtm_v2 import RTMClient

rtm = RTMClient(token='xoxb-YOUR-BOT-USER-OAUTH-TOKEN')

@rtm.on("message")
def handle(client: RTMClient, event: dict):
    request = event['text']
    if request != '':
        channel_id = event['channel']
        thread_ts = event['ts']

        client.web_client.chat_postMessage(
            channel=channel_id,
            text='응 안녕',
            thread_ts=thread_ts
        )


if __name__ == "__main__":
    rtm.connect()
    while True: # @rtm.on 상태를 유지
        sleep(1)
```

.

![Result](https://github.com/jihunparkme/blog/blob/main/img/slack-rtm/result.png?raw=true 'Result')


> [RTM Client](https://slack.dev/python-slack-sdk/real_time_messaging.html#real-time-messaging-rtm)
>
> [Legacy: Real Time Messaging API](https://api.slack.com/rtm)
>
> [Real Time Messaging API](https://slack.dev/node-slack-sdk/rtm-api)