package Investment_details.history;

import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import DAO.HistoryDAO;
import DTO.ExecutionDTO;

/**
 * 거래내역 메인 패널
 * 
 * 레이아웃:
 * ┌─────────────────────────────────────────┐
 * │  History_FilterContainerPanel  (NORTH)  │
 * ├─────────────────────────────────────────┤
 * │  History_Table_HistoryTablePanel        │
 * │  (CENTER)                               │
 * └─────────────────────────────────────────┘
 */
public class History_MainPanel extends JPanel {
    
    private final History_FilterContainerPanel filterPanel;
    private final History_Table_HistoryTablePanel tablePanel;
    
    private final HistoryDAO dao;
    private final String userId;
    
    // 현재 필터 상태
    private LocalDate currentStartDate;
    private LocalDate currentEndDate;
    private String currentType = "ALL";
    private String currentMarket = "ALL";
    
    public History_MainPanel(String userId) {
        this.userId = userId;
        this.dao = new HistoryDAO();
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 1. 필터 패널 생성
        filterPanel = new History_FilterContainerPanel();
        
        // 2. 테이블 패널 생성
        tablePanel = new History_Table_HistoryTablePanel();
        
        // 3. 레이아웃 조립
        add(filterPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        
        // 4. 이벤트 리스너 등록
        setupListeners();
        
        // 5. 초기 데이터 로드 (최근 1개월)
        loadRecentMonth();
    }
    
    /**
     * 이벤트 리스너 설정
     */
    private void setupListeners() {
        // 기간 조회 버튼
        filterPanel.getPeriodPanel().addSearchListener(e -> {
            currentStartDate = filterPanel.getPeriodPanel().getStartDate();
            currentEndDate = filterPanel.getPeriodPanel().getEndDate();
            refreshData();
        });
        
        // 종류 필터 변경
        filterPanel.getTypePanel().addFilterChangeListener(e -> {
            currentType = filterPanel.getTypePanel().getSelectedType();
            refreshData();
        });
        
        // 코인 필터 변경
        filterPanel.getSearchPanel().addFilterChangeListener(e -> {
            currentMarket = filterPanel.getSearchPanel().getSelectedMarket();
            refreshData();
        });
    }
    
    /**
     * 최근 1개월 데이터 로드
     */
    private void loadRecentMonth() {
        currentEndDate = LocalDate.now();
        currentStartDate = currentEndDate.minusMonths(1);
        refreshData();
    }
    
    /**
     * 특정 기간 데이터 로드
     */
    public void loadPeriod(LocalDate startDate, LocalDate endDate) {
        this.currentStartDate = startDate;
        this.currentEndDate = endDate;
        refreshData();
    }
    
    /**
     * 필터 조건에 따라 데이터 재조회
     */
    private void refreshData() {
        if (currentStartDate == null || currentEndDate == null) {
            return;
        }
        
        // LocalDate -> java.sql.Date 변환
        Date sqlStartDate = Date.valueOf(currentStartDate);
        Date sqlEndDate = Date.valueOf(currentEndDate);
        
        // DB 조회
        List<ExecutionDTO> executions;
        
        if ("ALL".equals(currentType) && "ALL".equals(currentMarket)) {
            // 필터 없음 - 전체 조회
            executions = dao.getExecutionHistory(userId, sqlStartDate, sqlEndDate);
        } else if ("ALL".equals(currentMarket)) {
            // 종류만 필터
            String side = "ALL".equals(currentType) ? null : currentType;
            executions = dao.getExecutionHistoryBySide(userId, sqlStartDate, sqlEndDate, side);
        } else if ("ALL".equals(currentType)) {
            // 코인만 필터
            executions = dao.getExecutionHistoryFiltered(
                userId, sqlStartDate, sqlEndDate, currentMarket, null);
        } else {
            // 종류 + 코인 필터
            executions = dao.getExecutionHistoryFiltered(
                userId, sqlStartDate, sqlEndDate, currentMarket, currentType);
        }
        
        // 테이블 업데이트
        tablePanel.updateData(executions);
    }
    
    /**
     * 외부에서 데이터 새로고침 (주문 체결 후 등)
     */
    public void refresh() {
        refreshData();
    }
    
    /**
     * 독립 실행 테스트
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("거래내역 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            
            History_MainPanel panel = new History_MainPanel("user_01");
            frame.add(panel);
            frame.setVisible(true);
        });
    }
}