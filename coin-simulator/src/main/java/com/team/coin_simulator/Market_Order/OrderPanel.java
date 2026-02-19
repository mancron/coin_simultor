package com.team.coin_simulator.Market_Order;

import DAO.*;
import DTO.*;
import com.team.coin_simulator.CoinConfig; // 코인 한글명 가져오기
import com.team.coin_simulator.Market_Panel.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class OrderPanel extends JPanel implements UpbitWebSocketDao.TickerListener {
    
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
        mockBalance.put("KRW", new BigDecimal("100000000")); // 테스트용 1억 세팅
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

        // 현재 어떤 코인을 거래 중인지 보여주는 정보창
        lblSelectedCoinInfo = new JLabel("비트코인 (BTC)");
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

        // 3-3. 중앙 공백 (글루)
        tradePanel.add(Box.createVerticalGlue());

        // 4. 하단 정보 및 버튼 패널
        Dimension btnSize = new Dimension(340, 50);

        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
        infoContainer.setBackground(Color.WHITE);
        infoContainer.setMaximumSize(btnSize);
        infoContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 주문 가능 행
        JPanel availRow = new JPanel(new BorderLayout());
        availRow.setBackground(Color.WHITE);
        availRow.add(new JLabel("주문 가능"), BorderLayout.WEST);
        valAvailable = new JLabel("0 KRW");
        valAvailable.setHorizontalAlignment(SwingConstants.RIGHT);
        availRow.add(valAvailable, BorderLayout.CENTER);

        // 주문 총액 행
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(Color.WHITE);
        totalRow.add(new JLabel("주문 총액"), BorderLayout.WEST);
        valTotal = new JLabel("0.00 KRW");
        valTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        totalRow.add(valTotal, BorderLayout.CENTER);

        infoContainer.add(availRow);
        infoContainer.add(Box.createVerticalStrut(10));
        infoContainer.add(totalRow);
        infoContainer.add(Box.createVerticalStrut(20));

        // [버튼 영역]
        btnAction = new JButton("매수") {
            @Override
            protected void paintComponent(Graphics g) {
                // 버튼을 마우스로 눌렀을 때(Armed) 살짝 어두워지는 디테일 추가
                if (getModel().isArmed()) {
                    g.setColor(getBackground().darker());
                } else {
                    g.setColor(getBackground());
                }
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        btnAction.setBackground(COLOR_BID); 
        btnAction.setForeground(Color.WHITE);
        btnAction.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        
        // OS의 회색 기본 스킨이 덧칠해지는 것을 완벽하게 차단
        btnAction.setContentAreaFilled(false); 
        btnAction.setOpaque(false); 
        btnAction.setFocusPainted(false);
        
        btnAction.setPreferredSize(btnSize);
        btnAction.setMinimumSize(btnSize);
        btnAction.setMaximumSize(btnSize);
        btnAction.setAlignmentX(Component.CENTER_ALIGNMENT);

        tradePanel.add(infoContainer);
        tradePanel.add(btnAction);

        // 5. 전체 레이아웃 조립 (정정 리스트 패널)
        editListPanel = new JPanel();
        editListPanel.setLayout(new BoxLayout(editListPanel, BoxLayout.Y_AXIS));
        editListPanel.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(editListPanel);
        scrollPane.setBorder(null);

        inputCardPanel.add(tradePanel, "TRADE");
        inputCardPanel.add(scrollPane, "EDIT");
        add(inputCardPanel, BorderLayout.CENTER);

        // 6. 이벤트 리스너 연결
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
            updateMarketCalculation(); 
        });

        btnAction.addActionListener(e -> handleOrderAction());

        // 초기 잔고 표시
        updateInfoLabel();
        
        // [추가] 실시간 웹소켓 가격 수신을 위해 자신을 리스너로 등록!
        UpbitWebSocketDao.getInstance().addListener(this);
    }

    // ==========================================================
    // 통신 및 실시간 업데이트 로직 (MainFrame, WebSocket 연동)
    // ==========================================================
    
    // [핵심] MainFrame에서 호출: 사용자가 HistoryPanel에서 다른 코인을 클릭했을 때
    public void setSelectedCoin(String coinSymbol) {
        this.selectedCoinCode = coinSymbol;
        
        // 코인의 한글 이름 가져오기
        String krName = CoinConfig.COIN_INFO.getOrDefault(coinSymbol, coinSymbol);
        
        if (lblSelectedCoinInfo != null) {
            lblSelectedCoinInfo.setText(krName + " (" + coinSymbol + ")");
        }

        // 입력창 초기화
        if (marketAmountField != null) marketAmountField.setText("");
        if (priceField != null) priceField.setText("");
        
        // 코인이 바뀌었으니 잔고 표시 갱신 (BTC 잔고 -> XRP 잔고 등)
        switchSide(sideIdx, null, null, null); 
        updateInfoLabel(); 
    }

    // [핵심] WebSocket에서 호출: 실시간 가격이 들어올 때 (인터페이스 구현)
    @Override
    public void onTickerUpdate(String symbol, String priceStr, String flucStr, String accPriceStr) {
        // 지금 보고 있는 코인이 아니면 무시
        if (!this.selectedCoinCode.equals(symbol)) return;

        String cleanPrice = priceStr.replace(",", "").replace(" KRW", "").trim();
        if (cleanPrice.isEmpty() || cleanPrice.equals("연결중...")) return;

        this.currentSelectedPrice = new BigDecimal(cleanPrice);

        // 상단 라벨에 현재가 반영
        String krName = CoinConfig.COIN_INFO.getOrDefault(symbol, symbol);
        SwingUtilities.invokeLater(() -> {
            lblSelectedCoinInfo.setText(krName + " - 현재가 " + String.format("%,.0f", currentSelectedPrice) + " KRW");
            
            // 지정가 모드이고 사용자가 입력 중이 아닐 때만 현재가 자동 채우기
            if (isLimitMode && !priceField.hasFocus()) {
                priceField.setText(cleanPrice);
                updateOrderSummary();
            }

            // 시장가 모드라면 예상 수량/금액 실시간 재계산
            if (!isLimitMode) {
                updateMarketCalculation();
            }
        });
    }

    // ==========================================================
    // 화면 및 계산 로직
    // ==========================================================

    private void updateInfoLabel() {
        String assetCode = (sideIdx == 0) ? "KRW" : selectedCoinCode; 
        BigDecimal balance = mockBalance.getOrDefault(assetCode, BigDecimal.ZERO);
        String format = assetCode.equals("KRW") ? "%,.0f" : "%.8f";
        valAvailable.setText(String.format(format + " %s", balance, assetCode));
    }

    private void updateOrderSummary() {
        updateInfoLabel();
        updateMarketCalculation(); 

        try {
            String pStr = priceField.getText().replace(",", "").trim();
            String qStr = qtyField.getText().replace(",", "").trim();

            if (!pStr.isEmpty() && !qStr.isEmpty()) {
                BigDecimal price = new BigDecimal(pStr);
                BigDecimal qty = new BigDecimal(qStr);
                BigDecimal total = OrderCalc.calcTotalCost(price, qty);
                SwingUtilities.invokeLater(() -> valTotal.setText(String.format("%,.2f KRW", total)));
            } else {
                valTotal.setText("0.00 KRW");
            }
        } catch (Exception e) {
            valTotal.setText("0.00 KRW");
        }
    }

    // [수정] 중괄호 어긋남 완전 해결
    private void updateMarketCalculation() {
        try {
            String amtStr = marketAmountField.getText().replace(",", "").trim();

            if (currentSelectedPrice.compareTo(BigDecimal.ZERO) <= 0 || amtStr.isEmpty()) {
                valExpected.setText("-");
                return;
            }

            BigDecimal inputVal = new BigDecimal(amtStr);

            if (sideIdx == 0) { 
                // 매수
                BigDecimal expectedQty = inputVal.divide(currentSelectedPrice, 8, BigDecimal.ROUND_DOWN);
                valExpected.setText("예상 수량: " + expectedQty.toPlainString() + " " + selectedCoinCode);
            } else { 
                // 매도
                BigDecimal expectedKRW = inputVal.multiply(currentSelectedPrice);
                valExpected.setText("예상 수령: " + String.format("%,.0f", expectedKRW) + " KRW");
            }
        } catch (Exception e) {
            valExpected.setText("계산 불가");
        }
    }

    private void switchSide(int side, TabButton selected, TabButton un1, TabButton un2) {
        this.sideIdx = side;
        if (selected != null) selected.setSelected(true);
        if (un1 != null) un1.setSelected(false);
        if (un2 != null) un2.setSelected(false);
        
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
                marketAmountField.setText("");
            }
        }
        updateInfoLabel();
    }

    // ==========================================================
    // 주문(매수/매도/정정/취소) 실행 로직
    // ==========================================================

    private void handleOrderAction() {
        if (isLimitMode) {
            handleLimitOrder();
        } else {
            handleMarketOrder();
        }
    }

    // [수정] try-catch 중괄호 어긋남 및 로직 버그 해결
    private void handleLimitOrder() {
        try {
            String pStr = priceField.getText().replace(",", "").trim();
            String qStr = qtyField.getText().replace(",", "").trim();

            if (pStr.isEmpty() || qStr.isEmpty()) {
                throw new RuntimeException("가격과 수량을 입력해주세요.");
            }

            BigDecimal price = new BigDecimal(pStr);
            BigDecimal qty = new BigDecimal(qStr);
            BigDecimal total = price.multiply(qty);

            String currency = (sideIdx == 0) ? "KRW" : selectedCoinCode;
            BigDecimal balance = mockBalance.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal requiredAmount = (sideIdx == 0) ? total : qty;

            if (balance.compareTo(requiredAmount) < 0) {
                throw new RuntimeException("주문 가능 잔고가 부족합니다.");
            }

            OrderDTO order = new OrderDTO();
            order.setOrderId(System.currentTimeMillis());
            order.setSide(sideIdx == 0 ? "BID" : "ASK");
            order.setOriginalPrice(price);
            order.setOriginalVolume(qty);
            order.setRemainingVolume(qty);
            order.setStatus("WAIT");

            boolean isSuccess = orderDAO.insertOrder(order, "test_user"); 

            if (!isSuccess) {
                throw new RuntimeException("데이터베이스 저장에 실패했습니다.");
            }

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

    private void handleMarketOrder() {
        try {
            if (currentSelectedPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("시세를 먼저 선택해주세요.");
            }
            
            String text = marketAmountField.getText().replace(",", "").trim();
            if (text.isEmpty()) throw new RuntimeException("주문 내용을 입력해주세요.");
            BigDecimal inputVal = new BigDecimal(text);

            if (sideIdx == 0) { // 시장가 매수
                BigDecimal krwBal = mockBalance.getOrDefault("KRW", BigDecimal.ZERO);
                if (krwBal.compareTo(inputVal) < 0) throw new RuntimeException("KRW 잔고가 부족합니다.");
                
                BigDecimal buyQty = OrderCalc.calculateMarketBuyQuantity(inputVal, currentSelectedPrice);
                
                OrderDTO marketOrder = new OrderDTO();
                marketOrder.setOrderId(System.currentTimeMillis());
                marketOrder.setSide("BID");
                marketOrder.setStatus("DONE");

                boolean isSuccess = orderDAO.executeMarketOrder(
                    marketOrder, "test_user", currentSelectedPrice, buyQty, inputVal
                );

                if (isSuccess) {
                    mockBalance.put("KRW", krwBal.subtract(inputVal));
                    BigDecimal coinBal = mockBalance.getOrDefault(selectedCoinCode, BigDecimal.ZERO);
                    mockBalance.put(selectedCoinCode, coinBal.add(buyQty));
                    
                    JOptionPane.showMessageDialog(this, 
                        String.format("[시장가 매수 체결]\n코인: %s\n체결가: %,.0f\n매수량: %.8f", 
                        selectedCoinCode, currentSelectedPrice, buyQty));
                } else {
                    throw new RuntimeException("DB 저장 실패");
                }
            } else { // 시장가 매도
                BigDecimal coinBal = mockBalance.getOrDefault(selectedCoinCode, BigDecimal.ZERO);
                if (coinBal.compareTo(inputVal) < 0) throw new RuntimeException(selectedCoinCode + " 잔고가 부족합니다.");

                BigDecimal sellTotalKRW = inputVal.multiply(currentSelectedPrice);

                OrderDTO marketOrder = new OrderDTO();
                marketOrder.setOrderId(System.currentTimeMillis());
                marketOrder.setSide("ASK");
                marketOrder.setStatus("DONE");

                boolean isSuccess = orderDAO.executeMarketOrder(
                    marketOrder, "test_user", currentSelectedPrice, inputVal, sellTotalKRW
                );

                if (isSuccess) {
                    mockBalance.put(selectedCoinCode, coinBal.subtract(inputVal));
                    BigDecimal krwBal = mockBalance.getOrDefault("KRW", BigDecimal.ZERO);
                    mockBalance.put("KRW", krwBal.add(sellTotalKRW));

                    JOptionPane.showMessageDialog(this, 
                        String.format("[시장가 매도 체결]\n코인: %s\n체결가: %,.0f\n수령액: %,.0f KRW", 
                        selectedCoinCode, currentSelectedPrice, sellTotalKRW));
                } else {
                    throw new RuntimeException("DB 저장 실패");
                }
            }

            updateInfoLabel();
            marketAmountField.setText(""); 

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "주문 실패: " + e.getMessage(), "에러", JOptionPane.ERROR_MESSAGE);
        }
    }

    // [수정] 괄호 오류 완전 해결
    private void cancelOrder(OrderDTO order) {
        String curr = order.getSide().equals("BID") ? "KRW" : selectedCoinCode;
        BigDecimal lockedAmt = order.getSide().equals("BID") ? order.getOriginalPrice().multiply(order.getOriginalVolume()) : order.getOriginalVolume();

        boolean isDBSuccess = orderDAO.cancelOrder(order.getOrderId(), "test_user", order.getSide(), lockedAmt);
        
        if (isDBSuccess) {
            mockLocked.put(curr, mockLocked.get(curr).subtract(lockedAmt));
            mockBalance.put(curr, mockBalance.get(curr).add(lockedAmt));
            openOrders.remove(order);
            refreshEditList();
            updateInfoLabel();
            JOptionPane.showMessageDialog(this, "주문이 취소되었습니다. (DB 반영 완료)");
        } else {
            JOptionPane.showMessageDialog(this, "DB 취소 처리에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showModifyDialog(OrderDTO order) {
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

                String curr = order.getSide().equals("BID") ? "KRW" : selectedCoinCode;
                BigDecimal oldLockedAmt = order.getSide().equals("BID") ? 
                        order.getOriginalPrice().multiply(order.getOriginalVolume()) : order.getOriginalVolume();

                BigDecimal currentLocked = mockLocked.getOrDefault(curr, BigDecimal.ZERO);
                BigDecimal currentBalance = mockBalance.getOrDefault(curr, BigDecimal.ZERO);

                BigDecimal tempBalance = currentBalance.add(oldLockedAmt);
                BigDecimal tempLocked = currentLocked.subtract(oldLockedAmt);

                BigDecimal newRequiredAmt = order.getSide().equals("BID") ? newPrice.multiply(newQty) : newQty;

                if (tempBalance.compareTo(newRequiredAmt) < 0) {
                    throw new RuntimeException("정정 주문을 위한 잔고가 부족합니다.");
                }

                boolean isDBSuccess = orderDAO.modifyOrder(
                        order.getOrderId(), "test_user", order.getSide(), oldLockedAmt, newRequiredAmt, newPrice, newQty
                );

                if (isDBSuccess) {
                    mockBalance.put(curr, tempBalance.subtract(newRequiredAmt));
                    mockLocked.put(curr, tempLocked.add(newRequiredAmt));
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

    // ==========================================================
    // UI 그리기 (기타 메서드)
    // ==========================================================
    
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
        item.setMaximumSize(new Dimension(360, 140)); 
        
        String sideTxt = order.getSide().equals("BID") ? "매수" : "매도";
        JLabel typeLbl = new JLabel(sideTxt);
        typeLbl.setForeground(order.getSide().equals("BID") ? COLOR_BID : COLOR_ASK);
        typeLbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        JPanel center = new JPanel(new GridLayout(2, 2));
        center.setBackground(Color.WHITE);
        center.add(new JLabel("가격"));
        center.add(new JLabel(String.format("%,.0f KRW", order.getOriginalPrice()), SwingConstants.RIGHT));
        center.add(new JLabel("수량"));
        center.add(new JLabel(order.getOriginalVolume() + " " + selectedCoinCode, SwingConstants.RIGHT));
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0)); 
        btnPanel.setBackground(Color.WHITE);

        JButton btnModify = new JButton("정정");
        btnModify.setBackground(new Color(240, 240, 240));
        btnModify.addActionListener(e -> showModifyDialog(order)); 

        JButton btnCancel = new JButton("취소");
        btnCancel.setBackground(new Color(255, 230, 230));
        btnCancel.setForeground(Color.RED);
        btnCancel.addActionListener(e -> cancelOrder(order)); 

        btnPanel.add(btnModify);
        btnPanel.add(btnCancel);

        item.add(typeLbl, BorderLayout.NORTH);
        item.add(center, BorderLayout.CENTER);
        item.add(btnPanel, BorderLayout.SOUTH);
        return item;
    }

    private JPanel createLimitForm() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Color.WHITE);
        p.add(new JLabel("주문가격 (KRW)")); priceField = new JTextField(); styleField(priceField); p.add(priceField);
        p.add(Box.createVerticalStrut(10));
        p.add(new JLabel("주문수량")); qtyField = new JTextField(); styleField(qtyField); p.add(qtyField);
        return p;
    }

    private JPanel createMarketForm() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Color.WHITE);
        lblMarketUnit = new JLabel("주문총액 (KRW)"); p.add(lblMarketUnit);
        marketAmountField = new JTextField(); styleField(marketAmountField); p.add(marketAmountField);
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