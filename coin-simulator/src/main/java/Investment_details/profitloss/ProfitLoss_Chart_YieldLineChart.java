package Investment_details.profitloss;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 누적 수익률 꺾은선(Area) 차트 패널
 * ProfitLoss_ChartAreaPanel 에서 호출
 */
public class ProfitLoss_Chart_YieldLineChart extends JPanel {

    private TimeSeries series;
    private JFreeChart chart;

    public ProfitLoss_Chart_YieldLineChart() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        series = new TimeSeries("누적 수익률");
        TimeSeriesCollection dataset = new TimeSeriesCollection(series);

        chart = ChartFactory.createTimeSeriesChart(
                "누적 수익률",   // 제목
                null,            // X축 레이블
                null,            // Y축 레이블
                dataset,
                false,           // 범례
                false,           // 툴팁
                false            // URL
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

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setOutlineVisible(false);

        // Area 렌더러 (색상 채움)
        XYAreaRenderer renderer = new XYAreaRenderer();
        renderer.setSeriesPaint(0, new Color(173, 216, 255, 120)); // 연한 파란 채움
        renderer.setDefaultOutlinePaint(new Color(100, 160, 220));
        renderer.setDefaultOutlineStroke(new BasicStroke(1.5f));
        plot.setRenderer(renderer);

        // X축 (날짜)
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("MM.dd"));
        domainAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 10));
        domainAxis.setTickLabelPaint(Color.GRAY);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);

        // Y축 (수익률 %)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 10));
        rangeAxis.setTickLabelPaint(Color.GRAY);
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setNumberFormatOverride(new java.text.DecimalFormat("0.0'%'"));

        // 타이틀
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            chart.getTitle().setPaint(Color.GRAY);
        }
    }

    /**
     * 외부에서 데이터 업데이트 시 호출
     * @param entries 날짜-누적수익률 목록
     */
    public void updateData(List<ExecutionDTO> entries) {
        series.clear();
        if (entries == null || entries.isEmpty()) return;

        for (ExecutionDTO e : entries) {
            series.addOrUpdate(new Day(e.getDate()), e.getCumulativeYield());
        }
    }
}
