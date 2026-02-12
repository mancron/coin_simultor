package com.team.coin_simulator.Market_Order;

import DTO.*;
import com.team.coin_simulator.Market_Panel.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class OrderPanel extends JPanel {
	private OrderDAO orderDAO = new OrderDAO();
    private Map<String, BigDecimal> mockBalance = new HashMap<>();
    private Map<String, BigDecimal> mockLocked = new HashMap<>();
    private List<OrderDTO> openOrders = new ArrayList<>();

    private CardLayout cardLayout;
    private JPanel inputCardPanel, editListPanel;
    private JTextField priceField, qtyField, marketAmountField;
    private JLabel valAvailable, valTotal;
    private JButton btnAction;
    private JLabel lblSelectedCoinInfo; 
    private JLabel lblMarketUnit;     

    private JLabel valExpected; // 예상 체결 수량/금액 표시 라벨
    private String selectedCoinCode = "BTC"; // 기본값
    private BigDecimal currentSelectedPrice = BigDecimal.ZERO; // HistoryPanel에서 받은 현재가 저장
    private int sideIdx = 0;
    private boolean isLimitMode = true;
    
    private final Color COLOR_BID = new Color(200, 30, 30);
    private final Color COLOR_ASK = new Color(30, 70, 200);

    public OrderPanel() {
        // 1. 초기 데이터 및 배경 설정
        mockBalance.put("KRW", new BigDecimal("10000000"));
        mockBalance.put("BTC", new BigDecimal("0.5"));
        mockLocked.put("KRW", BigDecimal.ZERO);
        mockLocked.put("BTC", BigDecimal.ZERO);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(350, 600));

        // 2. 상단 탭 (매수/매도/정정)
        JPanel topTabPanel = new JPanel(new GridLayout(1, 3));
        topTabPanel.setBackground(Color.WHITE);
        TabButton btnBid = new TabButton("매수");
        TabButton btnAsk = new TabButton("매도");
        TabButton btnEdit = new TabButton("주문정정");
        btnBid.setSelected(true);
        topTabPanel.add(btnBid);
        topTabPanel.add(btnAsk);
        topTabPanel.add(btnEdit);
        add(topTabPanel, BorderLayout.NORTH);

        // 3. 메인 거래 패널 구성 (BoxLayout)
        cardLayout = new CardLayout();
        inputCardPanel = new JPanel(cardLayout);

        JPanel tradePanel = new JPanel();
        tradePanel.setLayout(new BoxLayout(tradePanel, BoxLayout.Y_AXIS));
        tradePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tradePanel.setBackground(Color.WHITE);

        //현재 어떤 코인을 거래 중인지 보여주는 정보창
        lblSelectedCoinInfo = new JLabel("BTC - 비트코인");
        lblSelectedCoinInfo.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        lblSelectedCoinInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        tradePanel.add(lblSelectedCoinInfo);
        tradePanel.add(Box.createVerticalStrut(10));
        
        // 3-1. 지정가/시장가 모드 선택 버튼
        JPanel modePanel = new JPanel(new GridLayout(1, 2, 5, 0));
        modePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        TabButton btnLimit = new TabButton("지정가");
        TabButton btnMarket = new TabButton("시장가");
        btnLimit.setSelected(true);
        modePanel.add(btnLimit);
        modePanel.add(btnMarket);
        tradePanel.add(modePanel);
        tradePanel.add(Box.createVerticalStrut(20));

        // 3-2. 입력 폼 (지정가/시장가)
        CardLayout tradeCardLayout = new CardLayout();
        JPanel tradeInputPanel = new JPanel(tradeCardLayout);
        tradeInputPanel.add(createLimitForm(), "LIMIT");
        tradeInputPanel.add(createMarketForm(), "MARKET");
        tradePanel.add(tradeInputPanel);

        // 3-3. 중앙 공백 (글루) - 입력창과 하단 버튼/정보 사이를 띄워줌
        tradePanel.add(Box.createVerticalGlue());

        // 4. 하단 정보 및 버튼 패널 (여기가 핵심!)
        Dimension btnSize = new Dimension(340, 50);
        
        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
        infoContainer.setBackground(Color.WHITE);
        infoContainer.setMaximumSize(btnSize); // 버튼 너비만큼만 영역 확보
        infoContainer.setAlignmentX(Component.CENTER_ALIGNMENT); // 이 컨테이너 자체를 중앙 배치

        // 주문 가능 행 (BorderLayout으로 양끝 정렬)
        JPanel availRow = new JPanel(new BorderLayout());
        availRow.setBackground(Color.WHITE);
        availRow.add(new JLabel("주문 가능"), BorderLayout.WEST); // 왼쪽 고정
        valAvailable = new JLabel("10,000,000.00 KRW");
        valAvailable.setHorizontalAlignment(SwingConstants.RIGHT); // 오른쪽 정렬
        availRow.add(valAvailable, BorderLayout.CENTER);
        
        // 주문 총액 행
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(Color.WHITE);
        totalRow.add(new JLabel("주문 총액"), BorderLayout.WEST); // 왼쪽 고정
        valTotal = new JLabel("0.00 KRW");
        valTotal.setHorizontalAlignment(SwingConstants.RIGHT); // 오른쪽 정렬
        totalRow.add(valTotal, BorderLayout.CENTER);

        infoContainer.add(availRow);
        infoContainer.add(Box.createVerticalStrut(10));
        infoContainer.add(totalRow);
        infoContainer.add(Box.createVerticalStrut(20));

        // [버튼 영역]
        btnAction = new JButton("매수");
        btnAction.setBackground(COLOR_BID);
        btnAction.setForeground(Color.WHITE);
        btnAction.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        btnAction.setPreferredSize(btnSize);
        btnAction.setMinimumSize(btnSize);
        btnAction.setMaximumSize(btnSize);
        btnAction.setAlignmentX(Component.CENTER_ALIGNMENT); // 버튼 중앙 정렬

        // tradePanel에 순서대로 추가
        tradePanel.add(infoContainer);
        tradePanel.add(btnAction);

        // 5. 전체 레이아웃 조립
        editListPanel = new JPanel();
        editListPanel.setLayout(new BoxLayout(editListPanel, BoxLayout.Y_AXIS));
        editListPanel.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(editListPanel);
        scrollPane.setBorder(null);

        inputCardPanel.add(tradePanel, "TRADE");
        inputCardPanel.add(scrollPane, "EDIT");
        add(inputCardPanel, BorderLayout.CENTER);

        // 6. 이벤트 리스너 및 연결 (생략된 리스너 부분 포함)
        DocumentListener updateListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateOrderSummary(); }
            public void removeUpdate(DocumentEvent e) { updateOrderSummary(); }
            public void changedUpdate(DocumentEvent e) { updateOrderSummary(); }
        };
        priceField.getDocument().addDocumentListener(updateListener);
        qtyField.getDocument().addDocumentListener(updateListener);

        btnBid.addActionListener(e -> { switchSide(0, btnBid, btnAsk, btnEdit); cardLayout.show(inputCardPanel, "TRADE"); });
        btnAsk.addActionListener(e -> { switchSide(1, btnAsk, btnBid, btnEdit); cardLayout.show(inputCardPanel, "TRADE"); });
        btnEdit.addActionListener(e -> { switchSide(-1, btnEdit, btnBid, btnAsk); refreshEditList(); cardLayout.show(inputCardPanel, "EDIT"); });
        btnLimit.addActionListener(e -> { 
            isLimitMode = true; 
            btnLimit.setSelected(true); btnMarket.setSelected(false); 
            tradeCardLayout.show(tradeInputPanel, "LIMIT"); 
            updateOrderSummary();
        });
        btnMarket.addActionListener(e -> { 
            isLimitMode = false; 
            btnMarket.setSelected(true); btnLimit.setSelected(false); 
            tradeCardLayout.show(tradeInputPanel, "MARKET"); 
            updateMarketCalculation(); // 모드 변경 시 즉시 계산
        });
        
        btnAction.addActionListener(e -> handleOrderAction());
        
        // 초기 잔고 표시
        updateInfoLabel();
    }
    
    public void setSelectedCoin(String code, String price) {
        // 1. 가격 파싱
        String cleanPrice = price.replace(",", "").replace(" KRW", "").trim();
        if (cleanPrice.equals("연결중...") || cleanPrice.isEmpty()) return;

        // 2. 상태 업데이트
        this.selectedCoinCode = code;
        this.currentSelectedPrice = new BigDecimal(cleanPrice);

        // 3. UI 반영
        // 3-1. 상단 코인 이름 변경
        lblSelectedCoinInfo.setText(code + " - 현재가 " + String.format("%,.0f", currentSelectedPrice));
        
        // 3-2. 지정가 필드에 가격 자동 입력
        if (!priceField.hasFocus()) { // 사용자가 입력 중이 아닐 때만 업데이트
            priceField.setText(cleanPrice);
        }

        // 3-3. 각종 계산 로직 수행
        updateInfoLabel();       // 잔고 표시 (BTC -> ETH 등으로 변경될 수 있으므로)
        updateOrderSummary();    // 지정가 총액 계산
        updateMarketCalculation(); // 시장가 예상 수량 계산
    }
    
    //실시간 시세 반영 메서드
    public void updateRealTimePrice(String code, String newPrice) {
        // 1. 지금 선택된 코인이 아니면 무시 (BTC 보고 있는데 ETH 가격 오면 안 되니까)
        if (!this.selectedCoinCode.equals(code)) return;

        // 2. 가격 파싱
        String cleanPrice = newPrice.replace(",", "").replace(" KRW", "").trim();
        if (cleanPrice.isEmpty() || cleanPrice.equals("연결중...")) return;

        BigDecimal priceBD = new BigDecimal(cleanPrice);
        this.currentSelectedPrice = priceBD; // 내부 기준가 갱신

        // 3. 상단 정보 라벨 갱신 (여기는 실시간으로 계속 바뀜)
        lblSelectedCoinInfo.setText(code + " - 현재가 " + String.format("%,.0f", priceBD));

        // 4. 시장가 모드라면 예상 수량/금액 실시간 재계산
        if (!isLimitMode) {
            updateMarketCalculation();
        }
    }

    // 정보 갱신 (주문 가능 잔고)
    private void updateInfoLabel() {
        String assetCode;
        if (sideIdx == 0) { // 매수: KRW 필요
            assetCode = "KRW";
        } else { // 매도: 해당 코인(BTC, ETH 등) 필요
            assetCode = selectedCoinCode; 
        }

        BigDecimal balance = mockBalance.getOrDefault(assetCode, BigDecimal.ZERO);
        
        String format = assetCode.equals("KRW") ? "%,.0f" : "%.8f";
        valAvailable.setText(String.format(format + " %s", balance, assetCode));
    }
    

 // 2. 지정가 주문 시 최대 가능 수량 업데이트 (OrderCalc 연동)
    private void updateMaxVolume() {
        if (sideIdx == 0 && isLimitMode) { // 매수 & 지정가 모드일 때
            String priceText = priceField.getText();
            BigDecimal krwBalance = mockBalance.get("KRW");
            
            // 현재 잔고로 살 수 있는 최대 수량을 계산하여 로그나 툴팁 등에 활용 가능
            String maxQty = OrderCalc.getAvailableVolumeString(priceText, krwBalance);
            // 필요 시 UI에 "최대 가능: 0.123 BTC" 라벨을 추가하여 표시하면 좋습니다.
        }
    }

 // 3. 주문 요약 업데이트 수정
    private void updateOrderSummary() {
        updateInfoLabel();
        updateMarketCalculation(); // 시장가 계산도 함께 갱신
        
        try {
            String pStr = priceField.getText().replace(",", "").trim();
            String qStr = qtyField.getText().replace(",", "").trim();
            
            if (!pStr.isEmpty() && !qStr.isEmpty()) {
                BigDecimal price = new BigDecimal(pStr);
                BigDecimal qty = new BigDecimal(qStr);
                
                // 단순 총액 계산 (수수료 제외)
                BigDecimal total = OrderCalc.calcTotalCost(price, qty);
                SwingUtilities.invokeLater(() -> valTotal.setText(String.format("%,.2f KRW", total)));
            } else {
                valTotal.setText("0.00 KRW");
            }
        } catch (Exception e) {
            valTotal.setText("0.00 KRW");
        }
    }

    private void handleMockOrder() {
        try {
            String currency = (sideIdx == 0) ? "KRW" : "BTC";
            AssetDTO currentAsset = new AssetDTO();
            currentAsset.setCurrency(currency);
            currentAsset.setBalance(mockBalance.get(currency));
            currentAsset.setLocked(mockLocked.get(currency));

            if (isLimitMode) {
                BigDecimal price = new BigDecimal(priceField.getText().replace(",", ""));
                BigDecimal qty = new BigDecimal(qtyField.getText());
                BigDecimal total = price.multiply(qty);
                BigDecimal req = (sideIdx == 0) ? total : qty;

                if (currentAsset.getBalance().compareTo(req) < 0) throw new RuntimeException("잔고가 부족합니다.");

                currentAsset.setBalance(currentAsset.getBalance().subtract(req));
                currentAsset.setLocked(currentAsset.getLocked().add(req));
                mockBalance.put(currency, currentAsset.getBalance());
                mockLocked.put(currency, currentAsset.getLocked());

                OrderDTO order = new OrderDTO();
                order.setOrderId(System.currentTimeMillis());
                order.setSide(sideIdx == 0 ? "BID" : "ASK");
                order.setOriginalPrice(price);
                order.setOriginalVolume(qty);
                order.setRemainingVolume(qty);
                order.setStatus("WAIT");
                //리스트에 담기
                openOrders.add(order);
                refreshEditList();
                updateInfoLabel();
                JOptionPane.showMessageDialog(this, "지정가 주문 접수!");
            } else {
                handleMarketOrderExecution();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "알림", JOptionPane.ERROR_MESSAGE);
        }
    }

 // 4. 시장가 체결 실행 로직 수정
    private void handleMarketOrderExecution() {
        try {
            if (currentSelectedPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("시세를 먼저 선택해주세요.");
            }

            BigDecimal amount = new BigDecimal(marketAmountField.getText().replace(",", ""));
            
            // 실제 체결될 수량 계산 (수수료 반영)
            BigDecimal volume = OrderCalc.calculateMarketBuyQuantity(amount, currentSelectedPrice);

            if (sideIdx == 0) { // 매수
                if (mockBalance.get("KRW").compareTo(amount) < 0) throw new RuntimeException("잔고가 부족합니다.");
                mockBalance.put("KRW", mockBalance.get("KRW").subtract(amount));
                // 코인 잔고 증가 로직 필요 (현재 mockBalance에 BTC 등 추가)
            }
            
            JOptionPane.showMessageDialog(this, String.format("시장가 체결 완료!\n체결가: %,.2f\n수량: %s", currentSelectedPrice, volume));
            updateInfoLabel();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "시장가 주문 오류: " + ex.getMessage());
        }
    }

    private void refreshEditList() {
        editListPanel.removeAll();
        for (OrderDTO order : openOrders) {
            editListPanel.add(createOrderEditItem(order));
            editListPanel.add(Box.createVerticalStrut(10));
        }
        editListPanel.revalidate();
        editListPanel.repaint();
    }

    private JPanel createOrderEditItem(OrderDTO order) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        item.setMaximumSize(new Dimension(360, 140)); // 높이 살짝 늘림

        // 1. 매수/매도 라벨
        String sideTxt = order.getSide().equals("BID") ? "매수" : "매도";
        JLabel typeLbl = new JLabel(sideTxt);
        typeLbl.setForeground(order.getSide().equals("BID") ? COLOR_BID : COLOR_ASK);
        typeLbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        // 2. 정보 표시 (가격, 수량)
        JPanel center = new JPanel(new GridLayout(2, 2));
        center.setBackground(Color.WHITE);
        center.add(new JLabel("가격"));
        center.add(new JLabel(String.format("%,.0f KRW", order.getOriginalPrice()), SwingConstants.RIGHT));
        center.add(new JLabel("수량"));
        center.add(new JLabel(order.getOriginalVolume() + " BTC", SwingConstants.RIGHT));

        // 3. 버튼 패널 (정정 / 취소) -> 여기가 핵심 변경 사항!
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0)); // 1행 2열
        btnPanel.setBackground(Color.WHITE);

        // [추가] 정정 버튼
        JButton btnModify = new JButton("정정");
        btnModify.setBackground(new Color(240, 240, 240));
        btnModify.addActionListener(e -> showModifyDialog(order)); // 정정 팝업 호출

        // [기존] 취소 버튼
        JButton btnCancel = new JButton("취소");
        btnCancel.setBackground(new Color(255, 230, 230));
        btnCancel.setForeground(Color.RED);
        btnCancel.addActionListener(e -> cancelOrder(order)); // 취소 로직 분리

        btnPanel.add(btnModify);
        btnPanel.add(btnCancel);

        item.add(typeLbl, BorderLayout.NORTH);
        item.add(center, BorderLayout.CENTER);
        item.add(btnPanel, BorderLayout.SOUTH);
        return item;
    }

    private void cancelOrder(OrderDTO order) {
        String curr = order.getSide().equals("BID") ? "KRW" : "BTC"; // 매수면 KRW, 매도면 BTC 필요
        
        // 잠겨있던 자산 계산
        BigDecimal lockedAmt;
        if (order.getSide().equals("BID")) {
            // 매수: 가격 * 수량만큼 잠김
            lockedAmt = order.getOriginalPrice().multiply(order.getOriginalVolume());
        } else {
            // 매도: 수량만큼 잠김
            lockedAmt = order.getOriginalVolume();
        }

     // "test_user"는 현재 사용 중인 임시 아이디
        boolean isDBSuccess = orderDAO.cancelOrder(order.getOrderId(), "test_user", order.getSide(), lockedAmt);

        if (isDBSuccess) {
            //DB 성공 시에만 메모리(Mock) 자산 복구
            mockLocked.put(curr, mockLocked.get(curr).subtract(lockedAmt));
            mockBalance.put(curr, mockBalance.get(curr).add(lockedAmt));

            // 4. 리스트에서 삭제 및 갱신
            openOrders.remove(order);
            refreshEditList();
            updateInfoLabel();
            
            JOptionPane.showMessageDialog(this, "주문이 취소되었습니다. (DB 반영 완료)");
        } else {
            JOptionPane.showMessageDialog(this, "DB 취소 처리에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showModifyDialog(OrderDTO order) {
        // 1. 입력 팝업 UI 구성
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField txtPrice = new JTextField(order.getOriginalPrice().toString());
        JTextField txtQty = new JTextField(order.getOriginalVolume().toString());
        
        panel.add(new JLabel("정정 가격(KRW):"));
        panel.add(txtPrice);
        panel.add(new JLabel("정정 수량(BTC):"));
        panel.add(txtQty);

        int result = JOptionPane.showConfirmDialog(this, panel, "주문 정정", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                BigDecimal newPrice = new BigDecimal(txtPrice.getText().replace(",", ""));
                BigDecimal newQty = new BigDecimal(txtQty.getText().replace(",", ""));

                // 2. 자산 변동 계산 로직
                // (1) 먼저 기존 주문을 '가상 취소'하여 자산을 돌려받음
                String curr = order.getSide().equals("BID") ? "KRW" : "BTC";
                BigDecimal oldLockedAmt = order.getSide().equals("BID") ? 
                        order.getOriginalPrice().multiply(order.getOriginalVolume()) : order.getOriginalVolume();

                BigDecimal currentLocked = mockLocked.getOrDefault(curr, BigDecimal.ZERO);
                BigDecimal currentBalance = mockBalance.getOrDefault(curr, BigDecimal.ZERO);

                // 일단 복구 (임시)
                BigDecimal tempBalance = currentBalance.add(oldLockedAmt);
                BigDecimal tempLocked = currentLocked.subtract(oldLockedAmt);

                // (2) 새로운 주문 금액 계산
                BigDecimal newRequiredAmt = order.getSide().equals("BID") ? 
                        newPrice.multiply(newQty) : newQty;

                // (3) 잔고 부족 체크
                if (tempBalance.compareTo(newRequiredAmt) < 0) {
                    throw new RuntimeException("정정 주문을 위한 잔고가 부족합니다.\n필요: " + newRequiredAmt + " / 보유: " + tempBalance);
                }

                // (4) 실제 반영 (기존 것 취소 확정 + 새 것 잠금)
                boolean isDBSuccess = orderDAO.modifyOrder(
                		order.getOrderId(), 
                	    "test_user", 
                	    order.getSide(), 
                	    oldLockedAmt, 
                	    newRequiredAmt, 
                	    newPrice, 
                	    newQty
                	);

                	if (isDBSuccess) {
                	    // (4) 실제 반영 (DB 성공 시에만 메모리 업데이트)
                	    mockBalance.put(curr, tempBalance.subtract(newRequiredAmt));
                	    mockLocked.put(curr, tempLocked.add(newRequiredAmt));

                	    // (5) 주문 객체 정보 업데이트
                	    order.setOriginalPrice(newPrice);
                	    order.setOriginalVolume(newQty);
                	    order.setRemainingVolume(newQty);

                	    refreshEditList();
                	    updateInfoLabel();
                	    JOptionPane.showMessageDialog(this, "주문이 정정되었습니다. (DB 반영 완료)");
                	} else {
                	    JOptionPane.showMessageDialog(this, "DB 정정 처리에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                	}
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "정정 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void switchSide(int side, TabButton selected, TabButton un1, TabButton un2) {
        this.sideIdx = side;
        selected.setSelected(true);
        un1.setSelected(false);
        un2.setSelected(false);
        if (side != -1) {
            btnAction.setText(side == 0 ? "매수" : "매도");
            btnAction.setBackground(side == 0 ? COLOR_BID : COLOR_ASK);
            
            if (lblMarketUnit != null) {
                if (side == 0) { // 매수
                    lblMarketUnit.setText("주문총액 (KRW)");
                    valExpected.setText("예상 수량: -");
                } else { // 매도
                    lblMarketUnit.setText("주문수량 (" + selectedCoinCode + ")");
                    valExpected.setText("예상 수령액: -");
                }
                // 탭을 바꾸면 입력창 초기화
                marketAmountField.setText("");
            }
        }
        updateInfoLabel();
    }

    private JPanel createLimitForm() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Color.WHITE);
        p.add(new JLabel("주문가격 (KRW)")); priceField = new JTextField(); styleField(priceField); p.add(priceField);
        p.add(Box.createVerticalStrut(10));
        p.add(new JLabel("주문수량")); qtyField = new JTextField(); styleField(qtyField); p.add(qtyField);
        return p;
    }
    
 // 1. 시장가 실시간 계산 로직 수정 (OrderCalc 연동)
    private void updateMarketCalculation() {
        try {
            String amtStr = marketAmountField.getText().replace(",", "").trim();
            
            if (currentSelectedPrice.compareTo(BigDecimal.ZERO) <= 0 || amtStr.isEmpty()) {
                valExpected.setText("-");
                return;
            }

            BigDecimal inputVal = new BigDecimal(amtStr);
            
            if (sideIdx == 0) { 
                // [매수] 입력값 = 총액(KRW) -> 결과 = 살 수 있는 수량(BTC)
                // 수수료 로직은 OrderCalc에 있다고 가정 (여기선 단순 나누기 예시)
                BigDecimal expectedQty = inputVal.divide(currentSelectedPrice, 8, BigDecimal.ROUND_DOWN);
                valExpected.setText("예상 수량: " + expectedQty.toPlainString() + " " + selectedCoinCode);
                
            } else { 
                // [매도] 입력값 = 수량(BTC) -> 결과 = 받을 수 있는 돈(KRW)
                // 내 비트코인 0.5개를 개당 1억에 팔면? -> 5천만 원
                BigDecimal expectedKRW = inputVal.multiply(currentSelectedPrice);
                valExpected.setText("예상 수령: " + String.format("%,.0f", expectedKRW) + " KRW");
            }
        } catch (Exception e) {
            valExpected.setText("계산 불가");
        }
    }
    
    // 주문 실행 (매수/매도 버튼 클릭)
    private void handleOrderAction() {
        if (isLimitMode) {
            handleLimitOrder();
        } else {
            handleMarketOrder();
        }
    }
    
    private void handleMarketOrder() {
        try {
            // 1. 기초 검증
            if (currentSelectedPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("시세를 먼저 선택해주세요.");
            }
            
            String text = marketAmountField.getText().replace(",", "").trim();
            if (text.isEmpty()) throw new RuntimeException("주문 내용을 입력해주세요.");
            BigDecimal inputVal = new BigDecimal(text);

            // ============================================================
            // CASE 1: 시장가 매수 (BID)
            // 입력값(inputVal) = 총 사용 금액(KRW)
            // ============================================================
            if (sideIdx == 0) { 
                // 1) KRW 잔고 확인
                BigDecimal krwBal = mockBalance.getOrDefault("KRW", BigDecimal.ZERO);
                if (krwBal.compareTo(inputVal) < 0) {
                    throw new RuntimeException("KRW 잔고가 부족합니다.");
                }
                
                // 2) 수량 계산
                BigDecimal buyQty = OrderCalc.calculateMarketBuyQuantity(inputVal, currentSelectedPrice);
                
                // 3) [핵심] DB 저장 시도
                OrderDTO marketOrder = new OrderDTO();
                marketOrder.setOrderId(System.currentTimeMillis()); // ID 생성
                marketOrder.setSide("BID");
                marketOrder.setStatus("DONE"); // 시장가는 즉시 완료

                // executeMarketOrder(주문객체, 유저ID, 체결가, 체결수량, 총체결액)
                boolean isSuccess = orderDAO.executeMarketOrder(
                    marketOrder, 
                    "test_user", 
                    currentSelectedPrice, 
                    buyQty, 
                    inputVal // 매수일 땐 입력한 금액이 총 체결액
                );

                // 4) DB 성공 시 메모리/UI 반영
                if (isSuccess) {
                    mockBalance.put("KRW", krwBal.subtract(inputVal)); // 돈 차감
                    BigDecimal coinBal = mockBalance.getOrDefault(selectedCoinCode, BigDecimal.ZERO);
                    mockBalance.put(selectedCoinCode, coinBal.add(buyQty)); // 코인 증가
                    
                    JOptionPane.showMessageDialog(this, 
                        String.format("[시장가 매수 체결]\n코인: %s\n체결가: %,.0f\n매수량: %.8f", 
                        selectedCoinCode, currentSelectedPrice, buyQty));
                } else {
                    throw new RuntimeException("DB 저장 실패");
                }
            } 
            
            // ============================================================
            // CASE 2: 시장가 매도 (ASK)
            // 입력값(inputVal) = 판매 수량(BTC)
            // ============================================================
            else { 
                // 1) 코인 잔고 확인
                BigDecimal coinBal = mockBalance.getOrDefault(selectedCoinCode, BigDecimal.ZERO);
                if (coinBal.compareTo(inputVal) < 0) {
                    throw new RuntimeException(selectedCoinCode + " 잔고가 부족합니다.");
                }

                // 2) 받을 돈 계산
                BigDecimal sellTotalKRW = inputVal.multiply(currentSelectedPrice);

                // 3) [핵심] DB 저장 시도
                OrderDTO marketOrder = new OrderDTO();
                marketOrder.setOrderId(System.currentTimeMillis());
                marketOrder.setSide("ASK");
                marketOrder.setStatus("DONE");

                // executeMarketOrder(주문객체, 유저ID, 체결가, 체결수량, 총체결액)
                boolean isSuccess = orderDAO.executeMarketOrder(
                    marketOrder, 
                    "test_user", 
                    currentSelectedPrice, 
                    inputVal,    // 매도일 땐 입력한 값이 체결 수량
                    sellTotalKRW // 계산된 총액
                );

                // 4) DB 성공 시 메모리/UI 반영
                if (isSuccess) {
                    mockBalance.put(selectedCoinCode, coinBal.subtract(inputVal)); // 코인 차감
                    BigDecimal krwBal = mockBalance.getOrDefault("KRW", BigDecimal.ZERO);
                    mockBalance.put("KRW", krwBal.add(sellTotalKRW)); // 돈 증가

                    JOptionPane.showMessageDialog(this, 
                        String.format("[시장가 매도 체결]\n코인: %s\n체결가: %,.0f\n수령액: %,.0f KRW", 
                        selectedCoinCode, currentSelectedPrice, sellTotalKRW));
                } else {
                    throw new RuntimeException("DB 저장 실패");
                }
            }

            // 공통 마무리
            updateInfoLabel(); // 잔고 갱신
            marketAmountField.setText(""); // 입력창 초기화
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "주문 실패: " + e.getMessage(), "에러", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // 콘솔에 자세한 에러 출력
        }
    }
    
    private void handleLimitOrder() {
        OrderDTO order = null; // 밖에서 선언해야 하단 DB 로직에서 사용 가능합니다.
        try {
            // 1. 입력값 검증
            String pStr = priceField.getText().replace(",", "").trim();
            String qStr = qtyField.getText().replace(",", "").trim();
            
            if (pStr.isEmpty() || qStr.isEmpty()) {
                throw new RuntimeException("가격과 수량을 입력해주세요.");
            }

            BigDecimal price = new BigDecimal(pStr);
            BigDecimal qty = new BigDecimal(qStr);
            BigDecimal total = price.multiply(qty);

            // 2. 자산 확인 및 잠금 처리
            String currency = (sideIdx == 0) ? "KRW" : selectedCoinCode;
            BigDecimal balance = mockBalance.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal requiredAmount = (sideIdx == 0) ? total : qty;

            if (balance.compareTo(requiredAmount) < 0) {
                throw new RuntimeException("주문 가능 잔고가 부족합니다.");
            }

            // 3. 주문 객체(OrderDTO) 생성
            order = new OrderDTO();
            order.setOrderId(System.currentTimeMillis());
            order.setSide(sideIdx == 0 ? "BID" : "ASK");
            order.setOriginalPrice(price);
            order.setOriginalVolume(qty);
            order.setRemainingVolume(qty);
            order.setStatus("WAIT");

            // 4. [핵심] DB 저장 호출 (UI 변경 전에 먼저 실행)
            // "test_user"는 DB에 INSERT INTO users로 미리 넣어둔 ID여야 합니다!
            boolean isSuccess = orderDAO.insertOrder(order, "test_user"); 

            if (!isSuccess) {
                throw new RuntimeException("데이터베이스 저장에 실패했습니다.");
            }

            // 5. DB 저장 성공 시에만 메모리 잔고 및 UI 업데이트
            mockBalance.put(currency, balance.subtract(requiredAmount));
            BigDecimal currentLocked = mockLocked.getOrDefault(currency, BigDecimal.ZERO);
            mockLocked.put(currency, currentLocked.add(requiredAmount));

            openOrders.add(order);
            refreshEditList();
            updateInfoLabel();
            
            JOptionPane.showMessageDialog(this, "지정가 주문 접수 및 DB 저장 완료!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "주문 오류: " + e.getMessage(), "알림", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createMarketForm() {
        JPanel p = new JPanel(); 
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); 
        p.setBackground(Color.WHITE);
        
        lblMarketUnit = new JLabel("주문총액 (KRW)"); 
        p.add(lblMarketUnit);
        
        marketAmountField = new JTextField(); 
        styleField(marketAmountField); 
        p.add(marketAmountField);
        
        p.add(Box.createVerticalStrut(10));
        valExpected = new JLabel("예상 수량: -");
        valExpected.setForeground(Color.GRAY);
        p.add(valExpected);
        
        marketAmountField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateMarketCalculation(); }
            public void removeUpdate(DocumentEvent e) { updateMarketCalculation(); }
            public void changedUpdate(DocumentEvent e) { updateMarketCalculation(); }
        });
        return p;
    }
    
    private void styleField(JTextField tf) {
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setHorizontalAlignment(JTextField.RIGHT);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("주문 시스템");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new OrderPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}