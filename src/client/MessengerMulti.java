package client;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MessengerMulti {

    final static int ServerPort = 6000;   // 포트 번호
    protected JTextField messageInputField;
    protected JTextArea chattingRoomTextArea;
    DataInputStream is;
    DataOutputStream os;

    public MessengerMulti() throws IOException {
        MyFrame f = new MyFrame();                   // GUI 생성
        InetAddress ip = InetAddress.getByName("localhost");  // 서버 주소 확인
        System.out.println(ip);

        Socket s = new Socket(ip, ServerPort);       // 소켓 연결

        is = new DataInputStream(s.getInputStream());   // 입력 스트림
        os = new DataOutputStream(s.getOutputStream()); // 출력 스트림

        Thread thread2 = new Thread(() -> {
            while (true) {  // 서버로부터 메시지를 받아오는 스레드
                try {
                    String msg = is.readUTF();
                    chattingRoomTextArea.append("RECEIVED: " + msg + "\n");  // 메시지 표시
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        thread2.start();
    }

    class MyFrame extends JFrame implements ActionListener {
        private JPanel mainPanel;  // 메인 패널
        private JPanel startPanel; // 시작 화면 패널
        private JPanel homePanel;  // 로그인 후 홈 화면 패널
        private JPanel menuPanel; //홈 화면 중 메뉴바 패널
        private JPanel ChattingRoomListPanel; //홈 화면 중 채팅방 목록 패널
        private JPanel chattingRoomPanel;

        public MyFrame() {
            super("Messenger");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(400, 800);
            setLocation(200, 50);

            // 메인 패널 생성
            mainPanel = new JPanel(new BorderLayout());
            add(mainPanel);

            // 시작 화면 생성
            createStartPanel();

            // 홈 화면 생성
            createHomePanel();

            //채팅룸 화면 생성
            createChattingRoomPanel();

            // 초기 화면 설정
            showStartPanel();

            setVisible(true);
        }

        private void createStartPanel() {
            startPanel = new JPanel();
            startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
            startPanel.setBackground(new Color(0xFEE502));
            startPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

            // 이미지 추가
            ImageIcon kakao = new ImageIcon("/Users/juye0nlee/Desktop/NetworkProgrammingProject/src/assets/kakaoLogo.png");
            Image img = kakao.getImage();
            Image scaledImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(scaledImg);

            JLabel kakaoLabel = new JLabel(resizedIcon);
            kakaoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            kakaoLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

            // 아이디 입력 필드
            JTextField idField = new JTextField();
            idField.setMaximumSize(new Dimension(230, 43));

            // 비밀번호 입력 필드
            JPasswordField passwordField = new JPasswordField();
            passwordField.setMaximumSize(new Dimension(230, 43));

            // 로그인 버튼
            JButton loginButton = new JButton("로그인");
            loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            loginButton.setMaximumSize(new Dimension(230, 43));
            loginButton.addActionListener(e -> {
                String id = idField.getText();
                String password = new String(passwordField.getPassword());
                System.out.println("아이디: " + id + ", 비밀번호: " + password);

                // 화면 전환
                if(id.equals("test1") && password.equals("1234")) {
                    showHomePanel();
                }
                else {
                    System.out.println("존재하지 않는 유저입니다.");
                }
            });

            //컴포넌트 추가
            startPanel.add(kakaoLabel);
            startPanel.add(Box.createVerticalStrut(0));
            startPanel.add(idField);
            startPanel.add(Box.createVerticalStrut(0));
            startPanel.add(passwordField);
            startPanel.add(Box.createVerticalStrut(0));
            startPanel.add(loginButton);
        }
        // 홈화면 패널
        private void createHomePanel() {
            String[] chatRoomArray = {"김혜진","양인서","강다현","정예빈"};
            homePanel = new JPanel();
            homePanel.setLayout(new BorderLayout());
            homePanel.setBackground(Color.WHITE);

            // 메뉴 패널 생성
            menuPanel = new JPanel();
            menuPanel.setLayout(new FlowLayout());
            menuPanel.setBackground(new Color(0xEDEDED));
            menuPanel.setPreferredSize(new Dimension(80, 0)); // 너비 80, 높이는 자동 설정

            // 채팅방 리스트 패널 생성
            ChattingRoomListPanel = new JPanel();
            ChattingRoomListPanel.setLayout(new BoxLayout(ChattingRoomListPanel, BoxLayout.Y_AXIS)); // 수직 정렬
            ChattingRoomListPanel.setBackground(new Color(0xFFFFFF));


            // 채팅방 리스트에 더미 채팅방 추가 (예시)
            for (int i = 0; i < chatRoomArray.length; i++) {
                JButton chatRoom = new JButton(chatRoomArray[i]); // 중앙 정렬
                chatRoom.setOpaque(true);
                chatRoom.setBackground(new Color(0xFFFFFF));
                chatRoom.addActionListener( e-> {
                    showChattingRoomPanel();
                });

                // 너비를 패널에 맞추도록 설정
                chatRoom.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
                chatRoom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // 너비를 패널 크기에 맞추고 높이는 40px
                ChattingRoomListPanel.add(chatRoom);
            }


            // 홈화면 패널에 추가
            homePanel.add(menuPanel, BorderLayout.WEST); // 메뉴 패널은 왼쪽에 배치
            homePanel.add(ChattingRoomListPanel, BorderLayout.CENTER); // 채팅방 리스트는 중앙에 배치
        }

        private void createChattingRoomPanel() {
            // 메인 패널 설정
            chattingRoomPanel = new JPanel();
            chattingRoomPanel.setLayout(new BorderLayout());
            chattingRoomPanel.setBackground(new Color(0xB9CEE0));

            // 채팅 메시지 표시 영역 (JTextArea)
            chattingRoomTextArea = new JTextArea();
            chattingRoomTextArea.setEditable(false); // 메시지 표시용
            chattingRoomTextArea.setLineWrap(true); // 텍스트 줄바꿈
            chattingRoomTextArea.setWrapStyleWord(true); // 단어 단위로 줄바꿈
            chattingRoomTextArea.setBackground(new Color(0xB9CEE0));

            // 채팅 입력 영역 (JTextField)
            messageInputField = new JTextField();
            messageInputField.setPreferredSize(new Dimension(0, 40)); // 높이를 40으로 설정
            messageInputField.addActionListener(this);  // JTextField에 메시지를 입력하고 엔터를 치면 => 자동으로 호출할 함수(actionPerformed())가 들어있는 객체 이름(참조변수) 등록


            // 전송 버튼 (JButton)
            JButton sendButton = new JButton("전송");
            sendButton.setPreferredSize(new Dimension(80, 40)); // 버튼 크기 설정

            // 입력 영역과 버튼을 포함할 패널
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BorderLayout());
            inputPanel.setBackground(new Color(0xD6E4F2));
            inputPanel.add(messageInputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            // 스크롤 추가 (채팅 메시지 표시 영역)
            JScrollPane scrollPane = new JScrollPane(chattingRoomTextArea);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            // 메인 패널에 컴포넌트 추가
            chattingRoomPanel.add(scrollPane, BorderLayout.CENTER); // 메시지 영역
            chattingRoomPanel.add(inputPanel, BorderLayout.SOUTH); // 입력 영역
        }

        private void showStartPanel() {
            mainPanel.removeAll();
            mainPanel.add(startPanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        }

        private void showHomePanel() {
            mainPanel.removeAll();
            mainPanel.add(homePanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        }

        private void showChattingRoomPanel() {
            mainPanel.removeAll();
            mainPanel.add(chattingRoomPanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        }

        public void actionPerformed(ActionEvent evt) {
            String s = messageInputField.getText();               //   사용자가 JTextField에 입력한 데이터를 읽어들여서
            //   서버로 전송한 후, 본인 JTextArea에도 출력하는 등의 기능 수행
            try {
                os.writeUTF(s);                           // 서버로 전송
            } catch (IOException e) {
                e.printStackTrace();
            }
            chattingRoomTextArea.append("SENT: " + s + "\n");         // 이후 본인(클라이언트) 창에도 출력
            messageInputField.selectAll();                       // 텍스트 필드의 모든 텍스트를 선택하는 메소드, 다음 입력 때 기존 내용을 지우고 편하게 입력하도록 설정한 것
            chattingRoomTextArea.setCaretPosition(chattingRoomTextArea.getDocument().getLength());  //JtextArea라는 텍스트 영역의 커서 위치를 텍스트 영역의 맨 끝으로 설정
        }

    }



    public static void main(String[] args) throws IOException {
        new MessengerMulti();  // 메신저 실행
    }
}
