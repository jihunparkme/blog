# Enum Class가 싱글톤 성질을 가지고 있어서 발생한 이슈

troubleshooting #1

Enum Class가 싱글톤 성질을 가지고 있기 때문에 상용에서 발생했던 이슈를 기록하고자 한다.

그 이슈는 바로바로.. 🥁🥁🥁

...

## Issue

구매 완료 알림 푸시가 내가 주문한 상품명이 아닌 다른 사람이 주문한 상품명으로 전달된 이슈였다.

예를 들어 사용자는 불고기버거 기프트콘을 주문했지만, 무소음 키보드 주문이 완료되었다는 구매 완료 알리 푸시를 받게 된 것이다.

- 정상 푸시 메시지
  - 불고기버거 세트 기프트콘 주문이..

- 문제의 푸시 메시지
  - 무소음 키보드 주문이..

## Problem

푸시 톡 클래스는 Enum 클래스인데, 아래와 같이 setter 기능을 사용하고 있었다.

(setter 기능을 사용하기보다 생성자를 활용했다면 이러한 이슈가 발생하지 않았을 것이다..)

```java
public enum PushTak {

        IOS("", "this is IOS"),
        ANDROID("", "this is ANDROID");

        private String name;
        private String message;

        PushTak(String name, String message) {
            this.name = name;
            this.message = message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
```

```java
// Generate Push Message Logix
PushTak pushTalk = PushTak.IOS;

StringBuffer message = new StringBuffer();
message.append(messageSubject);
// ...
message.append(pushMessage);

pushTalk.setMessage(message.toString()); //=> 이슈의 원인
```

![Result](https://github.com/jihunparkme/blog/blob/main/img/enum-issue.jpg?raw=true 'Result')

- Enum Class의 message 필드를 세팅해 주고 있는데, Enum은 `싱글톤 성질`을 가지고 있으므로 모든 스레드가 하나의 Enum 객체를 공유하다 보니 message 세팅(setMessage)과 알림 푸시(sendPushTalk()) 메서드 호출 사이에 다른 스레드에서 message 필드를 세팅하면서 다른 request에서 세팅한 message 필드 값을 사용하며 이슈가 발생하게 되었다.

여기서 잠깐..

Enum Class는 왜 싱글톤 성질을 가지고 있을까?!

💡 Enum Class는 private 생성자로 인스턴스의 생성을 제어하고, 상수만 가지고 있는 특별한 클래스이기 때문에 싱글톤의 성질을 가지고 있다.

## Solution

해결책은 사실 단순하다.. 싱글톤 성질을 갖는 Enum Class에 필드 값을 세팅하지 않고, `새로운 객체`를 활용하여 message를 세팅해 주었다.

추가로, 새로운 객체의 생성자를 활용하여 필드의 NPE를 방지할 수도 있다.

```java
// Generate Push Message
StringBuffer message = new StringBuffer();

message.append(messageSubject);
// ...
message.append(pushMessage);

// (-) pushTalk.setMessage(message.toString());

(+) PushTalk pushTalk = new PushTalk(name, message.toString());
(+) result = this.sendPushTalk(memberNo, pushTalk, ...);
```