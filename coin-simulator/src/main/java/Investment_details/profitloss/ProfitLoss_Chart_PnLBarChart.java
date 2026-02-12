package Investment_details.profitloss;

import DTO.ExecutionDTO;
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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 일별 손익 막대(Bar) 차트 패널
 */
public class ProfitLoss_Chart_PnLBarChart extends JPanel {

    private DefaultCategoryDataset dataset;
    private JFreeChart chart;

    private static final Color COLOR_PROFIT = new Color(220, 60, 60);
    private static final Color COLOR_LOSS   = new Color(70, 100, 220);

    public ProfitLoss_Chart_PnLBarChart() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        dataset = new DefaultCategoryDataset();

        chart = ChartFactory.createBarChart(
                "손익", null, null, dataset,
                PlotOrientation.VERTICAL, false, false, false
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

        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                Number value = dataset.getValue(row, column);
                if (value == null) return Color.GRAY;
                return value.doubleValue() >= 0 ? COLOR_PROFIT : COLOR_LOSS;
            }
        };
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.1);
        renderer.setMaximumBarWidth(0.06);
        plot.setRenderer(renderer);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 9));
        domainAxis.setTickLabelPaint(Color.GRAY);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setCategoryMargin(0.2);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 10));
        rangeAxis.setTickLabelPaint(Color.GRAY);
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setNumberFormatOverride(new DecimalFormat("#,##0"));

        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            chart.getTitle().setPaint(Color.GRAY);
        }
    }

    /**
     * ExecutionDTO 리스트로 일별 손익 차트 업데이트
     */
    public void updateData(List<ExecutionDTO> executions) {
        dataset.clear();
        if (executions == null || executions.isEmpty()) return;

        // 날짜별로 그룹화하여 손익 합산
        Map<String, BigDecimal> dailyPnlMap = new TreeMap<>(Collections.reverseOrder());
        SimpleDateFormat sdf = new SimpleDateFormat("MM.dd");
        
        for (ExecutionDTO exec : executions) {
            if (!"ASK".equals(exec.getSide())) continue; // 매도만 집계
            if (exec.getRealizedPnl() == null) continue;
            
            String dateKey = sdf.format(exec.getExecutedAt());
            dailyPnlMap.merge(dateKey, exec.getRealizedPnl(), BigDecimal::add);
        }

        // 차트에 데이터 추가
        for (Map.Entry<String, BigDecimal> entry : dailyPnlMap.entrySet()) {
            dataset.addValue(entry.getValue().longValue(), "손익", entry.getKey());
        }
    }
}