package com.team.coin_simulator.Market_Panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;



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
    public void addNewCoin(int type, String coinName, String price, String fluc) {
        CoinRowPanel row = new CoinRowPanel(coinName, price, fluc);
        
        // 1. UI 패널에 추가
        switch (type) {
            case 0: allCoinPanel.add(row); break;
            case 1: ownedCoinPanel.add(row); break;
            case 2: interestCoinPanel.add(row); break;
        }
        
        // 2. 데이터 맵에 추가 (리스트 형태로 관리)
        // coinMap에 해당 코인 이름이 없으면 새 리스트 생성, 있으면 기존 리스트 가져옴
        coinMap.computeIfAbsent(coinName, k -> new ArrayList<>()).add(row);
        
        // UI 갱신 (추가된 컴포넌트 즉시 반영)
        revalidate();
        repaint();
    }
    
   
    public void updateCoinPrice(String symbol, String newPrice, String newFluc) {
        if (coinMap.containsKey(symbol)) {
            List<CoinRowPanel> rows = coinMap.get(symbol);
            
            for (CoinRowPanel row : rows) {
                // 색상 인자(Color.RED) 삭제 -> RowPanel이 알아서 판단함
                // newFluc에는 "2.5", "-1.2" 같은 순수 숫자 문자열이 들어감
                row.updateData(newPrice, newFluc); 
            }
        }
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("코인 시뮬레이터");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 패널 생성
        HistoryPanel historyPanel = new HistoryPanel();
        frame.add(historyPanel);
        
        frame.pack();
        frame.setMinimumSize(historyPanel.getMinimumSize()); // 패널 최소 사이즈 적용
        frame.setSize(400, 600);
        frame.setVisible(true);

        // --- 1. 초기 데이터 세팅 ---
        // 전체 탭(0)에 추가
        historyPanel.addNewCoin(0, "BTC", "95,000,000", "0.0");
        historyPanel.addNewCoin(0, "ETH", "3,500,000", "0.0");
        historyPanel.addNewCoin(0, "XRP", "800", "0.0");
        
        // 관심 탭(2)에 BTC 중복 추가 (테스트용)
        historyPanel.addNewCoin(2, "BTC", "95,000,000", "0.0"); 
        
        //탭추가
        // --- 2. 외부 스레드에서 가격 업데이트 관리 ---
        Thread simulationThread = new Thread(() -> {
            double btcPrice = 95000000;
            double ethPrice = 3500000;
            
            while (true) {
                try {
                    // 시뮬레이션 로직 (가격 변동)
                    btcPrice += (Math.random() * 100000 - 50000); //getprice(String coinname)으로 변경
                    ethPrice += (Math.random() * 5000 - 2500);
                    
                    // 변수 캡쳐를 위해 final 처럼 사용해야 하므로 임시 변수 할당
                    String currentBtc = String.format("%,.0f", btcPrice);
                    String currentEth = String.format("%,.0f", ethPrice);

                    // Swing UI 업데이트 요청 (invokeLater 필수)
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        // 패널에게 "BTC랑 ETH 가격 바꿔!" 라고 명령
                        historyPanel.updateCoinPrice("BTC", currentBtc, "0.5");
                        historyPanel.updateCoinPrice("ETH", currentEth, "-0.2");
                    });

                    Thread.sleep(1000); // 1초 대기
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        simulationThread.setDaemon(true);
        simulationThread.start();
    }
}