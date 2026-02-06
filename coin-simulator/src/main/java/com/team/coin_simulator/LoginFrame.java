package com.team.coin_simulator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("ONBIT 로그인");
        setSize(450, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 전체 배경 (연한 회색)
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));

        // 중앙 흰색 카드 영역
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(380, 550));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(40, 30, 40, 30)));

        // 상단 로고 및 타이틀
        JLabel logo = new JLabel("ONBIT");
        logo.setFont(new Font("Arial", Font.BOLD, 32));
        logo.setForeground(new Color(33, 99, 184));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("로그인");
        subtitle.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 입력 필드 설정
        JTextField idField = new JTextField();
        JPasswordField pwField = new JPasswordField();
        styleInput(idField, "아이디 (이메일)");
        styleInput(pwField, "비밀번호");

        // 로그인 버튼
        JButton loginBtn = new JButton("로그인");
        stylePrimaryButton(loginBtn, new Color(33, 99, 184), Color.WHITE);
        loginBtn.addActionListener(e -> {
            // 로그인 로직 추가 위치
        });

        // 링크 영역 (아이디/비번 찾기)
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        linkPanel.setBackground(Color.WHITE);
        JLabel findId = new JLabel("아이디 찾기");
        JLabel findPw = new JLabel("비밀번호 찾기");
        findId.setForeground(Color.GRAY);
        findId.setCursor(new Cursor(Cursor.HAND_CURSOR));
        findPw.setForeground(Color.GRAY);
        findPw.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkPanel.add(findId);
        linkPanel.add(new JLabel("|"));
        linkPanel.add(findPw);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 하단 회원가입 유도 영역
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel joinLabel = new JLabel("아직 회원이 아니신가요?");
        joinLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        JButton joinBtn = new JButton("회원가입");
        joinBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        joinBtn.setForeground(new Color(33, 99, 184));
        joinBtn.setContentAreaFilled(false);
        joinBtn.setFocusPainted(false);
        joinBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        joinBtn.addActionListener(e -> {
            new JoinFrame();
            this.dispose();
        });

        footer.add(joinLabel, BorderLayout.WEST);
        footer.add(joinBtn, BorderLayout.EAST);

        // 컴포넌트 배치
        card.add(logo);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(40));
        card.add(idField);
        card.add(Box.createVerticalStrut(15));
        card.add(pwField);
        card.add(Box.createVerticalStrut(25));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(linkPanel);
        card.add(Box.createVerticalGlue());
        card.add(footer);

        root.add(card);
        add(root);
        setVisible(true);
    }

    private void styleInput(JTextField field, String title) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, 55));
        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(new Color(220, 220, 220), 1), title);
        border.setTitleFont(new Font("맑은 고딕", Font.PLAIN, 11));
        field.setBorder(border);
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void stylePrimaryButton(JButton btn, Color bg, Color fg) {
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
    }
}