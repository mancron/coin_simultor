package Investment_details.history;

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * 필터 컨테이너 패널
 * 기간 / 종류 / 코인 필터를 담는 컨테이너
 */
public class History_FilterContainerPanel extends JPanel {
    
    private History_PeriodFilterPanel periodPanel;
    private History_TypeFilterPanel typePanel;
    private History_SearchFilterPanel searchPanel;
    
    public History_FilterContainerPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        // 1. 기간 필터 패널
        periodPanel = new History_PeriodFilterPanel();
        periodPanel.setBorder(new EmptyBorder(10, 15, 5, 15));
        
        // 2. 종류 + 코인 필터를 담는 행
        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        secondRow.setBackground(Color.WHITE);
        secondRow.setBorder(new EmptyBorder(5, 15, 10, 15));
        
        typePanel = new History_TypeFilterPanel();
        searchPanel = new History_SearchFilterPanel();
        
        secondRow.add(typePanel);
        secondRow.add(searchPanel);
        
        // 컨테이너에 추가
        add(periodPanel);
        add(secondRow);
    }
    
    public History_PeriodFilterPanel getPeriodPanel() {
        return periodPanel;
    }
    
    public History_TypeFilterPanel getTypePanel() {
        return typePanel;
    }
    
    public History_SearchFilterPanel getSearchPanel() {
        return searchPanel;
    }
}