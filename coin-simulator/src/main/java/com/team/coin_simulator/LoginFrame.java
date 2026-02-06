package com.team.coin_simulator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final Font fontBold = new Font("맑은 고딕", Font.BOLD, 14);
    private final Font fontPlain = new Font("맑은 고딕", Font.PLAIN, 12);
    private final Font fontSmall = new Font("맑은 고딕", Font.PLAIN, 11);

    public LoginFrame() {
        setTitle("ONBIT 로그인");
        setSize(460, 700); // 하단 바를 없앴으므로 높이를 적절히 조절
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 전체 배경
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));

        // 중앙 흰색 카드 영역
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(380, 580));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(45, 35, 40, 35)));

        // 상단 로고 및 타이틀
        JLabel logo = new JLabel("ONBIT");
        logo.setFont(new Font("Arial", Font.BOLD, 32));
        logo.setForeground(new Color(33, 99, 184));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("로그인");
        subtitle.setFont(fontPlain);
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logo);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(40));

        // 입력 필드
        JTextField idField = new JTextField();
        JPasswordField pwField = new JPasswordField();
        styleField(idField, "아이디 (이메일)");
        styleField(pwField, "비밀번호");

        card.add(idField);
        card.add(Box.createVerticalStrut(15));
        card.add(pwField);
        card.add(Box.createVerticalStrut(25));

        // 로그인 버튼
        JButton loginBtn = new JButton("로그인");
        stylePrimaryBtn(loginBtn);
        card.add(loginBtn);

        // 1. 아이디/비밀번호 찾기 영역
        card.add(Box.createVerticalStrut(25));
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        linkPanel.setOpaque(false);
        
        JLabel findId = new JLabel("아이디 찾기");
        JLabel findPw = new JLabel("비밀번호 찾기");
        findId.setFont(fontSmall);
        findPw.setFont(fontSmall);
        findId.setForeground(Color.GRAY);
        findPw.setForeground(Color.GRAY);
        findId.setCursor(new Cursor(Cursor.HAND_CURSOR));
        findPw.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        linkPanel.add(findId);
        linkPanel.add(new JLabel("|")).setForeground(Color.LIGHT_GRAY);
        linkPanel.add(findPw);
        card.add(linkPanel);

        // 2. 회원가입 안내 영역 (아이디/비번 찾기 바로 밑에 배치)
        card.add(Box.createVerticalStrut(35)); // 적절한 간격 추가
        
        JPanel joinHintPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        joinHintPanel.setOpaque(false);
        
        JLabel joinLabel = new JLabel("아직 회원이 아니신가요?");
        joinLabel.setFont(fontSmall);
        joinLabel.setForeground(Color.DARK_GRAY);
        
        JButton joinBtn = new JButton("회원가입");
        joinBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        joinBtn.setForeground(new Color(33, 99, 184));
        joinBtn.setContentAreaFilled(false);
        joinBtn.setBorderPainted(false);
        joinBtn.setFocusPainted(false);
        joinBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        joinBtn.setMargin(new Insets(0, 0, 0, 0)); // 버튼 여백 제거
        
        joinBtn.addActionListener(e -> {
            new JoinFrame();
            this.dispose();
        });

        joinHintPanel.add(joinLabel);
        joinHintPanel.add(joinBtn);
        card.add(joinHintPanel);

        root.add(card);
        add(root);
        setVisible(true);
    }

    private void styleField(JTextField field, String title) {
        field.setMaximumSize(new Dimension(320, 50));
        field.setPreferredSize(new Dimension(320, 50));
        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(new Color(230, 230, 230)), title);
        border.setTitleFont(fontSmall);
        field.setBorder(border);
        field.setFont(fontPlain);
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void stylePrimaryBtn(JButton btn) {
        btn.setMaximumSize(new Dimension(320, 50));
        btn.setPreferredSize(new Dimension(320, 50));
        btn.setBackground(new Color(33, 99, 184));
        btn.setForeground(Color.WHITE);
        btn.setFont(fontBold);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}