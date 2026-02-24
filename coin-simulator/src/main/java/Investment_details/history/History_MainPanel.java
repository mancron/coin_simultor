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

public class History_MainPanel extends JPanel {
    
    private final History_FilterContainerPanel filterPanel;
    private final History_Table_HistoryTablePanel tablePanel;
    
    private final HistoryDAO dao;
    private final String userId;
    private long sessionId; //세션 ID 필드 추가
    
    private LocalDate currentStartDate;
    private LocalDate currentEndDate;
    private String currentType = "ALL";
    private String currentMarket = "ALL";
    
    //생성자에 sessionId 추가
    public History_MainPanel(String userId, long sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.dao = new HistoryDAO();
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        filterPanel = new History_FilterContainerPanel();
        tablePanel = new History_Table_HistoryTablePanel();
        
        add(filterPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        
        setupListeners();
        loadRecentMonth();
    }
    
    //세션 변경 시 호출될 메서드 추가
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }
    
    private void setupListeners() {
        filterPanel.getPeriodPanel().addSearchListener(e -> {
            currentStartDate = filterPanel.getPeriodPanel().getStartDate();
            currentEndDate = filterPanel.getPeriodPanel().getEndDate();
            refreshData();
        });
        
        filterPanel.getTypePanel().addFilterChangeListener(e -> {
            currentType = filterPanel.getTypePanel().getSelectedType();
            refreshData();
        });
        
        filterPanel.getSearchPanel().addFilterChangeListener(e -> {
            currentMarket = filterPanel.getSearchPanel().getSelectedMarket();
            refreshData();
        });
    }
    
    private void loadRecentMonth() {
        currentEndDate = LocalDate.now();
        currentStartDate = currentEndDate.minusMonths(1);
        refreshData();
    }
    
    public void loadPeriod(LocalDate startDate, LocalDate endDate) {
        this.currentStartDate = startDate;
        this.currentEndDate = endDate;
        refreshData();
    }
    
    private void refreshData() {
        if (currentStartDate == null || currentEndDate == null) return;
        
        Date sqlStartDate = Date.valueOf(currentStartDate);
        Date sqlEndDate = Date.valueOf(currentEndDate);
        
        List<ExecutionDTO> executions;
        
        //DAO 호출 시 sessionId를 모두 넘겨주도록 수정
        if ("ALL".equals(currentType) && "ALL".equals(currentMarket)) {
            executions = dao.getExecutionHistory(userId, sessionId, sqlStartDate, sqlEndDate);
        } else if ("ALL".equals(currentMarket)) {
            String side = "ALL".equals(currentType) ? null : currentType;
            executions = dao.getExecutionHistoryBySide(userId, sessionId, sqlStartDate, sqlEndDate, side);
        } else if ("ALL".equals(currentType)) {
            executions = dao.getExecutionHistoryFiltered(userId, sessionId, sqlStartDate, sqlEndDate, currentMarket, null);
        } else {
            executions = dao.getExecutionHistoryFiltered(userId, sessionId, sqlStartDate, sqlEndDate, currentMarket, currentType);
        }
        
        tablePanel.updateData(executions);
    }
    
    public void refresh() {
        refreshData();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("거래내역 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            
            History_MainPanel panel = new History_MainPanel("user_01", 0L); // 테스트용 0L
            frame.add(panel);
            frame.setVisible(true);
        });
    }
}