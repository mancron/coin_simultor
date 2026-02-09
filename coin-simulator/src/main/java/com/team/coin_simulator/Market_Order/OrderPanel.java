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
    private Map<String, BigDecimal> mockBalance = new HashMap<>();
    private Map<String, BigDecimal> mockLocked = new HashMap<>();
    private List<OrderDTO> openOrders = new ArrayList<>();

    private CardLayout cardLayout;
    private JPanel inputCardPanel, editListPanel;
    private JTextField priceField, qtyField, marketAmountField;
    private JLabel valAvailable, valTotal;
    private JButton btnAction;

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
        btnLimit.addActionListener(e -> { isLimitMode = true; btnLimit.setSelected(true); btnMarket.setSelected(false); tradeCardLayout.show(tradeInputPanel, "LIMIT"); });
        btnMarket.addActionListener(e -> { isLimitMode = false; btnMarket.setSelected(true); btnLimit.setSelected(false); tradeCardLayout.show(tradeInputPanel, "MARKET"); });
        btnAction.addActionListener(e -> handleMockOrder());
    }
    

    private void updateMaxVolume() {
        // 필요 시 OrderCalc를 통해 계산 로직 추가
    }

    private void updateOrderSummary() {
    	updateInfoLabel();
    	try {
            String pStr = priceField.getText().replace(",", "").trim();
            String qStr = qtyField.getText().replace(",", "").trim();
            if (!pStr.isEmpty() && !qStr.isEmpty()) {
                BigDecimal price = new BigDecimal(pStr);
                BigDecimal qty = new BigDecimal(qStr);
                BigDecimal total = price.multiply(qty);
                // valTotal의 텍스트를 업데이트하도록 수정
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
                openOrders.add(order);
                updateInfoLabel();
                JOptionPane.showMessageDialog(this, "지정가 주문 접수!");
            } else {
                handleMarketOrderExecution();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "알림", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMarketOrderExecution() {
        BigDecimal currentPrice = new BigDecimal("95000000");
        BigDecimal amount = new BigDecimal(marketAmountField.getText());
        BigDecimal volume = amount.divide(currentPrice, 8, BigDecimal.ROUND_DOWN);
        mockBalance.put("KRW", mockBalance.get("KRW").subtract(amount));
        JOptionPane.showMessageDialog(this, String.format("시장가 체결 완료!\n체결가: %s\n수량: %s", currentPrice, volume));
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
        item.setMaximumSize(new Dimension(360,120));

        String sideTxt = order.getSide().equals("BID") ? "매수" : "매도";
        JLabel typeLbl = new JLabel(sideTxt);
        typeLbl.setForeground(order.getSide().equals("BID") ? COLOR_BID : COLOR_ASK);
        typeLbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        JPanel center = new JPanel(new GridLayout(2, 2));
        center.setBackground(Color.WHITE);
        center.add(new JLabel("가격"));
        center.add(new JLabel(String.format("%,.2f KRW", order.getOriginalPrice()), SwingConstants.RIGHT));
        center.add(new JLabel("수량"));
        center.add(new JLabel(order.getOriginalVolume() + " BTC", SwingConstants.RIGHT));

        JButton btnCancel = new JButton("취소");
        btnCancel.addActionListener(e -> {
            String curr = order.getSide().equals("BID") ? "KRW" : "BTC";
            BigDecimal amt = order.getSide().equals("BID") ? order.getOriginalPrice().multiply(order.getOriginalVolume()) : order.getOriginalVolume();
            mockLocked.put(curr, mockLocked.get(curr).subtract(amt));
            mockBalance.put(curr, mockBalance.get(curr).add(amt));
            openOrders.remove(order);
            refreshEditList();
            updateInfoLabel();
            JOptionPane.showMessageDialog(this, "주문이 취소되었습니다.");
        });

        item.add(typeLbl, BorderLayout.NORTH);
        item.add(center, BorderLayout.CENTER);
        item.add(btnCancel, BorderLayout.SOUTH);
        return item;
    }

    private void updateInfoLabel() {
    	String currency = (sideIdx == 1) ? "BTC" : "KRW";
        BigDecimal balance = mockBalance.get(currency);
        if (balance == null) balance = BigDecimal.ZERO;
        String format = currency.equals("KRW") ? "%,.2f" : "%,.8f";
        final String resultText = String.format(format + " " + currency, balance);
        SwingUtilities.invokeLater(() -> valAvailable.setText(resultText));
    }

    private void switchSide(int side, TabButton selected, TabButton un1, TabButton un2) {
        this.sideIdx = side;
        selected.setSelected(true);
        un1.setSelected(false);
        un2.setSelected(false);
        if (side != -1) {
            btnAction.setText(side == 0 ? "매수" : "매도");
            btnAction.setBackground(side == 0 ? COLOR_BID : COLOR_ASK);
        }
        updateInfoLabel();
    }
    
    private JPanel createLimitForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.add(new JLabel("주문가격 (KRW)"));
        priceField = new JTextField("95000000");
        styleField(priceField);
        p.add(priceField);
        p.add(Box.createVerticalStrut(15));
        p.add(new JLabel("주문수량 (BTC)"));
        qtyField = new JTextField("0.1");
        styleField(qtyField);
        p.add(qtyField);
        return p;
    }

    private JPanel createMarketForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.add(new JLabel("주문금액/수량"));
        marketAmountField = new JTextField("10000");
        styleField(marketAmountField);
        p.add(marketAmountField);
        return p;
    }

    private void styleField(JTextField tf) {
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setHorizontalAlignment(JTextField.RIGHT);
    }
    
    //코인 현재가 전달 받는 메서드 추가
    public void setSelectedCoin(String code, String price) {
    	// 1. 가격 필드 업데이트 (콤마 제거 후 숫자만 입력)
    	String cleanPrice = price.replace(",", "").replace(" KRW", "").trim();
        priceField.setText(cleanPrice);
        
        // 2. 주문 가능 잔고 라벨 업데이트를 위해 sideIdx 체크 및 갱신
        // 현재 OrderPanel은 BTC 전용으로 되어 있으나, 
        // 나중에 다중 코인을 지원하려면 여기서 코인 코드를 저장해야 합니다.
        updateInfoLabel();
        updateOrderSummary();
        
        // UI 피드백: 선택된 코인 알림 (필요 시)
        System.out.println("선택된 코인: " + code + " / 현재가: " + price);
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