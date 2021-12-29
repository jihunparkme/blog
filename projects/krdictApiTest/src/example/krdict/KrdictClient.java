package example.krdict;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class KrdictClient {

    private static final String KEY = "92FAC02C710BED25F78AA77973C420EB";
    private static final String apiURL = "https://krdict.korean.go.kr/api/search";

    public static void main(String[] args) {

        String q = "나무";
        RequestEntity requestEntity = new RequestEntity("q");

        try {
            URL url = new URL(apiURL + requestEntity.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setReadTimeout(30000);
            con.setRequestMethod("POST");
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

            System.out.println(response);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}