package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class ServerThread extends Thread {
    private final DataInputStream is;
    private final DataOutputStream os;
    private final Socket socket;
    private boolean active;
    private String name; // 클라이언트 이름
    private String userId; // 클라이언트 ID
    private String chattingWith; // 현재 채팅 중인 상대방 ID

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

                // 메시지 처리
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("[Server] 클라이언트 연결이 종료되었습니다: ID " + this.userId);
            active = false; // 스레드 종료
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String message) throws IOException {
        if (message.startsWith("SET_ID:")) {
            synchronized (clientsById) {
                this.userId = message.split(":")[1];
                clientsById.put(this.userId, this); // ID로 클라이언트를 저장
            }
            System.out.println("[Server] 클라이언트 ID가 설정됨: " + this.userId);

        } else if (message.startsWith("SET_NAME:")) {
            this.name = message.split(":")[1];
            System.out.println("[Server] 클라이언트 이름이 설정됨: " + this.name);

        } else if (message.startsWith("CHAT_WITH:")) {
            this.chattingWith = message.split(":")[1];
            System.out.println("[Server] ID " + this.userId + "와 ID " + this.chattingWith + "의 채팅 시작");

        } else if (message.startsWith("MESSAGE_TO_ID:")) {
            handleTextMessage(message);

        } else if (message.startsWith("EMOTICON_FILE:")) {
            handleFileTransfer(message);

        } else {
            System.out.println("[Server] 알 수 없는 명령어입니다: " + message);
        }
    }

    private void handleTextMessage(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length == 3) {
            String targetId = parts[1];
            String chatMessage = parts[2];
            System.out.println("[Server] ID " + this.userId + "에서 ID " + targetId + "로 메시지 전달 요청: " + chatMessage);
            sendMessageToId(targetId, "MESSAGE_FROM:" + this.userId + ":" + chatMessage);
        } else {
            System.out.println("[Server] MESSAGE_TO_ID 명령의 형식이 잘못되었습니다: " + message);
        }
    }

    private void handleFileTransfer(String message) throws IOException {
        String[] parts = message.split(":", 4);
        if (parts.length < 4) {
            System.out.println("[Server] EMOTICON_FILE 명령의 형식이 잘못되었습니다: " + message);
            return;
        }

        String targetId = parts[1];
        String fileName = parts[2];
        long fileSize = Long.parseLong(parts[3]);

        // 파일 크기 제한 (예: 5MB)
        final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
        if (fileSize > MAX_FILE_SIZE) {
            System.out.println("[Server] 파일 크기가 너무 큽니다: " + fileSize + " bytes");
            sendMessageToId(this.userId, "ERROR: 파일 크기가 서버 제한(5MB)을 초과했습니다. 전송 실패");
            return;
        }

        System.out.println("[Server] 파일 전송 요청: ID " + this.userId + " -> ID " + targetId + ", 파일명: " + fileName + ", 크기: " + fileSize);

        // 파일 데이터 읽기
        byte[] fileData = new byte[(int) fileSize];
        int totalBytesRead = 0;

        while (totalBytesRead < fileSize) {
            int bytesRead = is.read(fileData, totalBytesRead, (int) fileSize - totalBytesRead);
            if (bytesRead == -1) {
                System.out.println("[Server] 파일 데이터 수신 중 연결이 끊어졌습니다.");
                sendMessageToId(this.userId, "ERROR: 파일 데이터 수신 중 연결이 끊어졌습니다.");
                return;
            }
            totalBytesRead += bytesRead;
        }

        // 데이터 크기 불일치 확인
        if (totalBytesRead != fileSize) {
            System.out.println("[Server] 파일 데이터 수신 중 크기 불일치: 수신 = " + totalBytesRead + ", 예상 = " + fileSize);
            sendMessageToId(this.userId, "ERROR: 파일 크기 불일치로 전송 실패");
            return;
        }

        // 대상 클라이언트로 파일 데이터 전송
        sendFileToId(targetId, fileName, fileSize, fileData);
    }

    private void sendFileToId(String targetId, String fileName, long fileSize, byte[] fileData) {
        ServerThread targetClient = clientsById.get(targetId);
        if (targetClient != null && targetClient.active) {
            try {
                System.out.println("[Server] ID " + targetId + "에게 파일을 전송합니다: " + fileName + " (" + fileSize + " bytes)");

                // 헤더 전송
                targetClient.os.writeUTF("EMOTICON_FILE:" + this.userId + ":" + fileName + ":" + fileSize);
                // 파일 데이터 전송
                targetClient.os.write(fileData);
                targetClient.os.flush();

                // 전송 완료 메시지
                sendMessageToId(this.userId, "파일 전송 완료: " + fileName);
                System.out.println("[Server] 파일 전송 완료: " + fileName);
            } catch (IOException e) {
                System.out.println("[Server] ID " + targetId + "로 파일 전송 중 오류가 발생했습니다.");
                e.printStackTrace();
                sendMessageToId(this.userId, "ERROR: 파일 전송 중 오류가 발생했습니다.");
            }
        } else {
            System.out.println("[Server] ID " + targetId + " 클라이언트가 비활성 상태이거나 존재하지 않습니다.");
            sendMessageToId(this.userId, "ERROR: 대상 클라이언트가 비활성 상태이거나 존재하지 않습니다.");
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
