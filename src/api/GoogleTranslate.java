package api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;

public class GoogleTranslate {
    /**
     * "인증 없이" 구글 웹 번역기의 비공식 엔드포인트 호출 예시
     * - 공식 API가 아니므로 언제든지 동작이 중단될 수 있음
     */
    public String translate(String text, String targetLanguage) {
        try {
            // 원문 언어: 자동 감지(auto detect)를 의미하기 위해 'sl=auto' (또는 sl=en 등)
            // targetLanguage: ko, en, ja, etc.
            // dt=t: 번역 결과를 받기 위한 파라미터
            // client=gtx: 웹에서 사용하는 것으로 추정되는 파라미터 (비공식)

            String sourceLang = "auto"; // 자동 감지
            String encodedText = URLEncoder.encode(text, "UTF-8");

            // 예: https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=ko&dt=t&q=Hello
            String urlStr = String.format(
                    "https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                    sourceLang, targetLanguage, encodedText
            );

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            // User-Agent를 지정해주지 않으면 403이 날 때가 있음(크롤링 방지용)

            // 응답 읽기
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), "UTF-8")
                );
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                throw new RuntimeException(
                        "HTTP Error: " + responseCode + ", " + errorResponse
                );
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8")
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // translate_a/single 의 응답은 예: [[["안녕","Hello",null,null,1]],null,"en",...]
            // JSON 배열 형태지만, 공식 형식이 아님
            // 간단히 파싱: 첫 번째 배열의 첫 번째 배열의 첫 번째 요소가 번역 결과

            String result = parseTranslateResult(response.toString());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred during translation.";
        }
    }

    /**
     * translate_a/single 비공식 응답 파싱
     * 예) [[["안녕","Hello",null,null,1]],null,"en",...]
     *   -> "안녕"
     */
    private String parseTranslateResult(String rawJson) {
        // 간단 무식 파싱: 예) [[["안녕","Hello", ...
        // - 제일 앞쪽의 [[[" 이 이후, " 로 감싸진 문자열을 찾음
        // (실제 응답 형식이 변경될 수 있으므로, robust 하지 않음)
        try {
            // 첫 번째 ["...","..."] 패턴 찾기
            // rawJson 예: [[["안녕","Hello",null,null,1]],null,"en"]
            // 번역 텍스트는 첫 번째 2차원 배열 [0][0] 위치에 있음
            // 정규식을 써도 되고, JSON 파서를 써도 되지만 여기서는 간단히 문자열 조작

            // "[[[" 다음을 찾음
            int startIndex = rawJson.indexOf("[[[");
            if (startIndex == -1) return null;

            // 그 뒤로 " 문자열 찾기
            startIndex = rawJson.indexOf("\"", startIndex);
            if (startIndex == -1) return null;
            int endIndex = rawJson.indexOf("\"", startIndex + 1);
            if (endIndex == -1) return null;

            // 추출
            String translatedText = rawJson.substring(startIndex + 1, endIndex);

            return translatedText;
        } catch (Exception e) {
            // 응답 형식이 바뀌었거나 예외가 생긴 경우 처리
            e.printStackTrace();
            return null;
        }
    }
}
