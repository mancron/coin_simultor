package com.team.coin_simulator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.team.coin_simulator.Alerts.PriceAlertService;
import com.team.coin_simulator.Market_Order.OrderPanel;
import com.team.coin_simulator.Market_Panel.HistoryPanel;
import com.team.coin_simulator.backtest.BacktestSessionDialog;
import com.team.coin_simulator.backtest.BacktestTimeControlPanel;
import com.team.coin_simulator.backtest.BacktestTimeController;
import com.team.coin_simulator.backtest.CandleChartBacktestAdapter;
import com.team.coin_simulator.backtest.SessionManager;
import com.team.coin_simulator.chart.CandleChartPanel;
import com.team.coin_simulator.orderbook.OrderBookPanel;

import DAO.UpbitWebSocketDao;
import DTO.SessionDTO;
import Investment_details.Investment_details_MainPanel;
import databasetestdata.DownloadDatabase;


/**
 * 메인 프레임 - 전체 화면 전환 방식
 * 
 * 화면 구성:
 * 1. 거래 화면 (Trading View): 코인목록 + 차트 + 호가창 + 주문
 * 2. 투자내역 화면 (Investment View): 보유자산/투자손익/거래내역/미결
 * 
 * 상단 버튼으로 두 화면 전환
 */

public class MainFrame extends JFrame implements TimeController.TimeChangeListener {
    
    // 상단 패널
    private JPanel topPanel;
    private TimeControlPanel timeControlPanel;
    private JButton btnToggleView;
    
    // 메인 컨텐츠 (CardLayout)
    private CardLayout mainCardLayout;
    private JPanel mainContentPanel;
    
    // 거래 화면 컴포넌트
    private JPanel tradingPanel;
    private HistoryPanel historyPanel;
    private CandleChartPanel chartPanel;
    private OrderBookPanel orderBookPanel;
    private OrderPanel orderPanel;
    
    // 투자내역 화면 컴포넌트
    private Investment_details_MainPanel investmentPanel;
    
    // 상태 관리
    private TimeController timeController;

    private String currentUserId = "test_user1"; // 로그인 시스템 구현 전 임시 사용자
    private boolean isTradingView = true; // true: 거래화면, false: 투자내역
    
    // 카드 식별자
    private static final String CARD_TRADING = "TRADING";
    private static final String CARD_INVESTMENT = "INVESTMENT";
  
    //알림 감시자
    private PriceAlertService alertService;

    private BacktestTimeControlPanel backtestControlPanel;
    private CandleChartBacktestAdapter chartBacktestAdapter;
    
    private long currentSessionId = 0L;
    
    public MainFrame(String userId) {
        super("가상화폐 모의투자 시스템");
        this.currentUserId = userId;
        
        timeController = TimeController.getInstance();
        timeController.initialize(currentUserId);
        timeController.addTimeChangeListener(this);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        
        initComponents();
        initWebSocket();
        
        // 알림 서비스 (프레임 정보를 넘겨줌)
        alertService = new PriceAlertService(this);
        
        setVisible(true);
        syncMarketDataBackground();
    }
    
    private void syncMarketDataBackground() {
        new Thread(() -> {
            System.out.println("[MainFrame] 백그라운드에서 캔들 데이터 동기화를 시작합니다...");
            try {
                // 필요한 경우에만 최초 1회 실행
                // DownloadDatabase.importData(1); 
                
                // 평상시: DB에 있는 최신 시간부터 현재까지만 업데이트
                DownloadDatabase.updateData(1); 
                
                System.out.println("[MainFrame] 데이터 동기화 완료!");
                
                // 데이터 업데이트가 완료된 후 UI(차트나 리스트)를 새로고침해야 한다면
                // 반드시 EDT(Event Dispatch Thread)에서 실행되도록 invokeLater 사용
                SwingUtilities.invokeLater(() -> {
                    // 예시: 현재 선택된 코인의 차트를 다시 그리기
                	if (investmentPanel != null) {
                        investmentPanel.setSessionId(currentSessionId); // 주석 해제!
                        investmentPanel.refreshAll(); 
                    }
                    System.out.println("[MainFrame] UI 데이터 갱신 완료");
                });
                
            } catch (Exception e) {
                System.err.println("[MainFrame] 데이터 동기화 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 1. 상단 패널 (시간 제어 + 화면 전환 버튼)
        topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // 2. 메인 컨텐츠 영역 (CardLayout으로 화면 전환)
        mainCardLayout = new CardLayout();
        mainContentPanel = new JPanel(mainCardLayout);
        
        // 2-1. 거래 화면 생성
        tradingPanel = createTradingPanel();
        
        // 2-2. 투자내역 화면 생성
        // [수정] currentSessionId를 함께 전달하도록 변경
        investmentPanel = new Investment_details_MainPanel(currentUserId, currentSessionId);
        
        chartBacktestAdapter = new CandleChartBacktestAdapter(chartPanel, historyPanel);
        BacktestTimeController.getInstance().addTickListener(chartBacktestAdapter);
        
        // 카드에 추가
        mainContentPanel.add(tradingPanel, CARD_TRADING);
        mainContentPanel.add(investmentPanel, CARD_INVESTMENT);
        
        add(mainContentPanel, BorderLayout.CENTER);
        
        // 기본 화면: 거래 화면
        mainCardLayout.show(mainContentPanel, CARD_TRADING);
    }
    
    /**
     * 상단 패널 생성 (시간 제어 + 화면 전환 버튼)
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // 왼쪽: 백테스트 컨트롤 패널
        backtestControlPanel = new BacktestTimeControlPanel(this, currentUserId);
        panel.add(backtestControlPanel, BorderLayout.CENTER);
        
        // 오른쪽: 화면 전환 및 세션 버튼 묶음
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        // ==========================================
        // [추가] 세션 선택 버튼 
        // ==========================================
        JButton btnSelectSession = new JButton("백테스트 세션 선택");
        btnSelectSession.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnSelectSession.setBackground(new Color(155, 89, 182)); // 보라색 계열 (예시)
        btnSelectSession.setForeground(Color.WHITE);
        btnSelectSession.setFocusPainted(false);
        btnSelectSession.setPreferredSize(new Dimension(150, 35));
        btnSelectSession.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btnSelectSession.addActionListener(e -> {
            openSessionDialog(); // 방금 만든 메서드 호출!
        });
        buttonPanel.add(btnSelectSession);
        // ==========================================

        btnToggleView = new JButton("투자내역 보기");
        btnToggleView.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnToggleView.setForeground(Color.WHITE);
        btnToggleView.setBackground(new Color(52, 152, 219));
        btnToggleView.setFocusPainted(false);
        btnToggleView.setBorderPainted(false);
        btnToggleView.setPreferredSize(new Dimension(150, 35));
        btnToggleView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 호버 효과
        btnToggleView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnToggleView.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnToggleView.setBackground(new Color(52, 152, 219));
            }
        });
        
        btnToggleView.addActionListener(e -> toggleView());
        buttonPanel.add(btnToggleView);
        
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 거래 화면 생성
     */
    private JPanel createTradingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // 왼쪽: 코인 목록
        historyPanel = new HistoryPanel(currentUserId);
        historyPanel.setPreferredSize(new Dimension(350, 0));
        historyPanel.addCoinSelectionListener(this::onCoinSelected);
        
        // 중앙: 차트 + 호가창
        JPanel centerArea = new JPanel(new BorderLayout());
        
        chartPanel = new CandleChartPanel("BTC 차트");
        chartPanel.setPreferredSize(new Dimension(0, 500));
        
        orderBookPanel = new OrderBookPanel("BTC");
        orderBookPanel.setPreferredSize(new Dimension(0, 350));
        orderBookPanel.setBorder(BorderFactory.createTitledBorder("호가창"));
        
        centerArea.add(chartPanel, BorderLayout.CENTER);
        centerArea.add(orderBookPanel, BorderLayout.SOUTH);

        // 오른쪽: 주문 패널
        orderPanel = new OrderPanel(currentUserId);
        orderPanel.setPreferredSize(new Dimension(350, 0));
        
        // 조립
        panel.add(historyPanel, BorderLayout.WEST);
        panel.add(centerArea, BorderLayout.CENTER);
        panel.add(orderPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 화면 전환 (거래 화면 ↔ 투자내역 화면)
     */
    private void toggleView() {
        isTradingView = !isTradingView;
        
        if (isTradingView) {
            // 거래 화면으로 전환
            mainCardLayout.show(mainContentPanel, CARD_TRADING);
            btnToggleView.setText("투자내역 보기");
            btnToggleView.setBackground(new Color(52, 152, 219));
            
        } else {
            // 투자내역 화면으로 전환
            mainCardLayout.show(mainContentPanel, CARD_INVESTMENT);
            btnToggleView.setText("거래화면 보기");
            btnToggleView.setBackground(new Color(46, 204, 113));
            
            // 투자내역 데이터 새로고침
            investmentPanel.refreshAll();
        }
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
        
        // 호가창 업데이트
        updateOrderBookPanel(coinSymbol);
        
        //3. 주문 패널(OrderPanel)에도 알려주기!
        // ===================================================
        if (orderPanel != null) {
            // OrderPanel의 setSelectedCoin 메서드를 호출해서 코인 변경 지시
            orderPanel.setSelectedCoin(coinSymbol);
        }
    }
    
    /**
     * 호가창 패널 업데이트
     */
    private void updateOrderBookPanel(String coinSymbol) {
        if (orderBookPanel != null) {
            orderBookPanel.closeConnection();
        }
        
        orderBookPanel = new OrderBookPanel(coinSymbol);
        orderBookPanel.setPreferredSize(new Dimension(0, 350));
        orderBookPanel.setBorder(BorderFactory.createTitledBorder(
            coinSymbol + " 호가창"
        ));
        
        // tradingPanel의 centerArea 찾아서 업데이트
        JPanel centerArea = (JPanel) ((JPanel) tradingPanel.getComponent(1));
        centerArea.remove(1); // 기존 호가창 제거
        centerArea.add(orderBookPanel, BorderLayout.SOUTH);
        
        centerArea.revalidate();
        centerArea.repaint();
    }
    
    /**
     * 웹소켓 초기화
     */
    private void initWebSocket() {
        if (timeController.isRealtimeMode()) {
            UpbitWebSocketDao.getInstance().start();
        }
    }
    
    /**
     * [추가] 백테스팅 세션을 선택하고 UI에 적용하는 메서드
     */
    public void openSessionDialog() {
        // 1. 다이얼로그 띄우기
        BacktestSessionDialog dialog = new BacktestSessionDialog(this, currentUserId);
        dialog.setVisible(true); // 여기서 사용자가 선택할 때까지 대기

        // 2. 창이 닫힌 후 선택된 세션 가져오기
        SessionDTO selectedSession = dialog.getSelectedSession();

        if (selectedSession != null) {
            // 3. 전역 매니저 및 현재 프레임 변수 업데이트
            SessionManager.getInstance().setCurrentSession(selectedSession);
            this.currentSessionId = selectedSession.getSessionId();
            
            System.out.println("[MainFrame] 선택된 세션 ID: " + currentSessionId);

            // 4. 각 패널들에게 바뀐 세션 ID를 알려주고 새로고침
            if (investmentPanel != null) {
                // investmentPanel.setSessionId(currentSessionId); 
                investmentPanel.refreshAll(); // 새로고침
            }
            
            // 5. [수정됨] 시간 컨트롤러 초기화 및 백테스팅 엔진 시작!
            java.time.LocalDateTime startTime = selectedSession.getStartSimTime().toLocalDateTime();
            java.time.LocalDateTime currentSimTime = selectedSession.getCurrentSimTime().toLocalDateTime();
            java.time.LocalDateTime endTime = startTime.plusMonths(1); // 세션 종료 시각 계산

            // 5-1. TimeController: 실시간 웹소켓 종료 및 차트 기준 시간 변경
            timeController.switchToBacktestMode(currentSimTime); 
            
            // 5-2. BacktestTimeController: 1초마다 시계바늘을 움직이는 엔진 시작
            BacktestTimeController.getInstance().startSession(
                currentUserId, 
                currentSessionId, 
                startTime, 
                currentSimTime, 
                endTime
            );
        }
    }
    
    /**
     * 시간 변경 이벤트 처리
     */
    @Override
    public void onTimeChanged(java.time.LocalDateTime newTime, boolean isRealtime) {
        SwingUtilities.invokeLater(() -> {
            if (isRealtime) {
                System.out.println("[MainFrame] 실시간 모드로 전환됨");
                // 차트의 backtestTargetTime 초기화 후 실시간 복귀
                chartPanel.resetToRealtimeMode();
                UpbitWebSocketDao.getInstance().start();
            } else {
                System.out.println("[MainFrame] 백테스팅 모드로 전환됨: " + newTime);
                UpbitWebSocketDao.getInstance().close();
                chartPanel.loadHistoricalData(newTime);
            }
        });
    }
    
    /**
     * 리소스 정리
     */
    @Override
    public void dispose() {
        UpbitWebSocketDao.getInstance().close();
        
        if (orderBookPanel != null) {
            orderBookPanel.closeConnection();
        }
        
        DBConnection.close();
        super.dispose();
    }
    
    
    public static void main(String[] args) {
        // UI 룩앤필 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // UI 생성 및 실행
        SwingUtilities.invokeLater(() -> {
            new MainFrame("test_user1");
        });
    }
}