package Investment_details.profitloss;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 차트 영역 컨테이너 패널
 * - 왼쪽: ProfitLoss_Chart_YieldLineChart (누적 수익률)
 * - 오른쪽: ProfitLoss_Chart_PnLBarChart (일별 손익)
 * ProfitLoss_MainPanel 에서 호출해서 CENTER에 배치
 */
public class ProfitLoss_ChartAreaPanel extends JPanel {

    private final ProfitLoss_Chart_YieldLineChart yieldLineChart;
    private final ProfitLoss_Chart_PnLBarChart    pnlBarChart;

    public ProfitLoss_ChartAreaPanel() {
        setLayout(new GridLayout(1, 2, 8, 0)); // 1행 2열, 가로 간격 8px
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        yieldLineChart = new ProfitLoss_Chart_YieldLineChart();
        pnlBarChart    = new ProfitLoss_Chart_PnLBarChart();

        add(yieldLineChart);
        add(pnlBarChart);
    }

    /**
     * 메인패널에서 데이터가 바뀔 때 호출
     * 두 차트를 동시에 업데이트한다
     */
    public void updateCharts(List<ExecutionDTO> entries) {
        yieldLineChart.updateData(entries);
        pnlBarChart.updateData(entries);
    }
}
