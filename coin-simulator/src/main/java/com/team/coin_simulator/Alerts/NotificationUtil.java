package com.team.coin_simulator.Alerts;

import javax.swing.*;
import java.awt.*;

public class NotificationUtil {
	public static void showToast(JFrame parentFrame, String message) {
        JDialog dialog = new JDialog(parentFrame);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0)); // 투명 배경

        JLabel label = new JLabel(message);
        label.setOpaque(true);
        label.setBackground(new Color(50, 50, 50, 230)); // 반투명 검정
        label.setForeground(Color.WHITE);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        dialog.add(label);
        dialog.pack();

        // 부모 프레임의 우측 하단에 위치 계산
        if (parentFrame != null) {
            int x = parentFrame.getX() + parentFrame.getWidth() - dialog.getWidth() - 20;
            int y = parentFrame.getY() + parentFrame.getHeight() - dialog.getHeight() - 40;
            dialog.setLocation(x, y);
        }

        dialog.setVisible(true);

        // 3초 뒤 자동 종료 (비동기 처리)
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                SwingUtilities.invokeLater(dialog::dispose);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
