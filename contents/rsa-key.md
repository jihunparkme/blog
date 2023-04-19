# RSA Public/Private key in JAVA

업무 중 외부 API와 연동을 위해 RSA 암호화 알고리즘을 적용이 필요하게 되었는데, Public Key, Private key 에 대한 개념을 이해하고 적용해 보고자 간단하게 정리하게 되었다.

## RSA(Rivest-Shamir-Adleman)

**비대칭 암호화 알고리즘**

공개 키를 사용하여 데이터를 암호화하고 개인 키를 사용하여 암호를 해독

- 대칭 알고리즘인 [DES](https://en.wikipedia.org/wiki/Data_Encryption_Standard), [AES](https://www.baeldung.com/java-aes-encryption-decryption)와 다르게 2개의 키(Public Key, Private Key) 존재
  - Public Key : 누구와도 공유할 수 있는 공개 키(데이터를 암호화에 사용)
  - Private Key : 자신을 위한 개인 키(복호화에 사용)
- 메세지 암호화 시 해당 메세지가 공개 키(Public Key)로 암호화 되었다면, 복호화를 위해서는 공개 키(Public Key)와 쌍을 이루는 개인 키(Private Key)로만 가능

Example

```text
Public Key: AAA
Private Key: BBB

"배고파"라는 메세지를 AAA 라는 Public Key 로 암호화를 하게 되면 "Nnbt/FLisdw4qVpm..." 로 변환된다고 해보자.

그렇다면 암호화된 "Nnbt/FLisdw4qVpm..." 메세지는 BBB 라는 Private Key 로만 복호화가 가능하다.
```

## Example

### Generate RSA Key Pair

```java
import java.security.*;

KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
generator.initialize(2048);
KeyPair pair = generator.generateKeyPair();

PrivateKey privateKey = pair.getPrivate();
PublicKey publicKey = pair.getPublic();
```

### Storing Keys in Files

RSA Key Pair 는 보안과 편의상 파일에 저장/관리하는 것이 좋다.

```java
KeyPair rsaKeyPair = uptnApiUtil.getRsaKeyPair();
PublicKey publicKey = rsaKeyPair.getPublic();
PrivateKey privateKey = rsaKeyPair.getPrivate();

try (FileOutputStream fos = new FileOutputStream("public.key")) {
    // 주요 인코딩 형식으로 키 콘텐츠를 반환
    fos.write(publicKey.getEncoded());
}

try (FileOutputStream fos = new FileOutputStream("private.key")) {
    fos.write(privateKey.getEncoded());
}
```

### Reading Keys in Files

파일에서 키를 읽으려면 먼저 콘텐츠를 바이트 배열로 로드가 필요

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
public static final String encryptSHA256With(PublicKey publicKey, String message) throws Exception {
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
KeyPair rsaKeyPair = getRsaKeyPair();
String result = encryptSHA256With(rsaKeyPair.getPublic(), message);
```

참고

- [Cipher](https://www.baeldung.com/java-cipher-class)

### Decrypt

 ```java
 static final String decryptSHA256With(PrivateKey privateKey, String encryptedMessage) throws Exception {
    // 복호화를 위해 별도의 Cipher 인스턴스 필요
    Cipher decryptCipher = Cipher.getInstance("RSA");
    decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

    byte[] encryptedMessageBytes = encryptedMessage.getBytes(StandardCharsets.UTF_8);
    byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);

    // 암호화 과정과 동일하게 doFinal 메서드를 사용하여 복호화
    return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
}

```

## Reference

- [RSA in Java](https://www.baeldung.com/java-rsa)
- [Public – private key pairs & how they work](https://www.preveil.com/blog/public-and-private-key/)
- [RSA 암호화](https://namu.wiki/w/RSA%20%EC%95%94%ED%98%B8%ED%99%94)