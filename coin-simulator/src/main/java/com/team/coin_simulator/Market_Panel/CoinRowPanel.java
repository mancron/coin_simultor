package com.team.coin_simulator.Market_Panel;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.MatteBorder;

public class CoinRowPanel extends JPanel {
    // 실시간 업데이트를 위해 라벨을 멤버 변수로 선언
    private JLabel nameLabel;
    private JLabel priceLabel;
    private JLabel flucLabel;

    public CoinRowPanel(String name, String price, String fluc) {
        // 1. 패널 기본 설정
        setLayout(new GridLayout(1, 3));
        setPreferredSize(new Dimension(0, 50));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

        // 2. 컴포넌트 초기화 및 추가
        nameLabel = new JLabel(name, SwingConstants.CENTER);
        priceLabel = new JLabel(price, SwingConstants.CENTER);
        flucLabel = new JLabel(fluc, SwingConstants.CENTER);

        // 초기 색상 설정 (상승 가정)
        flucLabel.setForeground(Color.RED);

        add(nameLabel);
        add(priceLabel);
        add(flucLabel);
    }

    // 외부에서 실시간으로 값을 변경할 때 호출하는 메서드
    public void updateData(String price, String fluc, Color color) {
        priceLabel.setText(price);
        flucLabel.setText(fluc);
        flucLabel.setForeground(color);
    }
    
    public String getName() {
    	return nameLabel.getText();
    }
    
}