package Investment_details.profitloss;

import DTO.ExecutionDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * 투자손익 메인 패널 (ExecutionDTO 직접 사용 버전)
 *
 * 레이아웃:
 * ┌─────────────────────────────────────────┐
 * │  ProfitLoss_SummaryStatPanel  (NORTH)   │
 * ├─────────────────────────────────────────┤
 * │  ProfitLoss_ChartAreaPanel    (CENTER)  │
 * ├─────────────────────────────────────────┤
 * │  ProfitLoss_DetailTablePanel  (SOUTH)   │
 * └─────────────────────────────────────────┘
 */
public class ProfitLoss_MainPanel extends JPanel {

    private final ProfitLoss_SummaryStatPanel  summaryPanel;
    private final ProfitLoss_ChartAreaPanel    chartAreaPanel;
    private final ProfitLoss_DetailTablePanel  tablePanel;

    private final ProfitLossDAO dao;
    private final String userId;
    private List<ExecutionDTO> currentExecutions;

    /**
     * 투자손익 메인 패널 생성자
     * 
     * @param userId 사용자 ID
     */
    public ProfitLoss_MainPanel(String userId) {
        this.userId = userId;
        this.dao = new ProfitLossDAO();
        
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        // 1. 하위 패널 생성
        summaryPanel   = new ProfitLoss_SummaryStatPanel();
        chartAreaPanel = new ProfitLoss_ChartAreaPanel();
        tablePanel     = new ProfitLoss_DetailTablePanel();

        // 2. 상단 컨테이너 (요약 + 차트)
        JPanel topContainer = new JPanel(new BorderLayout(0, 0));
        topContainer.setBackground(Color.WHITE);
        topContainer.setBorder(new EmptyBorder(0, 0, 4, 0));
        topContainer.add(summaryPanel,   BorderLayout.NORTH);
        topContainer.add(chartAreaPanel, BorderLayout.CENTER);

        // 3. 테이블 높이 고정
        tablePanel.setPreferredSize(new Dimension(0, 260));

        // 4. 전체 레이아웃
        add(topContainer, BorderLayout.CENTER);
        add(tablePanel,   BorderLayout.SOUTH);

        // 5. 초기 데이터 로드 (최근 30일)
        loadRecentData(30);
    }

    /**
     * 최근 N일간의 체결 데이터를 DB에서 조회하여 화면에 표시
     * 
     * @param days 조회할 일수
     */
    public void loadRecentData(int days) {
        this.currentExecutions = dao.getSellExecutions(userId, days);
        refreshAll();
    }

    /**
     * 외부에서 데이터를 직접 설정 (테스트용)
     * 
     * @param executions 체결 내역 리스트
     */
    public void loadData(List<ExecutionDTO> executions) {
        this.currentExecutions = executions;
        refreshAll();
    }

    /**
     * 모든 하위 패널 갱신
     */
    private void refreshAll() {
        if (currentExecutions == null || currentExecutions.isEmpty()) {
            // 데이터 없을 때 초기화
            summaryPanel.updateSummary(0, 0.0, dao.getInitialSeedMoney(userId));
            chartAreaPanel.updateCharts(currentExecutions, userId);
            tablePanel.updateTable(currentExecutions, userId);
            return;
        }

        // 1. 총 실현 손익 계산
        long totalPnl = dao.getTotalRealizedPnl(userId).longValue();
        
        // 2. 초기 자본금
        long initialSeedMoney = dao.getInitialSeedMoney(userId);
        
        // 3. 총 수익률
        double totalYield = initialSeedMoney > 0 
            ? ((double) totalPnl / initialSeedMoney) * 100 
            : 0.0;
        
        // 4. 평균 투자금액 (간단히 초기자본 + 손익/2로 근사)
        long avgInvestment = initialSeedMoney + (totalPnl / 2);

        // 5. 각 패널 업데이트
        summaryPanel.updateSummary(totalPnl, totalYield, avgInvestment);
        chartAreaPanel.updateCharts(currentExecutions, userId);
        tablePanel.updateTable(currentExecutions, userId);

        revalidate();
        repaint();
    }

    /**
     * 데이터 새로고침 (DB 재조회)
     * 
     * @param days 조회할 일수
     */
    public void refresh(int days) {
        loadRecentData(days);
    }

    /**
     * 독립 실행 테스트
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("투자손익 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);

            ProfitLoss_MainPanel panel = new ProfitLoss_MainPanel("user_01");
            frame.add(panel);
            frame.setVisible(true);
        });
    }
}