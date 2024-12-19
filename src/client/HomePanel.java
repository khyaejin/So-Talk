package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * 홈 화면 패널
 * 메뉴 및 채팅방 리스트를 표시
 */
public class HomePanel extends JPanel {
    public HomePanel(MessengerFrame frame, String currentUserId) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 사용자 정보 매핑 (id -> 이름)
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("1", "김혜진");
        userMap.put("2", "이주연");
        userMap.put("3", "강다현");
        userMap.put("4", "정예빈");
        userMap.put("5", "양인서");

        ImageIcon profileImg = new ImageIcon("src/assets/profile.jpg");
        Image scaledProfileImg = profileImg.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);

        // 현재 사용자를 제외한 채팅방 리스트 생성
        JPanel chattingRoomListPanel = new JPanel();
        chattingRoomListPanel.setLayout(new BoxLayout(chattingRoomListPanel, BoxLayout.Y_AXIS));
        chattingRoomListPanel.setBackground(new Color(0xFFFFFF));

        for (String userId : userMap.keySet()) {
            if (!userId.equals(currentUserId)) { // 현재 로그인한 사용자는 제외
                String chatRoomName = userMap.get(userId);

                JButton chatRoomButton = new JButton(chatRoomName);
                chatRoomButton.setBackground(new Color(0xFFFFFF));
                chatRoomButton.setIcon(new ImageIcon(scaledProfileImg)); // 버튼에 아이콘 추가
                chatRoomButton.setHorizontalAlignment(SwingConstants.LEFT); // 텍스트와 아이콘을 왼쪽 정렬
                chatRoomButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                chatRoomButton.addActionListener(e -> {
                    try {
                        DataOutputStream os = frame.getOutputStream();
                        os.writeUTF("CHAT_WITH:" + userId); // 서버에 상대방 ID 전송
                        frame.setChattingPartner(chatRoomName); // 상대방 이름 설정
                        frame.showChattingRoomPanel();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "서버와 연결할 수 없습니다.");
                        ex.printStackTrace();
                    }
                });

                chattingRoomListPanel.add(chatRoomButton);
            }
        }

        // 메뉴 패널 생성
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(0xEDEDED));
        menuPanel.setPreferredSize(new Dimension(80, 0)); // 너비 80, 높이는 자동 설정

        add(menuPanel, BorderLayout.WEST);
        add(chattingRoomListPanel, BorderLayout.CENTER);
    }
}
