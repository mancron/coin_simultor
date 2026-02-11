package Investment_details.Asset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import DAO.AssetDAO;
import DAO.UpbitWebSocketDao;
import DTO.AssetDTO;
import DTO.MyAssetStatusDTO;

// [변경 1] TickerListener 구현 (시세 업데이트 수신)
public class Asset_MainPanel extends JPanel implements UpbitWebSocketDao.TickerListener {

    private Asset_SummaryPanel summaryPanel;
    private Asset_PortfolioChartPanel chartPanel;
    private Assets_TablePanel tablePanel;
    
    private AssetDAO assetDAO;
    private String userId;
    
    // [변경 2] 자산 리스트를 멤버 변수로 보관 (매번 DB 조회하지 않기 위해)
    private List<MyAssetStatusDTO> myAssetList = new ArrayList<>();
    private BigDecimal krwBalance = BigDecimal.ZERO;

    public Asset_MainPanel(String userId) {
        this.userId = userId;
        this.assetDAO = new AssetDAO();
        
        setLayout(new BorderLayout(0, 10)); 
        setBackground(Color.WHITE);

        summaryPanel = new Asset_SummaryPanel();
        chartPanel = new Asset_PortfolioChartPanel();
        tablePanel = new Assets_TablePanel();

        add(summaryPanel, BorderLayout.NORTH); 

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartPanel, tablePanel);
        splitPane.setDividerLocation(400); 
        splitPane.setResizeWeight(0.4);   
        splitPane.setBorder(null);        
        splitPane.setBackground(Color.WHITE);
        add(splitPane, BorderLayout.CENTER);
        
        // [변경 3] 리스너 등록 (나한테도 시세 정보 줘!)
        UpbitWebSocketDao.getInstance().addListener(this);
        
        // 초기 데이터 로드 (DB에서 가져오기)
        initAssetData();
    }
    
    // 1. 초기 데이터 로드 (DB 접근은 여기서만)
    public void initAssetData() {
        myAssetList.clear();
        List<AssetDTO> dbAssets = assetDAO.getAllAssets(userId);
        
        for (AssetDTO asset : dbAssets) {
            if ("KRW".equalsIgnoreCase(asset.getCurrency())) {
                krwBalance = asset.getTotalAmount();
                continue;
            }
            
            MyAssetStatusDTO dto = new MyAssetStatusDTO();
            dto.setCurrency(asset.getCurrency());
            dto.setBalance(asset.getTotalAmount()); 
            dto.setAvgPrice(asset.getAvgBuyPrice());
            // 초기 가격은 평단가로 설정 (이후 웹소켓으로 갱신)
            dto.setCurrentPrice(asset.getAvgBuyPrice()); 
            dto.setTotalValue(dto.getBalance().multiply(dto.getCurrentPrice()));
            
            myAssetList.add(dto);
        }
        
        // 화면 갱신
        updateAssetStatus();
    }
    
    // 2. [오버라이드] 시세가 들어올 때마다 호출되는 메서드
    @Override
    public void onTickerUpdate(String symbol, String priceStr, String flucStr, String accPriceStr) {
        // 내가 가진 코인 중에 시세가 변한게 있는지 확인
        boolean isRelated = myAssetList.stream()
                .anyMatch(dto -> dto.getCurrency().equals(symbol));
        
        // 관련된 코인이면 화면 갱신 (스윙 스레드에서 안전하게 처리)
        if (isRelated) {
            SwingUtilities.invokeLater(() -> {
            	updateAssetStatus();
            });
        }
    }

    // 3. 화면 갱신 로직 (메모리에 있는 리스트의 가격만 최신화)
    private void updateAssetStatus() {
        BigDecimal totalEvaluation = BigDecimal.ZERO; 
        BigDecimal totalBuy = BigDecimal.ZERO;        
        
        for (MyAssetStatusDTO dto : myAssetList) {
            // 웹소켓 DAO에서 최신 현재가 가져오기
            String market = "KRW-" + dto.getCurrency();
            BigDecimal currentPrice = UpbitWebSocketDao.getCurrentPrice(market); 
            
            // 아직 시세가 안 들어왔으면 기존 값(평단가 등) 유지
            if (currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0) {
                dto.setCurrentPrice(currentPrice);
            }
            
            // 평가금액 재계산
            BigDecimal valuation = dto.getBalance().multiply(dto.getCurrentPrice());
            dto.setTotalValue(valuation);
            
            // 매수금액 계산
            BigDecimal buyAmt = dto.getBalance().multiply(dto.getAvgPrice());
            
            totalEvaluation = totalEvaluation.add(valuation);
            totalBuy = totalBuy.add(buyAmt);
            
            // 수익률 재계산
            if (dto.getAvgPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diff = dto.getCurrentPrice().subtract(dto.getAvgPrice());
                double rate = diff.divide(dto.getAvgPrice(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
                dto.setProfitRate(rate);
            } else {
                dto.setProfitRate(0.0);
            }
        }
        
        // 전체 요약 정보 재계산
        BigDecimal totalAsset = krwBalance.add(totalEvaluation);
        BigDecimal totalPnl = totalEvaluation.subtract(totalBuy);
        
        double totalYield = 0.0;
        if (totalBuy.compareTo(BigDecimal.ZERO) > 0) {
            totalYield = totalPnl.divide(totalBuy, 4, RoundingMode.HALF_UP).doubleValue() * 100;
        }

        // 각 패널에 변경된 데이터 전달
        summaryPanel.updateSummary(totalAsset, totalBuy, totalPnl, totalYield);
        chartPanel.updateChart(myAssetList, krwBalance);
        tablePanel.updateTable(myAssetList);
        
        // 화면 다시 그리기 (깜빡임 방지 등)
        revalidate();
        repaint();
    }
    
    // 테스트용 메인
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame("자산 현황 패널 테스트");
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        // 테스트 유저 ID 입력
        Asset_MainPanel panel = new Asset_MainPanel("user_01"); 
        
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // 앱 시작 시 웹소켓 시작 (반드시 필요)
        UpbitWebSocketDao.getInstance().start();
    }
}