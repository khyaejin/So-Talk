package api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GPT {
    public static String chatWithGPT(String prompt, String apiKey) {
        try {
            String apiUrl = "https://api.openai.com/v1/completions";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // HTTP POST 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 요청 본문 작성
            String requestBody = String.format(
                    "{ \"model\": \"text-davinci-003\", \"prompt\": \"%s\", \"max_tokens\": 100 }",
                    prompt
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

            // JSON 파싱 -> 보완 필요
            String jsonResponse = response.toString();
            String generatedText = jsonResponse.split("\"text\":\"")[1].split("\"")[0];
            return generatedText.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while communicating with GPT.";
        }
    }

    public static void main(String[] args) {
        String apiKey = "YOUR_OPENAI_API_KEY"; // OpenAI API 키
        String prompt = "Translate this sentence to Korean: Hello, how are you?";

        String response = chatWithGPT(prompt, apiKey);
        System.out.println("GPT Response: " + response);
    }
}
