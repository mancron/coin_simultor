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

import okhttp3.OkHttpClient;
import okhttp3.Request;



//사용할떄 
//frame.setMinimumSize(panel.getMinimumSize()); 로 최소 크기 설정필요
//addNewCoin(int 탭번호,String 코인이름,String 가격,String 등락폭) 0:코인목록,1:보유코인,2:관심목록
//updateCoinPrice(String symbol, String newPrice, String newFluc)newFluc 는 숫자로 된 문자열 이어야함
//맨밑 main문에 예시 있음


public class HistoryPanel extends JPanel {
	
	String loginUser = "user_01";
    
    // UI 컴포넌트 선언
    JPanel coinHeadPanel, tabPanel;
    JScrollPane scrollPanel;
    
    // 리스트 전환을 위한 컴포넌트
    JPanel mainListPanel; // 카드를 담을 패널
    CardLayout cardLayout; // 레이아웃 매니저
    private AssetDAO assetDAO = new AssetDAO();
    // 각 탭별 패널 (전체, 보유, 관심)
    JPanel allCoinPanel, ownedCoinPanel, interestCoinPanel;

    // 현재 선택된 코인의 심볼 저장
    private String selectedCoinSymbol = null;
    
    
    private CoinRowPanel selectedRowPanel = null; // 현재 선택된 패널 객체 저장
    private final Color SELECTED_COLOR = new Color(240, 240, 240); // 선택 시 어두워질 색상
    private final Color DEFAULT_COLOR = Color.WHITE; // 기본 배경색

    // 데이터 관리: 코인 심볼 하나에 여러 개의 RowPanel(전체탭용, 관심탭용 등)이 매핑될 수 있음
    private HashMap<String, List<CoinRowPanel>> coinMap = new HashMap<>();
    private List<TabButton> tabButtons = new ArrayList<>();

    
    public HistoryPanel() {
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
        coinHeadPanel = new JPanel(new GridLayout(1, 4));
        coinHeadPanel.setBackground(Color.white);
        coinHeadPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        coinHeadPanel.add(new JLabel("코인명", SwingConstants.CENTER));
        coinHeadPanel.add(new JLabel("현재가", SwingConstants.CENTER));
        coinHeadPanel.add(new JLabel("전일대비", SwingConstants.CENTER));
        coinHeadPanel.add(new JLabel("거래대금", SwingConstants.CENTER)); 
        
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
        
        
        setupTabActions(btnAll, "ALL");
        setupTabActions(btnOwned, "OWNED");
        setupTabActions(btnInterest, "INTEREST");
        
        // 6. 메인 패널에 부착
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(tabPanel, BorderLayout.NORTH);      // 탭을 맨 위로
        topContainer.add(coinHeadPanel, BorderLayout.CENTER); // 헤더를 그 아래로
        
        add(topContainer, BorderLayout.NORTH); // 묶은 패널을 북쪽에 배치
        add(scrollPanel, BorderLayout.CENTER);         // 중앙 리스트
        
        
        //코인 데이터 추가
        initDataAndWebSocket();
        
    }
    
    
    public String getSelectedCoin() {
        return selectedCoinSymbol;
    }
    
    private void selectCoin(String symbol, CoinRowPanel clickedPanel) {
        this.selectedCoinSymbol = symbol;
        System.out.println("선택된 코인: " + symbol); // 디버깅용
        
        if (selectedRowPanel != null) {
            selectedRowPanel.setBackground(DEFAULT_COLOR);
        }

        // 2. 현재 클릭된 패널의 배경색 변경 및 저장
        this.selectedCoinSymbol = symbol;
        this.selectedRowPanel = clickedPanel;
        
        if (selectedRowPanel != null) {
            selectedRowPanel.setBackground(SELECTED_COLOR);
            selectedRowPanel.setOpaque(true); // 배경색이 보이도록 설정
        }

        // UI 갱신
        revalidate();
        repaint();
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
    
    private void updateHeadPanel(String type) {
        // 1. 기존 컴포넌트(라벨) 모두 제거
        coinHeadPanel.removeAll(); 

        // 2. 탭 타입에 따른 컬럼 배열 설정
        String[] headers;
        if (type.equals("OWNED")) {
            // [보유 코인] 탭일 때 보여줄 컬럼
            headers = new String[]{"코인명", "평단가", "수익률", "보유갯수"};
        } else {
            // [전체/관심 코인] 탭일 때 보여줄 컬럼
            headers = new String[]{"코인명", "현재가", "전일대비", "거래대금"};
        }

        // 3. 라벨 생성 및 추가
        for (String title : headers) {
            coinHeadPanel.add(new JLabel(title, SwingConstants.CENTER));
        }

        // 4. UI 갱신 (필수)
        coinHeadPanel.revalidate();
        coinHeadPanel.repaint();
    }
    
    private void setupTabActions(TabButton btn, String cardName) {
        btn.addActionListener(e -> {
            // 1. 메인 리스트 화면 전환
            cardLayout.show(mainListPanel, cardName);
            
            // 2. 탭 버튼 스타일 업데이트
            updateTabStyle(btn);
            
            // 3. [추가됨] 상단 헤더 패널 내용 변경
            updateHeadPanel(cardName); 
        });
    }
    
    // 리스트 패널 생성 헬퍼 메소드 (코드 중복 제거)
    private JPanel createListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.white);
        return panel;
    }
  
    
    
    //DAO 를 이용해서 데이터를 DTO 에 넣고 그걸 패널에서 보여줘야함
    public void loadOwnedAssets() {
        // 1. 기존 패널 초기화
        ownedCoinPanel.removeAll();
        
        // 2. DB에서 내 자산 조회
        List<AssetDTO> assets = assetDAO.getUserAssets(loginUser); // loginUser 변수 활용

        // 3. 자산이 없을 경우 처리
        if (assets.isEmpty()) {
            ownedCoinPanel.add(new JLabel("보유 중인 코인이 없습니다.", SwingConstants.CENTER));
        } else {
            // 4. 자산별 패널 생성 및 등록
            for (AssetDTO asset : assets) {
                String symbol = asset.getCurrency(); // "BTC"
                String krName = CoinConfig.COIN_INFO.getOrDefault(symbol, symbol);
                
                // 화면 표시용 이름
                String displayName = "<html><center>" + krName + "<br><font size='3' color='gray'>" + symbol + "</font></center></html>";

                // AssetRowPanel 생성
                AssetRowPanel row = new AssetRowPanel(displayName, asset);
                
                // [핵심] 1. UI에 추가
                ownedCoinPanel.add(row);
                
                // [핵심] 2. coinMap에 추가해야 웹소켓으로 실시간 가격 반영됨
                // 키값은 반드시 심볼("BTC")이어야 함
                coinMap.computeIfAbsent(symbol, k -> new ArrayList<>()).add(row);
            }
        }
        
        ownedCoinPanel.revalidate();
        ownedCoinPanel.repaint();
    }

    
    // 특정 패널에 코인을 추가하는 메소드로 변경
    /**
     * 외부에서 코인을 추가할 때 호출하는 메소드
     * @param type 0: 전체, 1: 관심 (어디 패널에 넣을지 결정)
     */
    public void addNewCoin(int type, String code, String price, String fluc,String Acc_trade_price) {
        // 1. CoinConfig에서 한글 이름 가져오기
        String krName = CoinConfig.COIN_INFO.getOrDefault(code, code);

        // 2. [핵심] HTML 태그를 사용하여 두 줄로 만들기
        // <br>: 줄바꿈, <font>: 폰트 스타일 (코드는 조금 작고 회색으로)
        String displayName = "<html><center>" + krName + "<br><font size='3' color='gray'>" + code + "</font></center></html>";
        
        // 3. 패널 생성 시에는 'displayName'(화면용)을 전달
        CoinRowPanel row = new CoinRowPanel(displayName, price, fluc,Acc_trade_price);
        row.setBackground(DEFAULT_COLOR);
        
        // --- 클릭 이벤트 리스너 추가 ---
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
            	selectCoin(code,row); // 클릭 시 해당 코인의 code(심볼)를 저장
            }
        });
        
        // 4. UI 탭에 추가
        switch (type) {
            case 0: allCoinPanel.add(row); break;
            case 1: interestCoinPanel.add(row); break;
        }
        
        // 5. [중요] 데이터 맵의 키는 반드시 'code'(BTC)를 사용해야 웹소켓과 연결됨
        coinMap.computeIfAbsent(code, k -> new ArrayList<>()).add(row);
        
        revalidate();
        repaint();
    }
    

    private void initDataAndWebSocket() {
        UpbitWebSocketDao webSocketDao = new UpbitWebSocketDao(this);
        
        // 전체 코인 목록 생성
        for (String code : CoinConfig.getCodes()) {
        	addNewCoin(0, code, "Loading...", "0.00", "0");
        }

        // 웹소켓 연결
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("wss://api.upbit.com/websocket/v1")
                .build();
        client.newWebSocket(request, webSocketDao);
        loadOwnedAssets();
    }
    
   
    public void updateCoinPrice(String symbol, String newPrice, String newFluc ,String Acc_trade_price) {
        if (coinMap.containsKey(symbol)) {
            List<CoinRowPanel> rows = coinMap.get(symbol);
            for (CoinRowPanel row : rows) {
                row.updateData(newPrice, newFluc,Acc_trade_price); 
            }
        }
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("코인 리스트 판넬");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 패널 생성
        HistoryPanel historyPanel = new HistoryPanel();
        frame.add(historyPanel);
        
        //메인 프레임 있으면 없어됨
        frame.pack();
        frame.setMinimumSize(historyPanel.getMinimumSize()); 
        frame.setSize(400, 600);
        frame.setVisible(true);
        

    }
}