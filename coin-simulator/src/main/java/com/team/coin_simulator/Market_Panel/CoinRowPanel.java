package com.team.coin_simulator.Market_Panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

public class CoinRowPanel extends JPanel {
    // 실시간 업데이트를 위해 라벨을 멤버 변수로 선언
    private JLabel nameLabel;
    private JLabel priceLabel;
    private JLabel flucLabel;
    private JLabel Trading_value;
    
    public CoinRowPanel(String name, String price, String fluc,String Acc_trade_price) {
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
        Trading_value = new JLabel(Acc_trade_price, SwingConstants.CENTER);
        // 초기 색상 설정 (상승 가정)
        flucLabel.setForeground(Color.RED);

        add(nameLabel);
        add(priceLabel);
        add(flucLabel);
        add(Trading_value);
    }

    // 외부에서 실시간으로 값을 변경할 때 호출하는 메서드
    // 보유는 (코인명,보유(평가금),매수평균가,수익률) 로 기입
public void updateData(String newPrice, String rawFluc,String Acc_trade_price) {
        
        // 1. 가격 업데이트
        priceLabel.setText(newPrice);
        Trading_value.setText(Acc_trade_price);
        // 2. 등락률 로직 처리
        try {
            // 문자열을 숫자로 변환 ("-1.2" -> -1.2)
            double flucValue = Double.parseDouble(rawFluc);
            
            String finalStr = "";
            Color finalColor = Color.BLACK; // 기본 색상

            if (flucValue > 0) {
                // 양수일 때: 빨간색, 앞에 "+" 붙이기
                finalColor = Color.RED;
                finalStr = "+" + rawFluc + "%"; 
            } else if (flucValue < 0) {
                // 음수일 때: 파란색, "-"는 숫자에 이미 있으므로 그대로
                finalColor = Color.BLUE;
                finalStr = rawFluc + "%";
            } else {
                // 0일 때: 검정색 (또는 회색)
                finalColor = Color.BLACK;
                finalStr = "0.00%";
            }

            // 3. UI 적용
            flucLabel.setText(finalStr);
            flucLabel.setForeground(finalColor);
            
            // (선택사항) 가격도 등락에 따라 색을 같이 바꿀지?
            // priceLabel.setForeground(finalColor); 

        } catch (NumberFormatException e) {
            // 만약 숫자가 아닌 이상한 값이 들어왔을 때 방어 코드
            flucLabel.setText("-");
            flucLabel.setForeground(Color.BLACK);
        }
    }

	//OrderPanel에 불러올 시장가
	public String getPrice() {
	    return priceLabel.getText();
	}
    
    
}