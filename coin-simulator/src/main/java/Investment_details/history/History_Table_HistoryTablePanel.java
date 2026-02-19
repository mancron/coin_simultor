package Investment_details.history;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import DTO.ExecutionDTO;

/**
 * 거래내역 테이블 패널
 * ExecutionDTO 리스트를 테이블로 표시
 */
public class History_Table_HistoryTablePanel extends JPanel {
    
    private History_Table_HistoryTableModel tableModel;
    private JTable table;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    
    public History_Table_HistoryTablePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 테이블 모델 및 테이블 생성
        tableModel = new History_Table_HistoryTableModel();
        table = new JTable(tableModel);
        
        // 테이블 스타일 설정
        styleTable();
        
        // 스크롤 패널에 추가
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 테이블 스타일 설정
     */
    private void styleTable() {
        // 기본 스타일
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(240, 245, 255));
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        
        // 헤더 스타일
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(248, 248, 248));
        header.setForeground(new Color(102, 102, 102));
        header.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        header.setPreferredSize(new Dimension(0, 35));
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        // 컬럼 너비 설정
        int[] widths = {150, 100, 80, 120, 120, 120, 100, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        
        // 렌더러 설정
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        
        // 체결시간 (0) - 좌측 정렬
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
        
        // 코인 (1) - 중앙 정렬
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        // 종류 (2) - 중앙 정렬 + 색상
        table.getColumnModel().getColumn(2).setCellRenderer(new History_Table_TradeTypeRenderer());
        
        // 거래단가 (3) ~ 정산금액 (7) - 우측 정렬
        for (int i = 3; i <= 7; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }
    }
    
    /**
     * 데이터 업데이트
     * @param executions 거래 내역 리스트
     */
    public void updateData(List<ExecutionDTO> executions) {
        // 기존 데이터 삭제
        tableModel.setRowCount(0);
        
        if (executions == null || executions.isEmpty()) {
            return;
        }
        
        // 새 데이터 추가
        for (ExecutionDTO exec : executions) {
            Object[] row = new Object[8];
            
            // 0. 체결시간
            row[0] = sdf.format(exec.getExecutedAt());
            
            // 1. 코인 (KRW-BTC -> BTC)
            String market = exec.getMarket();
            row[1] = market != null ? market.replace("KRW-", "") : "";
            
            // 2. 종류 (BID/ASK)
            row[2] = exec.getSide();
            
            // 3. 거래단가
            row[3] = formatPrice(exec.getPrice());
            
            // 4. 거래금액 (단가 * 수량)
            BigDecimal tradeAmount = exec.getPrice().multiply(exec.getVolume());
            row[4] = formatPrice(tradeAmount);
            
            // 5. 거래수량
            row[5] = formatVolume(exec.getVolume());
            
            // 6. 수수료
            row[6] = formatPrice(exec.getFee());
            
            // 7. 정산금액 (거래금액 - 수수료 for 매도, 거래금액 + 수수료 for 매수)
            BigDecimal settleAmount;
            if ("ASK".equals(exec.getSide())) {
                // 매도: 받은 금액 - 수수료
                settleAmount = tradeAmount.subtract(exec.getFee());
            } else {
                // 매수: 지불한 금액 + 수수료
                settleAmount = tradeAmount.add(exec.getFee());
            }
            row[7] = formatPrice(settleAmount);
            
            tableModel.addRow(row);
        }
    }
    
    /**
     * 가격 포맷팅 (KRW)
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 KRW";
        return String.format("%,d KRW", price.longValue());
    }
    
    /**
     * 수량 포맷팅 (코인)
     */
    private String formatVolume(BigDecimal volume) {
        if (volume == null) return "0";
        return String.format("%.8f", volume.doubleValue())
                     .replaceAll("0+$", "")
                     .replaceAll("\\.$", "");
    }
}