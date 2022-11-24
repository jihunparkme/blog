# Enum Class의 동시성 이슈

troubleshooting #1

Enum Class가 싱글톤 성질을 가지고 있기 때문에 발생했던 동시성 이슈를 기록하고자 한다.

(동시성 이슈﹖ 다수의 스레드가 동시에 같은 인스턴스 필드 또는 공용 필드 값을 변경하면서 발생하는 이슈)

.

그 이슈는 바로바로.. 🥁🥁🥁

.

...

## Issue

구매 완료 알림 푸시가 내가 주문한 상품명이 아닌 다른 사람이 주문한 상품명으로 전달된 이슈였다.

예를 들어 사용자는 불고기버거 기프트콘을 주문했지만, 무소음 키보드 주문이 완료되었다는 구매 완료 알리 푸시를 받게 된 것이다.

- 정상 푸시 메시지
  - 불고기버거 세트 기프트콘 주문이..

- 문제의 푸시 메시지
  - 무소음 키보드 주문이..

## Problem

푸시 톡 클래스는 Enum 클래스인데, 아래와 같이 setter 기능을 사용해서 값을 변경하고 있었다.

```java
@Getter
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

![Result](https://github.com/jihunparkme/blog/blob/main/img/enum-issue.png?raw=true 'Result')

- Enum Class의 message 필드를 수정하고 있는데, Enum은 `싱글톤 성질`을 가지고 있으므로 모든 스레드가 하나의 Enum 객체를 공유하다 보니 message 세팅(setMessage)과 알림 푸시(sendPushTalk()) 메서드 호출 사이에 다른 스레드에서 message 필드를 수정하면서 다른 request에서 수정한 message 필드 값을 사용하며 이슈가 발생하게 되었다.

.

여기서 잠깐..

Enum Class는 왜 싱글톤 성질을 가지고 있을까?!

.

💡 Enum Class는 private 생성자로 인스턴스의 생성을 제어하고, 상수만 가지고 있는 특별한 클래스이기 때문에 싱글톤의 성질을 가지고 있다.

## Solution

해결책은 사실 단순했다.. 싱글톤 성질을 갖는 Enum Class의 필드 값을 수정하지 않고, `새로운 객체`를 활용하여 message를 세팅해 주었다.

어떻게 보면 주로 상수 사용을 목적으로 사용되는 Enum 클래스를 수정한다는 것 자체가 문제의 시작일 수도 있다.

하지만 Enum 클래스뿐만 아니라 **스프링 빈처럼 싱글톤 객체의 필드를 변경하며 사용할 때 동시성 문제는 항상 주의**해야겠다.

동시성 문제는 스레드 로컬로도 해결이 가능한데 이 부분은 다음 시간에 알아보도록 하자.

[[ThreadLocal] 동시성 문제와 스레드 로컬](https://data-make.tistory.com/751)

```java
// Generate Push Message
StringBuffer message = new StringBuffer();

message.append(messageSubject);
// ...
message.append(pushMessage);

// (-) pushTalk.setMessage(message.toString());

(+) PushTalk pushTalk = new PushTalk(name, message.toString(), ...);
(+) result = this.sendPushTalk(memberNo, pushTalk, ...);
```

## Test

PushTalkService.java

```java
@Slf4j
public class PushTalkService {

    private PushTak pushTalk;

    public void logic(String name) {
        pushTalk = PushTak.IOS;

        StringBuffer message = new StringBuffer();
        message.append("[주문 완료] ");
        message.append(name);
        message.append(" 주문의 완료되었습니다.");


        log.info("수정 message={} -> enumMessage={}", message, pushTalk.getMessage());
        pushTalk.setMessage(message.toString());
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("조회 enumMessage={}", pushTalk.getMessage());
    }
}
```

PushTalkServiceTest.java

```java
@Slf4j
public class PushTalkServiceTest {

    private PushTalkService pushTalkService = new PushTalkService();

    @Test
    void field() throws InterruptedException {
        log.info("main start");

        Runnable userA = () -> {
            pushTalkService.logic("불고기버거 세트 기프트콘");
        };
        Runnable userB = () -> {
            pushTalkService.logic("무소음 키보드");
        };

        Thread threadA = new Thread(userA);
        threadA.setName("thread-A");
        Thread threadB = new Thread(userB);
        threadB.setName("thread-B");

        threadA.start();

        sleep(100);

        threadB.start();

        sleep(3000);
        log.info("main exit");
    }
}
```

Result

- thread-A 고객은 불고기버거 세트 기프트콘을 주문했지만 동시성 문제로 아래 로그와 같이 무소음 키보드 주문이 완료되었다는 푸시를 받게 될 것이다.

```console
[Test worker] INFO ..PushTalkServiceTest - main start
[thread-A] INFO ..code.PushTalkService - 수정 message=[주문 완료] 불고기버거 세트 기프트콘 주문의 완료되었습니다. -> enumMessage=this is IOS
[thread-B] INFO ..code.PushTalkService - 수정 message=[주문 완료] 무소음 키보드 주문의 완료되었습니다. -> enumMessage=[주문 완료] 불고기버거 세트 기프트콘 주문의 완료되었습니다.
[thread-A] INFO ..code.PushTalkService - 조회 enumMessage=[주문 완료] 무소음 키보드 주문의 완료되었습니다.
[thread-B] INFO ..code.PushTalkService - 조회 enumMessage=[주문 완료] 무소음 키보드 주문의 완료되었습니다.
[Test worker] INFO ..PushTalkServiceTest - main exit
```