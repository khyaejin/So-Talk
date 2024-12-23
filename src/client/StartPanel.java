package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 로그인 화면 패널
 * 아이디/비밀번호 입력 및 로그인 버튼 포함
 */
public class StartPanel extends JPanel {
    public StartPanel(MessengerFrame frame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(70, 50, 0, 50)); // 위쪽 여백 추가

        // 배경색에 그라데이션 적용
        setOpaque(false);

        // 공통 폰트 설정
        Font commonFont;
        try {
            File fontFile = new File("src/assets/font/Moneygraphy-Pixel.otf");
            commonFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(16f);
        } catch (IOException | FontFormatException e) {
            commonFont = new Font("Arial", Font.PLAIN, 16); // 기본 폰트로 대체
            e.printStackTrace();
        }

        // 이미지 추가
        ImageIcon kakao = new ImageIcon("src/assets/logo.png");
        Image img = kakao.getImage();
        Image scaledImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel kakaoLabel = new JLabel(new ImageIcon(scaledImg));
        kakaoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(kakaoLabel);

        // 문구 추가
        JLabel textLabel = new JLabel("글로벌 시대에 걸맞는 채팅 플랫폼");
        textLabel.setFont(commonFont.deriveFont(15f)); // 사이즈를 20pt로 조정
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(textLabel);

        // 위쪽 여백 추가
        add(Box.createVerticalStrut(10));

        // 아이디 입력 필드
        JTextField idField = new JTextField();
        idField.setFont(commonFont.deriveFont(15f));
        idField.setMaximumSize(new Dimension(230, 43));
        add(idField);

        // 비밀번호 입력 필드
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(commonFont.deriveFont(15f));
        passwordField.setMaximumSize(new Dimension(230, 43));
        add(passwordField);

        // 여백 추가
        add(Box.createVerticalStrut(7));

        // 로그인 버튼
        JButton loginButton = new JButton("So-Talk 바로 시작하기");
        loginButton.setFont(commonFont.deriveFont(12f));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(230, 43));
        loginButton.addActionListener(e -> {
            String id = idField.getText();
            String password = new String(passwordField.getPassword());

            if (id.equals("test1") && password.equals("1234") || id.equals("test2") && password.equals("1234") || id.equals("test3") && password.equals("1234")) {
                try {
                    DataOutputStream os = frame.getOutputStream();
                    // test1은 ID 1, test2는 ID 2 tet3은 ID 3
                    String userId = "";
                    if(id.equals("test1")) userId="1";
                    else if(id.equals("test2")) userId="2";
                    else userId="3";
                    os.writeUTF("SET_ID:" + userId); // 서버에 ID 전송
                    os.writeUTF("SET_NAME:" + id); // 서버에 사용자 이름 전송

                    // 사용자 이름과 ID를 프레임에 설정
                    frame.setUserNameAndId(id, userId);

                    // 홈 화면으로 전환
                    frame.showHomePanel();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "서버와 연결할 수 없습니다.");
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "아이디 혹은 비밀번호가 일치하지 않습니다.");
            }
        });
        add(loginButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        // 그라데이션 색상 설정
        Color color1 = new Color(0xC6DBE7);
        Color color2 = new Color(0xFFFFFF);
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);
    }
}
