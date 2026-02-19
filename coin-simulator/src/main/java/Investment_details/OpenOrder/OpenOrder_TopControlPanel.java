package Investment_details.OpenOrder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.team.coin_simulator.CoinConfig;

/**
 * 미체결 주문 상단 컨트롤 패널
 * - 마켓 필터 (전체주문 / 개별 코인)
 * - 일괄취소 버튼
 */
public class OpenOrder_TopControlPanel extends JPanel {
    
    private JComboBox<String> marketCombo;
    private JButton btnCancelAll;
    
    public OpenOrder_TopControlPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        // 1. 마켓 필터 라벨
        JLabel label = new JLabel("주문");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        add(label);
        
        // 2. 마켓 콤보박스
        marketCombo = new JComboBox<>();
        marketCombo.setPreferredSize(new Dimension(150, 30));
        marketCombo.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        marketCombo.setBackground(Color.WHITE);
        
        // 전체 옵션
        marketCombo.addItem("전체주문");
        
        // 등록된 코인들 추가
        for (String code : CoinConfig.getCodes()) {
            String krName = CoinConfig.COIN_INFO.get(code);
            marketCombo.addItem(krName + " (" + code + ")");
        }
        
        add(marketCombo);
        
        // 3. 일괄취소 버튼 (오른쪽 배치)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setPreferredSize(new Dimension(600, 40));
        
        btnCancelAll = new JButton("일괄취소");
        btnCancelAll.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnCancelAll.setBackground(Color.WHITE);
        btnCancelAll.setForeground(new Color(220, 60, 60));
        btnCancelAll.setFocusPainted(false);
        btnCancelAll.setBorder(new EmptyBorder(6, 12, 6, 12));
        
        rightPanel.add(btnCancelAll);
        add(rightPanel);
    }
    
    /**
     * 선택된 마켓 코드 반환
     * @return "ALL" 또는 "KRW-BTC" 형식
     */
    public String getSelectedMarket() {
        String selected = (String) marketCombo.getSelectedItem();
        
        if (selected == null || selected.equals("전체주문")) {
            return "ALL";
        }
        
        // "비트코인 (BTC)" 형식에서 BTC 추출
        int start = selected.lastIndexOf("(");
        int end = selected.lastIndexOf(")");
        
        if (start != -1 && end != -1) {
            String code = selected.substring(start + 1, end);
            return "KRW-" + code;
        }
        
        return "ALL";
    }
    
    /**
     * 마켓 필터 변경 리스너
     */
    public void addMarketFilterListener(ActionListener listener) {
        marketCombo.addActionListener(listener);
    }
    
    /**
     * 일괄취소 버튼 리스너
     */
    public void addCancelAllListener(ActionListener listener) {
        btnCancelAll.addActionListener(listener);
    }
}