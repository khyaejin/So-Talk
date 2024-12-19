package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 애플리케이션의 주요 로직을 담당하는 클래스
 * GUI 초기화 및 네트워크 연결 관리
 */
public class MessengerApp {
    final static int ServerPort = 6000; // 포트 번호
    private MessengerFrame frame; // 메인 GUI 프레임
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public void start() throws IOException {
        // GUI 생성
        frame = new MessengerFrame();

        // 네트워크 초기화
        InetAddress ip = InetAddress.getByName("localhost"); // 서버 주소 확인
        System.out.println("Connecting to server: " + ip);
        Socket socket = new Socket(ip, ServerPort); // 소켓 연결

        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        frame.setOutputStream(outputStream); // 프레임에 출력 스트림 전달

        // 로그인 후 사용자 ID 전송
        frame.showStartPanel();
        waitForLogin();

        // 메시지 수신 스레드 시작
        new Thread(() -> {
            while (true) {
                try {
                    String message = inputStream.readUTF();
                    frame.updateChattingRoomText("Server", message, false);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    private void waitForLogin() {
        new Thread(() -> {
            while (true) {
                try {
                    // 사용자 이름이 설정되었는지 확인
                    String userName = frame.getUserName();
                    if (userName != null && !userName.isEmpty()) {
                        String userId = frame.getUserId();
                        if (userId != null) {
                            // 서버에 ID와 이름 전송
                            outputStream.writeUTF("SET_ID:" + userId);
                            outputStream.writeUTF("SET_NAME:" + userName);
                            frame.showHomePanel(); // 홈 화면으로 전환
                            break;
                        }
                    }
                    Thread.sleep(100); // 짧은 대기 후 다시 확인
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
