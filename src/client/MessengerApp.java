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

                    if (message.startsWith("EMOTICON_FILE:")) {
                        // 이미지 파일 처리
                        handleEmoticonFile(message);
                    } else if (message.startsWith("MESSAGE_FROM:")) {
                        // 일반 메시지 처리
                        handleTextMessage(message);
                    } else {
                        System.out.println("[Client] 알 수 없는 메시지 유형: " + message);
                    }
                } catch (IOException e) {
                    System.out.println("[Client] 서버와의 연결이 끊어졌습니다.");
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    /**
     * 이미지 파일 처리
     */
    private void handleEmoticonFile(String message) {
        try {
            String[] parts = message.split(":");
            String sender = parts[1];
            String fileName = parts[2];
            long fileSize = Long.parseLong(parts[3]);

            // 이미지 데이터 읽기
            byte[] buffer = new byte[(int) fileSize];
            inputStream.readFully(buffer);

            // ChattingRoomPanel에 이미지 업데이트
            frame.getChattingRoomPanel().receiveEmoticon(sender, buffer);
            System.out.println("[Client] ID " + sender + "로부터 이미지 수신: " + fileName);

        } catch (IOException e) {
            System.out.println("[Client] 이모티콘 파일 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 텍스트 메시지 처리
     */
    private void handleTextMessage(String message) {
        String[] parts = message.split(":", 3);
        String senderId = parts[1];
        String chatMessage = parts[2];

        // ChattingRoomPanel에 텍스트 메시지 업데이트
        frame.getChattingRoomPanel().updateChattingText(senderId, chatMessage, false, null);
        System.out.println("[Client] ID " + senderId + "로부터 메시지 수신: " + chatMessage);
    }
}
