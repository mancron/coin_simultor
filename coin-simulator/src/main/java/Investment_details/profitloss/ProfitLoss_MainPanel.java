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
    private List<ProfitLossEntry> currentEntries = new ArrayList<>();

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
    public void loadData(List<ProfitLossEntry> entries) {
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
            ProfitLossEntry last = currentEntries.get(0); // 가장 최신 항목
            totalPnl   = last.getCumulativePnl();
            totalYield = last.getCumulativeYield();

            long sumBase = 0;
            for (ProfitLossEntry e : currentEntries) {
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
        List<ProfitLossEntry> dummy = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.FEBRUARY, 10);

        // 이미지에 표시된 데이터와 유사하게 세팅
        long[][] rawData = {
            // {dailyPnl, baseAsset, finalAsset, deposit, withdrawal}
            {-1223,    447399,   7,        0, 446168},
            {-12692,   460091,   447399,   0, 0},
            { 19166,   450572,   460091,   0, 0},
            {-22375,   470938,   450572,   0, 0},
            {  5736,   456898,   470938,   0, 0},
            { -8012,   462145,   456898,   0, 0},
            { -2430,   475893,   462145,   0, 0},
            { 25614,   448125,   475893,   0, 0},
            { -6839,   452360,   448125,   0, 0},
            { -7351,   462785,   452360,   0, 0},
        };

        long cumPnl = -24025L; // 이미지의 누적 손익

        for (int i = 0; i < rawData.length; i++) {
            Date date = cal.getTime();
            long daily     = rawData[i][0];
            long baseAsset = rawData[i][1];
            long finalAss  = rawData[i][2];
            long dep       = rawData[i][3];
            long with      = rawData[i][4];

            double dailyYield = baseAsset > 0
                    ? (double) daily / baseAsset * 100 : 0.0;

            // 누적 수익률 단순 계산 (실제는 DB에서 가져옴)
            long refBase  = 453791L; // 평균 투자금액
            double cumYield = refBase > 0 ? (double) cumPnl / refBase * 100 : 0.0;

            dummy.add(new ProfitLossEntry(
                    date, daily, dailyYield,
                    cumPnl, cumYield,
                    baseAsset, finalAss, dep, with
            ));

            cumPnl -= daily; // 역순이므로 역산
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
