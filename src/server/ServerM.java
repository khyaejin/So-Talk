package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerM {
    static ArrayList<ServerThread> list = new ArrayList<>();    // 3주차 컬렉션 참고, static으로 선언(클래스 이름으로 접근 가능(

    static int clientCount = 0;            // 접속한 클라이언트 유저 번호 저장, 0번 사용자부터 시작

    public static void main(String[] args) throws IOException {
        ServerSocket ssocket = new ServerSocket(6000);     // 1. 서버 소켓 생성

        Socket s;

        while (true) {    // 2. 무한 반복, 반복문 안에서 소켓 객체를 생성하고 있으므로 다중 클라이언트 가능

            s = ssocket.accept();   // 3. 클라이언트로부터의 연결 요청을 기다림, 특정 클라이언트와 연결되면 해당 클라이언트와 통신하기 위한 소켓 객체 생성후 s로 참조

            DataInputStream is = new DataInputStream(s.getInputStream());    // 4. 입력 스트림 객체 생성
            DataOutputStream os = new DataOutputStream(s.getOutputStream()); // 5. 출력 스트림 객체 생성

            ServerThread thread = new ServerThread(s, "client " + clientCount, is, os);    // 6. (서버 안에서 동시에 실행될) 스레드 객체를 생성하면서
            //    (위에서 만들어진) '해당 사용자와 통신할 수 있는 소켓 객체'와
            //    클라이언트 번호, 입출력 스트림 객체 모두 전달
            //   - 사용자 별로 각각 스레스 객체 생성
            list.add(thread);  // 7. (추후 모든 사용자에게 메시지를 전달하기 위해)
            //     생성된 스레드 객체를 ArrayList에 저장(해당 스레드 객체에 접근할 수 있는 참조값(주소) 저장)
            thread.start();    // 8. 생성된 스레드 실행
            clientCount++;     // 9. 클라이언트 유저 번호 증가

        }
    }
}
