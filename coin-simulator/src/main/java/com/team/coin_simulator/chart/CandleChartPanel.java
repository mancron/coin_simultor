package com.team.coin_simulator.chart;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CandleChartPanel extends JFrame {

    private JFreeChart chart;
    private XYPlot plot;
    private CandleDAO candleDAO = new CandleDAO(); // DB 접근용 DAO

    public CandleChartPanel(String title) {
        super(title);

        // 1. 초기 데이터셋 생성 (기본 4시간 단위)
        OHLCDataset dataset = createDatasetFromDB("KRW-BTC", 1); // factor 1 = 4시간

        // 2. 차트 생성
        chart = ChartFactory.createCandlestickChart("", "", "", dataset, false);

        plot = (XYPlot) chart.getPlot();
        plot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.TOP_OR_RIGHT);

        CandlestickRenderer renderer = (CandlestickRenderer) plot.getRenderer();
        renderer.setDrawVolume(false);
        renderer.setUpPaint(Color.RED);
        renderer.setDownPaint(Color.BLUE);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeIncludesZero(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 500));
        chartPanel.setMouseWheelEnabled(true);

        // 3. 상단 버튼 패널 (8h, 12h, 24h 추가)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        // 버튼명과 압축 배수(Factor) 매핑
        String[] intervals = {"4시간", "8시간", "12시간", "24시간"};
        int[] factors = {1, 2, 3, 6}; // 4h * 1, 4h * 2...

        for (int i = 0; i < intervals.length; i++) {
            String label = intervals[i];
            int factor = factors[i];
            JButton button = new JButton(label);
            button.addActionListener(e -> {
                plot.setDataset(createDatasetFromDB("KRW-BTC", factor));
                chart.setTitle("BTC Chart (" + label + ")");
            });
            buttonPanel.add(button);
        }

        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * DB에서 4시간 데이터를 가져와 N배수로 묶어서 데이터셋 생성
     */
    private OHLCDataset createDatasetFromDB(String market, int factor) {
        // 1. DB에서 원본 4시간 데이터(unit 240)를 넉넉히 가져옴
        List<CandleDTO> originalList = candleDAO.getCandles(market, 240, 200);
        
        // 2. 과거 -> 현재 순서로 정렬
        Collections.reverse(originalList);

        // 3. 사용자가 요청한 배수(factor)만큼 데이터 합성
        List<CandleDTO> resampledList = resampleCandles(originalList, factor);

        // 4. JFreeChart 형식으로 변환
        int count = resampledList.size();
        Date[] date = new Date[count];
        double[] high = new double[count];
        double[] low = new double[count];
        double[] open = new double[count];
        double[] close = new double[count];
        double[] volume = new double[count];

        for (int i = 0; i < count; i++) {
            CandleDTO dto = resampledList.get(i);
            date[i] = java.sql.Timestamp.valueOf(dto.getCandleDateTimeKst()); //
            open[i] = dto.getOpeningPrice(); //
            high[i] = dto.getHighPrice(); //
            low[i] = dto.getLowPrice(); //
            close[i] = dto.getTradePrice(); //
            volume[i] = dto.getCandleAccTradeVolume(); //
        }

        return new DefaultHighLowDataset(market, date, high, low, open, close, volume);
    }

    /**
     * 봉 합성 로직: 4시간 데이터를 묶어서 상위 봉 생성
     */
    public List<CandleDTO> resampleCandles(List<CandleDTO> originalList, int factor) {
        List<CandleDTO> resampledList = new ArrayList<>();
        if (originalList.isEmpty()) return resampledList;

        for (int i = 0; i < originalList.size(); i += factor) {
            int end = Math.min(i + factor, originalList.size());
            List<CandleDTO> subList = originalList.subList(i, end);

            CandleDTO newCandle = new CandleDTO();
            CandleDTO first = subList.get(0);
            CandleDTO last = subList.get(subList.size() - 1);

            newCandle.setMarket(first.getMarket());
            newCandle.setCandleDateTimeKst(first.getCandleDateTimeKst());
            newCandle.setOpeningPrice(first.getOpeningPrice()); // 첫 번째 봉의 시가
            newCandle.setTradePrice(last.getTradePrice());    // 마지막 봉의 종가

            double maxHigh = -1.0;
            double minLow = Double.MAX_VALUE;
            double totalVol = 0;

            for (CandleDTO dto : subList) {
                if (dto.getHighPrice() > maxHigh) maxHigh = dto.getHighPrice();
                if (dto.getLowPrice() < minLow) minLow = dto.getLowPrice();
                totalVol += dto.getCandleAccTradeVolume();
            }

            newCandle.setHighPrice(maxHigh);
            newCandle.setLowPrice(minLow);
            newCandle.setCandleAccTradeVolume(totalVol);
            resampledList.add(newCandle);
        }
        return resampledList;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CandleChartPanel frame = new CandleChartPanel("주기별 봉 합성 테스트");
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}