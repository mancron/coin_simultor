package Investment_details.profitloss;

import DTO.ExecutionDTO;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 누적 수익률 꺾은선(Area) 차트 패널
 */
public class ProfitLoss_Chart_YieldLineChart extends JPanel {

    private TimeSeries series;
    private JFreeChart chart;
    private ProfitLossDAO dao;

    public ProfitLoss_Chart_YieldLineChart() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        dao = new ProfitLossDAO();

        series = new TimeSeries("누적 수익률");
        TimeSeriesCollection dataset = new TimeSeriesCollection(series);

        chart = ChartFactory.createTimeSeriesChart(
                "누적 수익률", null, null, dataset,
                false, false, false
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

        XYAreaRenderer renderer = new XYAreaRenderer();
        renderer.setSeriesPaint(0, new Color(173, 216, 255, 120));
        renderer.setDefaultOutlinePaint(new Color(100, 160, 220));
        renderer.setDefaultOutlineStroke(new BasicStroke(1.5f));
        plot.setRenderer(renderer);

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("MM.dd"));
        domainAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 10));
        domainAxis.setTickLabelPaint(Color.GRAY);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 10));
        rangeAxis.setTickLabelPaint(Color.GRAY);
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setNumberFormatOverride(new java.text.DecimalFormat("0.0'%'"));

        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            chart.getTitle().setPaint(Color.GRAY);
        }
    }

    /**
     * ExecutionDTO 리스트로 누적 수익률 차트 업데이트
     */
    public void updateData(List<ExecutionDTO> executions, String userId) {
        series.clear();
        if (executions == null || executions.isEmpty()) return;

        // 초기 자본금 조회
        long initialSeedMoney = dao.getInitialSeedMoney(userId);
        if (initialSeedMoney <= 0) initialSeedMoney = 100000000L;

        // 날짜별로 그룹화하여 손익 합산
        Map<Date, BigDecimal> dailyPnlMap = new TreeMap<>();
        
        for (ExecutionDTO exec : executions) {
            if (!"ASK".equals(exec.getSide())) continue;
            if (exec.getRealizedPnl() == null) continue;
            
            Date date = new Date(exec.getExecutedAt().getTime());
            // 날짜만 비교 (시간 제거)
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dateOnly = cal.getTime();
            
            dailyPnlMap.merge(dateOnly, exec.getRealizedPnl(), BigDecimal::add);
        }

        // 누적 손익 계산 및 차트 추가
        BigDecimal cumulativePnl = BigDecimal.ZERO;
        
        for (Map.Entry<Date, BigDecimal> entry : dailyPnlMap.entrySet()) {
            cumulativePnl = cumulativePnl.add(entry.getValue());
            
            // 누적 수익률 = (누적 손익 / 초기 자본) * 100
            double yieldRate = cumulativePnl
                    .divide(new BigDecimal(initialSeedMoney), 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .doubleValue();
            
            series.addOrUpdate(new Day(entry.getKey()), yieldRate);
        }
    }
}
