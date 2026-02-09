package com.team.coin_simulator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public class JoinFrame extends JFrame {
    
    private final Font fontBold = new Font("맑은 고딕", Font.BOLD, 14);
    private final Font fontPlain = new Font("맑은 고딕", Font.PLAIN, 12);
    private final Font fontSmall = new Font("맑은 고딕", Font.PLAIN, 11);

    public JoinFrame() {
        setTitle("ONBIT 회원가입");
        setSize(460, 820); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(380, 740)); 
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(35, 30, 30, 30)));

        // 로고 및 타이틀
        JLabel logoLabel = new JLabel("ONBIT");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 28));
        logoLabel.setForeground(new Color(33, 99, 184));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitle = new JLabel("회원가입");
        subtitle.setFont(fontPlain);
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logoLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));

        // 입력 필드들
        String[] labels = {"이메일", "비밀번호", "비밀번호 확인", "휴대폰 번호", "초기 자산 설정 (KRW)"};
        for (String label : labels) {
            JTextField field = (label.contains("비밀번호")) ? new JPasswordField() : new JTextField();
            styleField(field, label);
            card.add(field);
            card.add(Box.createVerticalStrut(12));
        }

        JButton joinBtn = new JButton("가입하기");
        stylePrimaryBtn(joinBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(joinBtn);

        card.add(Box.createVerticalStrut(20));
        JLabel orLabel = new JLabel("또는");
        orLabel.setFont(fontSmall);
        orLabel.setForeground(Color.LIGHT_GRAY);
        orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(orLabel);
        card.add(Box.createVerticalStrut(20));

        // ===== 구글 버튼 (중앙 정렬 수정) =====
        JButton googleBtn = new JButton("구글로 시작하기");
        googleBtn.setIcon(createGoogleLogo());
        styleSocialBtn(googleBtn, Color.WHITE, new Color(33, 99, 184), true);
        // 중앙 정렬을 위해 수정
        googleBtn.setHorizontalAlignment(SwingConstants.CENTER);
        googleBtn.setIconTextGap(15); 

        // ===== 카카오 버튼 (중앙 정렬 수정) =====
        JButton kakaoBtn = new JButton("카카오로 시작하기");
        kakaoBtn.setIcon(createKakaoLogo());
        styleSocialBtn(kakaoBtn, new Color(254, 229, 0), new Color(60, 30, 30), false);
        // 중앙 정렬을 위해 수정
        kakaoBtn.setHorizontalAlignment(SwingConstants.CENTER);
        kakaoBtn.setIconTextGap(15);

        card.add(googleBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(kakaoBtn);
        
        card.add(Box.createVerticalGlue());
        JButton backBtn = new JButton("이미 계정이 있으신가요? 로그인");
        backBtn.setFont(fontSmall);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setForeground(Color.GRAY);
        backBtn.addActionListener(e -> this.dispose());

        card.add(backBtn);
        root.add(card);
        add(root);
        setVisible(true);
    }

    private Icon createGoogleLogo() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.setColor(new Color(66, 133, 244));
                g2.drawString("G", x, y + 15); // 높이 미세 조정
                g2.dispose();
            }
            @Override public int getIconWidth() { return 20; }
            @Override public int getIconHeight() { return 20; }
        };
    }

    private Icon createKakaoLogo() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 30, 30));
                g2.fill(new Ellipse2D.Double(x, y + 2, 18, 14));
                Path2D.Double path = new Path2D.Double();
                path.moveTo(x + 4, y + 14);
                path.lineTo(x + 1, y + 18);
                path.lineTo(x + 8, y + 15);
                path.closePath();
                g2.fill(path);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 20; }
            @Override public int getIconHeight() { return 20; }
        };
    }

    private void styleField(JTextField field, String title) {
        field.setMaximumSize(new Dimension(320, 50));
        field.setPreferredSize(new Dimension(320, 50));
        TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(new Color(230, 230, 230)), title);
        border.setTitleFont(fontSmall);
        field.setBorder(border);
        field.setFont(fontPlain);
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void stylePrimaryBtn(JButton btn) {
        btn.setMaximumSize(new Dimension(320, 48));
        btn.setPreferredSize(new Dimension(320, 48));
        btn.setBackground(new Color(33, 99, 184));
        btn.setForeground(Color.WHITE);
        btn.setFont(fontBold);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void styleSocialBtn(JButton btn, Color bg, Color fg, boolean hasBorder) {
        btn.setMaximumSize(new Dimension(320, 48));
        btn.setPreferredSize(new Dimension(320, 48));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(fontBold);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 핵심 수정: 왼쪽 마진을 없애고 전체 중앙 정렬로 변경
        btn.setMargin(new Insets(0, 0, 0, 0)); 
        if (hasBorder) btn.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        else btn.setBorderPainted(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JoinFrame::new);
    }
}