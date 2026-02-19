package com.team.coin_simulator.Alerts;

import DAO.PriceAlertDAO;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class AlertDialog extends JDialog {
    private PriceAlertDAO alertDAO = new PriceAlertDAO();
    private String market; // 예: "KRW-BTC"
    private String userId = "test_user";

    public AlertDialog(JFrame parent, String market, BigDecimal currentPrice) {
        super(parent, "가격 알림 설정", true); // true = 모달(창 닫기 전까지 뒤에 거 못 건드림)
        this.market = market;

        setLayout(new GridLayout(4, 2, 10, 10));
        setSize(300, 200);
        setLocationRelativeTo(parent);

        // 1. 코인 이름 표시
        add(new JLabel("코인명:"));
        add(new JLabel(market));

        // 2. 목표 가격 입력
        add(new JLabel("목표 가격(KRW):"));
        JTextField tfPrice = new JTextField(currentPrice.toString()); // 현재가 자동 입력
        add(tfPrice);

        // 3. 조건 선택 (이상/이하)
        add(new JLabel("조건:"));
        String[] conditions = {"이상 (상승돌파)", "이하 (하락돌파)"};
        JComboBox<String> cbCondition = new JComboBox<>(conditions);
        add(cbCondition);

        // 4. 버튼
        JButton btnCancel = new JButton("취소");
        JButton btnAdd = new JButton("알림 추가");
        
        btnCancel.addActionListener(e -> dispose()); // 닫기
        
        btnAdd.addActionListener(e -> {
            try {
                BigDecimal target = new BigDecimal(tfPrice.getText().replace(",", ""));
                String cond = (cbCondition.getSelectedIndex() == 0) ? "ABOVE" : "BELOW";
                
                // DAO 호출해서 DB에 저장!
                boolean success = alertDAO.addPriceAlert(userId, market, target, cond);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "알림이 설정되었습니다!");
                    dispose(); // 성공하면 창 닫기
                } else {
                    JOptionPane.showMessageDialog(this, "설정 실패 (DB 오류)");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "가격을 올바르게 입력해주세요.");
            }
        });

        add(btnCancel);
        add(btnAdd);
    }
}