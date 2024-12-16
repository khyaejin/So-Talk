package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ServerThread extends Thread {
    Scanner scn = new Scanner(System.in);
    private String name;
    final DataInputStream is;
    final DataOutputStream os;
    Socket s;
    boolean active;

    public ServerThread(Socket s, String name, DataInputStream is, DataOutputStream os) {
        this.is = is;
        this.os = os;
        this.name = name;
        this.s = s;
        this.active = true;
    }

    @Override
    public void run() {
        String message;
        while (true) {
            try {
                message = is.readUTF();        // 어떤 클라이언트로 부터 들어오는 데이터를 읽어들여서
                System.out.println(message);   // (일단 서버의 콘솔장에 출력해서 확인하고)
                for (ServerThread t : ServerM.list) {        // ArrayList에 등록되어 있는 모든 사용자에게 순서대로 그 메시지 전달
                    t.os.writeUTF(this.name + " : " + message);   // t 사용자와 통신하는 스레드 안의 os.writeUTF()를 호출하여 메시지 전달
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        try {
            this.is.close();
            this.os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
