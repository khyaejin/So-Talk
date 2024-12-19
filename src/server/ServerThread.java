package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class ServerThread extends Thread {
    private String name; // 클라이언트 이름
    private String userId; // 클라이언트 ID
    private String chattingWith; // 현재 채팅 중인 상대방 ID
    private final DataInputStream is;
    private final DataOutputStream os;
    private final Socket socket;
    private boolean active;

    // 서버 전체 클라이언트 ID 관리
    public static final HashMap<String, ServerThread> clientsById = new HashMap<>();

    public ServerThread(Socket socket, DataInputStream is, DataOutputStream os) {
        this.is = is;
        this.os = os;
        this.socket = socket;
        this.active = true;
    }

    @Override
    public void run() {
        try {
            while (active) {
                String message = is.readUTF(); // 클라이언트로부터 메시지 읽기
                System.out.println("[Server] 메시지 수신: " + message);

                // 메세지 형식으로 구분(이래도 되나? 몰루 ㅠ)
                // 클라이언트 ID 설정
                if (message.startsWith("SET_ID:")) {
                    this.userId = message.split(":")[1];
                    clientsById.put(this.userId, this); // ID로 클라이언트를 저장
                    System.out.println("[Server] 클라이언트 ID가 설정됨: " + this.userId);
                } // 클라이언트 Name 설정
                else if (message.startsWith("SET_NAME:")) {
                    this.name = message.split(":")[1];
                    System.out.println("[Server] 클라이언트 이름이 설정딤: " + this.name);
                } // 채팅 상대 설정(채팅방 입장 시)
                else if (message.startsWith("CHAT_WITH:")) {
                    // 채팅 상대 설정
                    this.chattingWith = message.split(":")[1];
                    System.out.println("[Server] ID " + this.userId + "와 ID " + this.chattingWith + "의 채팅 시작");
                } // 채팅 전송, 밑에 두개 하나로 수정해야함..
                else if (message.startsWith("MESSAGE:")) {
                    // 메시지를 설정된 채팅 상대에게 전송
                    if (this.chattingWith != null) {
                        String chatMessage = message.split(":", 2)[1];
                        System.out.println("[Server] ID " + this.userId + "에서 ID " + this.chattingWith + "로 메시지 전달: " + chatMessage);
                        sendMessageToId(this.chattingWith, "MESSAGE_FROM:" + this.userId + ":" + chatMessage);
                    } else {
                        System.out.println("[Server] 채팅 상대가 설정되지 않았습니다. 메시지를 전송할 수 없습니다.");
                    }
                } else if (message.startsWith("MESSAGE_TO_ID:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        String targetId = parts[1];
                        String chatMessage = parts[2];
                        System.out.println("[Server] ID " + this.userId + "에서 ID " + targetId + "로 메시지 전달 요청: " + chatMessage);
                        sendMessageToId(targetId, "MESSAGE_FROM:" + this.userId + ":" + chatMessage);
                    } else {
                        System.out.println("[Server] MESSAGE_TO_ID 명령의 형식이 잘못되었습니다: " + message);
                    }
                } else {
                    System.out.println("[Server] 알 수 없는 명령어입니다: " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("[Server] 클라이언트 연결이 종료되었습니다: ID " + this.userId);
            active = false; // 스레드 종료
        } finally {
            cleanup();
        }
    }

    private void sendMessageToId(String targetId, String message) {
        ServerThread targetClient = clientsById.get(targetId);
        if (targetClient != null && targetClient.active) {
            try {
                System.out.println("[Server] ID " + targetId + "에게 메시지를 전송합니다: " + message);
                targetClient.os.writeUTF(message); // 대상 클라이언트에 메시지 전송
            } catch (IOException e) {
                System.out.println("[Server] ID " + targetId + "로 메시지를 전송하는 중 오류가 발생했습니다.");
            }
        } else {
            System.out.println("[Server] ID " + targetId + " 클라이언트가 비활성 상태이거나 존재하지 않습니다.");
        }
    }
    private void cleanup() {
        try {
            if (userId != null) {
                clientsById.remove(userId);
                System.out.println("[Server] ID " + userId + " 클라이언트가 제거되었습니다.");
            }
            is.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
