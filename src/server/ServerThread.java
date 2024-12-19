package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;

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
                } else if (message.startsWith("JOIN_ROOM:")) {
                    this.roomName = message.split(":")[1];
                    ServerM.roomMap.computeIfAbsent(roomName, k -> new CopyOnWriteArrayList<>()).add(this);
                    System.out.println(this.name + " joined room: " + this.roomName);
                } else if (message.startsWith("MESSAGE_TO_ID:")) {
                    String[] parts = message.split(":", 3);
                    String targetId = parts[1];
                    String chatMessage = parts[2];
                    sendMessageToId(targetId, chatMessage);
                } else if (message.startsWith("MESSAGE:")) {
                    String chatMessage = message.split(":", 2)[1];
                    broadcastMessage(chatMessage);
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

    private void broadcastMessage(String message) {
        if (roomName == null) {
            System.out.println("Client " + this.name + " is not in a room.");
            return;
        }

        for (ServerThread client : ServerM.roomMap.get(roomName)) {
            try {
                if (client != this && client.active) {
                    client.os.writeUTF(message);
                }
            } catch (IOException e) {
                System.out.println("Error broadcasting to " + client.name);
            }
        }
    }

    private void sendMessageToId(String targetId, String message) {
        ServerThread targetClient = clientsById.get(targetId);
        if (targetClient != null && targetClient.active) {
            try {
                targetClient.os.writeUTF(message);
                System.out.println("Message sent to ID " + targetId + ": " + message);
            } catch (IOException e) {
                System.out.println("Error sending message to ID " + targetId);
            }
        } else {
            System.out.println("Client with ID " + targetId + " not found or inactive.");
        }
    }

    private void cleanup() {
        try {
            if (roomName != null) {
                ServerM.roomMap.get(roomName).remove(this);
                if (ServerM.roomMap.get(roomName).isEmpty()) {
                    ServerM.roomMap.remove(roomName);
                }
            }
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