package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChattingRoomPanel extends JPanel {
    private JPanel chatContainer; // 채팅 메시지 패널
    private JScrollPane scrollPane;
    private JTextField messageInputField; // 채팅 입력 필드
    private DataOutputStream outputStream; // 서버로 메시지 전송을 위한 출력 스트림
    private String targetId; // 메시지를 전송할 대상 ID

    public ChattingRoomPanel(MessengerFrame frame) {
        setLayout(new BorderLayout());
        setBackground(new Color(0xB9CEE0));

        // 채팅 메시지를 표시할 컨테이너 패널
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(new Color(0xB9CEE0));
        chatContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 좌우 여백

        // 스크롤 추가
        scrollPane = new JScrollPane(chatContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // 채팅 입력 영역 (JTextField)
        messageInputField = new JTextField();
        messageInputField.setPreferredSize(new Dimension(0, 20));
        messageInputField.addActionListener(e -> sendMessage());

        // 전송 버튼 (JButton)
        JButton sendButton = new JButton("전송");
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.addActionListener(e -> sendMessage());

        // 입력 영역과 버튼을 포함할 패널
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(0xD6E4F2));
        inputPanel.add(messageInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // 메인 패널에 컴포넌트 추가
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    public void setOutputStream(DataOutputStream os) {
        this.outputStream = os;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    // 채팅 메시지를 화면에 추가
    public void updateChattingText(String sender, String message, boolean isMyMessage) {
        // 말풍선 스타일의 패널
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);

        // 말풍선 스타일의 JLabel 생성
        JLabel messageLabel = new JLabel("<html><div style='padding: 13px;'>"
                + message.replaceAll("\n", "<br>") + "</div></html>");
        messageLabel.setOpaque(true);
        messageLabel.setBackground(isMyMessage ? new Color(0xFFEB3B) : Color.WHITE); // 노란색 / 흰색
        messageLabel.setForeground(Color.BLACK);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 정렬 설정
        JPanel wrapper = new JPanel(new FlowLayout(isMyMessage ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(messageLabel);

        // 메시지 패널 추가 및 간격 설정
        chatContainer.add(wrapper);
        chatContainer.add(Box.createVerticalStrut(3)); // 메시지 간 간격 3px

        // 스크롤을 최하단으로 이동
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });

        revalidate();
        repaint();
    }

    private void sendMessage() {
        String message = messageInputField.getText(); // 입력된 메시지 가져오기
        if (message.isEmpty()) {
            System.out.println("[Client] 빈 메시지는 전송되지 않습니다.");
            return;
        }

        try {
            // 내 메시지 UI 업데이트
            updateChattingText("Me", message, true);

            // 서버로 메시지 전송
//            System.out.println("targetId:"+targetId);
            if (targetId != null) {
                System.out.println("[Client] 메시지 전송: 대상 ID = " + targetId + ", 메시지 = " + message);
                outputStream.writeUTF("MESSAGE_TO_ID:" + targetId + ":" + message);
            } else {
                System.out.println("[Client] 대상 ID가 설정되지 않았습니다. 메시지를 전송할 수 없습니다.");
            }

            messageInputField.setText(""); // 입력 필드 초기화
        } catch (IOException e) {
            System.out.println("[Client] 메시지를 전송하는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

}
