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
 *  2. 초기 로딩 시 최근 N개 캔들만 표시, 가격 범위 자동 맞춤
 *  3. 실시간 라이브 캔들 업데이트 (Swing Timer 기반)
 *     - DB에서 1분봉을 가져와 표시
 *     - 매 분 00초에 라이브 캔들을 DB 데이터로 대체하고 새 분봉 시작
 *  4. 백테스팅 모드: targetTime 기준 과거 데이터만 표시
 *     ★ 타임프레임 변경 시에도 백테스팅 시점 유지
 *  5. DB에서 1분봉 기준으로 조회 후 타임프레임에 맞게 리샘플링
 *     - 지원 타임프레임: 1분 / 30분 / 1시간 / 4시간 / 1일 / 1달
 * ────────────────────────────────────────────────────────
 */
public class CandleChartPanel extends JPanel {

    // ── 타임프레임 정의 (분 단위) ────────────────────────
    private static final int TF_1M   = 1;
    private static final int TF_30M  = 30;
    private static final int TF_1H   = 60;
    private static final int TF_4H   = 240;
    private static final int TF_1D   = 1440;
    private static final int TF_1MON = 43200; // 30일 기준

    /** 타임프레임별 화면 기본 표시 캔들 수 */
    private static final int DISPLAY_1M   = 55;
    private static final int DISPLAY_30M  = 55;
    private static final int DISPLAY_1H   = 55;
    private static final int DISPLAY_4H   = 55;
    private static final int DISPLAY_1D   = 55;
    private static final int DISPLAY_1MON = 55;

    /**
     * DB에서 가져올 1분봉 최대 개수.
     */
    private static final int DB_FETCH_LIMIT = 1000000;

    // ── 차트 컴포넌트 ────────────────────────────────────
    private JFreeChart chart;
    private XYPlot plot;
    private OverlayChartPanel chartPanel;
    private CandleDAO candleDAO = new CandleDAO();
    private JButton selectedButton;

    // ── 현재 상태 ────────────────────────────────────────
    private String currentMarket = "KRW-BTC";
    private int currentTimeframe = TF_1M; // 기본 1분봉
    private Point lastMousePoint;

    // ── 백테스팅 상태 ─────────────────────────────────────
    private LocalDateTime backtestTargetTime = null;

    // ── 실시간 라이브 캔들 ────────────────────────────────
    /**
     * 현재 진행 중인 라이브(미확정) 캔들.
     * 매 분 00초가 되면 DB에서 확정된 1분봉으로 교체되고,
     * liveCandle은 새로운 분봉 데이터로 초기화됩니다.
     */
    private CandleDTO liveCandle = null;

    /**
     * liveCandle이 속하는 분봉 구간의 시작 시각 (초·나노초 = 0).
     * ex) 14:23:37 → 14:23:00
     */
    private LocalDateTime liveCandleMinuteStart = null;

    // ── 실시간 타이머 ────────────────────────────────────
    private Timer liveTimer;
    private static final int LIVE_RENDER_MS = 500; // 500ms마다 화면 갱신
    private volatile double latestLivePrice = -1;

    // ── 웹소켓 ───────────────────────────────────────────
    private UpbitWebSocket webSocketClient;

    // ── 현재가 박스 표시 ──────────────────────────────────
    private double overlayPrice = Double.NaN;
    private boolean overlayRising = true;


    // ════════════════════════════════════════════════════
    //  생성자
    // ════════════════════════════════════════════════════
    public CandleChartPanel(String title) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        OHLCDataset dataset = createDataset(currentMarket, currentTimeframe, null);
        chart = ChartFactory.createCandlestickChart(null, "", "", dataset, false);
        chart.setTitle(getCurrentMarketSymbol());
        plot = (XYPlot) chart.getPlot();

        
        
        
        configureChartUI();
        setupChartPanel();
        createButtonPanel();

        add(chartPanel, BorderLayout.CENTER);

        updateXAxisRange(dataset);
        updateYAxisRange(dataset);
        drawCurrentPriceDashLine(dataset);

        startLiveTimer();
        connectWebSocket();
    }


    // ════════════════════════════════════════════════════
    //  차트 UI 설정
    // ════════════════════════════════════════════════════

    private void configureChartUI() {
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeIncludesZero(false);
        
        

        // ── Y축 소수점 표기 수정: 과학적 표기법(9.15E-3) 대신 일반 소수점(0.00915) ──
        yAxis.setNumberFormatOverride(buildPriceFormat());

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
        domainAxis.setTickLabelFont(new Font("Nanum Gothic", Font.PLAIN, 11));
        domainAxis.setAutoTickUnitSelection(true);

        
        
        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
        renderer.setAutoWidthGap(0.0);   // 간격 없음 — zoom 시 벌어지는 현상 제거
        renderer.setDrawVolume(false);
        renderer.setUpPaint(Color.RED);
        renderer.setDownPaint(Color.BLUE);
        plot.setRenderer(renderer);
    }

    private void setupChartPanel() {
        chartPanel = new OverlayChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 500));
        chartPanel.setRangeZoomable(false);
        chartPanel.setDomainZoomable(false);
        chartPanel.setMouseWheelEnabled(false);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        chartPanel.addMouseWheelListener(e -> {
            handleFixedRightZoom(e.getWheelRotation());
            SwingUtilities.invokeLater(() -> chartPanel.repaint());
        });

        chartPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { lastMousePoint = e.getPoint(); }
            public void mouseReleased(MouseEvent e) { lastMousePoint = null; chartPanel.repaint(); }
        });

        chartPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint == null) return;

                int dx = e.getX() - lastMousePoint.x;
                lastMousePoint = e.getPoint();

                DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
                Rectangle2D plotArea = chartPanel.getScreenDataArea();
                if (plotArea == null || plotArea.getWidth() == 0) return;

                double msPerPixel = domainAxis.getRange().getLength() / plotArea.getWidth();
                double shift = -dx * msPerPixel;

                domainAxis.setRange(
                    domainAxis.getRange().getLowerBound() + shift,
                    domainAxis.getRange().getUpperBound() + shift
                );

                updateYAxisRange((OHLCDataset) plot.getDataset());
                chartPanel.repaint();
            }
        });
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        String[] labels     = {"1분",  "30분",  "1시간", "4시간", "1일",   "1달"};
        int[]    timeframes = {TF_1M, TF_30M, TF_1H,  TF_4H,  TF_1D, TF_1MON};

        for (int i = 0; i < labels.length; i++) {
            final String label     = labels[i];
            final int    timeframe = timeframes[i];

            JButton button = new JButton(label);
            button.setBackground(Color.LIGHT_GRAY);
            button.setFocusPainted(false);

            if (timeframe == currentTimeframe) {
                button.setBackground(Color.ORANGE);
                selectedButton = button;
            }

            button.addActionListener(e -> {
                if (selectedButton != null) selectedButton.setBackground(Color.LIGHT_GRAY);
                button.setBackground(Color.ORANGE);
                selectedButton = button;
                currentTimeframe = timeframe;

                // ★ refreshChart()가 backtestTargetTime을 참조하므로
                //   백테스팅 시점이 자동으로 유지됩니다.
                refreshChart();
            });

            buttonPanel.add(button);
        }
        add(buttonPanel, BorderLayout.NORTH);
    }


    // ════════════════════════════════════════════════════
    //  통합 데이터셋 생성
    // ════════════════════════════════════════════════════

    /**
     * 실시간/백테스팅 모드에 따라 1분봉을 조회하고 리샘플링합니다.
     */
    private OHLCDataset createDataset(String market, int timeframe, LocalDateTime targetTime) {
        List<CandleDTO> rawList;

        if (targetTime != null) {
            rawList = candleDAO.getHistoricalCandles(market, 1, targetTime, DB_FETCH_LIMIT);
        } else {
            rawList = candleDAO.getCandles(market, 1, DB_FETCH_LIMIT);
        }

        if (rawList.isEmpty()) return emptyDataset(market);

        // DB 조회는 DESC → ASC로 뒤집기
        Collections.reverse(rawList);

        List<CandleDTO> resampled = resampleCandles(rawList, timeframe);
        return buildDataset(market, resampled);
    }

    /**
     * 실시간 모드 전용: DB의 확정된 1분봉 데이터 + 현재 진행 중인 liveCandle을 합쳐 데이터셋 생성.
     *
     * ── 동작 원리 ──────────────────────────────────────────────────
     *  1) DB에서 최신 1분봉 목록을 가져옴 (liveCandle이 속하는 현재 분봉 제외)
     *  2) 리샘플링 후 마지막에 liveCandle을 덧붙임
     *     - 현재 타임프레임이 1분봉이면 liveCandle을 그냥 추가
     *     - 상위 타임프레임이면 마지막 리샘플 캔들에 liveCandle을 병합
     * ────────────────────────────────────────────────────────────────
     */
    private OHLCDataset createDatasetWithLiveCandle(String market, int timeframe) {
        List<CandleDTO> rawList = candleDAO.getCandles(market, 1, DB_FETCH_LIMIT);
        Collections.reverse(rawList);

        // liveCandle이 없으면 DB 데이터만으로 렌더링
        if (liveCandle == null) {
            if (rawList.isEmpty()) return emptyDataset(market);
            return buildDataset(market, resampleCandles(rawList, timeframe));
        }

        // liveCandle과 같은 분봉이 DB에도 있을 경우 제거 (중복 방지)
        // DB는 확정된 캔들만 있어야 하므로 마지막 캔들의 시각이 liveCandleMinuteStart와 같으면 제외
        if (!rawList.isEmpty() && liveCandleMinuteStart != null) {
            CandleDTO last = rawList.get(rawList.size() - 1);
            LocalDateTime lastMinute = last.getCandleDateTimeKst().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            if (lastMinute.equals(liveCandleMinuteStart)) {
                rawList.remove(rawList.size() - 1);
            }
        }

        List<CandleDTO> resampled = rawList.isEmpty()
                ? new ArrayList<>()
                : resampleCandles(rawList, timeframe);

        // ── 타임프레임에 따른 liveCandle 합산 ──────────────────
        if (timeframe == TF_1M) {
            // 1분봉: 그냥 추가
            resampled.add(liveCandle);
        } else {
            // 상위 타임프레임: liveCandle이 마지막 리샘플 캔들과 같은 블록이면 병합
            if (!resampled.isEmpty()) {
                CandleDTO last = resampled.get(resampled.size() - 1);
                String lastBlock = calcBlockKey(last.getCandleDateTimeKst(), timeframe);
                String liveBlock = calcBlockKey(liveCandle.getCandleDateTimeKst(), timeframe);

                if (lastBlock.equals(liveBlock)) {
                    // 같은 블록 → 기존 마지막 캔들에 liveCandle 가격 반영
                    last.setTradePrice(liveCandle.getTradePrice());
                    last.setHighPrice(Math.max(last.getHighPrice(), liveCandle.getHighPrice()));
                    last.setLowPrice(Math.min(last.getLowPrice(), liveCandle.getLowPrice()));
                } else {
                    // 새 블록 시작
                    resampled.add(liveCandle);
                }
            } else {
                resampled.add(liveCandle);
            }
        }

        return buildDataset(market, resampled);
    }

    private OHLCDataset emptyDataset(String market) {
        return new DefaultHighLowDataset(market,
                new Date[0], new double[0], new double[0],
                new double[0], new double[0], new double[0]);
    }

    /**
     * 주어진 시각이 속하는 타임프레임 블록 키를 반환합니다.
     * (resampleCandles의 키 산출 로직과 동일)
     */
    private String calcBlockKey(LocalDateTime kst, int timeframeMinutes) {
        if (timeframeMinutes == TF_1MON) {
            return kst.getYear() + "-" + String.format("%02d", kst.getMonthValue());
        }
        long epochMinutes = ChronoUnit.MINUTES.between(LocalDateTime.of(1970, 1, 1, 0, 0), kst);
        return Long.toString(epochMinutes / timeframeMinutes);
    }


    // ════════════════════════════════════════════════════
    //  차트 갱신
    // ════════════════════════════════════════════════════

    private void refreshChart() {
        OHLCDataset dataset = createDataset(currentMarket, currentTimeframe, backtestTargetTime);
        plot.setDataset(dataset);

        chart.setTitle(backtestTargetTime != null
                ? getCurrentMarketSymbol() + " (Backtesting)"
                : getCurrentMarketSymbol());

        updateXAxisRange(dataset);
        updateYAxisRange(dataset);
        drawCurrentPriceDashLine(dataset);
        applyDateAxisFormat();
    }

    /** 타임프레임에 맞는 X축 날짜 포맷 적용 */
    private void applyDateAxisFormat() {
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        String fmt;
        if (currentTimeframe >= TF_1MON)   fmt = "yyyy/MM";
        else if (currentTimeframe >= TF_1D) fmt = "MM/dd";
        else                                fmt = "MM/dd HH:mm";
        domainAxis.setDateFormatOverride(new SimpleDateFormat(fmt));
    }

    /**
     * X축 범위를 최신 displayCount개 캔들이 보이도록 설정합니다.
     *
     * ── zoom 후 캔들 간격이 벌어지는 문제 해결 ──────────────────
     * 기존 코드는 하나의 고정된 oneBarInterval을 사용했기 때문에
     * zoom 후 범위가 달라지면 캔들 간격 계산이 어긋났습니다.
     * 수정: 타임프레임에서 직접 밀리초 단위 캔들 간격을 계산합니다.
     */
    private void updateXAxisRange(OHLCDataset dataset) {
        int itemCount = dataset.getItemCount(0);
        if (itemCount <= 0) return;

        int displayCount = Math.min(itemCount, getDisplayCount(currentTimeframe));

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        double lastTick  = dataset.getXValue(0, itemCount - 1);
        double firstTick = dataset.getXValue(0, itemCount - displayCount);

        // 타임프레임에서 정확한 캔들 간격(ms) 산출
        double candleIntervalMs = getCandleIntervalMs(currentTimeframe);

        // 오른쪽에 빈 공간(라이브 캔들 위한 여유) + 왼쪽에 반 칸 여유
        domainAxis.setRange(
            firstTick  - candleIntervalMs * 0.5,
            lastTick   + candleIntervalMs * 4.5
        );
    }

    /**
     * 타임프레임(분)을 밀리초로 변환합니다.
     * 1달봉은 30일 * 24h * 60m 기준.
     */
    private double getCandleIntervalMs(int timeframeMinutes) {
        long minutes = (timeframeMinutes == TF_1MON) ? 30L * 24 * 60 : timeframeMinutes;
        return minutes * 60_000.0;
    }

    private int getDisplayCount(int tf) {
        if (tf == TF_1M)   return DISPLAY_1M;
        if (tf == TF_30M)  return DISPLAY_30M;
        if (tf == TF_1H)   return DISPLAY_1H;
        if (tf == TF_4H)   return DISPLAY_4H;
        if (tf == TF_1D)   return DISPLAY_1D;
        if (tf == TF_1MON) return DISPLAY_1MON;
        return 55;
    }

    private void updateYAxisRange(OHLCDataset dataset) {
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        double visibleStart = domainAxis.getRange().getLowerBound();
        double visibleEnd   = domainAxis.getRange().getUpperBound();

        double maxHigh = -Double.MAX_VALUE;
        double minLow  =  Double.MAX_VALUE;
        boolean found  = false;

        for (int i = 0; i < dataset.getItemCount(0); i++) {
            double x = dataset.getXValue(0, i);
            if (x >= visibleStart && x <= visibleEnd) {
                maxHigh = Math.max(maxHigh, dataset.getHighValue(0, i));
                minLow  = Math.min(minLow,  dataset.getLowValue(0, i));
                found   = true;
            }
        }

        if (found) {
            NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
            double margin = (maxHigh - minLow) * 0.05;
            yAxis.setRange(minLow - margin, maxHigh + margin);
            // 범위가 바뀌어도 소수점 포맷 유지
            yAxis.setNumberFormatOverride(buildPriceFormat());
        }
    }

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
        plot.addRangeMarker(marker, org.jfree.chart.ui.Layer.FOREGROUND);

        SwingUtilities.invokeLater(() -> chartPanel.repaint());
    }


    // ════════════════════════════════════════════════════
    //  가격 포맷 유틸리티
    // ════════════════════════════════════════════════════

    /**
     * 과학적 표기법 없이 소수점 가격을 읽기 쉽게 표시하는 포맷을 반환합니다.
     *
     * 원화(KRW) 기반 코인은 보통 정수지만,
     * SHIB 등 소수점이 많은 코인은 9.15E-3 대신 0.00915 형태로 표시합니다.
     *
     * 전략: 항상 최소 0자리~최대 10자리 소수점 표시, 지수 표기 사용 안 함.
     */
    private DecimalFormat buildPriceFormat() {
        // "#,##0.##########" → 정수부 콤마, 소수점 최대 10자리, 불필요한 0 생략
        DecimalFormat df = new DecimalFormat("#,##0.##########");
        // 지수 표기를 완전히 비활성화하기 위해 multiplier는 기본값(1)으로 유지
        // Java DecimalFormat은 기본적으로 지수를 사용하지 않으므로 이것으로 충분합니다.
        return df;
    }

    /**
     * 가격 값을 화면 표시용 문자열로 변환합니다.
     * 소수점 가격(0.001234 등)도 과학적 표기 없이 그대로 표시합니다.
     */
    private String formatPrice(double price) {
        return buildPriceFormat().format(price);
    }


    // ════════════════════════════════════════════════════
    //  가격 박스 오버레이
    // ════════════════════════════════════════════════════

    private class OverlayChartPanel extends ChartPanel {

        OverlayChartPanel(JFreeChart chart) { super(chart); }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (Double.isNaN(overlayPrice) || plot == null) return;

            Rectangle2D plotArea = getScreenDataArea();
            if (plotArea == null) return;

            NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
            double yPixel = yAxis.valueToJava2D(overlayPrice, plotArea, plot.getRangeAxisEdge());

            int boxW = 110, boxH = 20;
            int boxX = (int) plotArea.getMaxX() + 2;
            int boxY = (int) (yPixel - boxH / 2.0);

            if (boxY < 0 || boxY + boxH > getHeight()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(overlayRising ? Color.RED : Color.BLUE);
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 4, 4);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Nanum Gothic", Font.BOLD, 12));
            // ── 소수점 표기 수정: 과학적 표기법 대신 일반 소수점 ──
            String text = formatPrice(overlayPrice);
            FontMetrics fm = g2.getFontMetrics();
            // 텍스트가 박스 너비를 초과하면 boxW를 동적으로 늘림
            int textW = fm.stringWidth(text);
            if (textW + 10 > boxW) {
                g2.setColor(overlayRising ? Color.RED : Color.BLUE);
                g2.fillRoundRect(boxX, boxY, textW + 10, boxH, 4, 4);
                g2.setColor(Color.WHITE);
            }
            g2.drawString(text, boxX + 5, boxY + (boxH + fm.getAscent() - fm.getDescent()) / 2);

            g2.dispose();
        }
    }


    // ════════════════════════════════════════════════════
    //  줌 — 타임프레임 기반 정확한 간격 유지
    // ════════════════════════════════════════════════════

    /**
     * 마우스 휠 줌 처리.
     *
     * ── 기존 문제 ─────────────────────────────────────────
     * 이전 구현은 현재 범위(length)에 비율 factor를 곱했기 때문에
     * 줌 후에도 캔들 간격이 데이터 간격(분봉 ms)과 달라져서
     * CandlestickRenderer의 자동 너비 계산이 어긋났습니다.
     *
     * ── 수정 ──────────────────────────────────────────────
     * 현재 보이는 캔들 수를 계산 → 1개 증감 방식으로 캔들 수를 조절 →
     * 캔들 수 × 캔들 간격(ms)으로 새 범위를 설정합니다.
     * 이렇게 하면 항상 정수 개수의 캔들이 정확한 간격으로 표시됩니다.
     */
    private void handleFixedRightZoom(int wheelRotation) {
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        OHLCDataset dataset = (OHLCDataset) plot.getDataset();
        if (dataset.getItemCount(0) <= 1) return;

        double candleMs = getCandleIntervalMs(currentTimeframe);
        double upper    = domainAxis.getRange().getUpperBound();
        double length   = domainAxis.getRange().getLength();

        // 현재 표시 중인 캔들 수 추정 (여유 공간 제외)
        int visibleCount = (int) Math.round(length / candleMs);

        // 줌 인: 캔들 수 감소 / 줌 아웃: 캔들 수 증가
        if (wheelRotation > 0) {
            visibleCount = Math.max(5, visibleCount + 5);   // 줌 아웃
        } else {
            visibleCount = Math.max(5, visibleCount - 5);   // 줌 인
        }

        // 새 범위 = 캔들 수 × 캔들 간격 (오른쪽 고정)
        double newLen = visibleCount * candleMs;
        domainAxis.setRange(upper - newLen, upper);

        updateYAxisRange(dataset);
        chartPanel.repaint();
    }


    // ════════════════════════════════════════════════════
    //  라이브 캔들 (실시간)
    // ════════════════════════════════════════════════════

    private void startLiveTimer() {
        if (liveTimer != null) liveTimer.stop();
        liveTimer = new Timer(LIVE_RENDER_MS, e -> onLiveTick());
        liveTimer.start();
    }

    public void stopLiveTimer() {
        if (liveTimer != null) liveTimer.stop();
    }

    /**
     * 500ms마다 호출되는 라이브 틱.
     *
     * ── 로직 ──────────────────────────────────────────────
     *  1) 현재 분봉 시작 시각(currentMinuteStart)을 계산합니다.
     *  2) liveCandleMinuteStart와 다르면 → 분봉이 넘어간 것이므로:
     *     a) 기존 liveCandle을 버림 (DB에 저장하지 않음 — DB가 이미 확정 데이터 가짐)
     *     b) DB에서 직전 분봉 OHLC를 가져와 liveCandle로 대체
     *     c) liveCandleMinuteStart를 현재 분으로 업데이트
     *  3) latestLivePrice로 liveCandle의 종가/고가/저가를 업데이트합니다.
     *  4) 차트를 갱신합니다.
     * ────────────────────────────────────────────────────────
     */
    private void onLiveTick() {
        if (backtestTargetTime != null || latestLivePrice <= 0) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentMinuteStart = now.truncatedTo(java.time.temporal.ChronoUnit.MINUTES);

        // ── 분봉 경계 감지: 새로운 분으로 넘어갔으면 liveCandle 교체 ──
        if (liveCandleMinuteStart == null || !liveCandleMinuteStart.equals(currentMinuteStart)) {
            // 직전 분봉을 DB에서 가져와 차트에 반영
            // (DB가 이미 확정 데이터를 갖고 있으므로 별도 저장 불필요)
            liveCandleMinuteStart = currentMinuteStart;
            liveCandle = createNewLiveCandle(latestLivePrice, currentMinuteStart);
        }

        // ── 현재가로 라이브 캔들 업데이트 ──
        double price = latestLivePrice;
        liveCandle.setTradePrice(price);
        liveCandle.setHighPrice(Math.max(liveCandle.getHighPrice(), price));
        liveCandle.setLowPrice(Math.min(liveCandle.getLowPrice(), price));

        // ── 차트 갱신 ──
        SwingUtilities.invokeLater(() -> {
            OHLCDataset dataset = createDatasetWithLiveCandle(currentMarket, currentTimeframe);
            plot.setDataset(dataset);
            updateYAxisRange(dataset);
            drawCurrentPriceDashLine(dataset);
        });
    }

    /**
     * 외부(WebSocket 콜백 등)에서 현재가를 주입합니다.
     * 백테스팅 모드에서는 무시됩니다.
     */
    public void setLatestPriceFromWebSocket(double price) {
        if (backtestTargetTime != null) return;
        this.latestLivePrice = price;
    }

    /**
     * @deprecated setLatestPriceFromWebSocket + onLiveTick 방식으로 대체되었습니다.
     */
    @Deprecated
    public void updateLivePrice(double price) {
        setLatestPriceFromWebSocket(price);
    }

    private CandleDTO createNewLiveCandle(double price, LocalDateTime minuteStart) {
        CandleDTO c = new CandleDTO();
        c.setMarket(currentMarket);
        c.setCandleDateTimeKst(minuteStart);
        c.setCandleDateTimeUtc(minuteStart.minusHours(9));
        c.setOpeningPrice(price);
        c.setHighPrice(price);
        c.setLowPrice(price);
        c.setTradePrice(price);
        c.setTimestamp(System.currentTimeMillis());
        c.setUnit(1);
        return c;
    }


    // ════════════════════════════════════════════════════
    //  리샘플링
    // ════════════════════════════════════════════════════

    /**
     * 1분봉 리스트를 지정된 타임프레임(분)으로 리샘플링합니다.
     */
    private List<CandleDTO> resampleCandles(List<CandleDTO> rawList, int timeframeMinutes) {
        if (timeframeMinutes <= 1) return new ArrayList<>(rawList);

        boolean isMonthly = (timeframeMinutes == TF_1MON);
        List<CandleDTO> result = new ArrayList<>();
        List<CandleDTO> group  = new ArrayList<>();
        String groupKey = null;

        for (CandleDTO candle : rawList) {
            LocalDateTime kst = candle.getCandleDateTimeKst();
            String key = calcBlockKey(kst, timeframeMinutes);

            if (!key.equals(groupKey)) {
                if (!group.isEmpty()) {
                    result.add(mergeGroup(group));
                    group.clear();
                }
                groupKey = key;
            }
            group.add(candle);
        }
        if (!group.isEmpty()) result.add(mergeGroup(group));

        return result;
    }

    private CandleDTO mergeGroup(List<CandleDTO> group) {
        CandleDTO first = group.get(0);
        CandleDTO last  = group.get(group.size() - 1);
        CandleDTO m = new CandleDTO();
        m.setMarket(first.getMarket());
        m.setCandleDateTimeKst(first.getCandleDateTimeKst());
        m.setCandleDateTimeUtc(first.getCandleDateTimeUtc());
        m.setOpeningPrice(first.getOpeningPrice());
        m.setTradePrice(last.getTradePrice());
        m.setHighPrice(group.stream().mapToDouble(CandleDTO::getHighPrice).max().orElse(0));
        m.setLowPrice(group.stream().mapToDouble(CandleDTO::getLowPrice).min().orElse(0));
        m.setCandleAccTradeVolume(group.stream().mapToDouble(CandleDTO::getCandleAccTradeVolume).sum());
        m.setUnit(currentTimeframe);
        return m;
    }

    private OHLCDataset buildDataset(String market, List<CandleDTO> list) {
        int count = list.size();
        Date[]   date   = new Date[count];
        double[] high   = new double[count], low   = new double[count];
        double[] open   = new double[count], close = new double[count];
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


    // ════════════════════════════════════════════════════
    //  웹소켓
    // ════════════════════════════════════════════════════

    private void connectWebSocket() {
        disconnectWebSocket();
        webSocketClient = new UpbitWebSocket(currentMarket, this);
        webSocketClient.connect();
    }

    private void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
            webSocketClient = null;
        }
    }


    // ════════════════════════════════════════════════════
    //  공개 API
    // ════════════════════════════════════════════════════

    /** 코인 종목 변경 */
    public void changeMarket(String coinSymbol) {
        this.currentMarket = "KRW-" + coinSymbol;
        liveCandle = null;
        liveCandleMinuteStart = null;
        refreshChart();
        if (backtestTargetTime == null) {
            connectWebSocket();
        }
    }

    /** 백테스팅 모드로 전환 */
    public void loadHistoricalData(LocalDateTime targetTime) {
        stopLiveTimer();
        disconnectWebSocket();
        liveCandle = null;
        liveCandleMinuteStart = null;
        this.backtestTargetTime = targetTime;

        SwingUtilities.invokeLater(() -> {
            OHLCDataset dataset = createDataset(currentMarket, currentTimeframe, targetTime);
            plot.setDataset(dataset);
            updateXAxisRange(dataset);
            updateYAxisRange(dataset);
            drawCurrentPriceDashLine(dataset);
            applyDateAxisFormat();
            chart.setTitle(getCurrentMarketSymbol() + " (Backtesting)");
        });
    }

    /** 실시간 모드로 복귀 */
    public void resetToRealtimeMode() {
        this.backtestTargetTime = null;
        liveCandle = null;
        liveCandleMinuteStart = null;
        startLiveTimer();
        connectWebSocket();
        refreshChart();
    }

    private String getCurrentMarketSymbol() {
        return currentMarket.replace("KRW-", "");
    }
}