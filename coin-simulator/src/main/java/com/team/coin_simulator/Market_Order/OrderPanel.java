package com.team.coin_simulator.Market_Order;

import DAO.*;
import DTO.*;
import com.team.coin_simulator.CoinConfig; 
import com.team.coin_simulator.Market_Panel.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

public class OrderPanel extends JPanel implements UpbitWebSocketDao.TickerListener {
    
    private OrderDAO orderDAO = new OrderDAO();
    private AssetDAO assetDAO = new AssetDAO(); // 💡 진짜 자산을 불러올 DAO 추가
    private OpenOrderDAO openOrderDAO = new OpenOrderDAO();
    
    // 💡 가짜(mock) 데이터 삭제하고, DB에서 불러온 진짜 잔고를 담을 맵
    private Map<String, BigDecimal> realBalance = new HashMap<>();
    private Map<String, BigDecimal> realLocked = new HashMap<>();
    
    private List<OrderDTO> openOrders = new ArrayList<>();
    private Map<Long, String> orderCoinMap = new HashMap<>();

    private CardLayout cardLayout;
    private JPanel inputCardPanel, editListPanel;
    private JTextField priceField, qtyField, marketAmountField;
    private JLabel valAvailable, valTotal;
    private JButton btnAction;
    private JLabel lblSelectedCoinInfo; 
    private JLabel lblMarketUnit;       
    private JComboBox<String> filterComboBox;
    private boolean isUpdatingComboBox = false; 

    private String userId;
    private long sessionId = 1L; // 💡 현재 세션 ID 필드 추가
    
    private JLabel valExpected; 
    private String selectedCoinCode = "BTC"; 
    private BigDecimal currentSelectedPrice = BigDecimal.ZERO; 
    private int sideIdx = 0;
    private boolean isLimitMode = true;
    private Map<String, BigDecimal> latestPrices = new java.util.concurrent.ConcurrentHashMap<>();

    private final Color COLOR_BID = new Color(200, 30, 30);
    private final Color COLOR_ASK = new Color(30, 70, 200);
    
    public OrderPanel(String userId) {
        this.userId = userId;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(350, 600));

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

        cardLayout = new CardLayout();
        inputCardPanel = new JPanel(cardLayout);

        JPanel tradePanel = new JPanel();
        tradePanel.setLayout(new BoxLayout(tradePanel, BoxLayout.Y_AXIS));
        tradePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tradePanel.setBackground(Color.WHITE);

        lblSelectedCoinInfo = new JLabel("비트코인 (BTC)");
        lblSelectedCoinInfo.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        lblSelectedCoinInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        tradePanel.add(lblSelectedCoinInfo);
        tradePanel.add(Box.createVerticalStrut(10));

        JPanel modePanel = new JPanel(new GridLayout(1, 2, 5, 0));
        modePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        TabButton btnLimit = new TabButton("지정가");
        TabButton btnMarket = new TabButton("시장가");
        btnLimit.setSelected(true);
        modePanel.add(btnLimit);
        modePanel.add(btnMarket);
        tradePanel.add(modePanel);
        tradePanel.add(Box.createVerticalStrut(20));

        CardLayout tradeCardLayout = new CardLayout();
        JPanel tradeInputPanel = new JPanel(tradeCardLayout);
        tradeInputPanel.add(createLimitForm(), "LIMIT");
        tradeInputPanel.add(createMarketForm(), "MARKET");
        tradePanel.add(tradeInputPanel);
        tradePanel.add(Box.createVerticalStrut(10));
        tradePanel.add(Box.createVerticalGlue());

        Dimension btnSize = new Dimension(340, 50);

        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
        infoContainer.setBackground(Color.WHITE);
        infoContainer.setMaximumSize(btnSize);
        infoContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel availRow = new JPanel(new BorderLayout());
        availRow.setBackground(Color.WHITE);
        availRow.add(new JLabel("주문 가능"), BorderLayout.WEST);
        valAvailable = new JLabel("0 KRW");
        valAvailable.setHorizontalAlignment(SwingConstants.RIGHT);
        availRow.add(valAvailable, BorderLayout.CENTER);

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

        btnAction = new JButton("매수") {
            @Override
            protected void paintComponent(Graphics g) {
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
        btnAction.setContentAreaFilled(false); 
        btnAction.setOpaque(false); 
        btnAction.setFocusPainted(false);
        btnAction.setPreferredSize(btnSize);
        btnAction.setMinimumSize(btnSize);
        btnAction.setMaximumSize(btnSize);
        btnAction.setAlignmentX(Component.CENTER_ALIGNMENT);

        tradePanel.add(infoContainer);
        tradePanel.add(btnAction);

        JPanel editTabPanel = new JPanel(new BorderLayout());
        editTabPanel.setBackground(Color.WHITE);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(new JLabel("코인 필터: "));
        
        filterComboBox = new JComboBox<>(new String[]{"전체"});
        filterComboBox.setBackground(Color.WHITE);
        
        filterComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && !isUpdatingComboBox) {
                refreshEditList();
            }
        });
        filterPanel.add(filterComboBox);
        editTabPanel.add(filterPanel, BorderLayout.NORTH);

        editListPanel = new JPanel();
        editListPanel.setLayout(new BoxLayout(editListPanel, BoxLayout.Y_AXIS));
        editListPanel.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(editListPanel);
        scrollPane.setBorder(null);

        editTabPanel.add(scrollPane, BorderLayout.CENTER);

        inputCardPanel.add(tradePanel, "TRADE");
        inputCardPanel.add(editTabPanel, "EDIT"); 

        add(inputCardPanel, BorderLayout.CENTER);
        
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

        refreshDBData(); // 💡 초기 구동 시 DB에서 잔고 불러오기
        UpbitWebSocketDao.getInstance().addListener(this);
    }
    
    // 💡 [핵심 추가] 세션 변경 시 DB에서 잔고 및 미체결 주문 다시 불러오기
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
        refreshDBData();
    }
    
 // 💡 DB와 동기화하는 메서드 (가짜 데이터 대신 진짜 데이터를 읽어옴)
    private void refreshDBData() {
        realBalance.clear();
        realLocked.clear();
        
        // 1. 진짜 잔고 불러오기
        List<AssetDTO> assets = assetDAO.getAllAssets(this.userId, this.sessionId);
        
        // 🚨 [여기가 디버깅 핵심!] 이클립스/인텔리제이 하단 콘솔창에 결과를 출력합니다.
        System.out.println("\n[🔍 OrderPanel DB 조회 테스트]");
        System.out.println("▶ 조회 요청 ID: " + this.userId);
        System.out.println("▶ 조회 요청 세션방: " + this.sessionId);
        System.out.println("▶ DB에서 가져온 자산 개수: " + assets.size() + "개");
        
        for (AssetDTO a : assets) {
            System.out.println("  - " + a.getCurrency() + " 잔고: " + a.getBalance());
            realBalance.put(a.getCurrency(), a.getBalance());
            realLocked.put(a.getCurrency(), a.getLocked());
        }
        System.out.println("-----------------------------------\n");
        
        // 2. 이 세션의 미체결 주문 불러오기
        openOrders = openOrderDAO.getOpenOrders(this.userId, this.sessionId);
        orderCoinMap.clear();
        for (OrderDTO o : openOrders) {
            orderCoinMap.put(o.getOrderId(), o.getMarket().replace("KRW-", ""));
        }
        
        updateInfoLabel();
        refreshEditList();
    }

    public void setSelectedCoin(String coinSymbol) {
        this.selectedCoinCode = coinSymbol;
        String krName = CoinConfig.COIN_INFO.getOrDefault(coinSymbol, coinSymbol);
        
        BigDecimal cachedPrice = latestPrices.get(coinSymbol);
        if (cachedPrice != null) {
            this.currentSelectedPrice = cachedPrice;
            if (lblSelectedCoinInfo != null) {
                lblSelectedCoinInfo.setText(krName + " - 현재가 " + String.format("%,.0f", cachedPrice) + " KRW");
            }
            if (isLimitMode && priceField != null) {
                priceField.setText(cachedPrice.toPlainString());
            }
            updateOrderSummary();
        } else {
            if (lblSelectedCoinInfo != null) {
                lblSelectedCoinInfo.setText(krName + " (" + coinSymbol + ")");
            }
        }
        
        switchSide(sideIdx, null, null, null); 
        updateInfoLabel(); 
    }

    @Override
    public void onTickerUpdate(String symbol, String priceStr, String flucStr, String accPriceStr) {
        String cleanPrice = priceStr.replace(",", "").replace(" KRW", "").trim();
        if (cleanPrice.isEmpty() || cleanPrice.equals("연결중...")) return;

        BigDecimal priceBD = new BigDecimal(cleanPrice);
        latestPrices.put(symbol, priceBD);

        if (!this.selectedCoinCode.equals(symbol)) return;

        this.currentSelectedPrice = priceBD;
        String krName = com.team.coin_simulator.CoinConfig.COIN_INFO.getOrDefault(symbol, symbol);
        
        SwingUtilities.invokeLater(() -> {
            lblSelectedCoinInfo.setText(krName + " - 현재가 " + String.format("%,.0f", currentSelectedPrice) + " KRW");
            
            if (isLimitMode && priceField.getText().isEmpty()) {
                priceField.setText(cleanPrice);
                updateOrderSummary();
            }

            if (!isLimitMode) {
                updateMarketCalculation();
            }
        });
    }

    private void updateInfoLabel() {
        String assetCode = (sideIdx == 0) ? "KRW" : selectedCoinCode; 
        // 💡 가짜 데이터 대신 진짜 DB 데이터(realBalance)를 화면에 렌더링
        BigDecimal balance = realBalance.getOrDefault(assetCode, BigDecimal.ZERO);
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

    private void updateMarketCalculation() {
        try {
            String amtStr = marketAmountField.getText().replace(",", "").trim();

            if (currentSelectedPrice.compareTo(BigDecimal.ZERO) <= 0 || amtStr.isEmpty()) {
                valExpected.setText("-");
                return;
            }

            BigDecimal inputVal = new BigDecimal(amtStr);

            if (sideIdx == 0) { 
                BigDecimal expectedQty = inputVal.divide(currentSelectedPrice, 8, RoundingMode.DOWN);
                valExpected.setText("예상 수량: " + expectedQty.toPlainString() + " " + selectedCoinCode);
            } else { 
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
                if (side == 0) {
                    lblMarketUnit.setText("주문총액 (KRW)");
                    valExpected.setText("예상 수량: -");
                } else {
                    lblMarketUnit.setText("주문수량 (" + selectedCoinCode + ")");
                    valExpected.setText("예상 수령액: -");
                }
                marketAmountField.setText("");
            }
        }
        updateInfoLabel();
    }

    private void handleOrderAction() {
        if (isLimitMode) {
            handleLimitOrder();
        } else {
            handleMarketOrder();
        }
    }

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
            
            // 💡 진짜 잔고로 결제 여부 검사
            BigDecimal balance = realBalance.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal requiredAmount = (sideIdx == 0) ? total : qty;

            if (balance.compareTo(requiredAmount) < 0) {
                throw new RuntimeException("주문 가능 잔고가 부족합니다.");
            }

            OrderDTO order = new OrderDTO();
            order.setOrderId(System.currentTimeMillis());
            order.setUserId(this.userId); 
            order.setSessionId(this.sessionId); 
            order.setMarket("KRW-" + selectedCoinCode); 
            order.setSide(sideIdx == 0 ? "BID" : "ASK");
            order.setOriginalPrice(price);
            order.setOriginalVolume(qty);
            order.setRemainingVolume(qty);
            order.setStatus("WAIT");

            boolean isSuccess = orderDAO.insertOrder(order); 

            if (!isSuccess) {
                throw new RuntimeException("데이터베이스 저장에 실패했습니다.");
            }

            // 💡 자체 연산 대신 DB에서 다시 깔끔하게 불러옴
            refreshDBData();
            
            JOptionPane.showMessageDialog(this, "지정가 주문 접수 완료");

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

            if (sideIdx == 0) { 
                // 시장가 매수
                BigDecimal krwBal = realBalance.getOrDefault("KRW", BigDecimal.ZERO);
                if (krwBal.compareTo(inputVal) < 0) throw new RuntimeException("KRW 잔고가 부족합니다.");
                
                BigDecimal buyQty = OrderCalc.calculateMarketBuyQuantity(inputVal, currentSelectedPrice);
                
                OrderDTO marketOrder = new OrderDTO();
                marketOrder.setOrderId(System.currentTimeMillis());
                marketOrder.setUserId(this.userId); 
                marketOrder.setSessionId(this.sessionId); 
                marketOrder.setMarket("KRW-" + selectedCoinCode); 
                marketOrder.setSide("BID");
                marketOrder.setStatus("DONE");

                boolean isSuccess = orderDAO.executeMarketOrder(
                    marketOrder, this.userId, currentSelectedPrice, buyQty, inputVal
                );

                if (isSuccess) {
                    refreshDBData(); // 💡 DB 다시 조회
                    String msg = String.format("[체결] %s 시장가 매수 완료 (%.8f개)", selectedCoinCode, buyQty);
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    com.team.coin_simulator.Alerts.NotificationUtil.showToast(parentFrame, msg);
                } else {
                    throw new RuntimeException("DB 저장 실패");
                }
            } 
            else { 
                // 시장가 매도
                BigDecimal coinBal = realBalance.getOrDefault(selectedCoinCode, BigDecimal.ZERO);
                if (coinBal.compareTo(inputVal) < 0) throw new RuntimeException(selectedCoinCode + " 잔고가 부족합니다.");

                BigDecimal sellTotalKRW = inputVal.multiply(currentSelectedPrice);

                OrderDTO marketOrder = new OrderDTO();
                marketOrder.setOrderId(System.currentTimeMillis());
                marketOrder.setUserId(this.userId);
                marketOrder.setSessionId(this.sessionId); 
                marketOrder.setMarket("KRW-" + selectedCoinCode); 
                marketOrder.setSide("ASK");
                marketOrder.setStatus("DONE");

                boolean isSuccess = orderDAO.executeMarketOrder(
                    marketOrder, this.userId, currentSelectedPrice, inputVal, sellTotalKRW
                );

                if (isSuccess) {
                    refreshDBData(); // 💡 DB 다시 조회
                    String msg = String.format("[체결] %s 시장가 매도 완료 (%,.0f KRW)", selectedCoinCode, sellTotalKRW);
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    com.team.coin_simulator.Alerts.NotificationUtil.showToast(parentFrame, msg);
                } else {
                    throw new RuntimeException("DB 저장 실패");
                }
            }

            marketAmountField.setText(""); 

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "주문 실패: " + e.getMessage(), "에러", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelOrder(OrderDTO order) {
        String orderCoin = orderCoinMap.getOrDefault(order.getOrderId(), selectedCoinCode);
        String curr = order.getSide().equals("BID") ? "KRW" : orderCoin;
        
        BigDecimal lockedAmt = order.getSide().equals("BID") ? order.getOriginalPrice().multiply(order.getOriginalVolume()) : order.getOriginalVolume();

        boolean isDBSuccess = orderDAO.cancelOrder(order.getOrderId(), this.userId, order.getSide(), lockedAmt);
        
        if (isDBSuccess) {
            refreshDBData(); // 💡 취소 후 DB 동기화
            JOptionPane.showMessageDialog(this, "주문이 취소되었습니다.");
        } else {
            JOptionPane.showMessageDialog(this, "DB 취소 처리에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showModifyDialog(OrderDTO order) {
        String orderCoin = orderCoinMap.getOrDefault(order.getOrderId(), selectedCoinCode);
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField txtPrice = new JTextField(order.getOriginalPrice().toString());
        JTextField txtQty = new JTextField(order.getOriginalVolume().toString());

        panel.add(new JLabel("정정 가격(KRW):")); panel.add(txtPrice);
        panel.add(new JLabel("정정 수량(" + orderCoin + "):")); panel.add(txtQty);

        if (JOptionPane.showConfirmDialog(this, panel, "주문 정정", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                BigDecimal newPrice = new BigDecimal(txtPrice.getText().replace(",", ""));
                BigDecimal newQty = new BigDecimal(txtQty.getText().replace(",", ""));

                String curr = order.getSide().equals("BID") ? "KRW" : orderCoin;
                BigDecimal oldLockedAmt = order.getSide().equals("BID") ? 
                        order.getOriginalPrice().multiply(order.getOriginalVolume()) : order.getOriginalVolume();

                BigDecimal currentLocked = realLocked.getOrDefault(curr, BigDecimal.ZERO);
                BigDecimal currentBalance = realBalance.getOrDefault(curr, BigDecimal.ZERO);

                BigDecimal tempBalance = currentBalance.add(oldLockedAmt);
                BigDecimal newRequiredAmt = order.getSide().equals("BID") ? newPrice.multiply(newQty) : newQty;

                if (tempBalance.compareTo(newRequiredAmt) < 0) {
                    throw new RuntimeException("정정 주문을 위한 잔고가 부족합니다.");
                }

                if (orderDAO.modifyOrder(order.getOrderId(), this.userId, order.getSide(), oldLockedAmt, newRequiredAmt, newPrice, newQty)) {
                    refreshDBData(); // 💡 정정 성공 후 DB 동기화
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
        if (isUpdatingComboBox) return; 

        Set<String> activeCoins = new HashSet<>();
        for (OrderDTO o : openOrders) {
            activeCoins.add(orderCoinMap.getOrDefault(o.getOrderId(), "BTC"));
        }

        String currentSelection = (String) filterComboBox.getSelectedItem();
        isUpdatingComboBox = true;
        filterComboBox.removeAllItems();
        filterComboBox.addItem("전체");
        for (String coin : activeCoins) {
            filterComboBox.addItem(coin);
        }
        
        if (currentSelection != null && (activeCoins.contains(currentSelection) || currentSelection.equals("전체"))) {
            filterComboBox.setSelectedItem(currentSelection);
        } else {
            filterComboBox.setSelectedIndex(0);
            currentSelection = "전체";
        }
        isUpdatingComboBox = false;

        editListPanel.removeAll();
        for (OrderDTO order : openOrders) {
            String orderCoin = orderCoinMap.getOrDefault(order.getOrderId(), "BTC");
            
            if (currentSelection.equals("전체") || currentSelection.equals(orderCoin)) {
                editListPanel.add(createOrderEditItem(order, orderCoin));
                editListPanel.add(Box.createVerticalStrut(10));
            }
        }
        editListPanel.revalidate();
        editListPanel.repaint();
    }

    private JPanel createOrderEditItem(OrderDTO order, String coinCode) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        item.setMaximumSize(new Dimension(360, 140)); 
        
        String sideTxt = order.getSide().equals("BID") ? "매수" : "매도";
        JLabel typeLbl = new JLabel(sideTxt + " (" + coinCode + ")"); 
        typeLbl.setForeground(order.getSide().equals("BID") ? COLOR_BID : COLOR_ASK);
        typeLbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        JPanel center = new JPanel(new GridLayout(2, 2));
        center.setBackground(Color.WHITE);
        center.add(new JLabel("가격"));
        center.add(new JLabel(String.format("%,.0f KRW", order.getOriginalPrice()), SwingConstants.RIGHT));
        center.add(new JLabel("수량"));
        center.add(new JLabel(order.getOriginalVolume() + " " + coinCode, SwingConstants.RIGHT)); 
        
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
        
        JLabel lblPrice = new JLabel("주문가격 (KRW)"); lblPrice.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(lblPrice);
        
        JPanel priceRow = new JPanel(new BorderLayout(5, 0)); priceRow.setBackground(Color.WHITE);
        priceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        priceField = new JTextField(); styleField(priceField);
        
        priceRow.add(priceField, BorderLayout.CENTER);
        
        p.add(priceRow); p.add(Box.createVerticalStrut(10));
        
        JLabel lblQty = new JLabel("주문수량"); lblQty.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(lblQty);
        qtyField = new JTextField(); styleField(qtyField); p.add(qtyField);
        
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
}