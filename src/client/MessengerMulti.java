package client;

import java.io.IOException;

/**
 * 클라이언트측 메인 클래스
 * 애플리케이션 전체 관리 코드
 */
public class MessengerMulti {
    public static void main(String[] args) throws IOException {
        // GUI 및 네트워크 통신 시작
        MessengerApp app = new MessengerApp();
        app.start();
    }
}