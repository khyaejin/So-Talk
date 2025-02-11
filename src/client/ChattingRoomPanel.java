package client;

import api.GPTService;
import api.GoogleTranslate;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
    private String targetId;    // 메시지를 전송할 대상 ID
    private String targetName;  // 메시지를 전송할 대상 Name
    private JLabel currentUserLabel; // 상단에 현재 사용자 이름 표시

    // ---- 번역 관련 필드 ----
    private GoogleTranslate googleTranslate;
    private String targetLanguage = "ko";   // 기본 번역 언어: "ko" (한국어)

    // --- gpt 관련 필드 ---
    private GPTService gpt;

    // ---- 이미지들 ----
    // 버튼 이미지
    private ImageIcon translateImg = new ImageIcon("src/assets/Translate.png");
    private ImageIcon languageImg  = new ImageIcon("src/assets/Language.png");
    private ImageIcon emoticonImg  = new ImageIcon("src/assets/Emoticon.png");
    private ImageIcon pictureImg   = new ImageIcon("src/assets/Picture.png");
    private ImageIcon robotImg     = new ImageIcon("src/assets/Robot.png");
    // 이모티콘 이미지
    private ImageIcon ThumbsEmoticonImg = new ImageIcon("src/assets/Emoticon-Thumbs.png");
    private ImageIcon FallingInLoveEmoticonImg = new ImageIcon("src/assets/Emoticon-Falling-In-Love.png");
    private ImageIcon SmileEmoticonImg = new ImageIcon("src/assets/Emoticon-Smile.png");
    private ImageIcon FunnyAndSadEmoticonImg = new ImageIcon("src/assets/Emoticon-Funny-And-Sad.png");
    private ImageIcon DoubleHeartsEmoticonImg = new ImageIcon("src/assets/Emoticon-Double-Hearts.png");
    private ImageIcon SadEmoticonImg = new ImageIcon("src/assets/Emoticon-Sad.png");

    // 전송 버튼
    private JButton sendButton;

    // =======================
    // 이모티콘 팝업
    private JPopupMenu emoticonPopup;
    // =======================

    // 생성자 (채팅방 화면 초기화, 배피)
    public ChattingRoomPanel(MessengerFrame frame) {
        // GoogleTranslate 객체 초기화
        this.googleTranslate = new GoogleTranslate();
        this.gpt = new GPTService();
        setLayout(new BorderLayout());
        setOpaque(false); // 패널을 투명하게 설정하여 paintComponent로 배경을 그리도록 설정

        // --------------------
        // 1) 상단 패널 (상대방 이름)
        // --------------------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(0xD6E4F2), 0, height, new Color(0xB9CEE0));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        topPanel.setOpaque(false);
        topPanel.setPreferredSize(new Dimension(0, 50)); // 높이 50

        currentUserLabel = new JLabel("채팅중인 상대 이름: " + targetName);
        currentUserLabel.setFont(new Font("Pretendard", Font.PLAIN, 16));
        topPanel.add(currentUserLabel);

        // --------------------
        // 2) 중앙(채팅 메시지) 영역
        // --------------------
        chatContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(0xB9CEE0), 0, height, new Color(0xFFFFFF));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setOpaque(false);
        chatContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 좌우 여백

        scrollPane = new JScrollPane(chatContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // --------------------
        // 3) 하단 영역 (흰색 배경, 2줄)
        // --------------------
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(Color.WHITE);

        // (3-1) 첫 번째 줄: [입력창만 가로로 꽉 차게]
        JPanel inputRow = new JPanel(new BorderLayout());
        inputRow.setBackground(Color.WHITE);

        messageInputField = new JTextField();
        messageInputField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messageInputField.setFont(new Font("Arial", Font.PLAIN, 14));

        // 입력창 내용 변화 -> 전송 버튼 색상 변경 -> 안 됨.. 왜!!!!!
        messageInputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateSendButtonColor(); }
            @Override
            public void removeUpdate(DocumentEvent e)  { updateSendButtonColor(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateSendButtonColor(); }
        });

        // (첫 번째 줄) 입력창만 배치
        inputRow.add(messageInputField, BorderLayout.CENTER);

        // (3-2) 두 번째 줄: 아이콘들(왼쪽) + 전송 버튼(오른쪽)
        JPanel iconsRow = new JPanel(new BorderLayout());
        iconsRow.setBackground(Color.WHITE);

        // 왼쪽 아이콘 패널 (FlowLayout)
        JPanel leftIconsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftIconsPanel.setBackground(Color.WHITE);

        // --- 언어(드롭다운) 버튼 ---
        JButton languageButton = new JButton(
                new ImageIcon(languageImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))
        );
        languageButton.setContentAreaFilled(false);
        languageButton.setBorderPainted(false);
        languageButton.setFocusPainted(false);
        languageButton.setOpaque(false);

        // 팝업 메뉴(언어 선택)
        JPopupMenu languageMenu = new JPopupMenu();
        JMenuItem menuItemKo = new JMenuItem("한국어(ko)");
        menuItemKo.addActionListener(e -> {
            this.targetLanguage = "ko";
            System.out.println("[Client] 번역 언어 변경 -> ko");
        });
        languageMenu.add(menuItemKo);

        JMenuItem menuItemEn = new JMenuItem("영어(en)");
        menuItemEn.addActionListener(e -> {
            this.targetLanguage = "en";
            System.out.println("[Client] 번역 언어 변경 -> en");
        });
        languageMenu.add(menuItemEn);

        JMenuItem menuItemJa = new JMenuItem("일본어(ja)");
        menuItemJa.addActionListener(e -> {
            this.targetLanguage = "ja";
            System.out.println("[Client] 번역 언어 변경 -> ja");
        });
        languageMenu.add(menuItemJa);

        // 독일어 (de)
        JMenuItem menuItemDe = new JMenuItem("독일어(de)");
        menuItemDe.addActionListener(e -> {
            this.targetLanguage = "de";
            System.out.println("[Client] 번역 언어 변경 -> de");
        });
        languageMenu.add(menuItemDe);

        // 이탈리아어 (it)
        JMenuItem menuItemIt = new JMenuItem("이탈리아어(it)");
        menuItemIt.addActionListener(e -> {
            this.targetLanguage = "it";
            System.out.println("[Client] 번역 언어 변경 -> it");
        });
        languageMenu.add(menuItemIt);

        // 중국어 간체 (zh-CN)
        JMenuItem menuItemZhCN = new JMenuItem("중국어 간체(zh-CN)");
        menuItemZhCN.addActionListener(e -> {
            this.targetLanguage = "zh-CN";
            System.out.println("[Client] 번역 언어 변경 -> zh-CN");
        });
        languageMenu.add(menuItemZhCN);

        // 스페인어 (es)
        JMenuItem menuItemEs = new JMenuItem("스페인어(es)");
        menuItemEs.addActionListener(e -> {
            this.targetLanguage = "es";
            System.out.println("[Client] 번역 언어 변경 -> es");
        });
        languageMenu.add(menuItemEs);

        // 언어 변경 버튼 붙이기
        languageButton.addActionListener(e -> {
            languageMenu.show(languageButton, 0, languageButton.getHeight()-207); // 채팅 하단 라인과 일치하도록 조정
        });
        leftIconsPanel.add(languageButton);

        // --- 이모티콘 버튼 ---
        JButton emoticonButton = new JButton(
                new ImageIcon(emoticonImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))
        );
        styleIconButton(emoticonButton);
        // =======================
        emoticonButton.addActionListener(e -> {
            if (emoticonPopup == null) {
                emoticonPopup = createEmoticonPopup();
            }
            emoticonPopup.show(emoticonButton, 0, emoticonButton.getHeight()-123); // 채팅창 하단 네모박스에 맞도록 y축 위치 조정
        });
        // =======================
        leftIconsPanel.add(emoticonButton);

        // --- 사진 버튼 ---
        JButton pictureButton = new JButton(
                new ImageIcon(pictureImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))
        );
        styleIconButton(pictureButton);
        pictureButton.addActionListener(e -> {
            System.out.println("[Client] 사진 버튼 클릭");
        });
        leftIconsPanel.add(pictureButton);

        // --- 로봇 버튼 ---
        JButton robotButton = new JButton(
                new ImageIcon(robotImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))
        );
        styleIconButton(robotButton);

        // 로봇 버튼 클릭 시 GPT 통신
        robotButton.addActionListener(e -> {
            // GPT 통신
            String result = gpt.callGptApi();
            System.out.println("result:" + result);
            System.out.println("[Client] 로봇 버튼 클릭");

            // 텍스트 영역 생성
            JTextArea textArea = new JTextArea(result);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);

            // 스크롤 영역 생성
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(300, 130)); // 크기 설정

            // 커스텀 다이얼로그 생성
            JDialog dialog = new JDialog((Frame) null, "\uD83D\uDDFA\uFE0F오늘의 세계 지식\uD83E\uDD44", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 닫기 버튼 설정
            dialog.getContentPane().add(scrollPane); // 내용 추가
            dialog.pack(); // 내용에 맞게 크기 조정

            // 시작 위치 설정
//            dialog.setLocationRelativeTo(null); // 화면 중앙
             dialog.setLocation(25, 150);// 좌표 (100, 100))

            dialog.setVisible(true); // 팝업 표시
        });
        leftIconsPanel.add(robotButton);

        iconsRow.add(leftIconsPanel, BorderLayout.WEST);

        // --- 전송 버튼 (오른쪽) ---
        sendButton = new JButton("전송");
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.setFont(new Font("Arial", Font.PLAIN, 14));
        // 초기상태: 입력창 비어있으므로 회색
        sendButton.setBackground(Color.LIGHT_GRAY);
        sendButton.setForeground(Color.BLACK);
        // 클릭 시 sendMessage()
        sendButton.addActionListener(e -> sendMessage());

        iconsRow.add(sendButton, BorderLayout.EAST);

        // bottomPanel에 두 줄 추가
        bottomPanel.add(inputRow);
        bottomPanel.add(iconsRow);

        // --------------------
        // 메인 패널에 컴포넌트 배치
        // --------------------
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 아이콘/텍스트 버튼 공통 스타일
    private void styleIconButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
    }

    // (1) 전송 버튼 색상 변경 로직
    private void updateSendButtonColor() {
        String text = messageInputField.getText().trim();
        if (text.isEmpty()) {
            // 비어있으면 회색
            sendButton.setBackground(Color.LIGHT_GRAY);
        } else {
            // 텍스트가 있으면 노란색(FFE700)
            sendButton.setBackground(new Color(0xFFE700));
        }
    }

    public void setOutputStream(DataOutputStream os) {
        this.outputStream = os;
    }

    // 채팅중인 상대 Id와 이름 설정
    public void setTargetIdAndName(String targetId, String targetName) {
        this.targetId = targetId;
        this.targetName = targetName;

        if (currentUserLabel != null) {
            currentUserLabel.setText(targetName);
        }
    }

    // 채팅 메시지를 화면에 추가
    public void updateChattingText(String sender, String message, boolean isMyMessage, ImageIcon imageIcon) {
        // 메시지가 10개 초과 시 오래된 것 제거 (기존 로직 유지)
        if (!(chatContainer.getLayout() instanceof GridLayout)) {
            chatContainer.setLayout(new GridLayout(10, 1, 0, 9));
        }
        JPanel messagePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(isMyMessage ? new Color(0xFFEB3B) : Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        messagePanel.setOpaque(false);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel textContainer = new JPanel();
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
        textContainer.setOpaque(false);

        // 이미지가 있는 경우 처리 (이모티콘)
        if (imageIcon != null) {
            // 이모티콘 크기 조정 (예: 50x50)
            Image scaledImage = imageIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(scaledImage);

            // 조정된 이미지를 JLabel에 추가
            JLabel imageLabel = new JLabel(resizedIcon);
            textContainer.add(imageLabel);
        }

        // 텍스트 메시지 처리
        if (message != null && !message.isEmpty()) {
            JLabel messageLabel = new JLabel("<html>" + message.replaceAll("\n", "<br>") + "</html>");
            messageLabel.setForeground(Color.BLACK);
            messageLabel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
            textContainer.add(messageLabel);
        }

        messagePanel.add(textContainer, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new FlowLayout(isMyMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        wrapper.setOpaque(false);

        // 번역 버튼 추가
        if (imageIcon == null && message != null && !message.isEmpty()) {
            JButton translateButton = new JButton(
                    new ImageIcon(translateImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))
            );
            translateButton.setPreferredSize(new Dimension(25, 25));
            translateButton.setContentAreaFilled(false);
            translateButton.setBorderPainted(false);
            translateButton.setFocusPainted(false);

            translateButton.addActionListener(e -> {
                try {
                    String translatedText = googleTranslate.translate(message, targetLanguage);
                    System.out.println("[Client] 번역 결과 (" + targetLanguage + "): " + translatedText);

                    JLabel translationLabel = new JLabel("<html>"
                            + translatedText.replaceAll("\n", "<br>")
                            + "</html>");
                    translationLabel.setForeground(Color.GRAY);
                    translationLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

                    textContainer.add(translationLabel);
                    textContainer.revalidate();
                    textContainer.repaint();
                } catch (Exception ex) {
                    System.out.println("[Client] 번역 중 오류 발생: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            // 번역 버튼 위치를 송신자에 따라 다르게 추가
            if (isMyMessage) {
                wrapper.add(translateButton);
                wrapper.add(messagePanel);
            } else {
                wrapper.add(messagePanel);
                wrapper.add(translateButton);
            }
        } else {
            wrapper.add(messagePanel);
        }

        int currentComponentCount = chatContainer.getComponentCount();
        if (currentComponentCount < 10) {
            chatContainer.add(wrapper, currentComponentCount);
        } else {
            chatContainer.remove(0);
            chatContainer.add(wrapper, 9);
        }

        // 스크롤을 늘 최상단으로 이동
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMinimum());
        });

        chatContainer.revalidate();
        chatContainer.repaint();
    }

    // 메시지 전송
    private void sendMessage() {
        String message = messageInputField.getText().trim();
        if (message.isEmpty()) {
            System.out.println("[Client] 빈 메시지는 전송되지 않습니다.");
            return;
        }

        // outputStream 초기화 여부 확인
        if (outputStream == null) {
            System.out.println("[Client] 서버와의 연결이 설정되지 않았습니다. 메시지를 전송할 수 없습니다.");
            return;
        }

        // targetId 설정 여부 확인
        if (targetId == null || targetId.isEmpty()) {
            System.out.println("[Client] 대상 ID가 설정되지 않았습니다. 메시지를 전송할 수 없습니다.");
            return;
        }

        try {
            // UI 업데이트
            updateChattingText("Me", message, true, null);

            // 서버로 메시지 전송
            System.out.println("[Client] 메시지 전송: 대상 ID = " + targetId + ", 메시지 = " + message);
            outputStream.writeUTF("MESSAGE_TO_ID:" + targetId + ":" + message);

            // 입력 필드 초기화
            messageInputField.setText("");
        } catch (IOException e) {
            System.out.println("[Client] 메시지를 전송하는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 이모티콘 목록 팝업 생성
    private JPopupMenu createEmoticonPopup() {
        // 원본 JPopupMenu
        JPopupMenu popup = new JPopupMenu();

        // 이모티콘 패널 생성
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        // 이모티콘(메뉴아이템)들을 iconPanel에 추가
        iconPanel.add(createEmoticonMenuItem("src/assets/Emoticon-Thumbs.png", ThumbsEmoticonImg));
        iconPanel.add(createEmoticonMenuItem("src/assets/Emoticon-Falling-In-Love.png", FallingInLoveEmoticonImg));
        iconPanel.add(createEmoticonMenuItem("src/assets/Emoticon-Smile.png", SmileEmoticonImg));
        iconPanel.add(createEmoticonMenuItem("src/assets/Emoticon-Funny-And-Sad.png", FunnyAndSadEmoticonImg));
        iconPanel.add(createEmoticonMenuItem("src/assets/Emoticon-Double-Hearts.png", DoubleHeartsEmoticonImg));
        iconPanel.add(createEmoticonMenuItem("src/assets/Emoticon-Sad.png", SadEmoticonImg));

        // JPopupMenu에 패널 직접 추가
        popup.add(iconPanel);

        return popup;
    }

    // 이모티콘 받는 경우 처리
    public void receiveEmoticon(String sender, byte[] imageData) {
        try {
            // 바이트 배열을 ImageIcon으로 변환
            ImageIcon receivedIcon = new ImageIcon(imageData);

            // 이모티콘 크기 조정 (예: 50x50)
            Image scaledImage = receivedIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(scaledImage);

            // 채팅창에 이미지 표시
            updateChattingText(sender, null, false, resizedIcon);

            System.out.println("[Client] 이모티콘 수신 완료.");
        } catch (Exception e) {
            System.out.println("[Client] 이모티콘 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 이모티콘 메뉴아이템 생성
    private JButton createEmoticonMenuItem(String filePath, ImageIcon icon) {
        // 기존 icon 이미지를 축소
        Image scaledImg = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImg);

        // JButton을 사용하여 아이콘 버튼 생성
        JButton button = new JButton(resizedIcon);
        button.setPreferredSize(new Dimension(40, 40)); // 버튼 크기 설정
        button.setContentAreaFilled(false); // 버튼 투명하게
        button.setBorderPainted(false); // 테두리 없애기
        button.setFocusPainted(false);

        // 툴팁으로 파일 경로를 보여줌
        button.setToolTipText(filePath);

        // 버튼 클릭 시 이모티콘 파일 전송
        button.addActionListener(e -> sendEmoticonImage(filePath));

        return button;
    }

    /**
     * 이미지 파일 자체를 소켓으로 전송
     * 서버는 파일 크기와 바이트 배열을 수신 후
     * 동일하게 다른 클라이언트에게 파일을 전송하도록 구현해야 함
     */
    private void sendEmoticonImage(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("[Client] 파일이 존재하지 않습니다: " + filePath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            long fileSize = file.length();
            byte[] buffer = new byte[(int) fileSize];
            int readBytes = fis.read(buffer);

            ImageIcon emoticonIcon = new ImageIcon(filePath);

            // 1) 채팅창에 이미지 표시
            updateChattingText("Me", null, true, emoticonIcon);

            // 2) 서버로 파일 전송
            if (targetId != null) {
                outputStream.writeUTF("EMOTICON_FILE:" + targetId + ":" + file.getName() + ":" + fileSize);
                outputStream.write(buffer, 0, readBytes);
                outputStream.flush();

                System.out.println("[Client] 이모티콘 파일 전송 완료: " + filePath);
            } else {
                System.out.println("[Client] 대상 ID가 설정되지 않았습니다. 이모티콘을 전송할 수 없습니다.");
            }
        } catch (IOException ex) {
            System.out.println("[Client] 이모티콘 파일 전송 중 오류 발생.");
            ex.printStackTrace();
        }
    }
}