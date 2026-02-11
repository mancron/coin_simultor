package Investment_details.profitloss;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 일별 손익 막대(Bar) 차트 패널
 * ProfitLoss_ChartAreaPanel 에서 호출
 */
public class ProfitLoss_Chart_PnLBarChart extends JPanel {

    private DefaultCategoryDataset dataset;
    private JFreeChart chart;

    private static final Color COLOR_PROFIT = new Color(220, 60, 60);   // 수익 - 빨간색
    private static final Color COLOR_LOSS   = new Color(70, 100, 220);  // 손실 - 파란색

    public ProfitLoss_Chart_PnLBarChart() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        dataset = new DefaultCategoryDataset();

        chart = ChartFactory.createBarChart(
                "손익",             // 차트 제목
                null,               // X축 레이블
                null,               // Y축 레이블
                dataset,
                PlotOrientation.VERTICAL,
                false,              // 범례
                false,              // 툴팁
                false               // URL
        );

        styleChart();

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(460, 260));
        chartPanel.setMouseWheelEnabled(false);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        add(chartPanel, BorderLayout.CENTER);
    }

    private void styleChart() {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setPadding(new org.jfree.chart.ui.RectangleInsets(5, 0, 0, 0));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setOutlineVisible(false);

        // 수익/손실 색상 커스텀 렌더러
        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                Number value = dataset.getValue(row, column);
                if (value == null) return Color.GRAY;
                return value.doubleValue() >= 0 ? COLOR_PROFIT : COLOR_LOSS;
            }
        };
        renderer.setBarPainter(new StandardBarPainter()); // 그라데이션 제거
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.1);
        renderer.setMaximumBarWidth(0.06);
        plot.setRenderer(renderer);

        // X축
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 9));
        domainAxis.setTickLabelPaint(Color.GRAY);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setCategoryMargin(0.2);

        // Y축
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 10));
        rangeAxis.setTickLabelPaint(Color.GRAY);
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setNumberFormatOverride(new DecimalFormat("#,##0"));

        // 타이틀
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            chart.getTitle().setPaint(Color.GRAY);
        }
    }

    /**
     * 외부에서 데이터 업데이트 시 호출
     * @param entries 날짜-일별손익 목록
     */
    public void updateData(List<ProfitLossEntry> entries) {
        dataset.clear();
        if (entries == null || entries.isEmpty()) return;

        for (ProfitLossEntry e : entries) {
            String label = String.format("%02d.%02d",
                    e.getDate().getMonth() + 1,
                    e.getDate().getDate());
            dataset.addValue(e.getDailyPnl(), "손익", label);
        }
    }
}
