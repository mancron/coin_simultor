package Investment_details.Asset;

import DTO.MyAssetStatusDTO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 보유자산 상단 요약 패널 (업비트 스타일)
 *
 * 좌측: 보유KRW / 총보유자산 / 총매수·총평가·주문가능 / 총평가손익·수익률
 * 우측: 도넛 차트 + 범례
 */
public class Asset_SummaryPanel extends JPanel {

    // 값 라벨
    private final JLabel valKrw        = makeValLabel(16, true);
    private final JLabel valTotalAsset = makeValLabel(16, true);
    private final JLabel valBuy        = makeValLabel(12, false);
    private final JLabel valEval       = makeValLabel(12, false);
    private final JLabel valAvail      = makeValLabel(12, false);
    private final JLabel valPnl        = makeValLabel(12, false);
    private final JLabel valYield      = makeValLabel(12, false);

    // 도넛 차트
    private final DefaultPieDataset pieDataset = new DefaultPieDataset();
    private JFreeChart pieChart;
    private final JPanel legendPanel = new JPanel();

    // 색상
    private static final Color C_PROFIT  = new Color(214, 46,  46);
    private static final Color C_LOSS    = new Color(56,  97,  214);
    private static final Color C_LABEL   = new Color(102, 102, 102);
    private static final Color C_VALUE   = new Color(20,  20,  20);
    private static final Color C_DIV     = new Color(224, 224, 224);
    private static final Color[] PIE_CLR = {
        new Color(154, 192, 82),
        new Color(50,  130, 190),
        new Color(130, 86,  180),
        new Color(218, 145, 55),
        new Color(72,  185, 155),
    };

    public Asset_SummaryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, C_DIV));
        add(buildLeft(),  BorderLayout.CENTER);
        add(buildRight(), BorderLayout.EAST);
    }

    // ── 좌측 ──────────────────────────────────────────────────────
    private JPanel buildLeft() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(14, 20, 10, 20));

        // 상단: 보유KRW + 총보유자산
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setBackground(Color.WHITE);
        top.add(labelValueBlock("보유 KRW",    valKrw,        "KRW"));
        top.add(Box.createHorizontalStrut(50));
        top.add(labelValueBlock("총 보유자산", valTotalAsset, "KRW"));
        p.add(top, BorderLayout.NORTH);

        // 중단: 3행 4열 그리드
        JPanel grid = new JPanel(new GridLayout(3, 4, 0, 0));
        grid.setBackground(Color.WHITE);
        grid.setBorder(new EmptyBorder(12, 0, 4, 0));

        // 행1
        grid.add(lbl("총 매수",      C_LABEL));  grid.add(krwRow(valBuy));
        grid.add(lbl("총평가손익",   C_LABEL));  grid.add(valPnl);
        // 행2
        grid.add(lbl("총 평가",      C_LABEL));  grid.add(krwRow(valEval));
        grid.add(lbl("총평가수익률", C_LABEL));  grid.add(valYield);
        // 행3
        grid.add(lbl("주문가능",     C_LABEL));  grid.add(krwRow(valAvail));
        grid.add(new JLabel());                   grid.add(new JLabel());

        p.add(grid, BorderLayout.CENTER);

        JLabel note = new JLabel("* 보유자산 유의사항 ⓘ");
        note.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        note.setForeground(new Color(170, 170, 170));
        note.setBorder(new EmptyBorder(6, 0, 4, 0));
        p.add(note, BorderLayout.SOUTH);
        return p;
    }

    // ── 우측 (도넛 + 범례) ────────────────────────────────────────
    private JPanel buildRight() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Color.WHITE);
        outer.setPreferredSize(new Dimension(310, 0));
        outer.setBorder(new EmptyBorder(8, 0, 8, 14));

        // 새로고침 버튼
        JButton btnR = new JButton("↻");
        btnR.setFont(new Font("Dialog", Font.PLAIN, 14));
        btnR.setForeground(Color.GRAY);
        btnR.setBackground(Color.WHITE);
        btnR.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btnR.setFocusPainted(false);
        btnR.setPreferredSize(new Dimension(30, 30));
        btnR.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        topBar.setBackground(Color.WHITE);
        topBar.add(btnR);
        outer.add(topBar, BorderLayout.NORTH);

        // 도넛 차트 생성
        pieChart = ChartFactory.createRingChart("", pieDataset, false, false, false);
        applyRingStyle();

        ChartPanel cp = new ChartPanel(pieChart);
        cp.setPreferredSize(new Dimension(160, 145));
        cp.setBackground(Color.WHITE);
        cp.setMouseWheelEnabled(false);
        cp.setDomainZoomable(false);
        cp.setRangeZoomable(false);
        cp.setBorder(BorderFactory.createEmptyBorder());

        // 범례
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(Color.WHITE);
        legendPanel.setBorder(new EmptyBorder(14, 0, 0, 10));
        legendPanel.setPreferredSize(new Dimension(110, 0));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        center.add(legendPanel, BorderLayout.WEST);
        center.add(cp,          BorderLayout.CENTER);
        outer.add(center, BorderLayout.CENTER);
        return outer;
    }

    private void applyRingStyle() {
        pieChart.setBackgroundPaint(Color.WHITE);
        pieChart.setPadding(new org.jfree.chart.ui.RectangleInsets(2, 2, 2, 2));

        RingPlot plot = (RingPlot) pieChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSectionDepth(0.36);
        plot.setSeparatorsVisible(false);
        plot.setSectionOutlinesVisible(false);
        plot.setLabelGenerator(null);
        plot.setCenterTextMode(org.jfree.chart.plot.CenterTextMode.FIXED);
        plot.setCenterText("보유 비중\n(%)");
        plot.setCenterTextFont(new Font("맑은 고딕", Font.PLAIN, 9));
        plot.setCenterTextColor(Color.GRAY);
    }

    // ── 데이터 업데이트 ───────────────────────────────────────────

    public void updateSummary(BigDecimal krw, BigDecimal totalAsset,
                              BigDecimal totalBuy, BigDecimal totalEval,
                              BigDecimal available, BigDecimal pnl, double yield) {
        valKrw.setText(fmt(krw));
        valTotalAsset.setText(fmt(totalAsset));
        valBuy.setText(fmt(totalBuy));
        valEval.setText(fmt(totalEval));
        valAvail.setText(fmt(available));
        applyPnlColor(valPnl,   fmt(pnl) + " KRW",      pnl.doubleValue());
        applyPnlColor(valYield, String.format("%,.2f %%", yield), yield);
    }

    public void updateChart(List<MyAssetStatusDTO> assetList, BigDecimal krwBalance) {
        pieDataset.clear();
        legendPanel.removeAll();

        RingPlot plot = (RingPlot) pieChart.getPlot();

        // 전체 합산
        BigDecimal total = krwBalance != null ? krwBalance : BigDecimal.ZERO;
        if (assetList != null)
            for (MyAssetStatusDTO d : assetList)
                if (d.getTotalValue() != null && d.getTotalValue().compareTo(BigDecimal.ZERO) > 0)
                    total = total.add(d.getTotalValue());

        int idx = 0;

        // KRW 먼저
        if (krwBalance != null && krwBalance.compareTo(BigDecimal.ZERO) > 0) {
            pieDataset.setValue("KRW", krwBalance);
            Color c = PIE_CLR[idx % PIE_CLR.length];
            plot.setSectionPaint("KRW", c);
            double pct = total.compareTo(BigDecimal.ZERO) > 0
                    ? krwBalance.divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;
            legendPanel.add(legendItem(c, "KRW", pct));
            idx++;
        }

        // 코인
        if (assetList != null) {
            for (MyAssetStatusDTO dto : assetList) {
                if (dto.getTotalValue() == null || dto.getTotalValue().compareTo(BigDecimal.ZERO) <= 0) continue;
                pieDataset.setValue(dto.getCurrency(), dto.getTotalValue());
                Color c = PIE_CLR[idx % PIE_CLR.length];
                plot.setSectionPaint(dto.getCurrency(), c);
                double pct = total.compareTo(BigDecimal.ZERO) > 0
                        ? dto.getTotalValue().divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;
                legendPanel.add(legendItem(c, dto.getCurrency(), pct));
                idx++;
            }
        }

        legendPanel.revalidate();
        legendPanel.repaint();
    }

    // ── 범례 아이템 ───────────────────────────────────────────────
    private JPanel legendItem(Color color, String name, double pct) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(140, 22));

        // 색상 동그라미
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(color);
                g.fillOval(1, 1, 9, 9);
            }
        };
        dot.setPreferredSize(new Dimension(11, 11));
        dot.setOpaque(false);

        JLabel nLbl = new JLabel(name);
        nLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        nLbl.setForeground(C_LABEL);

        JLabel pLbl = new JLabel(String.format("%.1f%%", pct));
        pLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        pLbl.setForeground(C_LABEL);

        row.add(dot);
        row.add(nLbl);
        row.add(pLbl);
        return row;
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────
    private JLabel makeValLabel(int size, boolean bold) {
        JLabel l = new JLabel("0");
        l.setFont(new Font("맑은 고딕", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(C_VALUE);
        return l;
    }

    private JLabel lbl(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        l.setForeground(c);
        return l;
    }

    /** "[라벨]" 위에 제목, 아래에 큰 값 */
    private JPanel labelValueBlock(String title, JLabel valLbl, String unit) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);

        JLabel t = new JLabel(title);
        t.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        t.setForeground(C_LABEL);
        p.add(t);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        row.setBackground(Color.WHITE);
        row.add(valLbl);
        JLabel u = new JLabel(unit);
        u.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        u.setForeground(C_LABEL);
        row.add(u);
        p.add(row);
        return p;
    }

    /** 값 + KRW 단위 한 줄 */
    private JPanel krwRow(JLabel valLbl) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        row.setBackground(Color.WHITE);
        row.add(valLbl);
        JLabel u = new JLabel("KRW");
        u.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        u.setForeground(C_LABEL);
        row.add(u);
        return row;
    }

    private void applyPnlColor(JLabel lbl, String text, double val) {
        if (val > 0) { lbl.setText("+" + text); lbl.setForeground(C_PROFIT); }
        else if (val < 0) { lbl.setText(text);  lbl.setForeground(C_LOSS); }
        else { lbl.setText(text);                lbl.setForeground(C_VALUE); }
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "0";
        return String.format("%,d", v.setScale(0, RoundingMode.HALF_UP).longValue());
    }
}