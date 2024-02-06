# 모든 파일도 시그니처가 있다.(파일 시그니처로 유효성 검사하기)

기존 반품 신청 시 이미지만 업로드가 가능했었는데요. 이번에 동영상도 업로드가 가능하도록 기능을 추가하게 되었습니다. 클레임 영역에서 동영상 업로드/재생 로직을 신규로 구현하다 보니 신경 써야 할 요소들이 다소 있었습니다. 그중에서 **파일 시그니처로 파일 유효성 검사**에 대한 내용을 다뤄보려고 합니다.<br/><br/>

어느 날 점심을 먹으러 가는 길에 팀원분과 파일 업로드 관련하여 이야기를 나누던 중 팀원분이 문득 **"바이러스 파일을 업로드 가능한 확장자로 변경해서 업로드하게 되면 바이러스도 업로드가 가능하지 않을까?"**라는 의문을 공유해 주셨습니다. 당시에는 "설마 클레임 신청 시 바이러스를 업로드하는 사람이 있을까?"라고 단순하게 생각했었지만, 계속 생각하다 보니 충분히 가능성이 있는 사례였습니다.<br/><br/>

만일 실제 이러한 상황이 발생하면 셀러는 서비스에 대한 신뢰를 잃게 될 수도 있겠다고 생각하게 되었습니다. 그렇게 파일 유효성 검사에 사용되는 **File Signatures (aka. Magic Numbers)**라는 것을 알게 되었고, 처음 알게 된 재미있는 개념을 혼자만 알고 있기에 아까워서(?) 공유하게 되었습니다. (아이디어 뱅크 팀원분에게 감사의 박수를..👏🏼)<br/><br/>

참고로, 이미지 업로드 로직에서 이미지 파일 유효성 검사를 이미 하고 있었는데, 그냥 "유효성을 검사하는 로직이구나~!"하고 무심코 지나갔던 로직을 이번 기회를 통해 다시 볼 수 있었답니다.😅<br/><br/>

## File Signatures

모든 파일은 표준화된 시그니처를 가지고 있는데, `File Signatures(aka. Magic Numbers)`는 **파일의 형태를 식별하기 위해 사용되는 고유한 패턴**입니다.<br/><br/>

예를 들어 PNG 이미지 파일의 시그니처는 `89 50 4E 47 0D 0A 1A 0A`, JPEG 이미지 파일의 시그니처는 `FF D8 FF E0`로 나타낼 수 있습니다.</br>

파일 시그니처는 일반적으로 파일의 시작 부분에 위치하고, 바이너리 형태로 표현됩니다. 파일 마지막 부분에 파일 시그니처가 존재하는 경우도 있는데 파일 시그니처의 위치에 따라 **헤더 시그니처**(파일 시작 부분에 존재), **푸터 시그니처**(파일 마지막에 존재)라고 부르기도 합니다.</br><br/>

파일 시그니처를 통해 해당 파일이 어떤 종류의 데이터를 포함하고, 어떤 포맷을 가졌는지 식별할 수 있습니다.
파일 시그니처는 파일 포맷 분석뿐만 아니라 악성코드 분석, 파일 복구 등에도 적용할 수 있습니다.</br>

...<br/><br/>

파일 시그니처를 통해 파일의 유효성을 검사하기 전에 테스트에 사용할 몇 가지 확장자의 시그니처를 확인해 보겠습니다.</br>

### Video file

**QuickTime MPEG-4 Audio-Video (*.MP4)**

- `MPEG-4 Part 14` 또는 `MP4`는 비디오, 오디오를 저장하는 데 가장 일반적으로 사용되는 형식
- 인터넷을 통한 스트리밍 가능
- 공식 확장자는 `.mp4`이지만 대부분 다른 확장자를 가지고 있으며 일반적으로 `.m4a`, `.m4p`가 있다.
- QuickTime 포맷 사양 기반
- 유효성 검사에는 QuickTime Container File Type을 정의하는 offset 4 이후 signature ftype(hex: `66 74 79 70`)을 확인할 예정입니다.
- File signature
  - MPEG-4 video file(ftypMSNV): `66 74 79 70 4D 53 4E 56`
  - ISO Base Media file(MPEG-4) (ftypisom): `66 74 79 70 69 73 6F 6D`

![출처: https://www.file-recovery.com/mp4-signature-format.htm](/files/post/2024-02-20-file-signatures/mp4-signatures.png)

**Apple QuickTime Video Format (*.MOV)**

- 애플의 Quicktime에서 자주 사용되는 포맷
- Macintosh, Windows 플랫폼에 모두 호환되는 애플이 개발한 압축 알고리즘을 사용
- MPEG-4나 OGG와 같은 다양한 비디오 데이터 형식 저장 가능
- 유효성 검사에는 MP4 형식과 동일하게 QuickTime Container File Type을 정의하는 offset 4 이후 signature ftype(hex: `66 74 79 70`)을 확인할 예정입니다.
- File signature
  - QuickTime movie file(ftypqt): `66 74 79 70 71 74 20 20`

![출처: https://www.file-recovery.com/mov-signature-format.htm](/files/post/2024-02-20-file-signatures/mov-signatures.png)

### Image file

**JPEG Image File Format (*.JPG)**

- 디지털 이미지를 위해 일반적으로 사용되는 손실 압축 방법
- 압축 정도를 조정할 수 있어 저장 크기와 이미지 품질 간의 절충 가능
- JPEG 압축은 다양한 이미지 파일 형식으로 사용
- 압축된 JPEG 파일은 항상 마커 코드로 16진수 값 `FF D8 FF`를 포함하는 이미지 마커로 시작
- File signature
  - Generic JPEG Image file: `FF D8 FF ...`

![출처: https://www.file-recovery.com/jpg-signature-format.htm](/files/post/2024-02-20-file-signatures/jpg-signatures.png)

**Portable Network Graphic (*.PNG)**

- 비손실 그래픽 파일 포맷의 하나
- 특허 문제가 얽힌 GIF 포맷의 문제를 해결 및 개선하기 위해 고안
- PNG 파일은 8 바이트의 시그니처(`89 50 4E 47 0D 0A 1A 0A`)를 갖는데, 이 중 `50 4E 47`이 ASCII 값으로 PNG를 의미
- File signature
  - Portable Network Graphics file: `89 50 4E 47 0D 0A 1A 0A`

### Manipulated file

**파일의 확장자를 변경한 테스트**
- 바이러스가 담긴 파일(zip, pkg)의 확장자만 변경하여 파일 업로드 검증을 시도해 보겠습니다.
- 실제로 해당 파일에는 바이러스가 담기지 않았습니다...🤥

## Validate File

### FileValidator

유효성 검사를 적용해 볼 파일의 시그니처를 알아보았으니 파일 시그니처 정보를 바탕으로 테스트용 FileValidator 클래스를 만들어 보았습니다.

```java
@Slf4j
public class FileValidator {
    
    /**
     * 파일을 읽고, 헤더 시그니처(시작 부분 8byte) 정보 출력 및 파일 유효성 검사 메소드 호출
     */
    public static boolean validate(String filePath) {
        String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1); // 파일 확장자 추출

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] data = new byte[8];
            fis.read(data);

            printBytesToHex(data);
            return validateFile(fileExtension, data);
        } catch (Exception e) {
            log.error("FileValidator#validate exception." + e.getMessage(), e);
        }

        return false;
    }

    /**
     * 확장자별 파일 시그니처로 유효성을 검사하는 메소드 호출
     */
    private static boolean validateFile(String fileExtension, byte[] data) {
        if (fileExtension.equals("mp4")) {
            return validateMP4File(data);
        } else if (fileExtension.equals("mov")) {
            return validateMOVFile(data);
        } else if (fileExtension.equals("jpeg") || fileExtension.equals("jpg")) {
            return validateJPGFile(data);
        } else if (fileExtension.equals("png")) {
            return validatePNGFile(data);
        }
        return false;
    }

    private static void printBytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b)).append(" ");
        }
        System.out.println(sb);
    }

    /**
     * PNG 파일 시그니처로 유효성 검사
     */
    private static boolean validatePNGFile(byte[] data) {
        if ((data[1] == 0x50) && (data[2] == 0x4E) && (data[3] == 0x47)) {
            return true;
        }
        return false;
    }

    /**
     * JPG 파일 시그니처로 유효성 검사
     */
    private static boolean validateJPGFile(byte[] data) {
        if ((data[0] == (byte) 0xFF) && (data[1] == (byte) 0xD8) && (data[2] == (byte) 0xFF)) {
            return true;
        }
        return false;
    }

    /**
     * MOV 파일 시그니처로 유효성 검사
     */
    private static boolean validateMOVFile(byte[] data) {
        if ((data[4] == 0x66) && (data[5] == 0x74) && (data[6] == 0x79) && (data[7] == 0x70)) {
            return true;
        }
        return false;
    }

    /**
     * MP4 파일 시그니처로 유효성 검사
     */
    private static boolean validateMP4File(byte[] data) {
        if ((data[4] == 0x66) && (data[5] == 0x74) && (data[6] == 0x79) && (data[7] == 0x70)) {
            return true;
        }
        return false;
    }
}
```

### Test File Validate

파일의 헤더 시그니처(시작 부분 8byte) offset 정보를 보면 정상적인 파일은 패턴이 동일한 것을 확인할 수 있습니다.
- MP4: `66 74 79 70`
- MOV: `66 74 79 70`
- JPED: `FF D8 FF`
- PNG: `50 4E 47`

```java
@Test
@DisplayName("540p MP4 비디오 파일 유효성 검사")
public void validate_mp4_540p_video_file() throws Exception {
    // 00 00 00 14 "66 74 79 70"
    assertTrue(FileValidator.validate("../sample/540p.mp4"));
}

@Test
@DisplayName("720p MP4 비디오 파일 유효성 검사")
public void validate_mp4_720p_video_file() throws Exception {
    // 00 00 00 1C "66 74 79 70"
    assertTrue(FileValidator.validate("../sample/720p.mp4"));
}

@Test
@DisplayName("1440p MP4 비디오 파일 유효성 검사")
public void validate_mp4_1440p_video_file() throws Exception {
    // 00 00 00 1C "66 74 79 70"
    assertTrue(FileValidator.validate("../sample/1440p.mp4"));
}

@Test
@DisplayName("540p MOV 비디오 파일 유효성 검사")
public void validate_mov_540p_video_file() throws Exception {
    // 00 00 00 14 "66 74 79 70"
    assertTrue(FileValidator.validate("../sample/540p.mov"));
}

@Test
@DisplayName("720p MOV 비디오 파일 유효성 검사")
public void validate_mov_720p_video_file() throws Exception {
    // 00 00 00 14 "66 74 79 70"
    assertTrue(FileValidator.validate("../sample/720p.mov"));
}

@Test
@DisplayName("1440p MOV 비디오 파일 유효성 검사")
public void validate_mov_1440p_video_file() throws Exception {
    // 00 00 00 14 "66 74 79 70"
    assertTrue(FileValidator.validate("../sample/1440p.mov"));
}

@Test
@DisplayName("JPEG 이미지 파일 유효성 검사")
public void validate_jpeg_file() throws Exception {
    // "FF D8 FF" E0 00 10 4A 46
    assertTrue(FileValidator.validate("../sample/test.jpg"));
}

@Test
@DisplayName("PNG 이미지 파일 유효성 검사")
public void validate_png_file() throws Exception {
    // 89 "50 4E 47" 0D 0A 1A 0A
    assertTrue(FileValidator.validate("../sample/aaa.png"));
}
```

하지만, 업로드 가능한 확장자로 변경한 바이러스 파일은 유효성 검사에서 실패하게 됩니다. 

```java
@Test
@DisplayName("MP4로 확장자만 변경한 ZIP 파일은 유효성 검사 실패")
public void validate_zip_to_mp4_file() throws Exception {
    // 50 4B 03 04 "14 00 08 00"
    assertFalse(FileValidator.validate("../sample/zip_file.mp4"));
}

@Test
@DisplayName("JPG로 확장자만 변경한 ZIP 파일은 유효성 검사 실패")
public void validate_zip_to_jpg_file() throws Exception {
    // "50 4B 03" 04 14 00 08 00
    assertFalse(FileValidator.validate("../sample/zip_file.jpg"));
}

@Test
@DisplayName("MOV로 확장자만 변경한 PKG 파일은 유효성 검사 실패")
public void validate_pkg_to_mov_file() throws Exception {
    // 78 61 72 21 "00 1C 00 01"
    assertFalse(FileValidator.validate("../sample/pkg_file.mov"));
}
```

파일 업로드 시 단순하게 업로드 가능한 확장자인지만 검사하고 넘어갈 수도 있었는데, 팀원의 호기심 덕분에 파일 유효성 검사를 추가하여 안전성을 높일 수 있었답니다.🙇🏻‍♂️

## 마무리

동영상 업로드 기능을 구현하면서 모든 파일에 시그니처가 있고, 해당 시그니처로 파일 유효성을 검사할 수 있다는 재미있는 사실을 알게 되었던 시간이었답니다.<br/>
읽으시면서 궁금하신 사항이나 개선 사항이 보이신다면 언제든 아래 코멘트 부탁드립니다.<br/>
글을 읽어주신 모든 분께 감사드립니다. 🙇🏻‍

## Reference

- [GCK'S FILE SIGNATURES TABLE](https://www.garykessler.net/library/file_sigs.html)
- [List of file signatures](https://en.wikipedia.org/wiki/List_of_file_signatures)
- [Active File Recovery](https://www.file-recovery.com/signatures.htm)
- [PNG](https://ko.wikipedia.org/wiki/PNG)