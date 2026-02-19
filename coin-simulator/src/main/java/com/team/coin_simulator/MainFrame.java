package com.team.coin_simulator;

import javax.swing.*;
import java.awt.*;
import com.team.coin_simulator.Market_Panel.HistoryPanel;
import com.team.coin_simulator.Market_Order.OrderPanel;
import com.team.coin_simulator.chart.CandleChartPanel;
import com.team.coin_simulator.orderbook.OrderBookPanel;
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
    private CandleChartPanel chartPanel;
    private OrderBookPanel orderBookPanel; // 패널로 변경
    private OrderPanel orderPanel;
    
    private TimeController timeController;
    private String currentUserId = "jjh153702@naver.com"; // 로그인 시스템 구현 전 임시 사용자
    
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
        historyPanel.addCoinSelectionListener(this::onCoinSelected);
        
        // 2-2. 중앙: 차트 + 호가창 영역
        JPanel centerArea = new JPanel(new BorderLayout());
        
        // 차트 패널 (상단)
        chartPanel = new CandleChartPanel("BTC 차트");
        chartPanel.setPreferredSize(new Dimension(0, 500));
        
        // 호가창 패널 (하단) - 기본 BTC로 시작
        orderBookPanel = new OrderBookPanel("BTC");
        orderBookPanel.setPreferredSize(new Dimension(0, 350));
        orderBookPanel.setBorder(BorderFactory.createTitledBorder("호가창"));
        
        centerArea.add(chartPanel, BorderLayout.CENTER);
        centerArea.add(orderBookPanel, BorderLayout.SOUTH);
        
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
     * 코인 선택 이벤트 핸들러
     */
    private void onCoinSelected(String coinSymbol) {
        if (coinSymbol == null || coinSymbol.isEmpty()) {
            return;
        }
        
        System.out.println("[MainFrame] 코인 선택됨: " + coinSymbol);
        
        // 차트 업데이트
        chartPanel.changeMarket(coinSymbol);
        
        // 기존 호가창 패널 제거 및 새 호가창 생성
        updateOrderBookPanel(coinSymbol);
    }
    
    /**
     * 호가창 패널 업데이트
     */
    private void updateOrderBookPanel(String coinSymbol) {
        // 기존 호가창의 웹소켓 연결 종료
        if (orderBookPanel != null) {
            orderBookPanel.closeConnection();
        }
        
        // 새 호가창 패널 생성
        orderBookPanel = new OrderBookPanel(coinSymbol);
        orderBookPanel.setPreferredSize(new Dimension(0, 350));
        orderBookPanel.setBorder(BorderFactory.createTitledBorder(
            coinSymbol + " 호가창"
        ));
        
        // 레이아웃 업데이트
        Container parent = getContentPane();
        JPanel mainPanel = (JPanel) parent.getComponent(1); // BorderLayout.CENTER
        JPanel centerArea = (JPanel) mainPanel.getComponent(1); // 중앙 영역
        
        // 기존 호가창 제거하고 새 호가창 추가
        centerArea.remove(1); // SOUTH 위치 제거
        centerArea.add(orderBookPanel, BorderLayout.SOUTH);
        
        centerArea.revalidate();
        centerArea.repaint();
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
        
        // 호가창 연결 종료
        if (orderBookPanel != null) {
            orderBookPanel.closeConnection();
        }
        
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