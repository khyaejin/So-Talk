package client;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 로그인 화면 패널
 * 아이디/비밀번호 입력 및 로그인 버튼 포함
 */
public class StartPanel extends JPanel {
    public StartPanel(MessengerFrame frame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(0xFEE502));
        setBorder(BorderFactory.createEmptyBorder(70, 50, 0, 50)); // 위쪽 여백 추가

        // 이미지 추가
        ImageIcon kakao = new ImageIcon("src/assets/kakaoLogo.png");
        Image img = kakao.getImage();
        Image scaledImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel kakaoLabel = new JLabel(new ImageIcon(scaledImg));
        kakaoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(kakaoLabel);

        // 위쪽 여백 추가
        add(Box.createVerticalStrut(10));

        // 아이디 입력 필드
        JTextField idField = new JTextField();
        idField.setMaximumSize(new Dimension(230, 43));
        add(idField);

        // 비밀번호 입력 필드
        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(230, 43));
        add(passwordField);

        // 여백 추가
        add(Box.createVerticalStrut(7));

        // 로그인 버튼
        JButton loginButton = new JButton("로그인");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(230, 43));
        loginButton.addActionListener(e -> {
            String id = idField.getText();
            String password = new String(passwordField.getPassword());

            if (id.equals("test1") && password.equals("1234") || id.equals("test2") && password.equals("1234")) {
                try {
                    DataOutputStream os = frame.getOutputStream();
                    os.writeUTF(id); // 서버에 사용자 이름 전송
                    frame.setUserName(id); // 사용자 이름 설정

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
}
