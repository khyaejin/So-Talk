package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 홈 화면 패널
 * 메뉴 및 채팅방 리스트를 표시
 */
public class HomePanel extends JPanel {
    public HomePanel(MessengerFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        String[] chatRoomArray = {"김혜진", "양인서", "강다현", "정예빈"};

        // 메뉴 패널 생성
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new FlowLayout());
        menuPanel.setBackground(new Color(0xEDEDED));
        menuPanel.setPreferredSize(new Dimension(80, 0)); // 너비 80, 높이는 자동 설정

        // 채팅방 리스트 패널 생성
        JPanel chattingRoomListPanel = new JPanel();
        chattingRoomListPanel.setLayout(new BoxLayout(chattingRoomListPanel, BoxLayout.Y_AXIS));
        chattingRoomListPanel.setBackground(new Color(0xFFFFFF));

        for (String chatRoomName : chatRoomArray) {
            JButton chatRoomButton = new JButton(chatRoomName);
            chatRoomButton.setBackground(new Color(0xFFFFFF));
            chatRoomButton.addActionListener(e -> {
                try {
                    DataOutputStream os = frame.getOutputStream();
                    os.writeUTF(chatRoomName); // 서버에 상대방 정보 전송
                    frame.setChattingPartner(chatRoomName); // 상대방 이름 설정
                    frame.showChattingRoomPanel();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "서버와 연결할 수 없습니다.");
                    ex.printStackTrace();
                }
            });

            chatRoomButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            chattingRoomListPanel.add(chatRoomButton);
        }

        add(menuPanel, BorderLayout.WEST);
        add(chattingRoomListPanel, BorderLayout.CENTER);
    }
}
