package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerThread extends Thread {
    private final String name; // 클라이언트 이름
    private final DataInputStream is;
    private final DataOutputStream os;
    private final Socket socket;
    private boolean active;

    public ServerThread(Socket socket, String name, DataInputStream is, DataOutputStream os) {
        this.is = is;
        this.os = os;
        this.name = name;
        this.socket = socket;
        this.active = true;
    }

    @Override
    public void run() {
        try {
            String message;
            while (active) {
                try {
                    message = is.readUTF(); // 클라이언트로부터 메시지 읽기
                    System.out.println(message);

                    // 메시지 브로드캐스트
                    broadcastMessage(name, message);
                } catch (IOException e) {
                    System.out.println(name + " disconnected.");
                    active = false; // 스레드 종료
                    break;
                }
            }
        } finally {
            cleanup(); // 리소스 정리
        }
    }

    private void broadcastMessage(String sender, String message) {
        for (ServerThread client : ServerM.list) {
            try {
                if (client != this && client.active) { // 자신에게는 보내지 않음
                    client.os.writeUTF(message);
                }
            } catch (IOException e) {
                System.out.println("Error broadcasting to " + client.name);
            }
        }
    }

    private void cleanup() {
        try {
            ServerM.list.remove(this); // 서버 리스트에서 제거
            is.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
