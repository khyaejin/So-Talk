package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 채팅룸 화면 패널.
 * 메시지 전송 및 수신을 처리.
 */
public class ChattingRoomPanel extends JPanel {
    private JTextArea chattingRoomTextArea; // 채팅 메시지 출력 영역
    private JTextField messageInputField;  // 채팅 입력 필드
    private DataOutputStream outputStream; // 서버로 메시지 전송을 위한 출력 스트림

    public ChattingRoomPanel(MessengerFrame frame) {
        setLayout(new BorderLayout());
        setBackground(new Color(0xB9CEE0));

        // 채팅 메시지 표시 영역 (JTextArea)
        chattingRoomTextArea = new JTextArea();
        chattingRoomTextArea.setEditable(false); // 메시지 표시용
        chattingRoomTextArea.setLineWrap(true); // 텍스트 줄바꿈
        chattingRoomTextArea.setWrapStyleWord(true); // 단어 단위로 줄바꿈
        chattingRoomTextArea.setBackground(new Color(0xB9CEE0));

        // 채팅 입력 영역 (JTextField)
        messageInputField = new JTextField();
        messageInputField.setPreferredSize(new Dimension(0, 40)); // 높이를 40으로 설정
        messageInputField.addActionListener(e -> sendMessage());  // JTextField에 메시지를 입력하고 엔터를 치면 호출

        // 전송 버튼 (JButton)
        JButton sendButton = new JButton("전송");
        sendButton.setPreferredSize(new Dimension(80, 40)); // 버튼 크기 설정
        sendButton.addActionListener(e -> sendMessage());   // 버튼 클릭 시 메시지 전송

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
        add(scrollPane, BorderLayout.CENTER); // 메시지 영역
        add(inputPanel, BorderLayout.SOUTH); // 입력 영역
    }

    // 서버로 메시지 전송을 위한 출력 스트림 설정
    public void setOutputStream(DataOutputStream os) {
        this.outputStream = os;
    }

    // 채팅 메시지 출력 영역에 텍스트 업데이트
    public void updateChattingText(String message) {
        chattingRoomTextArea.append(message);
    }

    // 메시지 전송 처리
    private void sendMessage() {
        String message = messageInputField.getText(); // 입력된 메시지 가져오기
        try {
            outputStream.writeUTF(message); // 서버로 메시지 전송
            chattingRoomTextArea.append("SENT: " + message + "\n"); // 전송된 메시지 출력
            messageInputField.setText(""); // 입력 필드 초기화
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
