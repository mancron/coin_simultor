package com.team.coin_simulator;

import javax.swing.*;

import com.team.coin_simulator.user.User;
import com.team.coin_simulator.user.UserDAO;

import java.awt.*;

public class LoginFrame extends JFrame {

    private UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("코인 시뮬레이터 로그인");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField emailField = new JTextField(15);
        JPasswordField pwField = new JPasswordField(15);

        JButton loginBtn = new JButton("로그인");
        JButton joinBtn = new JButton("회원가입");

        // 로그인
        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String pw = new String(pwField.getPassword());

            User user = userDAO.findByEmail(email);
            if (user != null && user.getPasswordHash().equals(pw)) {
                JOptionPane.showMessageDialog(this, "로그인 성공");
            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패");
            }
        });

        // 회원가입
        joinBtn.addActionListener(e -> {
            String email = emailField.getText();
            String pw = new String(pwField.getPassword());

            if (userDAO.existsByEmail(email)) {
                JOptionPane.showMessageDialog(this, "이미 존재하는 계정");
            } else {
                userDAO.save(email, pw);
                JOptionPane.showMessageDialog(this, "회원가입 성공");
            }
        });

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Email"));
        panel.add(emailField);
        panel.add(new JLabel("Password"));
        panel.add(pwField);
        panel.add(loginBtn);
        panel.add(joinBtn);

        add(panel);
        setVisible(true);
    }
}
