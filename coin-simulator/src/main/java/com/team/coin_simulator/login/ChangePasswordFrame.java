package com.team.coin_simulator.login;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import DAO.UserDAO;

public class ChangePasswordFrame extends JFrame {

    public ChangePasswordFrame(String userId) {
        setTitle("비밀번호 변경");
        setSize(360, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel();
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        JPasswordField currentPw = new JPasswordField();
        JPasswordField newPw = new JPasswordField();
        JPasswordField newPw2 = new JPasswordField();

        root.add(new JLabel("현재 비밀번호"));
        root.add(currentPw);
        root.add(Box.createVerticalStrut(10));

        root.add(new JLabel("새 비밀번호"));
        root.add(newPw);
        root.add(Box.createVerticalStrut(10));

        root.add(new JLabel("새 비밀번호 확인"));
        root.add(newPw2);
        root.add(Box.createVerticalStrut(16));

        JButton changeBtn = new JButton("변경하기");
        changeBtn.addActionListener(e -> {
            String cur = new String(currentPw.getPassword());
            String np = new String(newPw.getPassword());
            String np2 = new String(newPw2.getPassword());

            if (cur.isEmpty() || np.isEmpty() || np2.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 칸을 입력해주세요.");
                return;
            }
            if (!np.equals(np2)) {
                JOptionPane.showMessageDialog(this, "새 비밀번호 확인이 일치하지 않습니다.");
                return;
            }
            if (np.length() < 6) {
                JOptionPane.showMessageDialog(this, "새 비밀번호는 최소 6자 이상을 권장합니다.");
                return;
            }

            boolean ok = UserDAO.changePassword(userId, cur, np);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "현재 비밀번호가 틀렸거나 변경에 실패했습니다.", "실패", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "비밀번호가 변경되었습니다!");
            dispose();
        });

        root.add(changeBtn);
        add(root);
        setVisible(true);
    }
}
