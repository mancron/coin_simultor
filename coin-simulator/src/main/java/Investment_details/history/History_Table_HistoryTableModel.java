package Investment_details.history;

import javax.swing.table.DefaultTableModel;

/**
 * 거래내역 테이블 모델
 * 편집 불가능하도록 설정
 */
public class History_Table_HistoryTableModel extends DefaultTableModel {
    
    private static final long serialVersionUID = 1L;
    
    private static final String[] COLUMN_NAMES = {
        "체결시간", "코인", "종류", "거래단가", "거래금액", 
        "거래수량", "수수료", "정산금액"
    };
    
    public History_Table_HistoryTableModel() {
        super(COLUMN_NAMES, 0);
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // 모든 셀 편집 불가
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        // 모든 컬럼을 Object로 처리 (렌더러에서 포맷팅)
        return Object.class;
    }
}