package com.team.coin_simulator.profile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

import DAO.UserDAO;
import DTO.UserDTO;

public class PasswordChangeDialog extends JDialog {

    private final String userId;

    private final JPasswordField curPw = new JPasswordField();
    private final JPasswordField newPw = new JPasswordField();
    private final JPasswordField newPw2 = new JPasswordField();

    private final Font fontBold  = new Font("맑은 고딕", Font.BOLD, 14);
    private final Font fontPlain = new Font("맑은 고딕", Font.PLAIN, 12);
    private final Font fontSmall = new Font("맑은 고딕", Font.PLAIN, 11);

    public PasswordChangeDialog(Dialog owner, String userId) {
        super(owner, "비밀번호 변경", true);
        this.userId = userId;

        setSize(430, 430);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));
        setContentPane(root);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(360, 330));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(30, 25, 25, 25)
        ));

        JLabel title = new JLabel("비밀번호 변경");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(18));

        styleField(curPw, "현재 비밀번호");
        styleField(newPw, "새 비밀번호");
        styleField(newPw2, "새 비밀번호 확인");

        card.add(curPw);
        card.add(Box.createVerticalStrut(12));
        card.add(newPw);
        card.add(Box.createVerticalStrut(12));
        card.add(newPw2);
        card.add(Box.createVerticalStrut(14));

        JLabel hint = new JLabel("<html>* 영문, 숫자, 특수문자(!@#$%^&*) 포함 8~16자</html>");
        hint.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        hint.setForeground(new Color(33, 99, 184));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(hint);

        card.add(Box.createVerticalStrut(18));

        JButton btnChange = new JButton("변경하기");
        stylePrimaryBtn(btnChange);
        btnChange.addActionListener(e -> handleChange());
        card.add(btnChange);

        // Enter = 변경하기
        getRootPane().setDefaultButton(btnChange);

        root.add(card);
    }

    private void handleChange() {
        String cur = new String(curPw.getPassword());
        String np  = new String(newPw.getPassword());
        String np2 = new String(newPw2.getPassword());

        if (cur.isBlank() || np.isBlank() || np2.isBlank()) {
            JOptionPane.showMessageDialog(this, "모든 칸을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!np.equals(np2)) {
            JOptionPane.showMessageDialog(this, "새 비밀번호가 서로 다릅니다.", "확인 필요", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!isValidPassword(np)) {
            JOptionPane.showMessageDialog(this,
                    "비밀번호 규칙에 맞지 않습니다.\n(영문, 숫자, 특수문자 조합 8~16자)",
                    "보안 약함", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 현재 비번 검증
            UserDTO user = UserDAO.loginCheck(userId, cur);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "현재 비밀번호가 올바르지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ✅ 네 DAO에 이미 있는 메서드 사용
            boolean ok = UserDAO.updatePassword(userId, np);
            if (ok) {
                JOptionPane.showMessageDialog(this, "비밀번호가 변경되었습니다.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "비밀번호 변경(DB)에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "비밀번호 변경 중 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,16}$";
        return password.matches(regex);
    }

    private void styleField(JTextField field, String title) {
        field.setMaximumSize(new Dimension(310, 52));
        field.setPreferredSize(new Dimension(310, 52));
        field.setFont(fontPlain);

        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(new Color(230, 230, 230), 1), title);
        border.setTitleFont(fontSmall);
        border.setTitleColor(Color.DARK_GRAY);

        field.setBorder(BorderFactory.createCompoundBorder(
                border,
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void stylePrimaryBtn(JButton btn) {
        btn.setMaximumSize(new Dimension(310, 48));
        btn.setPreferredSize(new Dimension(310, 48));
        btn.setFont(fontBold);
        btn.setBackground(new Color(33, 99, 184));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}