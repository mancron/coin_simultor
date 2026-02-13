package com.team.coin_simulator;

import javax.swing.*;
import java.awt.*;
import com.team.coin_simulator.Market_Panel.HistoryPanel;
import com.team.coin_simulator.Market_Order.OrderPanel;
import com.team.coin_simulator.chart.CandleChartPanel;
import com.team.coin_simulator.orderbook.OrderBookFrame;
import DAO.UpbitWebSocketDao;

/**
 * 메인 프레임 - 전체 레이아웃 통합
 * 
 * 레이아웃 구조:
 * ┌──────────────────────────────────────────────────┐
 * │  TimeControlPanel (시간 제어)                     │
 * ├──────────┬──────────────────────┬────────────────┤
 * │          │                      │                │
 * │ History  │   CandleChartPanel   │  OrderPanel    │
 * │ Panel    │   (차트)              │  (주문)        │
 * │ (코인목록)│                      │                │
 * │          ├──────────────────────┤                │
 * │          │   OrderBookPanel     │                │
 * │          │   (호가창)            │                │
 * └──────────┴──────────────────────┴────────────────┘
 */
public class MainFrame extends JFrame implements TimeController.TimeChangeListener {
    
    private TimeControlPanel timeControlPanel;
    private HistoryPanel historyPanel;
    private CandleChartPanel chartPanel; // JPanel로 변경됨
    private OrderBookFrame orderBookFrame; // 독립 창으로 관리
    private OrderPanel orderPanel;
    
    private TimeController timeController;
    private String currentUserId = "user_01"; // 로그인 시스템 구현 전 임시 사용자
    
    public MainFrame() {
        super("가상화폐 모의투자 시스템");
        
        // TimeController 초기화
        timeController = TimeController.getInstance();
        timeController.initialize(currentUserId);
        timeController.addTimeChangeListener(this);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        
        initComponents();
        initWebSocket();
        
        setVisible(true);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 1. 상단: 시간 제어 패널
        timeControlPanel = new TimeControlPanel();
        add(timeControlPanel, BorderLayout.NORTH);
        
        // 2. 중앙: 메인 컨텐츠 영역 (3분할)
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 2-1. 왼쪽: 코인 목록 패널 (350px 고정)
        historyPanel = new HistoryPanel();
        historyPanel.setPreferredSize(new Dimension(350, 0));
        
        // 코인 선택 이벤트 리스너 추가
        // HistoryPanel에서 코인이 선택되면 차트 업데이트
        // (HistoryPanel에 리스너 메커니즘 추가 필요)
        
        // 2-2. 중앙: 차트 + 호가창 영역
        JPanel centerArea = new JPanel(new BorderLayout());
        
        // 차트 패널 (상단 - JPanel로 변경됨)
        chartPanel = new CandleChartPanel("BTC 차트");
        chartPanel.setPreferredSize(new Dimension(0, 500));
        
        // 호가창 플레이스홀더 (하단)
        JPanel orderBookPlaceholder = createOrderBookPlaceholder();
        
        centerArea.add(chartPanel, BorderLayout.CENTER);
        centerArea.add(orderBookPlaceholder, BorderLayout.SOUTH);
        
        // 2-3. 오른쪽: 주문 패널 (350px 고정)
        orderPanel = new OrderPanel();
        orderPanel.setPreferredSize(new Dimension(350, 0));
        
        // 메인 패널 조립
        mainPanel.add(historyPanel, BorderLayout.WEST);
        mainPanel.add(centerArea, BorderLayout.CENTER);
        mainPanel.add(orderPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * 호가창 플레이스홀더 생성
     */
    private JPanel createOrderBookPlaceholder() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 350));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createTitledBorder("호가창"));
        
        JButton btnShowOrderBook = new JButton("호가창 열기 (새 창)");
        btnShowOrderBook.setPreferredSize(new Dimension(200, 40));
        btnShowOrderBook.addActionListener(e -> {
            String selectedCoin = historyPanel.getSelectedCoin();
            if (selectedCoin != null) {
                showOrderBook(selectedCoin);
            } else {
                JOptionPane.showMessageDialog(this, "코인을 먼저 선택해주세요.");
            }
        });
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(new Color(250, 250, 250));
        btnPanel.add(btnShowOrderBook);
        
        panel.add(btnPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 웹소켓 초기화 (실시간 시세용)
     */
    private void initWebSocket() {
        // 실시간 모드에서만 웹소켓 연결
        if (timeController.isRealtimeMode()) {
            UpbitWebSocketDao.getInstance().start();
        }
    }
    
    /**
     * 호가창 표시 (별도 창)
     */
    private void showOrderBook(String coinSymbol) {
        if (orderBookFrame != null) {
            orderBookFrame.dispose();
        }
        orderBookFrame = new OrderBookFrame(coinSymbol);
        orderBookFrame.setVisible(true);
    }
    
    /**
     * 시간 변경 이벤트 처리
     */
    @Override
    public void onTimeChanged(java.time.LocalDateTime newTime, boolean isRealtime) {
        SwingUtilities.invokeLater(() -> {
            if (isRealtime) {
                // 실시간 모드로 전환됨
                System.out.println("[MainFrame] 실시간 모드로 전환됨");
                
                // 웹소켓 재연결
                UpbitWebSocketDao.getInstance().start();
                
                // 각 패널들을 실시간 데이터로 갱신
                // (HistoryPanel, OrderPanel은 웹소켓 리스너로 자동 갱신됨)
                
            } else {
                // 백테스팅 모드로 전환됨
                System.out.println("[MainFrame] 백테스팅 모드로 전환됨: " + newTime);
                
                // 웹소켓 중단
                UpbitWebSocketDao.getInstance().close();
                
                // 과거 데이터 로드 및 각 패널 업데이트
                loadHistoricalData(newTime);
            }
        });
    }
    
    /**
     * 과거 데이터 로드 (백테스팅용)
     */
    private void loadHistoricalData(java.time.LocalDateTime targetTime) {
        System.out.println("[MainFrame] 과거 데이터 로드 중: " + targetTime);
        
        // 차트 패널 업데이트
        chartPanel.loadHistoricalData(targetTime);
        
        // TODO: HistoryPanel, OrderPanel도 과거 데이터로 업데이트
        // 예시:
        // DAO.HistoricalDataDAO dao = new DAO.HistoricalDataDAO();
        // Map<String, TickerDto> tickers = dao.getTickersAtTime(targetTime);
        // historyPanel.updateWithHistoricalData(tickers);
    }
    
    /**
     * 애플리케이션 종료 시 정리 작업
     */
    @Override
    public void dispose() {
        // 웹소켓 연결 종료
        UpbitWebSocketDao.getInstance().close();
        
        // DB 커넥션 풀 종료
        DBConnection.close();
        
        super.dispose();
    }
    
    public static void main(String[] args) {
        // Swing Look and Feel 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new MainFrame();
        });
    }
}