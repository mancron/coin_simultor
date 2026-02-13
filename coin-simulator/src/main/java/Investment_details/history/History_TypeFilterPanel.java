package Investment_details.history;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * 거래 종류 필터 패널
 * 전체 / 매수 / 매도 선택
 */
public class History_TypeFilterPanel extends JPanel {
    
    private JRadioButton btnAll, btnBuy, btnSell;
    private ButtonGroup group;
    
    public History_TypeFilterPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        setBackground(Color.WHITE);
        
        // 라벨
        JLabel label = new JLabel("종류");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        add(label);
        
        // 라디오 버튼 그룹
        group = new ButtonGroup();
        
        btnAll = createRadioButton("전체");
        btnBuy = createRadioButton("매수");
        btnSell = createRadioButton("매도");
        
        group.add(btnAll);
        group.add(btnBuy);
        group.add(btnSell);
        
        add(btnAll);
        add(btnBuy);
        add(btnSell);
        
        // 기본 선택
        btnAll.setSelected(true);
    }
    
    private JRadioButton createRadioButton(String text) {
        JRadioButton btn = new JRadioButton(text);
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        return btn;
    }
    
    /**
     * 선택된 거래 종류 반환
     * @return "ALL", "BID", "ASK"
     */
    public String getSelectedType() {
        if (btnAll.isSelected()) return "ALL";
        if (btnBuy.isSelected()) return "BID";
        if (btnSell.isSelected()) return "ASK";
        return "ALL";
    }
    
    /**
     * 필터 변경 리스너 등록
     */
    public void addFilterChangeListener(ActionListener listener) {
        btnAll.addActionListener(listener);
        btnBuy.addActionListener(listener);
        btnSell.addActionListener(listener);
    }
}