package com.team.coin_simulator.chart;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;

/**
 * [CandleChartPanel]
 * JFreeChart를 이용한 암호화폐 캔들 차트 컴포넌트.
 *
 * ── 주요 기능 ──────────────────────────────────────────
 *  1. 현재가 점선 + Y축 영역 위 가격 박스 표시
 *  2. 초기 로딩 시 최근 55개 캔들만 표시, 가격 범위 자동 맞춤
 *  3. 실시간 라이브 캔들 업데이트 (Swing Timer 기반)
 *     - 현재 진행 중인 캔들을 메모리에서 관리 (LiveCandle)
 *     - 매 tick마다 실시간 가격으로 마지막 캔들의 OHLC 갱신
 *     - 캔들 기간 종료 시 DB 저장 후 새 캔들 시작
 * ────────────────────────────────────────────────────────
 */
public class CandleChartPanel extends JPanel {

    // ── 차트 컴포넌트 ────────────────────────────────────
    private JFreeChart chart;
    private XYPlot plot;
    private OverlayChartPanel chartPanel; // 가격 박스를 직접 그리는 커스텀 ChartPanel
    private CandleDAO candleDAO = new CandleDAO();
    private JButton selectedButton;

    // ── 현재 상태 ────────────────────────────────────────
    private String currentMarket = "KRW-BTC";
    private int currentFactor = 1;
    private Point lastMousePoint;

    // ── 실시간 라이브 캔들 ────────────────────────────────
    /**
     * 현재 진행 중인 캔들 데이터를 메모리에서 관리합니다.
     * DB에는 캔들이 완성(종료)된 시점에만 저장합니다.
     *
     * [업데이트 전략]
     * ┌─────────────────────────────────────────────────────┐
     * │ ① Swing Timer가 N초마다 실시간 API 가격 수신        │
     * │ ② liveCandle의 Close = 현재가                       │
     * │    High = max(High, 현재가)                         │
     * │    Low  = min(Low,  현재가)                         │
     * │ ③ DB 캔들 목록 + liveCandle 합쳐서 데이터셋 생성    │
     * │ ④ 캔들 기간 종료 → DB insert → 새 liveCandle 시작  │
     * └─────────────────────────────────────────────────────┘
     *
     * ※ 외부에서 updateLivePrice(double price) 를 호출하면
     *   위 ②~③ 과정이 자동으로 처리됩니다.
     */
    private CandleDTO liveCandle = null;
    private LocalDateTime liveCandleEndTime = null;

    // ── 실시간 자동 갱신 타이머 ──────────────────────────
    private Timer liveTimer;
    private static final int LIVE_TICK_MS = 3000; // 3초마다 갱신

    // ── 현재가 박스 표시용 상태 ──────────────────────────
    private double overlayPrice = Double.NaN;
    private boolean overlayRising = true;


    // ════════════════════════════════════════════════════
    //  생성자
    // ════════════════════════════════════════════════════
    public CandleChartPanel(String title) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. 초기 데이터셋 생성
        OHLCDataset dataset = createDatasetFromDB(currentMarket, currentFactor);
        chart = ChartFactory.createCandlestickChart(null, "", "", dataset, false);
        chart.setTitle(getCurrentMarketSymbol());
        plot = (XYPlot) chart.getPlot();

        // 2. 차트 UI 설정
        configureChartUI();

        // 3. 커스텀 ChartPanel 생성 (가격 박스 오버레이 포함)
        setupChartPanel();

        // 4. 버튼 패널 생성
        createButtonPanel();

        add(chartPanel, BorderLayout.CENTER);

        // 5. 초기 X/Y 범위 + 현재가 마커 설정
        updateXAxisRange(dataset);
        updateYAxisRange(dataset);
        drawCurrentPriceDashLine(dataset);

        // 6. 라이브 타이머 시작
        startLiveTimer();
    }


    // ════════════════════════════════════════════════════
    //  차트 UI 설정
    // ════════════════════════════════════════════════════

    private void configureChartUI() {
        // Y축 위치 및 0 포함 안함 설정
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeIncludesZero(false);

        // X축(날짜) 포맷 설정
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
        domainAxis.setTickLabelFont(new Font("Nanum Gothic", Font.PLAIN, 11));
        domainAxis.setAutoTickUnitSelection(true);

        // 렌더러 설정 (상승: 빨강, 하락: 파랑)
        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
        renderer.setAutoWidthGap(-0.1);
        renderer.setDrawVolume(false);
        renderer.setUpPaint(Color.RED);
        renderer.setDownPaint(Color.BLUE);
        plot.setRenderer(renderer);
    }

    private void setupChartPanel() {
        // ChartPanel을 서브클래스로 생성 → paintComponent에서 가격 박스 직접 그림
        chartPanel = new OverlayChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 500));
        chartPanel.setMouseWheelEnabled(true);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        chartPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { lastMousePoint = e.getPoint(); }
            public void mouseReleased(MouseEvent e) { chartPanel.repaint(); }
        });
        chartPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint != null) lastMousePoint = e.getPoint();
            }
        });
        chartPanel.addMouseWheelListener(e ->
            SwingUtilities.invokeLater(() -> chartPanel.repaint())
        );
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        String[] intervals = {"4시간", "8시간", "12시간", "24시간"};
        int[] factors = {1, 2, 3, 6};

        for (int i = 0; i < intervals.length; i++) {
            String label = intervals[i];
            int factor = factors[i];
            JButton button = new JButton(label);
            button.setBackground(Color.LIGHT_GRAY);
            button.setFocusPainted(false);
            if (factor == currentFactor) {
                button.setBackground(Color.ORANGE);
                selectedButton = button;
            }
            button.addActionListener(e -> {
                if (selectedButton != null) selectedButton.setBackground(Color.LIGHT_GRAY);
                button.setBackground(Color.ORANGE);
                selectedButton = button;
                currentFactor = factor;
                refreshChart();
            });
            buttonPanel.add(button);
        }
        add(buttonPanel, BorderLayout.NORTH);
    }


    // ════════════════════════════════════════════════════
    //  차트 갱신
    // ════════════════════════════════════════════════════

    private void refreshChart() {
        OHLCDataset dataset = createDatasetFromDB(currentMarket, currentFactor);
        plot.setDataset(dataset);
        chart.setTitle(getCurrentMarketSymbol());

        updateXAxisRange(dataset);
        updateYAxisRange(dataset);
        drawCurrentPriceDashLine(dataset);

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat(
                currentFactor >= 6 ? "MM/dd" : "MM/dd HH:mm"));
    }

    private void updateXAxisRange(OHLCDataset dataset) {
        int itemCount = dataset.getItemCount(0);
        if (itemCount <= 0) return;

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        int displayCount = Math.min(itemCount, 55);
        double lastTick  = dataset.getXValue(0, itemCount - 1);
        double firstTick = dataset.getXValue(0, itemCount - displayCount);
        double oneBarInterval = (displayCount > 1)
                ? (lastTick - firstTick) / (displayCount - 1) : 1;

        double rightOffset = oneBarInterval * 4.5;
        domainAxis.setRange(firstTick - oneBarInterval * 0.5, lastTick + rightOffset);
    }

    private void updateYAxisRange(OHLCDataset dataset) {
        int itemCount = dataset.getItemCount(0);
        if (itemCount <= 0) return;

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        int displayCount = Math.min(itemCount, 55);
        int startIndex = itemCount - displayCount;

        double maxHigh = Double.MIN_VALUE, minLow = Double.MAX_VALUE;
        for (int i = startIndex; i < itemCount; i++) {
            maxHigh = Math.max(maxHigh, dataset.getHighValue(0, i));
            minLow  = Math.min(minLow,  dataset.getLowValue(0, i));
        }
        double range = maxHigh - minLow;
        yAxis.setRange(minLow - range * 0.05, maxHigh + range * 0.05);
    }

    /**
     * 현재가 수평 점선을 그리고, 오버레이 박스용 상태를 업데이트합니다.
     */
    private void drawCurrentPriceDashLine(OHLCDataset dataset) {
        plot.clearRangeMarkers();
        int itemCount = dataset.getItemCount(0);
        if (itemCount <= 0) return;

        double currentPrice  = dataset.getCloseValue(0, itemCount - 1);
        double previousPrice = (itemCount > 1) ? dataset.getCloseValue(0, itemCount - 2) : currentPrice;
        overlayRising = (currentPrice >= previousPrice);
        overlayPrice  = currentPrice;

        Color color = overlayRising ? Color.RED : Color.BLUE;

        ValueMarker marker = new ValueMarker(currentPrice);
        marker.setPaint(color);
        marker.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, new float[]{5.0f}, 0.0f));
        // 레이블 없음 → OverlayChartPanel의 paintComponent가 박스로 직접 표시
        plot.addRangeMarker(marker, org.jfree.chart.ui.Layer.FOREGROUND);

        SwingUtilities.invokeLater(() -> chartPanel.repaint());
    }


    // ════════════════════════════════════════════════════
    //  커스텀 ChartPanel — 가격 박스 직접 오버레이
    // ════════════════════════════════════════════════════

    /**
     * ChartPanel을 상속해 paintComponent 마지막에 현재가 박스를 직접 그립니다.
     * JLayeredPane 없이 단순하게 동작하므로 레이아웃 문제가 없습니다.
     */
    private class OverlayChartPanel extends ChartPanel {

        private final DecimalFormat fmt = new DecimalFormat("#,###.#####");

        OverlayChartPanel(JFreeChart chart) {
            super(chart);
        }

        @Override
        public void paintComponent(Graphics g) {
            // 1. 차트 본체 먼저 그리기
            super.paintComponent(g);

            // 2. 가격이 없으면 박스 생략
            if (Double.isNaN(overlayPrice) || plot == null) return;

            // 3. plot 영역 픽셀 좌표 가져오기
            Rectangle2D plotArea = getScreenDataArea();
            if (plotArea == null) return;

            // 4. 현재가 → Y 픽셀 변환
            NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
            double yPixel = yAxis.valueToJava2D(
                    overlayPrice, plotArea, plot.getRangeAxisEdge());

            // 5. 박스 위치 (plot 오른쪽 끝 바로 다음 = Y축 레이블 영역)
            int boxW = 95;
            int boxH = 20;
            int boxX = (int) plotArea.getMaxX() + 2;
            int boxY = (int) (yPixel - boxH / 2.0);

            // 6. 박스가 화면 밖으로 나가면 그리지 않음
            if (boxY < 0 || boxY + boxH > getHeight()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color boxColor = overlayRising ? Color.RED : Color.BLUE;

            // 7. 왼쪽 삼각형 포인터
            g2.setColor(boxColor);
            //int[] xs = {boxX - 6, boxX, boxX};
            //int[] ys = {(int) yPixel, boxY, boxY + boxH};
            //g2.fillPolygon(xs, ys, 3);

            // 8. 박스 배경
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 4, 4);

            // 9. 가격 텍스트 (왼쪽 정렬)
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Nanum Gothic", Font.BOLD, 13));
            String text = fmt.format(overlayPrice);
            FontMetrics fm = g2.getFontMetrics();
            int tx = boxX + 5; // 왼쪽 여백 5px
            int ty = boxY + (boxH + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }


    // ════════════════════════════════════════════════════
    //  라이브 캔들 실시간 업데이트
    // ════════════════════════════════════════════════════

    private void startLiveTimer() {
        liveTimer = new Timer(LIVE_TICK_MS, e -> onLiveTick());
        liveTimer.start();
    }

    public void stopLiveTimer() {
        if (liveTimer != null) liveTimer.stop();
    }

    /**
     * 타이머 tick: 실시간 가격을 받아와서 라이브 캔들 갱신
     *
     * TODO: 이 메서드 내부에서 업비트 REST 또는 WebSocket 가격을 수신하세요.
     * 예: updateLivePrice(UpbitAPI.getTicker(currentMarket));
     */
    private void onLiveTick() {
        // 현재는 타이머만 실행 (가격 주입 대기 상태)
        // updateLivePrice(latestPrice); ← 여기에 연결
    }
    
    
    // CandleChartPanel 클래스 내부에 추가
    /**
     * WebSocket 서비스로부터 실시간 가격을 전달받아 마지막 캔들을 갱신합니다.
     */
   
    

    /**
     * 외부(실시간 API 콜백 등)에서 현재가를 주입할 때 호출합니다.
     *
     * <pre>
     * // 사용 예시:
     * candleChartPanel.updateLivePrice(98_500_000.0);
     * </pre>
     */
    public void updateLivePrice(double price) {
        LocalDateTime now = LocalDateTime.now();

        if (liveCandle == null) {
            liveCandle = createNewLiveCandle(price, now);
            liveCandleEndTime = calculateCandleEndTime(now);
        }

        if (now.isAfter(liveCandleEndTime)) {
            finalizeLiveCandle();
            liveCandle = createNewLiveCandle(price, now);
            liveCandleEndTime = calculateCandleEndTime(now);
        }

        liveCandle.setTradePrice(price);
        liveCandle.setHighPrice(Math.max(liveCandle.getHighPrice(), price));
        liveCandle.setLowPrice(Math.min(liveCandle.getLowPrice(), price));

        SwingUtilities.invokeLater(() -> {
            OHLCDataset dataset = createDatasetWithLiveCandle(currentMarket, currentFactor);
            plot.setDataset(dataset);
            drawCurrentPriceDashLine(dataset);
        });
    }
    
    

    private void finalizeLiveCandle() {
        if (liveCandle == null) return;
        liveCandle.setUnit(getCurrentUnitMinutes());
        candleDAO.insertCandles(Collections.singletonList(liveCandle));
        liveCandle = null;
    }

    private CandleDTO createNewLiveCandle(double price, LocalDateTime time) {
        CandleDTO c = new CandleDTO();
        c.setMarket(currentMarket);
        c.setCandleDateTimeKst(time);
        c.setCandleDateTimeUtc(time.minusHours(9));
        c.setOpeningPrice(price);
        c.setHighPrice(price);
        c.setLowPrice(price);
        c.setTradePrice(price);
        c.setTimestamp(System.currentTimeMillis());
        c.setUnit(getCurrentUnitMinutes());
        return c;
    }

    private LocalDateTime calculateCandleEndTime(LocalDateTime now) {
        int totalMinutes = getCurrentUnitMinutes();
        long minutesSinceEpoch = ChronoUnit.MINUTES.between(LocalDateTime.of(1970, 1, 1, 0, 0), now);
        long blockStart = (minutesSinceEpoch / totalMinutes) * totalMinutes;
        return LocalDateTime.of(1970, 1, 1, 0, 0).plusMinutes(blockStart + totalMinutes);
    }

    private int getCurrentUnitMinutes() {
        return currentFactor * 240;
    }

    private OHLCDataset createDatasetWithLiveCandle(String market, int factor) {
        List<CandleDTO> dbList = candleDAO.getCandles(market, 240, 200);
        if (dbList.isEmpty() && liveCandle == null) {
            return new DefaultHighLowDataset(market, new Date[0], new double[0],
                    new double[0], new double[0], new double[0], new double[0]);
        }
        Collections.reverse(dbList);
        List<CandleDTO> resampledList = resampleCandles(dbList, factor);
        if (liveCandle != null) resampledList.add(liveCandle);
        return buildDataset(market, resampledList);
    }


    // ════════════════════════════════════════════════════
    //  데이터셋 생성 헬퍼
    // ════════════════════════════════════════════════════

    private OHLCDataset createDatasetFromDB(String market, int factor) {
        List<CandleDTO> originalList = candleDAO.getCandles(market, 240, 200);
        if (originalList.isEmpty()) {
            return new DefaultHighLowDataset(market, new Date[0], new double[0],
                    new double[0], new double[0], new double[0], new double[0]);
        }
        Collections.reverse(originalList);
        return buildDataset(market, resampleCandles(originalList, factor));
    }

    private OHLCDataset buildDataset(String market, List<CandleDTO> list) {
        int count = list.size();
        Date[]   date   = new Date[count];
        double[] high   = new double[count], low    = new double[count];
        double[] open   = new double[count], close  = new double[count];
        double[] volume = new double[count];

        for (int i = 0; i < count; i++) {
            CandleDTO dto = list.get(i);
            date[i]   = java.sql.Timestamp.valueOf(dto.getCandleDateTimeKst());
            open[i]   = dto.getOpeningPrice();
            high[i]   = dto.getHighPrice();
            low[i]    = dto.getLowPrice();
            close[i]  = dto.getTradePrice();
            volume[i] = dto.getCandleAccTradeVolume();
        }
        return new DefaultHighLowDataset(market, date, high, low, open, close, volume);
    }

    private List<CandleDTO> resampleCandles(List<CandleDTO> originalList, int factor) {
        List<CandleDTO> resampledList = new ArrayList<>();
        for (int i = 0; i < originalList.size(); i += factor) {
            int end = Math.min(i + factor, originalList.size());
            List<CandleDTO> sub = originalList.subList(i, end);

            CandleDTO c = new CandleDTO();
            c.setMarket(sub.get(0).getMarket());
            c.setCandleDateTimeKst(sub.get(0).getCandleDateTimeKst());
            c.setCandleDateTimeUtc(sub.get(0).getCandleDateTimeUtc());
            c.setOpeningPrice(sub.get(0).getOpeningPrice());
            c.setTradePrice(sub.get(sub.size() - 1).getTradePrice());
            c.setHighPrice(sub.stream().mapToDouble(CandleDTO::getHighPrice).max().orElse(0));
            c.setLowPrice(sub.stream().mapToDouble(CandleDTO::getLowPrice).min().orElse(0));
            c.setCandleAccTradeVolume(sub.stream().mapToDouble(CandleDTO::getCandleAccTradeVolume).sum());
            resampledList.add(c);
        }
        return resampledList;
    }


    // ════════════════════════════════════════════════════
    //  공개 API
    // ════════════════════════════════════════════════════

    public void changeMarket(String coinSymbol) {
        this.currentMarket = "KRW-" + coinSymbol;
        liveCandle = null;
        refreshChart();
    }

    private String getCurrentMarketSymbol() {
        return currentMarket.replace("KRW-", "");
    }
}