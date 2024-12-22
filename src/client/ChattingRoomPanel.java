package client;

import api.GoogleTranslate;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 채팅방 화면 패널 (채팅방 상세보기)
 * 메시지 송수신 처리
 */

public class ChattingRoomPanel extends JPanel {
    private JPanel chatContainer; // 채팅 메시지 패널
    private JScrollPane scrollPane;
    private JTextField messageInputField; // 채팅 입력 필드
    private DataOutputStream outputStream; // 서버로 메시지 전송을 위한 출력 스트림
    private String targetId; // 메시지를 전송할 대상 ID
    private String targetName; // 메시지를 전송할 대상 Name
    private JLabel currentUserLabel; // 상단에 현재 사용자 이름 표시
    private GoogleTranslate googleTranslate; // google cloud translation api 사용을 위한 클래스
    ImageIcon translateImg = new ImageIcon("src/assets/Translate.png");

    public ChattingRoomPanel(MessengerFrame frame) {
        // GoogleTranslate 객체 초기화
        this.googleTranslate = new GoogleTranslate();

        setLayout(new BorderLayout());
        setBackground(new Color(0xB9CEE0));

        // 현재 로그인한 사용자 이름을 표시하는 상단 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(0xD6E4F2));
        topPanel.setPreferredSize(new Dimension(0, 50)); // 높이 50으로 설정

//        System.out.println("targetName: "+targetName);
        currentUserLabel = new JLabel("채팅중인 상대 이름: " + targetName); // 채팅방 상단에 상대방 이름 보여주기
        currentUserLabel.setFont(new Font("Pretendard", Font.PLAIN, 16));
        topPanel.add(currentUserLabel);

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
        messageInputField.setPreferredSize(new Dimension(0, 30));
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
        add(topPanel, BorderLayout.NORTH); // 상단 패널 추가
        add(scrollPane, BorderLayout.CENTER); // 채팅 메시지 패널 추가
        add(inputPanel, BorderLayout.SOUTH); // 입력 패널 추가
    }

    // 현재 로그인한 사용자 이름 설정
    public void setCurrentUserName(String userName) {
        currentUserLabel.setText("로그인한 사용자: " + userName);
    }

    public void setOutputStream(DataOutputStream os) {
        this.outputStream = os;
    }

    // 채팅중인 상대 Id와 이름 설정
    public void setTargetIdAndName(String targetId, String targetName) {
        this.targetId = targetId;
        this.targetName = targetName;

        // 상단 이름 업데이트
        if (currentUserLabel != null) {
//            currentUserLabel.setText("채팅중인 상대 이름: " + targetName);
            currentUserLabel.setText(targetName);
        }
    }

    // 채팅 메시지를 화면에 추가
    public void updateChattingText(String sender, String message, boolean isMyMessage) {
        if (!(chatContainer.getLayout() instanceof GridLayout)) {
            chatContainer.setLayout(new GridLayout(10, 1, 0, 9)); // 10개의 행, 1열, 행 간격 9px
        }

        // 말풍선 스타일의 패널
        JPanel messagePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 둥근 사각형 배경 그리기
                g2d.setColor(isMyMessage ? new Color(0xFFEB3B) : Color.WHITE); // 노란색 / 흰색
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); // 둥근 사각형 (border-radius 15px)
            }
        };
        messagePanel.setOpaque(false); // 배경 투명 처리
        messagePanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // 내부 여백 설정

        // 메시지 라벨 생성 (텍스트만 포함)
        JLabel messageLabel = new JLabel("<html>" + message.replaceAll("\n", "<br>") + "</html>");
        messageLabel.setForeground(Color.BLACK); // 텍스트 색상 설정
        messageLabel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE)); // 말풍선의 최대 너비 제한
        messagePanel.add(messageLabel, BorderLayout.CENTER); // 라벨을 패널에 추가

        // 번역 버튼 생성
        JButton translateButton = new JButton(new ImageIcon(translateImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        translateButton.setPreferredSize(new Dimension(25, 25));
        translateButton.setContentAreaFilled(false); // 배경 투명
        translateButton.setBorderPainted(false); // 버튼 외곽선 제거
        translateButton.setFocusPainted(false); // 버튼 포커스 제거
        translateButton.addActionListener(e -> {
            try {
                // Google Translate API를 통해 번역
                String translatedText = googleTranslate.translate(message, "ko"); // 번역할 텍스트와 대상 언어 지정
                System.out.println("[Client] 번역 결과: " + translatedText);

                // 번역 결과를 별도의 말풍선으로 추가
                updateChattingText("Translated", translatedText, false);
            } catch (Exception ex) {
                System.out.println("[Client] 번역 중 오류 발생: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // 메시지와 버튼을 포함할 패널 (말풍선 옆에 버튼 배치)
        JPanel wrapper = new JPanel(new FlowLayout(isMyMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        wrapper.setOpaque(false); // 배경 투명

        if (isMyMessage) {
            wrapper.add(translateButton); // 버튼을 왼쪽에 추가
            wrapper.add(messagePanel);   // 말풍선을 오른쪽에 추가
        } else {
            wrapper.add(messagePanel);   // 말풍선을 왼쪽에 추가
            wrapper.add(translateButton); // 버튼을 오른쪽에 추가
        }

        // 현재 채팅 컨테이너의 컴포넌트 개수를 확인
        int currentComponentCount = chatContainer.getComponentCount();

        // 메시지를 추가할 위치 계산
        if (currentComponentCount < 10) {
            // 메시지가 10개 이하일 경우: 빈 공간을 채우며 추가
            chatContainer.add(wrapper, currentComponentCount);
        } else {
            // 메시지가 10개 이상일 경우: 가장 오래된 메시지를 제거하고 추가
            chatContainer.remove(0); // 첫 번째 메시지 제거
            chatContainer.add(wrapper, 9); // 새 메시지를 마지막에 추가
        }

        // 스크롤을 항상 최하단으로 이동
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });

        // 화면 갱신
        chatContainer.revalidate();
        chatContainer.repaint();
    }

    // 메세지 전송
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
