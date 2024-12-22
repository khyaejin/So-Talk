package client;

import javax.swing.*;
import java.awt.*;
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
                    // 메시지의 첫 부분은 항상 UTF-8로 읽음 (프로토콜 헤더)
                    String header = inputStream.readUTF();
                    System.out.println("[Client] 메시지 수신: " + header);

                    if (header.startsWith("MESSAGE_FROM:")) {
                        // 텍스트 메시지 처리
                        String[] parts = header.split(":", 3);
                        String senderId = parts[1];
                        String chatMessage = parts[2];

                        frame.getChattingRoomPanel().updateChattingText("ID " + senderId, chatMessage, false);
                        System.out.println("[Client] ID " + senderId + "로부터 메시지 수신: " + chatMessage);
                    } else if (header.startsWith("EMOTICON_FILE:")) {
                        // 파일 메시지 처리
                        String[] parts = header.split(":", 4);
                        String senderId = parts[1];
                        String fileName = parts[2];
                        long fileSize = Long.parseLong(parts[3]);

                        System.out.println("[Client] 파일 수신 요청: " + fileName + " (" + fileSize + " bytes)");

                        // 파일 데이터를 읽음
                        byte[] fileData = new byte[(int) fileSize];
                        inputStream.readFully(fileData);

                        // 채팅창에 이미지로 표시
                        ImageIcon receivedIcon = new ImageIcon(fileData);
                        Image scaledImage = receivedIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        frame.getChattingRoomPanel().updateChattingText("ID " + senderId, new ImageIcon(scaledImage), false);

                        System.out.println("[Client] 파일 수신 완료: " + fileName);
                    } else {
                        System.out.println("[Client] 알 수 없는 메시지 형식: " + header);
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
