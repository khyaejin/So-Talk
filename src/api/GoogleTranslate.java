package api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleTranslate {
    public static String translate(String text, String targetLanguage, String apiKey) {
        try {
            String urlStr = "https://translation.googleapis.com/language/translate/v2";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // HTTP POST 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // 요청 본문 작성
            String requestBody = String.format(
                    "{\"q\":\"%s\",\"target\":\"%s\",\"format\":\"text\",\"key\":\"%s\"}",
                    text, targetLanguage, apiKey
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes("UTF-8"));
            }

            // 응답 읽기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // JSON 파싱 (단순 문자열 처리)
            String jsonResponse = response.toString();
            String translatedText = jsonResponse.split("\"translatedText\":\"")[1].split("\"")[0];
            return translatedText;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred during translation.";
        }
    }

    public static void main(String[] args) {
        String apiKey = "AIzaSyB_Zkh4d4Y2LteeF5wwiCPFvWmIfI_eK0U"; // Google Cloud API 키
        String text = "안녕하세요";
        String targetLanguage = "en"; //영어로

        String translatedText = translate(text, targetLanguage, apiKey);
        System.out.println("Translated Text: " + translatedText);
    }
}
