# Request multipart/form-data type API by attaching a file

서버단에서 multipart/form-data 타입으로 파일을 첨부해서 요청을 보내본 적은 없었는데..

이번에 관련 작업을 하면서 정리를 해보고자 한다.

(여기서 파일은 이미지 URL 을 저장해서 보낸다.)

## MultipartFile

multipart/form-data 타입으로 Request 시 MultipartFile 객체가 필요하다.

```java
@PostMapping(value = "/sample/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
SampleResponse uploadImage(MultipartFile upload, @RequestHeader(value = "Authorization") String token);
```

MultipartFile 객체를 생성하는 방법은 다양하나, FileItem 객체를 활용하여 CommonsMultipartFile 객체를 생성하게 되었다. 
(이 방법을 선택한 이유는 딱히 없다.. 가장 괜찮아 보여서..? 더 좋은 방법이 있다면 알려쥬세요..😯)

```java
MultipartFile image = new CommonsMultipartFile(fileItem);
```

### CommonsMultipartFile

참고로.. 이상하게 `@RequestPart(value = "upload") MultipartFile upload` upload 라는 이름으로 MultipartFile 내용을 보내는데, MultipartFile key name 을 @RequestPart 의 value 값을 보지 않고, CommonsMultipartFile 생성 시 넣어준 fileItem 의 fileName 을 참조하고 있다.

fileItem 의 이름이 우선권이 있는 건지 이 부분은 아직 자세히는 모르겠다..🥲

```java
@Override
public String getName() {
    return this.fileItem.getFieldName();
}
```

결론은, 파일을 담고 있는 MultipartFile key name 이 upload 라면 (ex. `--form 'upload=@..'`)

upload 라는 이름은 FileItem 생성 시 명시해 준 fileName 을 참조하게 된다.

```java
final FileItem fileItem = new DiskFileItem("upload" // fileName
    , Files.probeContentType(tempFile.toPath())
    , false
    , tempFile.getName()
    , (int) tempFile.length(),
    tempFile.getParentFile());
```

### MultipartFile 생성 과정

서버에 저장된 이미지를 tmpFile 이 MultipartFile 객체로 변환되는 과정이다.

```java
@Override
public void uploadImage(SampleRequestBundle bundle) throws SampleException {
    String serverType = System.getProperty("server.type", "real");
    String imageUrl = bundle.getPrdImgUrl();
    File tempFile = null;
    InputStream is = null;
    OutputStream os = null;
    try {
        String[] tokens = imageUrl.split("\\.");
        String extension = tokens[tokens.length - 1];
        final URL url = new URL(imageUrl);
        if ("dev".equals(serverType)) {
            try (InputStream inputStream = url.openStream()) {
                tempFile = File.createTempFile(String.valueOf(inputStream.hashCode()), "." + extension);
                copyInputStreamToFile(inputStream, tempFile);
            }

            is = new FileInputStream(tempFile);
        } else { // 이미지가 저장된 서버에 접근하기 위해 Proxy 사용 시
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.sample.co.kr", 3333)); 
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, new SecureRandom());

            HttpsURLConnection connection = getProxyHttpsURLConnection(url, proxy, sslContext);
            try (InputStream tmpInputStream = connection.getInputStream()) {
                tempFile = File.createTempFile(String.valueOf(tmpInputStream.hashCode()), "." + extension);
                copyInputStreamToFile(tmpInputStream, tempFile);
            }

            // connection 을 다시 얻어오고 있는데.. 
            // copyInputStreamToFile() 에서 InputStream 의 스트림을 닫아버려서 어쩔 수 없는 것 같다..
            HttpsURLConnection reConnection = getProxyHttpsURLConnection(url, proxy, sslContext);
            is = reConnection.getInputStream();
        }

        final FileItem fileItem = new DiskFileItem("upload"
                , Files.probeContentType(tempFile.toPath())
                , false
                , tempFile.getName()
                , (int) tempFile.length(),
                tempFile.getParentFile());
        os = fileItem.getOutputStream();
        IOUtils.copy(is, os);

        MultipartFile image = new CommonsMultipartFile(fileItem);
        bundle.setSampleImageResponse(sampleApiService.uploadImage(image, bundle.getAuthToken()));
    } catch (Exception e) {
        log.error("fail...");
        throw new SampleException(SampleErrorMessage.GLOBAL_ERROR_MESSAGE.getMessage(), e);
    } finally {
        // 이쪽도 더 깔끔하게 만들 수 있는 방법이 없을까..
        if (tempFile != null) {
            tempFile.deleteOnExit();
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}

private HttpsURLConnection getProxyHttpsURLConnection(URL url, Proxy proxy, SSLContext sslContext) throws IOException {
    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(proxy);
    connection.setSSLSocketFactory(sslContext.getSocketFactory());
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(5000);
    connection.setUseCaches(false);
    connection.setDoInput(true);
    connection.setDoOutput(true);
    return connection;
}
```

### Service

여기는 MultipartFile 객체를 전달해주는 거쳐가는 단계..

**SampleApiServiceImpl.java**

```java
@Override
public SampleImageResponse uploadImage(MultipartFile image, String token) throws SampleException {
    SampleImageResponse response = null;
    int httpStatus = -1;
    try {
        response = sampleClient.uploaddImage(image, token);
        httpStatus = response.getHttpStatus();
    } catch (Exception e) {
        log.error("fail..", e);
        httpStatus = response.getHttpStatus();
        throw new SampleException(SampleErrorMessage.GLOBAL_ERROR_MESSAGE.getMessage(), e);
    }

    return response;
}
```

**SampleApiClientConfiguration.java**

요청은 FeignClient 방식으로 하고 있다.

multipart/form-data 타입으로 요청을 하기 위해 Encoder, Decoder 가 필요하므로 FeignClient 에 사용할 Configuration 를 생성하자.

```java
public class SampleApiClientConfiguration {
    @Bean
    public Encoder multipartFormEncoder() {
        return new SpringEncoder(() -> new HttpMessageConverters(new RestTemplate().getMessageConverters()));
    }

    @Bean
    public Decoder multipartFormDecoder() {
        return new SpringDecoder(() -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter()));
    }
}
```

**SampleClient.java**

encoder, decoder 를 빈으로 등록하는 Configuration 을 같이 주입해 주자.

```java
@FeignClient(
        name = "sample",
        url = "https://api.domain.com",
        fallbackFactory = SampleApiClientFallbackFactory.class,
        configuration = {SampleApiClientConfiguration.class}
)
public interface SampleClient {
    //...

    @PostMapping(value = "/sample/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    SampleImageResponse uploadImage(MultipartFile upload, @RequestHeader(value = "Authorization") String token);
}
```

curl 로 요청시에는 아래와 같다.

```bash
curl --location 'https://api.domain.com/sample/image' \
--header 'Content-Type: multipart/form-data;charset=UTF-8;' \
--header 'Authorization: abCDeF...' \
--form 'upload=@"/var/../../image.jpg"'
```

.

마무리..

.

@RequestPart value 값이 제대로 적용되지 않고 FileItem fileName 이 우선으로 적용되는 부분. (이건 미스테리를 좀 풀어보아야겠다...)

FeignClient 에서 multipart/form-data 타입으로 요청을 하기 위해 Encoder, Decoder 가 필요했던 부분에서 많이 헤맸던 것 같다..