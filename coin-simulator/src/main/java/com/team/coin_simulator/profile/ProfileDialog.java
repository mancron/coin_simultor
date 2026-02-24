package com.team.coin_simulator.profile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

import DAO.UserDAO;
import DTO.UserDTO;
import com.team.coin_simulator.login.LoginFrame;

public class ProfileDialog extends JDialog {

    private final JFrame owner;
    private final String userId;

    private final CircleImageLabel profileImage = new CircleImageLabel(120);
    private final JLabel lblNickname = new JLabel("", SwingConstants.CENTER);

    public ProfileDialog(JFrame owner, String userId) {
        super(owner, "내 프로필", true);
        this.owner = owner;
        this.userId = userId;

        setSize(420, 560);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel();
        root.setBackground(Color.WHITE);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(25, 25, 25, 25));
        setContentPane(root);

        JLabel title = new JLabel("내 프로필");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        root.add(title);
        root.add(Box.createVerticalStrut(18));

        profileImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(profileImage);

        JButton btnPhoto = new JButton("사진 등록/변경");
        styleLink(btnPhoto);
        btnPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPhoto.addActionListener(e -> pickAndSavePhoto());

        root.add(Box.createVerticalStrut(10));
        root.add(btnPhoto);
        root.add(Box.createVerticalStrut(14));

        lblNickname.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        lblNickname.setForeground(new Color(40, 40, 40));
        lblNickname.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(lblNickname);

        root.add(Box.createVerticalStrut(25));

        JButton btnNick = new JButton("닉네임 변경");
        JButton btnPw   = new JButton("비밀번호 변경");
        JButton btnOut  = new JButton("로그아웃");

        styleMenu(btnNick);
        styleMenu(btnPw);
        styleDanger(btnOut);

        btnNick.addActionListener(e -> changeNickname());
        btnPw.addActionListener(e -> new PasswordChangeDialog(this, userId).setVisible(true));
        btnOut.addActionListener(e -> logout());

        root.add(btnNick);
        root.add(Box.createVerticalStrut(10));
        root.add(btnPw);
        root.add(Box.createVerticalStrut(18));
        root.add(btnOut);

        loadUser();
    }

    private void loadUser() {
        try {
            UserDTO user = UserDAO.getUserById(userId);

            String nick = (user != null && user.getNickname() != null && !user.getNickname().isBlank())
                    ? user.getNickname()
                    : userId;
            lblNickname.setText(nick);

            String path = (user != null) ? user.getProfileImagePath() : null;

            // ✅ 원인 추적 로그
            System.out.println("[ProfileDialog] loadUser userId=" + userId);
            System.out.println("[ProfileDialog] loadUser profile_image_path=" + path);

            profileImage.setImagePath(path);

        } catch (Exception e) {
            e.printStackTrace();
            lblNickname.setText(userId);
            profileImage.setImagePath(null);
        }
    }

    private void pickAndSavePhoto() {
        FileDialog fd = new FileDialog((Frame) SwingUtilities.getWindowAncestor(this), "프로필 사진 선택", FileDialog.LOAD);
        fd.setFilenameFilter((dir, name) -> {
            String n = name.toLowerCase();
            return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")
                    || n.endsWith(".gif") || n.endsWith(".bmp") || n.endsWith(".webp");
        });
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();
        if (file == null || dir == null) return; // 취소

        String path = dir + file;

        // ✅ 파일 존재 체크 로그
        File f = new File(path);
        System.out.println("[ProfileDialog] selected path=" + path);
        System.out.println("[ProfileDialog] exists=" + f.exists() + ", length=" + (f.exists() ? f.length() : -1));

        // UI 반영
        profileImage.setImagePath(path);

        // DB 저장
        boolean ok = UserDAO.updateProfileImagePath(userId, path);
        System.out.println("[ProfileDialog] updateProfileImagePath ok=" + ok);

        if (!ok) {
            JOptionPane.showMessageDialog(this, "사진 경로 DB 저장 실패", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ✅ DB에서 바로 다시 읽어서 정말 저장됐는지 확인 + 반영
        UserDTO re = UserDAO.getUserById(userId);
        String dbPath = (re != null) ? re.getProfileImagePath() : null;
        System.out.println("[ProfileDialog] dbPath(after update)=" + dbPath);

        profileImage.setImagePath(dbPath);
    }

    private void changeNickname() {
        String input = JOptionPane.showInputDialog(this, "새 닉네임을 입력하세요.");
        if (input == null) return;

        String newNick = input.trim();
        if (newNick.isEmpty()) return;

        try {
            boolean ok = UserDAO.updateNickname(userId, newNick);
            if (ok) {
                lblNickname.setText(newNick);
                JOptionPane.showMessageDialog(this, "닉네임이 변경되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "닉네임 변경(DB)에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "닉네임 변경 중 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        dispose();
        if (owner != null) owner.dispose();
        new LoginFrame();
    }

    private void styleMenu(JButton btn) {
        btn.setMaximumSize(new Dimension(320, 48));
        btn.setPreferredSize(new Dimension(320, 48));
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setBackground(new Color(245, 247, 250));
        btn.setForeground(new Color(40, 40, 40));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void styleDanger(JButton btn) {
        btn.setMaximumSize(new Dimension(320, 48));
        btn.setPreferredSize(new Dimension(320, 48));
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setBackground(new Color(220, 53, 69));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void styleLink(JButton btn) {
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btn.setForeground(new Color(33, 99, 184));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}