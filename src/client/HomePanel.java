package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

        ImageIcon profileImg = new ImageIcon("src/assets/profile.jpg");
        Image scaledProfileImg = profileImg.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel profileImgLabel = new JLabel(new ImageIcon(scaledProfileImg));
        profileImgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 메뉴 패널 생성
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); // BoxLayout 유지
        menuPanel.setBackground(new Color(0xEDEDED));
        menuPanel.setPreferredSize(new Dimension(80, 0)); // 너비 80, 높이는 자동 설정

        ImageIcon personImg = new ImageIcon("src/assets/Person.png");
        ImageIcon speechBubbleImg = new ImageIcon("src/assets/SpeechBubble.png");
        ImageIcon ellipsisImg = new ImageIcon("src/assets/Ellipsis.png");

        Image scaledPersonImg = personImg.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        Image scaledSpeechBubbleImg = speechBubbleImg.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        Image scaledEllipsisImg = ellipsisImg.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);

        JLabel personLabel = new JLabel(new ImageIcon(scaledPersonImg));
        JLabel speechBubbleLabel = new JLabel(new ImageIcon(scaledSpeechBubbleImg));
        JLabel ellipsisLabel = new JLabel(new ImageIcon(scaledEllipsisImg));

        personLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        personLabel.setBorder(new EmptyBorder(40, 0, 0, 0));
        speechBubbleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        speechBubbleLabel.setBorder(new EmptyBorder(30, 0, 0, 0));
        ellipsisLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ellipsisLabel.setBorder(new EmptyBorder(30, 0, 0, 0));

        menuPanel.add(personLabel);
        menuPanel.add(speechBubbleLabel);
        menuPanel.add(ellipsisLabel);

        // 채팅방 리스트 패널 생성
        JPanel chattingRoomListPanel = new JPanel();
        chattingRoomListPanel.setLayout(new BoxLayout(chattingRoomListPanel, BoxLayout.Y_AXIS));
        chattingRoomListPanel.setBackground(new Color(0xFFFFFF));

        for (String chatRoomName : chatRoomArray) {
            JButton chatRoomButton = new JButton(chatRoomName);
            chatRoomButton.setBackground(new Color(0xFFFFFF));
            chatRoomButton.setIcon(new ImageIcon(scaledProfileImg)); // 버튼에 아이콘 추가
            chatRoomButton.setHorizontalAlignment(SwingConstants.LEFT); // 텍스트와 아이콘을 왼쪽 정렬
            chatRoomButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

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

            chattingRoomListPanel.add(chatRoomButton);
        }

        add(menuPanel, BorderLayout.WEST);
        add(chattingRoomListPanel, BorderLayout.CENTER);
    }
}
