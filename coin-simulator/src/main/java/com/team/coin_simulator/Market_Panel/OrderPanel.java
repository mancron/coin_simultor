package com.team.coin_simulator.Market_Panel;

import DTO.OrderDTO;
import DTO.AssetDTO;
import DTO.ExecutionDTO;
import com.team.coin_simulator.Market_Order.OrderCalc;
import com.team.coin_simulator.Market_Panel.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderPanel extends JPanel {
    private Map<String, BigDecimal> mockBalance = new HashMap<>();
    private Map<String, BigDecimal> mockLocked = new HashMap<>();
    private List<OrderDTO> openOrders = new ArrayList<>(); // DTO 리스트로 변경

    private CardLayout cardLayout;
    private JPanel inputCardPanel, editListPanel; // 정정 리스트 패널 추가
    private JTextField priceField, qtyField, marketAmountField;
    private JLabel lblAvailable, lblTotal;
    private JButton btnAction;

    private int sideIdx = 0; 
    private boolean isLimitMode = true;
    private final Color COLOR_BID = new Color(200, 30, 30);
    private final Color COLOR_ASK = new Color(30, 70, 200);

    public OrderPanel() {
        mockBalance.put("KRW", new BigDecimal("10000000"));
        mockBalance.put("BTC", new BigDecimal("0.5"));
        mockLocked.put("KRW", BigDecimal.ZERO);
        mockLocked.put("BTC", BigDecimal.ZERO);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(350, 600));
        

        // 1. 상단 탭 (주문정정 이벤트 추가)
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

        // 2. 메인 컨테이너
        cardLayout = new CardLayout();
        inputCardPanel = new JPanel(cardLayout);

        // 2-1. 매수/매도 화면 구성
        JPanel tradePanel = new JPanel();
        tradePanel.setLayout(new BoxLayout(tradePanel, BoxLayout.Y_AXIS));
        tradePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tradePanel.setBackground(Color.WHITE);

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
        
        //리스너 연결
        priceField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateMaxVolume(); }
            public void removeUpdate(DocumentEvent e) { updateMaxVolume(); }
            public void changedUpdate(DocumentEvent e) { updateMaxVolume(); }
        });
        
        qtyField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateMaxVolume(); }
            public void removeUpdate(DocumentEvent e) { updateMaxVolume(); }
            public void changedUpdate(DocumentEvent e) { updateMaxVolume(); }
        });
        
        DocumentListener updateListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateOrderSummary(); }
            public void removeUpdate(DocumentEvent e) { updateOrderSummary(); }
            public void changedUpdate(DocumentEvent e) { updateOrderSummary(); }
        };
        
        priceField.getDocument().addDocumentListener(updateListener);
        qtyField.getDocument().addDocumentListener(updateListener);

        tradePanel.add(Box.createVerticalGlue());
        lblAvailable = new JLabel("주문 가능    10,000,000 KRW");
        lblTotal = new JLabel("주문 총액              0 KRW");
        tradePanel.add(lblAvailable);
        tradePanel.add(Box.createVerticalStrut(10));
        tradePanel.add(lblTotal);
        tradePanel.add(Box.createVerticalStrut(20));

        btnAction = new JButton("매수");
        btnAction.setBackground(COLOR_BID);
        btnAction.setForeground(Color.WHITE);
        btnAction.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        btnAction.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        Dimension btnSize = new Dimension(320, 50); // 원하는 너비와 높이 설정
        btnAction.setPreferredSize(btnSize);
        btnAction.setMinimumSize(btnSize);
        btnAction.setMaximumSize(btnSize);
        btnAction.setAlignmentX(Component.CENTER_ALIGNMENT);
        tradePanel.add(btnAction);

        // 2-2. 주문정정(미체결) 화면 구성
        editListPanel = new JPanel();
        editListPanel.setLayout(new BoxLayout(editListPanel, BoxLayout.Y_AXIS));
        editListPanel.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(editListPanel);
        scrollPane.setBorder(null);

        inputCardPanel.add(tradePanel, "TRADE");
        inputCardPanel.add(scrollPane, "EDIT");
        add(inputCardPanel, BorderLayout.CENTER);

        // --- 이벤트 연결 ---
        btnBid.addActionListener(e -> { switchSide(0, btnBid, btnAsk, btnEdit); cardLayout.show(inputCardPanel, "TRADE"); });
        btnAsk.addActionListener(e -> { switchSide(1, btnAsk, btnBid, btnEdit); cardLayout.show(inputCardPanel, "TRADE"); });
        btnEdit.addActionListener(e -> { 
            switchSide(-1, btnEdit, btnBid, btnAsk); 
            refreshEditList(); // 리스트 갱신
            cardLayout.show(inputCardPanel, "EDIT"); 
        });

        btnLimit.addActionListener(e -> { isLimitMode = true; btnLimit.setSelected(true); btnMarket.setSelected(false); tradeCardLayout.show(tradeInputPanel, "LIMIT"); });
        btnMarket.addActionListener(e -> { isLimitMode = false; btnMarket.setSelected(true); btnLimit.setSelected(false); tradeCardLayout.show(tradeInputPanel, "MARKET"); });

        btnAction.addActionListener(e -> handleMockOrder());
    }

    private void updateMaxVolume() {
        String priceText = priceField.getText();
        String qtyText = qtyField.getText();

        SwingUtilities.invokeLater(() -> {
        });
    }
    
    private void updateOrderSummary() {
        // 1. 주문 가능 수량 계산 (기존 로직 호출)
        updateMaxVolume(); 

        // 2. 주문 총액 실시간 계산 (가격 * 수량)
        try {
            String priceText = priceField.getText().replace(",", "").trim();
            String qtyText = qtyField.getText().replace(",", "").trim();

            if (!priceText.isEmpty() && !qtyText.isEmpty()) {
                BigDecimal price = new BigDecimal(priceText);
                BigDecimal qty = new BigDecimal(qtyText);
                
                // 총액 계산 (가격 * 수량)
                BigDecimal total = price.multiply(qty);

                SwingUtilities.invokeLater(() -> {
                    // 천 단위 콤마 포맷팅 적용
                    lblTotal.setText("주문 총액    " + String.format("%,.2f", total) + " KRW");
                });
            } else {
                lblTotal.setText("주문 총액              0 KRW");
            }
        } catch (Exception e) {
            // 숫자 형식이 아닐 경우 총액을 0으로 초기화
            lblTotal.setText("주문 총액              0 KRW");
        }
    }
    
    private void handleMockOrder() {
        try {
            // 1. 사용할 자산 DTO 시뮬레이션 (원래는 DB나 Wallet 클래스에서 가져옴)
            String currency = (sideIdx == 0) ? "KRW" : "BTC";
            AssetDTO currentAsset = new AssetDTO(); 
            currentAsset.setCurrency(currency);
            currentAsset.setBalance(mockBalance.get(currency));
            currentAsset.setLocked(mockLocked.get(currency));

            if (isLimitMode) { // 지정가 주문
                BigDecimal price = new BigDecimal(priceField.getText().replace(",", ""));
                BigDecimal qty = new BigDecimal(qtyField.getText());
                BigDecimal total = price.multiply(qty);
                BigDecimal req = (sideIdx == 0) ? total : qty;

                // 잔고 체크
                if (currentAsset.getBalance().compareTo(req) < 0) throw new RuntimeException("잔고가 부족합니다.");

                // [변경] AssetDTO를 통한 자산 동결 처리
                currentAsset.setBalance(currentAsset.getBalance().subtract(req));
                currentAsset.setLocked(currentAsset.getLocked().add(req));

                // 가상 DB(Mock Map) 업데이트
                mockBalance.put(currency, currentAsset.getBalance());
                mockLocked.put(currency, currentAsset.getLocked());

                // OrderDTO 생성 (나중에 정정/취소 시 사용)
                OrderDTO order = new OrderDTO();
                order.setOrderId(System.currentTimeMillis());
                order.setSide(sideIdx == 0 ? "BID" : "ASK");
                order.setOriginalPrice(price);
                order.setOriginalVolume(qty);
                order.setRemainingVolume(qty); // 부분 체결을 위해 초기 잔여량 설정
                order.setStatus("WAIT");
                openOrders.add(order);
                
                updateInfoLabel();
                JOptionPane.showMessageDialog(this, "지정가 주문 접수!");

            } else {
                // 2. 시장가 주문 시 ExecutionDTO 생성
                handleMarketOrderExecution(); 
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "알림", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //체결 내역
    private void handleMarketOrderExecution() {
        // 예시: 시장가 매수 시 (현재가가 9,500만 원이라고 가정)
        BigDecimal currentPrice = new BigDecimal("95000000");
        BigDecimal amount = new BigDecimal(marketAmountField.getText());
        BigDecimal volume = amount.divide(currentPrice, 8, BigDecimal.ROUND_DOWN);

        // [추가] 체결 내역 DTO 생성
        ExecutionDTO execution = new ExecutionDTO();
        execution.setOrderId(System.currentTimeMillis()); // 가상 주문 ID
        execution.setPrice(currentPrice);
        execution.setVolume(volume);
        execution.setSide(sideIdx == 0 ? "BID" : "ASK");
        execution.setMarket("KRW-BTC");
        execution.setFee(amount.multiply(new BigDecimal("0.0005"))); // 수수료 0.05%

        // 가상 잔고 차감 로직 수행...
        mockBalance.put("KRW", mockBalance.get("KRW").subtract(amount));
        
        JOptionPane.showMessageDialog(this, String.format("시장가 체결 완료!\n체결가: %s\n수량: %s", currentPrice, volume));
    }

    // 주문정정 리스트 그리기
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
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        String sideTxt = order.getSide().equals("BID") ? "매수" : "매도";
        JLabel typeLbl = new JLabel(sideTxt);
        typeLbl.setForeground(order.getSide().equals("BID") ? COLOR_BID : COLOR_ASK);
        typeLbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        JPanel center = new JPanel(new GridLayout(2, 2));
        center.setBackground(Color.WHITE);
        center.add(new JLabel("가격")); center.add(new JLabel(String.format("%,.0f KRW", order.getOriginalPrice()), SwingConstants.RIGHT));
        center.add(new JLabel("수량")); center.add(new JLabel(order.getOriginalVolume() + " BTC", SwingConstants.RIGHT));

        JButton btnCancel = new JButton("취소");
        btnCancel.addActionListener(e -> {
            // 자산 복구 로직
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

    // (기존 updateInfoLabel, switchSide, createForm 메서드들 유지)
    private void updateInfoLabel() {
        String currency = (sideIdx == -1 || sideIdx == 0) ? "KRW" : "BTC";
        lblAvailable.setText("주문 가능    " + String.format("%,.2f", mockBalance.get(currency)) + " " + currency);
    }

    private void switchSide(int side, TabButton selected, TabButton un1, TabButton un2) {
        this.sideIdx = side;
        selected.setSelected(true); un1.setSelected(false); un2.setSelected(false);
        if(side != -1) {
            btnAction.setText(side == 0 ? "매수" : "매도");
            btnAction.setBackground(side == 0 ? COLOR_BID : COLOR_ASK);
        }
        updateInfoLabel();
    }

    private JPanel createLimitForm() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Color.WHITE);
        p.add(new JLabel("주문가격 (KRW)")); priceField = new JTextField("95000000"); styleField(priceField); p.add(priceField);
        p.add(Box.createVerticalStrut(15)); p.add(new JLabel("주문수량 (BTC)")); qtyField = new JTextField("0.1"); styleField(qtyField); p.add(qtyField);
        return p;
    }

    private JPanel createMarketForm() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Color.WHITE);
        p.add(new JLabel("주문금액/수량")); marketAmountField = new JTextField("10000"); styleField(marketAmountField); p.add(marketAmountField);
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