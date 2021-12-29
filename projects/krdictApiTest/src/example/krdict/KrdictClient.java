package example.krdict;

import example.krdict.entity.RequestEntity;
import example.krdict.entity.ResponseEntity;
import example.krdict.entity.Sense;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class KrdictClient {

    private static final String KEY = "";
    private static final String apiURL = "https://krdict.korean.go.kr/api/search";

    public static void main(String[] args) {

        String q = "나무";
        RequestEntity requestEntity = new RequestEntity(KEY, q);

        try {
            System.out.println(apiURL + requestEntity.getParameter());
            URL url = new URL(apiURL + requestEntity.getParameter());
            StringBuffer response = getResponse(url);

            ResponseEntity responseEntity = responseParsing(response.toString());
            System.out.println("responseEntity = " + responseEntity);
            //https://krdict.korean.go.kr/api/search?key=012A1EDE631d838316C58FFD480AC286&q=나무&advanced=y&method=exact&translated=y&trans_lang=1
            //responseEntity = ResponseEntity(word=나무, pos=명사, senseList=[Sense(definition=단단한 줄기에 가지와 잎이 달린, 여러 해 동안 자라는 식물., enWord=tree, enDefinition=A plant with a hard stem, branches and leaves.), Sense(definition=집이나 가구 등을 만드는 데 사용하는 재목., enWord=wood, enDefinition=The material used to build a house or to make furniture.), Sense(definition=불을 때기 위해 베어 놓은 나무의 줄기나 가지., enWord=timber; log, enDefinition=The trunk or branches of a tree cut to be used as firewood.)])

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