package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * 홈 화면 패널
 * 메뉴 및 채팅방 리스트를 표시 (채팅방 전체보기)
 */
public class HomePanel extends JPanel {

    private JPanel menuPanel; // 홈 화면 중 메뉴바 패널
    private JPanel chattingRoomListPanel; // 홈 화면 중 기존 채팅방 목록 패널

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

        // 이미지 아이콘 생성 및 크기 조정
        ImageIcon profileImg = new ImageIcon("src/assets/profile.jpg");
        ImageIcon personImg = new ImageIcon("src/assets/Person.png");
        ImageIcon speechBubbleImg = new ImageIcon("src/assets/SpeechBubble.png");
        ImageIcon ellipsisImg = new ImageIcon("src/assets/Ellipsis.png");

        Image scaledProfileImg = profileImg.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        Image scaledPersonImg = personImg.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        Image scaledSpeechBubbleImg = speechBubbleImg.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        Image scaledEllipsisImg = ellipsisImg.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);

        JLabel personLabel = new JLabel(new ImageIcon(scaledPersonImg));
        JLabel speechBubbleLabel = new JLabel(new ImageIcon(scaledSpeechBubbleImg));
        JLabel ellipsisLabel = new JLabel(new ImageIcon(scaledEllipsisImg));

        // 기존 채팅방 리스트 패널 생성
        chattingRoomListPanel = new JPanel();
        chattingRoomListPanel.setLayout(new BoxLayout(chattingRoomListPanel, BoxLayout.Y_AXIS)); // 수직 정렬
        chattingRoomListPanel.setBackground(new Color(0xFFFFFF));

        // 기존 사용자 버튼 생성
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
                        frame.setChattingPartner(userId, chatRoomName); // 상대방 ID와 이름 설정
                        frame.showChattingRoomPanel(userId, chatRoomName); // 채팅방 화면으로 전환
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "서버와 연결할 수 없습니다.");
                        ex.printStackTrace();
                    }
                });

                chattingRoomListPanel.add(chatRoomButton);
            }
        }

        // 메뉴 패널 생성
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(0xEDEDED));
        menuPanel.setPreferredSize(new Dimension(80, 0)); // 너비 80, 높이 자동 설정

        // 컴포넌트 추가 전에 중앙 정렬 설정
        personLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        personLabel.setBorder(new EmptyBorder(40, 0, 0, 0));
        speechBubbleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        speechBubbleLabel.setBorder(new EmptyBorder(30, 0, 0, 0));
        ellipsisLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ellipsisLabel.setBorder(new EmptyBorder(30, 0, 0, 0));

        // 컴포넌트 추가
        menuPanel.add(personLabel);
        menuPanel.add(speechBubbleLabel);
        menuPanel.add(ellipsisLabel);

        // 추가 채팅방 리스트 패널 (오른쪽)
        JPanel additionalChattingRoomListPanel = new JPanel();
        additionalChattingRoomListPanel.setLayout(new BoxLayout(additionalChattingRoomListPanel, BoxLayout.Y_AXIS));
        additionalChattingRoomListPanel.setBackground(new Color(0xFFFFFF));


        // 홈 화면 패널에 추가
        add(menuPanel, BorderLayout.WEST); // 메뉴 패널은 왼쪽에 배치
        add(chattingRoomListPanel, BorderLayout.CENTER); // 기존 채팅방 리스트는 중앙에 배치
        add(additionalChattingRoomListPanel, BorderLayout.EAST); // 추가 채팅방 리스트는 오른쪽에 배치
    }
}
