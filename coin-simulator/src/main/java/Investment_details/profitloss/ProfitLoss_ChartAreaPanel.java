package Investment_details.profitloss;

import DTO.ExecutionDTO;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 차트 영역 컨테이너 패널
 * - 왼쪽: ProfitLoss_Chart_YieldLineChart (누적 수익률)
 * - 오른쪽: ProfitLoss_Chart_PnLBarChart (일별 손익)
 */
public class ProfitLoss_ChartAreaPanel extends JPanel {

    private final ProfitLoss_Chart_YieldLineChart yieldLineChart;
    private final ProfitLoss_Chart_PnLBarChart    pnlBarChart;

    public ProfitLoss_ChartAreaPanel() {
        setLayout(new GridLayout(1, 2, 8, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        yieldLineChart = new ProfitLoss_Chart_YieldLineChart();
        pnlBarChart    = new ProfitLoss_Chart_PnLBarChart();

        add(yieldLineChart);
        add(pnlBarChart);
    }

    /**
     * 두 차트를 동시에 업데이트
     * 
     * @param executions 체결 내역 리스트
     * @param userId 사용자 ID (초기 자본금 조회용)
     */
    public void updateCharts(List<ExecutionDTO> executions, String userId) {
        yieldLineChart.updateData(executions, userId);
        pnlBarChart.updateData(executions);
    }
}