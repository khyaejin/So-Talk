package client;

import javax.swing.*;
import java.awt.*;

/**
 * 로그인 화면 패널
 * 아이디/비밀번호 입력 및 로그인 버튼 포함
 */
public class StartPanel extends JPanel {
    public StartPanel(MessengerFrame frame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(0xFEE502));
        setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // 이미지 추가
        ImageIcon kakao = new ImageIcon("src/assets/kakaoLogo.png");
        Image img = kakao.getImage();
        Image scaledImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel kakaoLabel = new JLabel(new ImageIcon(scaledImg));
        kakaoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(kakaoLabel);

        // 아이디 입력 필드
        JTextField idField = new JTextField();
        idField.setMaximumSize(new Dimension(230, 43));
        add(idField);

        // 비밀번호 입력 필드
        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(230, 43));
        add(passwordField);

        // 로그인 버튼
        JButton loginButton = new JButton("로그인");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(230, 43));
        loginButton.addActionListener(e -> {
            String id = idField.getText();
            String password = new String(passwordField.getPassword());
            System.out.println("아이디: " + id + ", 비밀번호: " + password);

            if (id.equals("test1") && password.equals("1234") || id.equals("test2") && password.equals("1234")) {
                frame.showHomePanel();
            } else {
                JOptionPane.showMessageDialog(this, "아이디 혹은 비밀번호가 일치하지 않습니다.");
            }
        });
        add(loginButton);
    }
}