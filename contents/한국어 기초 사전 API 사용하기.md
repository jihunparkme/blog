## 한국어 기초 사전 API 사용하기 (aka. 백과사전 API)

[한국어기초사전](https://krdict.korean.go.kr/openApi/openApiInfo)

백과사전 API 를 찾아 보다가, 다국어 번역도 지원하는 무료 API를 발견하여 사용해 보려고 한다.👀

## Start

- Open API 사용 신청 후 인증키를 발급받자.
- `인증키는 사용자 당 한 개만 발급받을 수 있으며 서비스 요청은 하루에 50,000건으로 제한`

**Dependency**

- lombok

**Open Api Request URL**

- https://krdict.korean.go.kr/openApi/openApiInfo
- 검색 요청 변수는 Request Parameters 로 요청되므로 생각보다 간단하다.

**HTTP Request**

```url
https://krdict.korean.go.kr/api/search?key=your_key&q=나무&advanced=y&method=exact&translated=y&trans_lang=1
```

**HTTP Response**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<channel>
    <title>한국어 기초사전 개발 지원(Open API) - 사전 검색</title>
    <link>https://krdict.korean.go.kr</link>
    <description>한국어 기초사전 개발 지원(Open API) - 사전 검색 결과</description>
    <lastBuildDate>20211229150351</lastBuildDate>
    <total>1</total>
    <start>1</start>
    <num>10</num>
    <item>
        <target_code>32750</target_code>
        <word>나무</word>
        <sup_no>0</sup_no>
        <pronunciation>나무</pronunciation>
        <word_grade>초급</word_grade>
        <pos>명사</pos>
        <link>https://krdict.korean.go.kr/dicSearch/SearchView?ParaWordNo=32750</link>
        <sense>
            <sense_order>1</sense_order>
            <definition>단단한 줄기에 가지와 잎이 달린, 여러 해 동안 자라는 식물.</definition>
            <translation>
                <trans_lang>영어</trans_lang>
                <trans_word>
                    <![CDATA[tree]]>
                </trans_word>
                <trans_dfn>
                    <![CDATA[A plant with a hard stem, branches and leaves.]]>
                </trans_dfn>
            </translation>
        </sense>
        <sense>
            <sense_order>2</sense_order>
            <definition>집이나 가구 등을 만드는 데 사용하는 재목.</definition>
            <translation>
                <trans_lang>영어</trans_lang>
                <trans_word>
                    <![CDATA[wood]]>
                </trans_word>
                <trans_dfn>
                    <![CDATA[The material used to build a house or to make furniture.]]>
                </trans_dfn>
            </translation>
        </sense>
        <sense>
            <sense_order>3</sense_order>
            <definition>불을 때기 위해 베어 놓은 나무의 줄기나 가지.</definition>
            <translation>
                <trans_lang>영어</trans_lang>
                <trans_word>
                    <![CDATA[timber; log]]>
                </trans_word>
                <trans_dfn>
                    <![CDATA[The trunk or branches of a tree cut to be used as firewood.]]>
                </trans_dfn>
            </translation>
        </sense>
    </item>
</channel>
```

## Class

### Entity

**RequestEntity.java**

- 결과에 따라 필요한 검색 요청 변수를 추가하면 된다.

```java
@Data
@AllArgsConstructor
public class RequestEntity {

    private String key;
    private String q;
    private String translated;
    private String transLang;
    private String advanced;
    private String method;

    public RequestEntity(String key, String q) {
        this.key = key; // 인증키
        this.q = q; // 검색어
        this.translated = "y"; //다국어 번역 여부
        this.transLang = "1"; //영어
        this.advanced = "y"; //자세히찾기 여부
        this.method = "exact"; //일치 검색
    }

    public String getParameter() {
        return "?key=" + this.key +
                "&q=" + this.q +
                "&advanced=" + this.advanced +
                "&method=" + this.method +
                "&translated=" + this.translated +
                "&trans_lang=" + this.transLang;
    }
}
```

**ResponseEntity.java**

- Response 형태에 맞게 디자인 해보았다.
- 동음이음어&다의어의 경우 모든 결과를 받기 위해 List 에 담았다.

```java
@Data
public class ResponseEntity {

    private String word;
    private String pos;
    private List<Sense> senseList;

}
```

**Sense.java**

```java
@Data
@AllArgsConstructor
public class Sense {

    private String definition;
    private String enWord;
    private String enDefinition;

}

```

### Client

**KrdictClient.java**

```java
public class KrdictClient {

    private static final String KEY = "your_key";
    private static final String apiURL = "https://krdict.korean.go.kr/api/search";

    public static void main(String[] args) {

        String q = "나무";
        RequestEntity requestEntity = new RequestEntity(KEY, q);

        try {
            URL url = new URL(apiURL + requestEntity.getParameter());
            StringBuffer response = getResponse(url);

            ResponseEntity responseEntity = responseParsing(response.toString());
            System.out.println("responseEntity = " + responseEntity);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static StringBuffer getResponse(URL url) throws IOException {

        // refer : https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/HttpURLConnection.html
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setReadTimeout(30000);
        con.setRequestMethod("GET");
        con.connect();

        int responseCode = con.getResponseCode();
        BufferedReader br;
        if (responseCode == 200) {
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
        br.close();

        return response;
    }

    private static ResponseEntity responseParsing(String response) {

        ResponseEntity responseEntity = new ResponseEntity();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // process XML securely, avoid attacks like XML External Entities (XXE) -> Optional
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(response)));

            // DocumentElement normalize -> Optional
            document.getDocumentElement().normalize();

            // get item tag
            NodeList nodeList = document.getElementsByTagName("item");

            for (int idx = 0; idx < nodeList.getLength(); idx++) {

                Node node = nodeList.item(idx);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;
                    String word = element.getElementsByTagName("word").item(0).getTextContent();
                    String pos = element.getElementsByTagName("pos").item(0).getTextContent();

                    List<Sense> senseList = getSenseList(document);
                    responseEntity.setWord(word);
                    responseEntity.setPos(pos);
                    responseEntity.setSenseList(senseList);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        return responseEntity;
    }

    private static List<Sense> getSenseList(Document document) {

        List<Sense> result = new ArrayList<>();
        // get sense tag
        NodeList list = document.getElementsByTagName("sense");

        for (int idx = 0; idx < list.getLength(); idx++) {
            
            Node node = list.item(idx);

            Element element = (Element) node;
            String definition = element.getElementsByTagName("definition").item(0).getTextContent();
            String trans_word = element.getElementsByTagName("trans_word").item(0).getTextContent();
            String trans_dfn = element.getElementsByTagName("trans_dfn").item(0).getTextContent();

            result.add(new Sense(definition, trans_word, trans_dfn));
        }

        return result;
    }
}
```

## Result

```console
responseEntity = ResponseEntity(
	word=나무, 
	pos=명사, 
	senseList=[
		Sense(
			definition=단단한 줄기에 가지와 잎이 달린, 여러 해 동안 자라는 식물., 
			enWord=tree, 
			enDefinition=A plant with a hard stem, branches and leaves.), 
		Sense(
			definition=집이나 가구 등을 만드는 데 사용하는 재목., 
			enWord=wood, 
			enDefinition=The material used to build a house or to make furniture.), 	
		Sense(
			definition=불을 때기 위해 베어 놓은 나무의 줄기나 가지., 
			enWord=timber; log, 
			enDefinition=The trunk or branches of a tree cut to be used as firewood.)])
```

## Code

[project](https://github.com/jihunparkme/blog/tree/main/projects/krdictApiTest)

## Reference

- https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html
- https://docs.oracle.com/javase/8/docs/api/org/w3c/dom/Document.html
- https://docs.oracle.com/javase/8/docs/api/org/w3c/dom/Element.html
- https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/