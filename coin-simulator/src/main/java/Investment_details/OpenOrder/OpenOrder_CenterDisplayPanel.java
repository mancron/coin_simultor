package Investment_details.OpenOrder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import DTO.OrderDTO;

/**
 * 미체결 주문 목록 테이블 패널
 * 컬럼: 시간 | 마켓명 | 거래종류 | 주문가격 | 주문수량 | 미체결량 | 취소
 */
public class OpenOrder_CenterDisplayPanel extends JPanel {
    
    private static final String[] COLUMNS = {
        "시간", "마켓명", "거래종류", "주문가격", 
        "주문수량", "미체결량", "취소"
    };
    
    private DefaultTableModel tableModel;
    private JTable table;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM.dd HH:mm");
    
    // 취소 버튼 클릭 리스너
    public interface CancelOrderListener {
        void onCancelOrder(long orderId);
    }
    
    private CancelOrderListener cancelListener;
    
    public OpenOrder_CenterDisplayPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 테이블 모델 및 테이블 생성
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 취소 버튼만 클릭 가능
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) return JButton.class;
                return Object.class;
            }
        };
        
        table = new JTable(tableModel);
        styleTable();
        
        // 스크롤 패널
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
        int[] widths = {100, 100, 80, 120, 120, 120, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        
        // 렌더러 설정
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        
        // 시간, 마켓명, 거래종류 - 중앙 정렬
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(new OpenOrder_SideRenderer());
        
        // 가격, 수량 - 우측 정렬
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        
        // 취소 버튼 렌더러 및 에디터
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor());
    }
    
    /**
     * 데이터 업데이트
     */
    public void updateData(List<OrderDTO> orders) {
        tableModel.setRowCount(0);
        
        if (orders == null || orders.isEmpty()) {
            return;
        }
        
        for (OrderDTO order : orders) {
            Object[] row = new Object[7];
            
            // 0. 시간
            row[0] = order.getCreatedAt() != null ? sdf.format(order.getCreatedAt()) : "";
            
            // 1. 마켓명 (KRW-BTC -> BTC)
            String market = order.getMarket();
            row[1] = market != null ? market.replace("KRW-", "") : "";
            
            // 2. 거래종류
            row[2] = order.getSide();
            
            // 3. 주문가격
            row[3] = formatPrice(order.getOriginalPrice());
            
            // 4. 주문수량
            row[4] = formatVolume(order.getOriginalVolume());
            
            // 5. 미체결량
            row[5] = formatVolume(order.getRemainingVolume());
            
            // 6. 취소 버튼 (주문 ID 저장)
            row[6] = order.getOrderId();
            
            tableModel.addRow(row);
        }
    }
    
    /**
     * 가격 포맷팅
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 KRW";
        return String.format("%,d KRW", price.longValue());
    }
    
    /**
     * 수량 포맷팅
     */
    private String formatVolume(BigDecimal volume) {
        if (volume == null) return "0";
        return String.format("%.8f", volume.doubleValue())
                     .replaceAll("0+$", "")
                     .replaceAll("\\.$", "");
    }
    
    /**
     * 취소 리스너 등록
     */
    public void setCancelOrderListener(CancelOrderListener listener) {
        this.cancelListener = listener;
        // ButtonEditor에 리스너 전달
        if (table.getColumnModel().getColumn(6).getCellEditor() instanceof ButtonEditor) {
            ((ButtonEditor) table.getColumnModel().getColumn(6).getCellEditor())
                .setListener(listener);
        }
    }
    
    /**
     * 취소 버튼 렌더러
     */
    class ButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = new JButton("취소");
            button.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            button.setForeground(new Color(220, 60, 60));
            button.setBackground(Color.WHITE);
            button.setFocusPainted(false);
            return button;
        }
    }
    
    /**
     * 취소 버튼 에디터
     */
    class ButtonEditor extends javax.swing.DefaultCellEditor {
        private JButton button;
        private long orderId;
        private CancelOrderListener listener;
        
        public ButtonEditor() {
            super(new javax.swing.JCheckBox());
            button = new JButton("취소");
            button.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            button.setForeground(new Color(220, 60, 60));
            button.setBackground(Color.WHITE);
            button.setFocusPainted(false);
            
            button.addActionListener(e -> {
                if (listener != null) {
                    listener.onCancelOrder(orderId);
                }
                fireEditingStopped();
            });
        }
        
        public void setListener(CancelOrderListener listener) {
            this.listener = listener;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            orderId = (Long) value;
            return button;
        }
    }
}