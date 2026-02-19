package com.team.coin_simulator.login;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import DAO.UserDAO;
import DTO.UserDTO;

public class JoinFrame extends JFrame {
    private final Font fontBold = new Font("맑은 고딕", Font.BOLD, 14);
    private final Font fontPlain = new Font("맑은 고딕", Font.PLAIN, 12);
    private final Font fontSmall = new Font("맑은 고딕", Font.PLAIN, 11);

    private UserDAO userDAO = new UserDAO();
    private JTextField[] fields = new JTextField[5]; 

    public JoinFrame() {
        setTitle("ONBIT 회원가입");
        setSize(460, 750); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(380, 650));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        JLabel titleLabel = new JLabel("회원가입");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(25));

        String[] labels = {"이메일", "비밀번호", "비밀번호 확인", "휴대폰 번호", "초기 자산 설정 (KRW)"};
        for (int i = 0; i < labels.length; i++) {
            JTextField field = (labels[i].contains("비밀번호")) ? new JPasswordField() : new JTextField();
            styleField(field, labels[i]);
            fields[i] = field; 
            card.add(field);
            card.add(Box.createVerticalStrut(12));
        }

        // 비밀번호 도움말 문구 추가
        JLabel pwHint = new JLabel(" * 비밀번호: 영문, 숫자, 특수문자 포함 8~16자");
        pwHint.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        pwHint.setForeground(new Color(33, 99, 184));
        pwHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(pwHint);
        card.add(Box.createVerticalStrut(10));

        JButton joinBtn = new JButton("가입하기");
        stylePrimaryBtn(joinBtn);
        
        joinBtn.addActionListener(e -> {
            String email = fields[0].getText().trim();
            String pw = new String(((JPasswordField)fields[1]).getPassword());
            String pwConfirm = new String(((JPasswordField)fields[2]).getPassword());
            String phone = fields[3].getText().trim();
            
            // 1. 공백 검사
            if (email.isEmpty() || pw.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 정보를 입력해주세요.");
                return;
            }

            // 2. 비밀번호 유효성 검사 (정규식 적용)
            if (!isValidPassword(pw)) {
                JOptionPane.showMessageDialog(this, 
                    "비밀번호 형식이 올바르지 않습니다.\n(영문, 숫자, 특수문자 조합 8~16자)", 
                    "보안 취약", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. 비밀번호 일치 확인
            if (!pw.equals(pwConfirm)) {
                JOptionPane.showMessageDialog(this, "비밀번호 확인이 일치하지 않습니다.");
                return;
            }

            // 4. 가입 처리
            if (userDAO.isIdDuplicate(email)) {
                JOptionPane.showMessageDialog(this, "이미 가입된 이메일입니다.");
            } else {
                UserDTO newUser = new UserDTO(email, pw, "별명미지정");
                // DB 설계에 따라 phone 정보도 저장하려면 DTO/DAO 수정 필요
                if (userDAO.insertUser(newUser)) {
                    JOptionPane.showMessageDialog(this, "가입 성공!");
                    new LoginFrame();
                    this.dispose();
                }
            }
        });
        card.add(joinBtn);

        card.add(Box.createVerticalStrut(15));
        JButton backBtn = new JButton("이미 계정이 있으신가요? 로그인");
        backBtn.setFont(fontSmall);
        backBtn.setForeground(Color.GRAY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(ev -> {
            new LoginFrame();
            this.dispose();
        });
        card.add(backBtn);

        root.add(card);
        add(root);
        setVisible(true);
    }

    /**
     * 비밀번호 검증 메서드
     */
    private boolean isValidPassword(String password) {
        // 영문, 숫자, 특수문자($@$!%*#?&)가 최소 하나씩 포함된 8~16자
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,16}$";
        return password.matches(regex);
    }

    private void styleField(JTextField field, String title) {
        field.setMaximumSize(new Dimension(320, 50));
        TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(new Color(230, 230, 230)), title);
        border.setTitleFont(fontSmall);
        field.setBorder(border);
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void stylePrimaryBtn(JButton btn) {
        btn.setMaximumSize(new Dimension(320, 50));
        btn.setBackground(new Color(33, 99, 184));
        btn.setForeground(Color.WHITE);
        btn.setFont(fontBold);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JoinFrame());
    }
}