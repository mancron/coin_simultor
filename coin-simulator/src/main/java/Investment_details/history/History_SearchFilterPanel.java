package Investment_details.history;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.team.coin_simulator.CoinConfig;

/**
 * 코인 검색 필터 패널
 * 드롭다운으로 코인 선택
 */
public class History_SearchFilterPanel extends JPanel {
    
    private JComboBox<String> coinCombo;
    
    public History_SearchFilterPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        setBackground(Color.WHITE);
        
        // 라벨
        JLabel label = new JLabel("코인");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        add(label);
        
        // 콤보박스 생성
        coinCombo = new JComboBox<>();
        coinCombo.setPreferredSize(new Dimension(150, 30));
        coinCombo.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        coinCombo.setBackground(Color.WHITE);
        
        // 전체 옵션 추가
        coinCombo.addItem("전체");
        
        // 등록된 코인들 추가
        for (String code : CoinConfig.getCodes()) {
            String krName = CoinConfig.COIN_INFO.get(code);
            coinCombo.addItem(krName + " (" + code + ")");
        }
        
        add(coinCombo);
    }
    
    /**
     * 선택된 코인의 마켓 코드 반환
     * @return "KRW-BTC" 형식 또는 "ALL"
     */
    public String getSelectedMarket() {
        String selected = (String) coinCombo.getSelectedItem();
        
        if (selected == null || selected.equals("전체")) {
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
     * 필터 변경 리스너 등록
     */
    public void addFilterChangeListener(ActionListener listener) {
        coinCombo.addActionListener(listener);
    }
}