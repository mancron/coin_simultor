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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 캔들 차트 패널 (JPanel로 변경)
 * - MainFrame에 임베드 가능
 * - 시간 여행 기능과 연동
 */
public class CandleChartPanel extends JPanel {

    private JFreeChart chart;
    private XYPlot plot;
    private ChartPanel chartPanel;
    private CandleDAO candleDAO = new CandleDAO();
    
    private JPanel buttonPanel; // 버튼 패널
    private String currentMarket = "KRW-BTC"; // 기본 마켓
    private int currentFactor = 1; // 기본 배수 (4시간)

    public CandleChartPanel(String title) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 1. 초기 데이터셋 생성 (기본 4시간 단위)
        OHLCDataset dataset = createDatasetFromDB(currentMarket, currentFactor);

        // 2. 차트 생성
        chart = ChartFactory.createCandlestickChart("", "", "", dataset, false);

        plot = (XYPlot) chart.getPlot();
        plot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.TOP_OR_RIGHT);

        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setDrawVolume(false);
        renderer.setUpPaint(Color.RED);
        renderer.setDownPaint(Color.BLUE);
        plot.setRenderer(renderer);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeIncludesZero(false);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 500));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setBackground(Color.WHITE);

        // 3. 상단 버튼 패널 (8h, 12h, 24h 추가)
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        // 버튼명과 압축 배수(Factor) 매핑
        String[] intervals = {"4시간", "8시간", "12시간", "24시간"};
        int[] factors = {1, 2, 3, 6}; // 4h * 1, 4h * 2...

        for (int i = 0; i < intervals.length; i++) {
            String label = intervals[i];
            int factor = factors[i];
            JButton button = new JButton(label);
            button.addActionListener(e -> {
                currentFactor = factor;
                refreshChart();
                chart.setTitle(getCurrentMarketSymbol() + " Chart (" + label + ")");
            });
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * 차트 새로고침
     */
    private void refreshChart() {
        OHLCDataset newDataset = createDatasetFromDB(currentMarket, currentFactor);
        plot.setDataset(newDataset);
    }

    /**
     * DB에서 4시간 데이터를 가져와 N배수로 묶어서 데이터셋 생성
     */
    private OHLCDataset createDatasetFromDB(String market, int factor) {
        // 1. DB에서 원본 4시간 데이터(unit 240)를 넉넉히 가져옴
        List<CandleDTO> originalList = candleDAO.getCandles(market, 240, 200);
        
        if (originalList.isEmpty()) {
            // 빈 데이터셋 반환
            return new DefaultHighLowDataset(
                market, 
                new Date[0], 
                new double[0], 
                new double[0], 
                new double[0], 
                new double[0], 
                new double[0]
            );
        }
        
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
            date[i] = java.sql.Timestamp.valueOf(dto.getCandleDateTimeKst());
            open[i] = dto.getOpeningPrice();
            high[i] = dto.getHighPrice();
            low[i] = dto.getLowPrice();
            close[i] = dto.getTradePrice();
            volume[i] = dto.getCandleAccTradeVolume();
        }

        return new DefaultHighLowDataset(market, date, high, low, open, close, volume);
    }

    /**
     * 봉 합성 로직: 4시간 데이터를 묶어서 상위 봉 생성
     */
    private List<CandleDTO> resampleCandles(List<CandleDTO> originalList, int factor) {
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
            newCandle.setOpeningPrice(first.getOpeningPrice());
            newCandle.setTradePrice(last.getTradePrice());

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

    /**
     * 마켓 변경 (코인 선택 시 호출)
     */
    public void changeMarket(String coinSymbol) {
        this.currentMarket = "KRW-" + coinSymbol;
        refreshChart();
        chart.setTitle(coinSymbol + " Chart");
    }

    /**
     * 과거 데이터 로드 (백테스팅용)
     */
    public void loadHistoricalData(LocalDateTime targetTime) {
        // TODO: targetTime까지의 데이터만 표시
        // 현재는 전체 데이터를 보여주지만, 나중에 시간 필터링 추가 가능
        refreshChart();
    }

    /**
     * 현재 마켓의 심볼 추출 (KRW-BTC -> BTC)
     */
    private String getCurrentMarketSymbol() {
        return currentMarket.replace("KRW-", "");
    }

    // === 독립 실행 테스트용 메서드 (옵션) ===
    public static JFrame createTestFrame() {
        JFrame frame = new JFrame("주기별 봉 합성 테스트");
        CandleChartPanel panel = new CandleChartPanel("BTC 차트");
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = createTestFrame();
            frame.setVisible(true);
        });
    }
}