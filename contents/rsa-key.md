# RSA Public/Private key in JAVA

업무 중 외부 API와 연동을 위해 RSA 암호화 알고리즘을 적용이 필요하게 되었는데, Public Key, Private key 에 대한 개념을 이해하고 적용해 보고자 간단하게 정리하게 되었다.

## RSA(Rivest-Shamir-Adleman)

**비대칭 암호화 알고리즘**

`공개 키`를 사용하여 데이터를 암호화하고, `개인 키`를 사용하여 암호를 해독

- 대칭 알고리즘인 [DES](https://en.wikipedia.org/wiki/Data_Encryption_Standard), [AES](https://www.baeldung.com/java-aes-encryption-decryption)와 다르게 2개의 키(Public Key, Private Key) 존재
  - Public Key : 누구와도 공유할 수 있는 공개 키(데이터를 암호화에 사용)
  - Private Key : 자신을 위한 개인 키(복호화에 사용)
- 메세지 암호화 시 해당 메세지가 공개 키(Public Key)로 암호화 되었다면, 복호화를 위해서는 공개 키(Public Key)와 쌍을 이루는 개인 키(Private Key)로만 가능

예를 들어..

```text
Public Key: AAA
Private Key: BBB

"배고파"라는 메세지를 AAA 라는 Public Key 로 암호화하였고, "Nnbt/FLisdw4qVpm..." 로 변환된다고 해보자.

그렇다면 암호화된 "Nnbt/FLisdw4qVpm..." 메세지는 BBB 라는 Private Key 로만 복호화(해독)가 가능하다.
```

## Example

### Generate RSA Key Pair

```java
import java.security.*;

KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
generator.initialize(2048);
KeyPair pair = generator.generateKeyPair();

PublicKey publicKey = pair.getPublic();
PrivateKey privateKey = pair.getPrivate();
```

### Storing Keys in Files

RSA Key Pair 는 고정된 값이고, 보안과 편의상 파일에 저장/관리하는 것이 좋다.

Generate RSA Key Pair 단계에서 얻은 publicKey, privateKey 를 파일로 저장해 보자.

```java
try (FileOutputStream fos = new FileOutputStream("public.key")) {
    // 주요 인코딩 형식으로 키 콘텐츠를 반환
    fos.write(publicKey.getEncoded());
}

try (FileOutputStream fos = new FileOutputStream("private.key")) {
    fos.write(privateKey.getEncoded());
}
```

### Reading Keys in Files

파일에서 키를 읽으려면 먼저 바이트 배열로 로드가 필요

```java
private PublicKey getRsaPublicKey() throws Exception {
    File publicKeyFile = new File("uptn-public.key");
    byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());

    // KeyFactory를 사용하여 실제 인스턴스 생성
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
    return keyFactory.generatePublic(publicKeySpec);
}

private PrivateKey getRsaPrivateKey() throws Exception {
    File privateKeyFile = new File("uptn-private.key");
    byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    return keyFactory.generatePrivate(privateKeySpec);
}
```

### Encrypt

```java
public static final String rsaEncryptWith(PublicKey publicKey, String message) throws Exception {
    // 공용 키를 사용한 암호화를 위해 초기화된 Cipher 필요
    Cipher encryptCipher = getInstance("RSA");
    encryptCipher.init(ENCRYPT_MODE, publicKey);

    // 암호화 메서드는 바이트 배열 인수만 받으므로 문자열 변환
    byte[] secretMessageBytes = message.getBytes(StandardCharsets.UTF_8);
    // doFinal 메서드를 호출하여 메시지 암호화
    byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

    // Base64 인코딩
    return Base64.getEncoder().encodeToString(encryptedMessageBytes);
}

...

String message = "홍길동";
String result = rsaEncryptWith(getRsaPublicKey(), message);
```

[Cipher](https://www.baeldung.com/java-cipher-class)

### Decrypt

 ```java
 static final String rsaDecryptWith(PrivateKey privateKey, String encryptedMessage) throws Exception {
    // 복호화를 위해 별도의 Cipher 인스턴스 필요
    Cipher decryptCipher = Cipher.getInstance("RSA");
    decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

    byte[] encryptedMessageBytes = encryptedMessage.getBytes(StandardCharsets.UTF_8);
    byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);

    // 암호화 과정과 동일하게 doFinal 메서드를 사용하여 복호화
    return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
}

...

String message = "Nnbt/FLisdw4qVpm...";
String result = rsaDecryptWith(getRsaPrivateKey(), message);
```

### 마무리

API 통신에서 RSA Key를 적용할 경우 바이너리 데이터 전송이 어려우므로 Base64 Encoding/Decoding 과정 이 필요하다.

아래는 간단한 사례를 그려보았다.

- dest 쪽으로 RSA Public Key 를 Base64 로 Encoding 해주면
- dest 에서는 RSA 를 바이너리 형태로 변환하기 위해 Base64 Decoding 을 해주고, dest 쪽 Seed Key 를 전달받은 RSA Public Key 를 사용하여 암호화한 후 Base64 Encoding 된 결과를 응답
- 응답받은 Seed Key 를 바디너리 형태로 변환하기 위해 Base64 Decoding 을 해주고, Private 를 사용하여 복호화하여 Seed Key 획득
- 획득한 Seed Key 를 사용하여 개인정보를 암호화한 후 전달

![Result](https://github.com/jihunparkme/blog/blob/main/img/rsa.png?raw=true 'Result')

## Reference

- [RSA in Java](https://www.baeldung.com/java-rsa)
- [Public – private key pairs & how they work](https://www.preveil.com/blog/public-and-private-key/)
- [RSA 암호화](https://namu.wiki/w/RSA%20%EC%95%94%ED%98%B8%ED%99%94)