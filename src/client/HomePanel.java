package client;

import javax.swing.*;
import java.awt.*;

/**
 * 홈 화면 패널
 * 메뉴 및 채팅방 리스트를 표시 (채팅방 전체보기)
 */
public class HomePanel extends JPanel {

    private JPanel menuPanel; // 홈 화면 중 메뉴바 패널
    private JPanel ChattingRoomListPanel; // 홈 화면 중 채팅방 목록 패널

    public HomePanel(MessengerFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        String[] chatRoomArray = {"김혜진", "양인서", "강다현", "정예빈"};

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
            chatRoom.addActionListener(e -> {
                frame.showChattingRoomPanel();
            });

            // 너비를 패널에 맞추도록 설정
            chatRoom.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
            chatRoom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // 너비를 패널 크기에 맞추고 높이는 40px
            ChattingRoomListPanel.add(chatRoom);
        }

        // 홈 화면 패널에 추가
        add(menuPanel, BorderLayout.WEST); // 메뉴 패널은 왼쪽에 배치
        add(ChattingRoomListPanel, BorderLayout.CENTER); // 채팅방 리스트는 중앙에 배치
    }
}
