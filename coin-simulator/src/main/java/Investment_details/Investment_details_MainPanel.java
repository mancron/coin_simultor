package Investment_details;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Investment_details.Asset.Asset_MainPanel;
import Investment_details.OpenOrder.OpenOrder_MainPanel;
import Investment_details.history.History_MainPanel;
import Investment_details.profitloss.ProfitLoss_MainPanel;

/**
 * 투자내역 통합 메인 패널
 * 
 * 레이아웃:
 * ┌──────────────────────────────────────────────┐
 * │  Investment_details_NavigationPanel (NORTH)  │ ← 탭 버튼
 * ├──────────────────────────────────────────────┤
 * │  CardLayout 패널 (CENTER)                    │
 * │  ┌─────────────────────────────────┐        │
 * │  │ Asset_MainPanel (보유자산)       │        │
 * │  │ ProfitLoss_MainPanel (투자손익)  │        │
 * │  │ History_MainPanel (거래내역)     │        │
 * │  │ OpenOrder_MainPanel (미체결)     │        │
 * │  └─────────────────────────────────┘        │
 * └──────────────────────────────────────────────┘
 */
public class Investment_details_MainPanel extends JPanel {
    
    private Investment_details_NavigationPanel navPanel;
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    private Asset_MainPanel assetPanel;
    private ProfitLoss_MainPanel profitLossPanel;
    private History_MainPanel historyPanel;
    private OpenOrder_MainPanel openOrderPanel;
    
    private final String userId;
    
    // 카드 식별자
    private static final String CARD_ASSET = "ASSET";
    private static final String CARD_PROFIT_LOSS = "PROFIT_LOSS";
    private static final String CARD_HISTORY = "HISTORY";
    private static final String CARD_OPEN_ORDER = "OPEN_ORDER";
    
    public Investment_details_MainPanel(String userId) {
        this.userId = userId;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 1. 탭 네비게이션 패널
        navPanel = new Investment_details_NavigationPanel();
        
        // 2. 컨텐츠 영역 (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        
        // 3. 각 탭 패널 생성 및 추가
        assetPanel = new Asset_MainPanel(userId);
        profitLossPanel = new ProfitLoss_MainPanel(userId);
        historyPanel = new History_MainPanel(userId);
        openOrderPanel = new OpenOrder_MainPanel(userId);
        
        contentPanel.add(assetPanel, CARD_ASSET);
        contentPanel.add(profitLossPanel, CARD_PROFIT_LOSS);
        contentPanel.add(historyPanel, CARD_HISTORY);
        contentPanel.add(openOrderPanel, CARD_OPEN_ORDER);
        
        // 4. 레이아웃 조립
        add(navPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        // 5. 탭 리스너 등록
        setupTabListeners();
        
        // 6. 기본 화면 표시 (보유자산)
        cardLayout.show(contentPanel, CARD_ASSET);
    }
    
    /**
     * 탭 리스너 설정
     */
    private void setupTabListeners() {
        // 보유자산 탭
        navPanel.addAssetTabListener(e -> {
            cardLayout.show(contentPanel, CARD_ASSET);
            assetPanel.initAssetData(); // 데이터 새로고침
        });
        
        // 투자손익 탭
        navPanel.addProfitLossTabListener(e -> {
            cardLayout.show(contentPanel, CARD_PROFIT_LOSS);
            profitLossPanel.refresh(30); // 최근 30일 데이터 새로고침
        });
        
        // 거래내역 탭
        navPanel.addHistoryTabListener(e -> {
            cardLayout.show(contentPanel, CARD_HISTORY);
            historyPanel.refresh(); // 데이터 새로고침
        });
        
        // 미체결 탭
        navPanel.addOpenOrderTabListener(e -> {
            cardLayout.show(contentPanel, CARD_OPEN_ORDER);
            openOrderPanel.refresh(); // 데이터 새로고침
        });
    }
    
    /**
     * 전체 데이터 새로고침 (주문 체결 후 등)
     */
    public void refreshAll() {
        assetPanel.initAssetData();
        profitLossPanel.refresh(30);
        historyPanel.refresh();
        openOrderPanel.refresh();
    }
    
    /**
     * 독립 실행 테스트
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("투자내역 통합 패널 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            
            Investment_details_MainPanel panel = new Investment_details_MainPanel("user_01");
            frame.add(panel);
            frame.setVisible(true);
            
            // 웹소켓 시작 (실시간 시세용)
            DAO.UpbitWebSocketDao.getInstance().start();
        });
    }
}