package com.team.coin_simulator.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class CandleChartPanel extends JFrame {

    private JFreeChart chart;
    private XYPlot plot;

    public CandleChartPanel(String title) {
        super(title);

        // 1. 초기 데이터셋 생성 (기본 '분' 단위)
        OHLCDataset dataset = createDataset("1분");

        // 2. 차트 생성
        chart = ChartFactory.createCandlestickChart(
                "", "", "", dataset, false);

        plot = (XYPlot) chart.getPlot();
        plot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.TOP_OR_RIGHT);
        
        plot.setDomainPannable(true); // X축 이동 활성화
        plot.setRangePannable(true);  // Y축 이동 활성화
        

        // 렌더러 설정
        CandlestickRenderer renderer = (CandlestickRenderer) plot.getRenderer();
        renderer.setDrawVolume(false);
        renderer.setUpPaint(Color.RED);
        renderer.setDownPaint(Color.BLUE);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeIncludesZero(false);

        // 3. 차트 패널
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 500));
        chartPanel.setMouseWheelEnabled(true);

        // 4. 상단 버튼 패널 추가
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        String[] intervals = {"1분", "5분", "30분", "1시간", "일"};
        for (String interval : intervals) {
            JButton button = new JButton(interval);
            button.addActionListener(e -> updateChart(interval)); // 클릭 이벤트
            buttonPanel.add(button);
        }

        // 전체 레이아웃 구성
        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH); // 상단에 버튼 배치
        add(chartPanel, BorderLayout.CENTER); // 중앙에 차트 배치
    }

    /**
     * 버튼 클릭 시 차트 데이터를 업데이트하는 메서드
     */
    private void updateChart(String interval) {
        System.out.println(interval + " 데이터로 갱신 중...");
        
        // 새로운 가상 데이터셋 생성
        OHLCDataset newDataset = createDataset(interval);
        
        // 차트의 데이터셋만 교체 (차트가 자동으로 다시 그려짐)
        plot.setDataset(newDataset);
        chart.setTitle("Coin Price Test (" + interval + ")");
    }

    /**
     * 시간 단위별 가상 데이터 생성 로직
     */
    private OHLCDataset createDataset(String interval) {
        int count = 100;
        Date[] date = new Date[count];
        double[] high = new double[count];
        double[] low = new double[count];
        double[] open = new double[count];
        double[] close = new double[count];
        double[] volume = new double[count];

        long now = System.currentTimeMillis();
        
        // 시간 단위에 따른 밀리초 계산
        long timeStep;
        switch (interval) {
            case "1분":   timeStep = 60 * 1000L; break;
            case "5분":   timeStep = 5 * 60 * 1000L; break;
            case "30분":  timeStep = 30 * 60 * 1000L; break;
            case "1시간": timeStep = 60 * 60 * 1000L; break;
            default:      timeStep = 24 * 60 * 60 * 1000L; // 일
        }

        double lastPrice = 60000.0;
        for (int i = 0; i < count; i++) {
            // 역순으로 시간을 배치하여 현재가 가장 오른쪽에 오게 함
            date[i] = new Date(now - (long)(count - i) * timeStep);
            
            open[i] = lastPrice;
            double vol = open[i] * 0.02;
            high[i] = open[i] + (Math.random() * vol);
            low[i] = open[i] - (Math.random() * vol);
            close[i] = low[i] + (Math.random() * (high[i] - low[i]));
            volume[i] = 100.0;
            lastPrice = close[i];
        }

        return new DefaultHighLowDataset("BTC/KRW", date, high, low, open, close, volume);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CandleChartPanel frame = new CandleChartPanel("주기별 코인 차트 테스트");
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}