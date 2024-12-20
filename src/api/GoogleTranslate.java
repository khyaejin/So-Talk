package api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class GoogleTranslate {
    private static final String PROJECT_ID = "so-talk-445218";
    private static final String CREDENTIALS_FILE_PATH = "src/api/so-talk-OAuth2.json";

    // OAuth2 토큰 생성 메서드
    private String getAccessToken() throws Exception {
        // 서비스 계정 JSON 파일 읽기
        StringBuilder credentialsJson = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(CREDENTIALS_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                credentialsJson.append(line.trim());
            }
        }

        // JSON에서 필요한 필드 추출 (수동 파싱)
        String clientEmail = extractJsonValue(credentialsJson.toString(), "client_email");
        String privateKey = extractJsonValue(credentialsJson.toString(), "private_key").replace("\\n", "\n");

        // JWT 생성
        long now = System.currentTimeMillis() / 1000;
        long expiry = now + 3600; // 1시간 유효

        String jwtHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes("UTF-8"));
        String jwtClaim = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((
                        "{\"iss\":\"" + clientEmail + "\",\"scope\":\"https://www.googleapis.com/auth/cloud-translation\","
                                + "\"aud\":\"https://oauth2.googleapis.com/token\",\"exp\":" + expiry + ",\"iat\":" + now + "}"
                ).getBytes("UTF-8"));

        String jwtUnsigned = jwtHeader + "." + jwtClaim;
        String signedJwt = jwtUnsigned + "." + RSASigner.sign(jwtUnsigned, privateKey);

        // 토큰 요청
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        String requestBody = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=" + signedJwt;
        System.out.println("Request Body: " + requestBody); // 디버깅용 출력

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes("UTF-8"));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();

        // 응답에서 액세스 토큰 추출
        return extractJsonValue(response.toString(), "access_token");
    }

    public String translate(String text, String targetLanguage) {
        try {
            String urlStr = String.format(
                    "https://translation.googleapis.com/v3beta1/projects/%s:translateText", PROJECT_ID
            );
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // HTTP POST 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + getAccessToken()); // OAuth2 액세스 토큰 추가
            conn.setDoOutput(true);

            // 요청 본문 작성
            String requestBody = String.format(
                    "{ \"contents\": [\"%s\"], \"targetLanguageCode\": \"%s\", \"mimeType\": \"text/plain\" }",
                    text, targetLanguage
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes("UTF-8"));
            }

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.err.println("Error Response: " + errorResponse.toString());
                throw new RuntimeException("HTTP Error: " + responseCode + ", " + errorResponse);
            }

            // 응답 읽기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // JSON 응답 파싱
            return extractJsonValue(response.toString(), "translatedText");

        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred during translation.";
        }
    }

    // JSON에서 값 추출 (수동 파싱)
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey) + searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (startIndex == -1 || endIndex == -1) {
            throw new RuntimeException("Invalid JSON or missing key: " + key);
        }
        return json.substring(startIndex, endIndex);
    }
}
