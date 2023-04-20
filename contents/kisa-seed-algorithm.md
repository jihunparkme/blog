# KISA SEED 암호화

전자상거래, 금융, 무선통신 등에서 전송되는 개인정보와 같은 중요한 정보를 보호하기 위해 1999년 2월 한국인터넷진흥원과 국내 암호전문가들이 순수 국내기술로 개발한 128비트 블록 암호 알고리즘

- 128bit의 암/복호화키를 이용하여 임의의 길이를 갖는 입력 메시지를 128bit의 블록단위로 처리하는 128bit 블록암호 알고리즘
- 따라서, 임의의 길이를 가지는 평문 메시지를 128bit씩 블록단위로 나누어 암호화하여 암호문을 생성
- 암/복호화하는 방법에 대한 운영모드로 `ECB`(Electronic Code Book), `CBC`(Cipher Block Chaining), `CTR`(CounTeR), `CCM`(Counter with CBC-MAC), `GCM`(Galois/Counter Mode), `CMAC`(Cipher-based MAC) 제공

활용 메뉴얼에 내용들이 자세하게 설명되어 있어 자세한 내용은 메뉴얼을 참고해 보자.

- [암호 알고리즘 소스코드](https://seed.kisa.or.kr/kisa/Board/17/detailView.do)

여기서 필자는 CBC 운영모드를 사용하게 되어 CBC 관련 내용만 간단하게 다루려고 한다.

## CBC(Cipher Block Chaining)

동일한 평문 블록과 암호문 블록 쌍이 발생하지 않도록 전 단계의 암/복호화 결과가 현 단계에 영향을 주는 운영모드

**암호화 과정**

- 현 단계에서 평문 블록과 전 단계의 암호문 블록을 배타적 논리합 연산한 결과를 현 단계의 입력 블록으로 설정하고, 이를 암호화한 출력 블록을 현 단계의 암호문 블록으로 설정

**복호화 과정**

- 현 단계의 암호문 블록을 입력 블록으로 설정하고, 이를 복호화한 출력 블록을 전 단계의 입력 블록인 암호문 블록과 배타적 논리합 연산한 결과를
현 단계의 평문 블록

## Example

KISA SEED 암호화 알고리즘 적용을 위해 [암호 알고리즘 소스코드](https://seed.kisa.or.kr/kisa/Board/17/detailView.do)에서 사용하고자 하는 언어, 운영모드에 해당하는 파일이 필요하다.

.

필자는 JAVA 언어를 사용하고 CBC 운영모드를 적용할 것이라서 `KISA_SEED_CBC.java` 파일을 import 해주었다.

### Encrypt

SEED-CBC 알고리즘 암호화를 위해 KISA_SEED_CBC.SEED_CBC_Encrypt 메서드 호출

`KISA_SEED_CBC.SEED_CBC_Encrypt`

```java
/**
 * @Return 사용자 입력에 대한 암호문 출력 byte 
 */
public static byte[] SEED_CBC_Encrypt(
    byte[] pbszUserKey, // 사용자가 지정하는 입력 키 (16 bytes)
    byte[] pbszIV, // 사용자가 지정하는 초기화 벡터 (16 bytes)
    byte[] message, // 사용자 입력 평문
    int message_offset, // 사용자 입력 길이 시작 오프셋
    int message_length // 사용자 입력 길이
)
```

sample.

```java
public static String seedCbcEncryptWith(String encodedSeedKey, String personalInfo) throws Exception {
    try {
        byte[] seedKey = Base64.getDecoder().decode(encodedSeedKey);
        byte[] encryptedInfo = KISA_SEED_CBC.SEED_CBC_Encrypt(
                getPublicUserKey(seedKey), getPublicIV(seedKey),
                personalInfo.getBytes(UTF_8), 0, personalInfo.getBytes(UTF_8).length
        );
        
        byte[] encodedEncryptedInfo = Base64.getEncoder().encode(encryptedInfo);
        return new String(encodedEncryptedInfo, UTF_8);
    } catch (Exception e) {
        log.error("fail seed cbc encrypt.", e);
        throw new Exception(e);
    }
}

private static byte[] getPublicIV(byte[] seedKey) {
    return Arrays.copyOfRange(seedKey, 16, 32);
}

private static byte[] getPublicUserKey(byte[] seedKey) {
    return Arrays.copyOfRange(seedKey, 0, 16);
}
```

### Decrypt

SEED-CBC 알고리즘 복호화를 위해 KISA_SEED_CBC.SEED_CBC_Decrypt 메서드 호출

`KISA_SEED_CBC.SEED_CBC_Decrypt`

```java
/**
 *  @Return 사용자 입력에 대한 평문 출력 byte
 */
public static byte[] SEED_CBC_Decrypt(
    byte[] pbszUserKey, // 사용자가 지정하는 입력 키(16 bytes)
    byte[] pbszIV, // 사용자가 지정하는 초기화 벡터(16 bytes)
    byte[] message, // 사용자 입력 평문
    int message_offset, // 사용자 입력 길이 시작 오프셋
    int message_length // 사용자 입력 길이
)
```

sample.

```java
public static String seedCbcDecryptWith(String encodedSeedKey, String encodedCipherInfo) throws Exception {
    try {
        byte[] seedKey = Base64.getDecoder().decode(encodedSeedKey);
        byte[] cipherInfo = Base64.getDecoder().decode(encodedCipherInfo);
        byte[] decryptedInfo = KISA_SEED_CBC.SEED_CBC_Decrypt(
                getPublicUserKey(seedKey), getPublicIV(seedKey),
                cipherInfo, 0, cipherInfo.length
        );

        return new String(decryptedInfo, UTF_8);
    } catch (Exception e) {
        log.error("fail seed cbc decrypt.", e);
        throw new Exception(e);
    }
}
```

### Test

```java
@Test
void seedCbcEncryptWith() throws Exception {
    String seedKey = "ABCDefghijk...=";
    String result = util.seedCbcEncryptWith(seedKey, "홍길동");
    Assertions.assertEquals("ADFASdfa...SDS=", result);
}

@Test
void seedCbcDecryptWith() throws Exception {
    String seedKey = "ABCDefghijk...";
    String cipherInfo = "ADFASdfa...SDS=";
    String result = util.seedCbcDecryptWith(seedKey, cipherInfo);
    Assertions.assertEquals("홍길동", result);
}
```