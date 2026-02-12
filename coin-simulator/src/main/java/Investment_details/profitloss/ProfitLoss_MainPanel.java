package Investment_details.profitloss;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 투자손익 메인 패널
 *
 * 레이아웃 구조:
 * ┌─────────────────────────────────────────┐
 * │  ProfitLoss_SummaryStatPanel  (NORTH)   │  ← 누적손익/수익률/평균투자금액
 * ├─────────────────────────────────────────┤
 * │  ProfitLoss_ChartAreaPanel    (CENTER)  │  ← 수익률 라인 + 손익 바 차트
 * ├─────────────────────────────────────────┤
 * │  ProfitLoss_DetailTablePanel  (SOUTH)   │  ← 일별 상세 테이블
 * └─────────────────────────────────────────┘
 */

public class ProfitLoss_MainPanel extends JPanel {

    // ── 하위 패널 ──────────────────────────────────────────────────
    private final ProfitLoss_SummaryStatPanel  summaryPanel;
    private final ProfitLoss_ChartAreaPanel    chartAreaPanel;
    private final ProfitLoss_DetailTablePanel  tablePanel;

    // ── 데이터 ─────────────────────────────────────────────────────
    private List<ExecutionDTO> currentEntries = new ArrayList<>();

    // ── 생성자 ─────────────────────────────────────────────────────

    public ProfitLoss_MainPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        // 1. 하위 패널 생성
        summaryPanel   = new ProfitLoss_SummaryStatPanel();
        chartAreaPanel = new ProfitLoss_ChartAreaPanel();
        tablePanel     = new ProfitLoss_DetailTablePanel();

        // 2. 차트 + 요약을 묶는 상단 컨테이너
        //    위: SummaryStatPanel / 아래: ChartAreaPanel
        JPanel topContainer = new JPanel(new BorderLayout(0, 0));
        topContainer.setBackground(Color.WHITE);
        topContainer.setBorder(new EmptyBorder(0, 0, 4, 0));
        topContainer.add(summaryPanel,   BorderLayout.NORTH);
        topContainer.add(chartAreaPanel, BorderLayout.CENTER);

        // 3. 테이블 패널 높이 고정 (전체 중 절반 정도)
        tablePanel.setPreferredSize(new Dimension(0, 260));

        // 4. 전체 레이아웃 조립
        add(topContainer, BorderLayout.CENTER);
        add(tablePanel,   BorderLayout.SOUTH);

        // 5. 테스트용 더미 데이터 로드
        loadDummyData();
    }

    // ── 데이터 로드 ────────────────────────────────────────────────

    /**
     * DB 또는 서비스에서 받아온 데이터로 전체 화면 갱신
     * 외부(탭 패널, 컨트롤러 등)에서 이 메서드를 호출한다
     *
     * @param entries 날짜별 투자손익 목록 (최신 순 또는 날짜 순 모두 가능)
     */
    public void loadData(List<ExecutionDTO> entries) {
        this.currentEntries = entries != null ? entries : new ArrayList<>();
        refreshAll();
    }

    /** 모든 하위 패널을 현재 데이터로 갱신 */
    private void refreshAll() {
        // 1. 요약 통계 계산
        long   totalPnl    = 0;
        double totalYield  = 0;
        long   totalAvgInv = 0;

        if (!currentEntries.isEmpty()) {
            ExecutionDTO last = currentEntries.get(0); // 가장 최신 항목
            totalPnl   = last.getCumulativePnl();
            totalYield = last.getCumulativeYield();

            long sumBase = 0;
            for (ExecutionDTO e : currentEntries) {
                sumBase += e.getBaseAsset();
            }
            totalAvgInv = sumBase / currentEntries.size();
        }

        // 2. 각 패널 업데이트
        summaryPanel.updateSummary(totalPnl, totalYield, totalAvgInv);
        chartAreaPanel.updateCharts(currentEntries);
        tablePanel.updateTable(currentEntries);

        revalidate();
        repaint();
    }

    // ── 테스트용 더미 데이터 ───────────────────────────────────────

    private void loadDummyData() {
        List<ExecutionDTO> dummy = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.FEBRUARY, 10);

        // [수정 1] 주석 수정 (입출금 데이터 제거됨)
        // {dailyPnl, baseAsset, finalAsset}
        long[][] rawData = {
            {-1223,    447399,   447399}, // (참고: 기말자산이 7원이면 이상해서 447399로 임의 수정함, 원본대로 쓰셔도 됨)
            {-12692,   460091,   447399},
            { 19166,   450572,   460091},
            {-22375,   470938,   450572},
            {  5736,   456898,   470938},
            { -8012,   462145,   456898},
            { -2430,   475893,   462145},
            { 25614,   448125,   475893},
            { -6839,   452360,   448125},
            { -7351,   462785,   452360},
        };

        long cumPnl = -24025L; 

        for (int i = 0; i < rawData.length; i++) {
            Date date = cal.getTime();
            
            // [수정 2] 배열 인덱스 0, 1, 2까지만 가져오도록 수정
            long daily     = rawData[i][0];
            long baseAsset = rawData[i][1];
            long finalAss  = rawData[i][2];
            
            // [수정 3] 입출금 데이터가 없으므로 0으로 고정
            long dep       = 0;
            long with      = 0;

            double dailyYield = baseAsset > 0
                    ? (double) daily / baseAsset * 100 : 0.0;

            long refBase  = 453791L; 
            double cumYield = refBase > 0 ? (double) cumPnl / refBase * 100 : 0.0;

            // [수정 4] ProfitLossEntry 생성자 호출
            // (만약 ProfitLossEntry 클래스에서도 입출금 필드를 지웠다면, dep, with 인자를 아예 삭제하세요)
            dummy.add(new ExecutionDTO(
                    date, daily, dailyYield,
                    cumPnl, cumYield,
                    baseAsset, finalAss, 
                    dep, with // 입출금 0 전달
            ));

            cumPnl -= daily; 
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        loadData(dummy);
    }

    // ── 독립 실행 테스트 ──────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("투자손익 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);

            ProfitLoss_MainPanel panel = new ProfitLoss_MainPanel();
            frame.add(panel);
            frame.setVisible(true);
        });
    }
}
