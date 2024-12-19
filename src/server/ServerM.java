package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerM {
    // 채팅방별 사용자 리스트를 저장하는 맵
    static ConcurrentHashMap<String, CopyOnWriteArrayList<ServerThread>> roomMap = new ConcurrentHashMap<>(); // 채팅방 관리

    public static void main(String[] args) throws IOException {
        // 1. 서버 소켓 생성
        ServerSocket ssocket = new ServerSocket(6000);

        System.out.println("Server started on port 6000.");

        // 2. 클라이언트 연결을 기다리는 무한 루프
        while (true) {
            try {
                Socket clientSocket = ssocket.accept(); // 3. 클라이언트 연결 요청 수락
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // 클라이언트의 입출력 스트림 생성
                DataInputStream is = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());

                // 새로운 스레드 생성 및 실행
                ServerThread clientThread = new ServerThread(clientSocket, is, os);
                clientThread.start();

            } catch (IOException e) {
                System.err.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }
}
