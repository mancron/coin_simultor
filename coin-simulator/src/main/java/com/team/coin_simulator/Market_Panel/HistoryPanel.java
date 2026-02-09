package com.team.coin_simulator.Market_Panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import com.team.coin_simulator.CoinConfig;
import com.team.coin_simulator.Market_Order.OrderPanel;

import okhttp3.OkHttpClient;
import okhttp3.Request;



//사용할떄 
//frame.setMinimumSize(panel.getMinimumSize()); 로 최소 크기 설정필요
//addNewCoin(int 탭번호,String 코인이름,String 가격,String 등락폭) 0:코인목록,1:보유코인,2:관심목록
//updateCoinPrice(String symbol, String newPrice, String newFluc)newFluc 는 숫자로 된 문자열 이어야함
//맨밑 main문에 예시 있음


public class HistoryPanel extends JPanel {
    
    // UI 컴포넌트 선언
    JPanel coinHeadPanel, tabPanel;
    JScrollPane scrollPanel;
    
    // 리스트 전환을 위한 컴포넌트
    JPanel mainListPanel; // 카드를 담을 패널
    CardLayout cardLayout; // 레이아웃 매니저
    
    // 각 탭별 패널 (전체, 보유, 관심)
    JPanel allCoinPanel, ownedCoinPanel, interestCoinPanel;
  

    // 데이터 관리: 코인 심볼 하나에 여러 개의 RowPanel(전체탭용, 관심탭용 등)이 매핑될 수 있음
    private HashMap<String, List<CoinRowPanel>> coinMap = new HashMap<>();
    private List<TabButton> tabButtons = new ArrayList<>();
    
    //OrderPanel과 연결
    private OrderPanel orderPanel;

    
    public HistoryPanel(OrderPanel orderPanel) {
    	this.orderPanel = orderPanel;
    	
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setMinimumSize(new Dimension(300, 500));
        
        // 1. 탭 버튼 패널 구성
        tabPanel = new JPanel(new GridLayout(1, 3, 5, 3)); // GridLayout(row, col, hGap, vGap) : 버튼 사이 간격 5px 추가
        tabPanel.setBackground(Color.WHITE); // 배경색을 흰색으로 변경하여 버튼 돋보이게 함
        tabPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        
        
        TabButton btnAll = new TabButton("전체 코인");
        TabButton btnOwned = new TabButton("보유 코인");
        TabButton btnInterest = new TabButton("관심 코인");
        
        tabButtons.add(btnAll);
        tabButtons.add(btnOwned);
        tabButtons.add(btnInterest);
        
        tabPanel.add(btnAll);
        tabPanel.add(btnOwned);
        tabPanel.add(btnInterest);
        
        // 2. 헤더 패널 구성 (고정 영역)
        coinHeadPanel = new JPanel(new GridLayout(1, 3));
        coinHeadPanel.setBackground(Color.white);
        coinHeadPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        coinHeadPanel.add(new JLabel("코인명", SwingConstants.CENTER));
        coinHeadPanel.add(new JLabel("현재가", SwingConstants.CENTER));
        coinHeadPanel.add(new JLabel("전일대비", SwingConstants.CENTER));
        
        // 3. 리스트 컨테이너 구성 (CardLayout 적용)
        cardLayout = new CardLayout();
        mainListPanel = new JPanel(cardLayout);
        
        // 3-1. 각 탭에 들어갈 패널 생성 및 설정
        allCoinPanel = createListPanel();
        ownedCoinPanel = createListPanel();
        interestCoinPanel = createListPanel();
        
        // 3-2. CardLayout에 패널 추가 (식별자 부여)
        mainListPanel.add(allCoinPanel, "ALL");
        mainListPanel.add(ownedCoinPanel, "OWNED");
        mainListPanel.add(interestCoinPanel, "INTEREST");
        
        // 4. 스크롤 패널에 mainListPanel 장착
        scrollPanel = new JScrollPane(mainListPanel);
        scrollPanel.setBorder(null);
        
        // 5. 버튼 이벤트 리스너 연결 (화면 전환 로직)
        btnAll.addActionListener(e -> cardLayout.show(mainListPanel, "ALL"));
        btnOwned.addActionListener(e -> cardLayout.show(mainListPanel, "OWNED"));
        btnInterest.addActionListener(e -> cardLayout.show(mainListPanel, "INTEREST"));
        
        
        setupTabActions(btnAll, "ALL");
        setupTabActions(btnOwned, "OWNED");
        setupTabActions(btnInterest, "INTEREST");
        
        // 6. 메인 패널에 부착
        add(tabPanel, BorderLayout.BEFORE_FIRST_LINE); // 상단 탭
        add(coinHeadPanel, BorderLayout.NORTH);        // 헤더
        add(scrollPanel, BorderLayout.CENTER);         // 중앙 리스트
        
        // 테스트 데이터 추가

        
    }
    
    
    
    
    private void updateTabStyle(TabButton selectedBtn) {
        for (TabButton btn : tabButtons) {
            // 클릭된 버튼이면 true, 아니면 false 전달
            if (btn == selectedBtn) {
                btn.setSelected(true);
            } else {
                btn.setSelected(false);
            }
        }
    }
    
    // 리스트 패널 생성 헬퍼 메소드 (코드 중복 제거)
    private JPanel createListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.white);
        return panel;
    }
  
    
    private void setupTabActions(TabButton btn, String cardName) {
        btn.addActionListener(e -> {
            cardLayout.show(mainListPanel, cardName);
            updateTabStyle(btn);
        });
    }


    
    // 특정 패널에 코인을 추가하는 메소드로 변경
    /**
     * 외부에서 코인을 추가할 때 호출하는 메소드
     * @param type 0: 전체, 1: 보유, 2: 관심 (어디 패널에 넣을지 결정)
     */
    public void addNewCoin(int type, String code, String price, String fluc) {
        // 1. CoinConfig에서 한글 이름 가져오기
        String krName = CoinConfig.COIN_INFO.getOrDefault(code, code);

        // 2. [핵심] HTML 태그를 사용하여 두 줄로 만들기
        // <br>: 줄바꿈, <font>: 폰트 스타일 (코드는 조금 작고 회색으로)
        String displayName = "<html><center>" + krName + "<br><font size='3' color='gray'>" + code + "</font></center></html>";
        
        // 3. 패널 생성 시에는 'displayName'(화면용)을 전달
        CoinRowPanel row = new CoinRowPanel(displayName, price, fluc);
        
        //OrderPanel과 연결하기 위해 클릭 이벤트 리스너 추가
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (orderPanel != null) {
                    // 클릭된 row의 현재가와 코드를 OrderPanel로 전달
                    orderPanel.setSelectedCoin(code, row.getPrice());
                }
            }
        });
        
        // 4. UI 탭에 추가
        switch (type) {
            case 0: allCoinPanel.add(row); break;
            case 1: ownedCoinPanel.add(row); break;
            case 2: interestCoinPanel.add(row); break;
        }
        
        // 5. [중요] 데이터 맵의 키는 반드시 'code'(BTC)를 사용해야 웹소켓과 연결됨
        coinMap.computeIfAbsent(code, k -> new ArrayList<>()).add(row);
        
        revalidate();
        repaint();
    }
    
   
    public void updateCoinPrice(String symbol, String newPrice, String newFluc) {
        if (coinMap.containsKey(symbol)) {
            List<CoinRowPanel> rows = coinMap.get(symbol);
            for (CoinRowPanel row : rows) {
                row.updateData(newPrice, newFluc); 
            }
        }
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("코인 시뮬레이터");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 2));//반반씩 배치
        
        //주문 패널 먼저 생성
        OrderPanel orderPanel = new OrderPanel();
        
        // 주문 패널을 인자로 주면서 히스토리 패널 생성
        HistoryPanel historyPanel = new HistoryPanel(orderPanel);
        frame.add(historyPanel);
        
        // DAO 생성 (패널 연결)
        UpbitWebSocketDao webSocketDao = new UpbitWebSocketDao(historyPanel);
        
        //메인 프레임 있으면 없어됨
        frame.pack();
        frame.setMinimumSize(historyPanel.getMinimumSize()); 
        frame.setSize(400, 600);
        frame.setVisible(true); // 창을 먼저 띄우고 데이터 연결하는 것이 보기 좋습니다.
        //메인 프레임 있으면 없어됨 
        
        // --- 1. 초기 데이터 세팅 ---
        // (웹소켓에서 코드가 "BTC"로 변환되어 오므로, 여기서도 "BTC"로 등록해야 매칭됨)
        for (String code : CoinConfig.getCodes()) {
            // addNewCoin(탭번호, 코드, 가격, 등락률)
            historyPanel.addNewCoin(0, code, "연결 중...", "0.00");
        }
        
        
        // --- 2. 웹소켓 연결 ---
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("wss://api.upbit.com/websocket/v1")
                .build();
                
        // 연결 시작 (백그라운드에서 계속 돔)
        client.newWebSocket(request, webSocketDao);
        
        // shutdown() 코드는 삭제했습니다.
    }
}