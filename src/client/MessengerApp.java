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

        // 메시지 수신 스레드 시작
        new Thread(() -> {
            while (true) {
                try {
                    String message = inputStream.readUTF();
                    System.out.println("[Client] 메시지 수신: " + message);

                    // 메시지가 수신되면 ChattingRoomPanel에 업데이트
                    if (message.startsWith("MESSAGE_FROM:")) {
                        String[] parts = message.split(":", 3);
                        String senderId = parts[1];
                        String chatMessage = parts[2];

                        // 상대방의 메시지를 채팅방에 업데이트
                        frame.getChattingRoomPanel().updateChattingText("ID " + senderId, chatMessage, false, null);
                        System.out.println("[Client] ID " + senderId + "로부터 메시지 수신: " + chatMessage);
                    }
                } catch (IOException e) {
                    System.out.println("[Client] 서버와의 연결이 끊어졌습니다.");
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
}
