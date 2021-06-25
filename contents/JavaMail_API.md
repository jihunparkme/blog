# JavaMail API

## Add Dependency

- pom.xml

```xml
<!-- https://mvnrepository.com/artifact/com.sun.mail/javax.mail -->
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>
```

- build.gradle

```gradle
implementation group: 'com.sun.mail', name: 'javax.mail', version: '1.6.2'
```

## Set Gmail SMTP

- [Gmail IMAP 액세스 설정](https://support.google.com/mail/answer/7126229?hl=ko&rd=3&visit_id=637595769711568664-1727186650#ts=1665018)
- [앱 허용 보안 수준 설정](https://myaccount.google.com/intro/security)
  - 구글 내계정 -> 보안 -> 보안 수준이 낮은 앱의 엑세스 허용

> 보안 수준이 낮은 앱의 엑세스를 허용하지 않으면 아래 Exception 발생
>
> javax.mail.AuthenticationFailedException: ... Username and Password not accepted. Learn more at ..

## SMTP TLS vs SSL

SSL(Secure Sockets Layer) Protocol 은 서버 구성 측면에서 취약점, 구식 암호 제품군 및 브라우저 보안 경고 등으로 인해 `TLS(Transport Layer Security) Protocol`사용 선호

**Reference**

> [https://smartits.tistory.com/209](https://smartits.tistory.com/209)
>
> [https://www.itworld.co.kr/tags/52416/SSL/113007](https://www.itworld.co.kr/tags/52416/SSL/113007)

## Google SMTP TLS

```java
public static void sendMail(String _email, String _password) {

    System.out.println("Start JavaMail API Test ~!");

    String subject = "Hello JavaMail API Test";
    String fromMail = "from.email@gmail.com";
    String fromName = "Cristoval";
    String toMail = "to.email@gmail.com"; // 콤마(,) 나열 가능

    // mail contents
    StringBuffer contents = new StringBuffer();
    contents.append("<h1>Hello</h1>\n");
    contents.append("<p>Nice to meet you ~! :)</p><br>");

    // mail properties
    Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.gmail.com"); // use Gmail
    props.put("mail.smtp.port", "587"); // set port

    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true"); // use TLS

    Session mailSession = Session.getInstance(props,
            new javax.mail.Authenticator() { // set authenticator
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(_email, _password);
                }
            });

    try {
        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(new InternetAddress(fromMail, MimeUtility.encodeText(fromName, "UTF-8", "B"))); // 한글의 경우 encoding 필요
        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(toMail)
        );
        message.setSubject(subject);
        message.setContent(contents.toString(), "text/html;charset=UTF-8"); // 내용 설정 (HTML 형식)
        message.setSentDate(new java.util.Date());

        Transport t = mailSession.getTransport("smtp");
        t.connect(_email, _password);
        t.sendMessage(message, message.getAllRecipients());
        t.close();

        System.out.println("Done Done ~!");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

## Reference

> [https://mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/](https://mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/)
>
> [https://mkyong.com/spring-boot/spring-boot-how-to-send-email-via-smtp/](https://mkyong.com/spring-boot/spring-boot-how-to-send-email-via-smtp/)
