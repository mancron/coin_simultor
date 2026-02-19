package com.team.coin_simulator.login;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import DAO.UserDAO;
import DTO.UserDTO;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {

    private final Font fontBold = new Font("맑은 고딕", Font.BOLD, 14);
    private final Font fontPlain = new Font("맑은 고딕", Font.PLAIN, 12);
    private final Font fontSmall = new Font("맑은 고딕", Font.PLAIN, 11);

    // UserDAO 인스턴스 생성 (static 에러 방지)
    private UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("ONBIT 로그인");
        setSize(460, 700); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 전체 배경 설정
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));

        // 중앙 카드 레이아웃
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(380, 580));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(45, 35, 40, 35)));

        // 로고 및 타이틀
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

        // 입력 필드 생성
        JTextField idField = new JTextField();
        JPasswordField pwField = new JPasswordField();
        styleField(idField, "아이디 (이메일)");
        styleField(pwField, "비밀번호");

        card.add(idField);
        card.add(Box.createVerticalStrut(15));
        card.add(pwField);
        card.add(Box.createVerticalStrut(25));

        // 로그인 버튼 및 기능 구현
        JButton loginBtn = new JButton("로그인");
        stylePrimaryBtn(loginBtn);
        loginBtn.addActionListener(e -> {
            String userId = idField.getText().trim();
            String password = new String(pwField.getPassword());

            if (userId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.");
                return;
            }

            // DB 대조 작업
            UserDTO user = userDAO.loginCheck(userId, password);
            if (user != null) {
                JOptionPane.showMessageDialog(this, user.getNickname() + "님, 환영합니다!");
                // TODO: 메인 화면 프레임 호출 로직 (예: new MainFrame(user);)
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(loginBtn);

        // 아이디/비밀번호 찾기 영역
        card.add(Box.createVerticalStrut(25));
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        linkPanel.setOpaque(false);
        
        JLabel findIdLabel = new JLabel("아이디 찾기");
        JLabel findPwLabel = new JLabel("비밀번호 찾기");
        findIdLabel.setFont(fontSmall);
        findPwLabel.setFont(fontSmall);
        findIdLabel.setForeground(Color.GRAY);
        findPwLabel.setForeground(Color.GRAY);
        findIdLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        findPwLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 아이디 찾기 이벤트
        findIdLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String phone = JOptionPane.showInputDialog(LoginFrame.this, 
                    "가입 시 등록한 휴대폰 번호를 입력해주세요.", "아이디 찾기", JOptionPane.QUESTION_MESSAGE);
                
                if (phone != null && !phone.isEmpty()) {
                    String foundId = userDAO.findIdByPhone(phone);
                    if (foundId != null) {
                        JOptionPane.showMessageDialog(LoginFrame.this, 
                            "찾으시는 아이디는 [" + foundId + "] 입니다.", "아이디 찾기 성공", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, 
                            "해당 번호로 가입된 정보가 없습니다.", "찾기 실패", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // 비밀번호 찾기 이벤트
        findPwLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String email = JOptionPane.showInputDialog(LoginFrame.this, 
                    "아이디(이메일)를 입력해주세요.", "비밀번호 찾기", JOptionPane.QUESTION_MESSAGE);
                
                if (email != null && !email.isEmpty()) {
                    if (userDAO.isIdDuplicate(email)) {
                        JOptionPane.showMessageDialog(LoginFrame.this, 
                            email + "님, 비밀번호 재설정 안내를 확인해주세요.\n(실제 메일 발송은 SMTP 설정이 필요합니다.)", 
                            "안내", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, 
                            "존재하지 않는 아이디입니다.", "실패", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        linkPanel.add(findIdLabel);
        JLabel separator = new JLabel("|");
        separator.setForeground(Color.LIGHT_GRAY);
        linkPanel.add(separator);
        linkPanel.add(findPwLabel);
        card.add(linkPanel);

        // 회원가입 유도 영역
        card.add(Box.createVerticalStrut(35)); 
        JPanel joinHintPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        joinHintPanel.setOpaque(false);
        
        JLabel joinLabel = new JLabel("아직 회원이 아니신가요?");
        joinLabel.setFont(fontSmall);
        joinLabel.setForeground(Color.DARK_GRAY);
        
        JButton goToJoinBtn = new JButton("회원가입"); // 변수명 중복 방지
        goToJoinBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        goToJoinBtn.setForeground(new Color(33, 99, 184));
        goToJoinBtn.setContentAreaFilled(false);
        goToJoinBtn.setBorderPainted(false);
        goToJoinBtn.setFocusPainted(false);
        goToJoinBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        goToJoinBtn.setMargin(new Insets(0, 0, 0, 0));
        
        goToJoinBtn.addActionListener(e -> {
            new JoinFrame();
            this.dispose();
        });

        joinHintPanel.add(joinLabel);
        joinHintPanel.add(goToJoinBtn);
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