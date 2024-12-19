package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;

/**
 * 메인 프레임 클래스
 * 여러 화면 패널을 포함
 * 화면 전환 및 메시지 관리
 */
public class MessengerFrame extends JFrame {
    private JPanel mainPanel;
    private StartPanel startPanel;
    private HomePanel homePanel;
    private ChattingRoomPanel chattingRoomPanel;
    private DataOutputStream outputStream; // 서버로 메시지 전송을 위한 출력 스트림
    private String userName; // 로그인한 사용자 이름
    private String userId; // 로그인한 사용자 ID
    private String chattingPartner; // 현재 채팅 중인 상대방 이름

    public MessengerFrame() {
        super("Messenger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 640);
        setLocation(0, 0);

        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // 패널 초기화
        startPanel = new StartPanel(this);
        chattingRoomPanel = new ChattingRoomPanel(this);

        // 초기 화면 표시
        showStartPanel();

        setVisible(true);
    }

    // OutputStream 설정
    public void setOutputStream(DataOutputStream os) {
        this.outputStream = os;
        chattingRoomPanel.setOutputStream(os); // 채팅방 패널에도 출력 스트림 전달
    }

    // OutputStream 가져오기
    public DataOutputStream getOutputStream() {
        return this.outputStream; // OutputStream 반환
    }

    // 사용자 이름 및 ID 설정
    public void setUserNameAndId(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    // 채팅 상대 설정 및 가져오기
    public void setChattingPartner(String chattingPartner) {
        this.chattingPartner = chattingPartner;
    }

    public String getChattingPartner() {
        return chattingPartner;
    }

    // 채팅 메시지 업데이트
    public void updateChattingRoomText(String sender, String message, boolean isMyMessage) {
        chattingRoomPanel.updateChattingText(sender, message, isMyMessage); // 채팅방 텍스트 업데이트
    }

    // 시작 화면 표시
    public void showStartPanel() {
        mainPanel.removeAll();
        mainPanel.add(startPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // 홈 화면 표시
    public void showHomePanel() {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalStateException("User ID is not set. Please log in first.");
        }
        homePanel = new HomePanel(this, userId);
        mainPanel.removeAll();
        mainPanel.add(homePanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // 채팅방 화면 표시
    public void showChattingRoomPanel() {
        mainPanel.removeAll();
        mainPanel.add(chattingRoomPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ChattingRoomPanel 객체를 가져오는 메서드
    public ChattingRoomPanel getChattingRoomPanel() {
        return chattingRoomPanel;
    }

}
