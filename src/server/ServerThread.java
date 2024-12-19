package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerThread extends Thread {
    private String name; // 클라이언트 이름
    private String roomName; // 클라이언트가 속한 채팅방 이름
    private String userId; // 클라이언트 ID
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

                if (message.startsWith("SET_ID:")) {
                    this.userId = message.split(":")[1];
                    clientsById.put(this.userId, this); // ID로 클라이언트를 저장
                    System.out.println("Client set ID: " + this.userId);
                } else if (message.startsWith("SET_NAME:")) {
                    this.name = message.split(":")[1];
                    System.out.println("Client set name: " + this.name);
                } else if (message.startsWith("MESSAGE_TO_ID:")) {
                    // 특정 사용자에게 메시지 전송
                    String[] parts = message.split(":", 3);
                    String targetId = parts[1];
                    String chatMessage = parts[2];
                    sendMessageToId(targetId, "From " + userId + ": " + chatMessage);
                } else {
                    System.out.println("Unknown command: " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + this.name);
            active = false; // 스레드 종료
        } finally {
            cleanup();
        }
    }

    private void sendMessageToId(String targetId, String message) {
        ServerThread targetClient = clientsById.get(targetId);
        if (targetClient != null && targetClient.active) {
            try {
                targetClient.os.writeUTF(message); // 대상 클라이언트에 메시지 전송
                System.out.println(this.userId + "로 부터 " + targetId + "에게 메세지 보내기: " + message);
            } catch (IOException e) {
                System.out.println("Error sending message to ID " + targetId);
            }
        } else {
            System.out.println("Client with ID " + targetId + " not found or inactive.");
        }
    }

    private void cleanup() {
        try {
            if (userId != null) {
                clientsById.remove(userId);
            }
            is.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
