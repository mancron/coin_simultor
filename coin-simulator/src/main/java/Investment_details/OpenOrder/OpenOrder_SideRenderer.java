package Investment_details.OpenOrder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * 미체결 주문 테이블의 거래종류 셀 렌더러
 * 매수는 빨간색, 매도는 파란색으로 표시
 */
public class OpenOrder_SideRenderer extends DefaultTableCellRenderer {
    
    private static final Color COLOR_BID = new Color(214, 46, 46);   // 매수 빨간색
    private static final Color COLOR_ASK = new Color(56, 97, 214);  // 매도 파란색
    
    public OpenOrder_SideRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(new Font("맑은 고딕", Font.BOLD, 12));
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
        
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value != null) {
            String side = value.toString();
            
            if ("매수".equals(side) || "BID".equals(side)) {
                setForeground(COLOR_BID);
                setText("매수");
            } else if ("매도".equals(side) || "ASK".equals(side)) {
                setForeground(COLOR_ASK);
                setText("매도");
            } else {
                setForeground(Color.BLACK);
            }
        }
        
        // 선택된 행 배경색
        if (isSelected) {
            setBackground(new Color(240, 245, 255));
        } else {
            setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 252));
        }
        
        return this;
    }
}