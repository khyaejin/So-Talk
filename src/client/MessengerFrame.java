package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;

/**
 * 메인 프레임 클래스
 * 여러 화면 패널을 포함하며, 화면 전환 및 메시지 관리
 */
public class MessengerFrame extends JFrame {
    private JPanel mainPanel;
    private StartPanel startPanel;
    private HomePanel homePanel;
    private ChattingRoomPanel chattingRoomPanel;
    private DataOutputStream outputStream; // 서버로 메시지 전송을 위한 출력 스트림

    public MessengerFrame() {
        super("Messenger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 800);
        setLocation(200, 50);

        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // 패널 초기화
        startPanel = new StartPanel(this);
        homePanel = new HomePanel(this);
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
}
