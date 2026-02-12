package com.team.coin_simulator.orderbook;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OrderBookFrame extends JFrame {
    private OrderBookPanel orderBookPanel;
    private String coinSymbol;
    public OrderBookFrame(String coinSymbol) { // 생성자에서 코인 심볼을 받음
        this.coinSymbol = coinSymbol;
        setTitle("Real-time Exchange - " + coinSymbol + "/KRW");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 850);
        setLocationRelativeTo(null);

        // 1. 상단 컨테이너 (현재가 레이블 제거됨)
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(Color.WHITE);

        // 2. 패널 초기화 (심볼 전달)
        orderBookPanel = new OrderBookPanel(coinSymbol);
        
        // 3. 테이블 헤더 추출 및 추가
        javax.swing.table.JTableHeader tableHeader = orderBookPanel.getTableHeader();
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        topContainer.add(tableHeader);

        setLayout(new BorderLayout());
        add(topContainer, BorderLayout.NORTH);
        add(orderBookPanel, BorderLayout.CENTER);

        connectUpbit();
    }
    
    private void connectUpbit() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("wss://api.upbit.com/websocket/v1")
                .build();

        // market 변수를 동적으로 설정 (KRW-BTC, KRW-ETH 등)
        String market = "KRW-" + coinSymbol;
        UpbitOrderBookService listener = new UpbitOrderBookService(this, orderBookPanel, market);
        client.newWebSocket(request, listener);
    }
    
    public void updatePrice(double currentPrice) {}

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        // 실행 시 코인 심볼을 넘겨줍니다.
        SwingUtilities.invokeLater(() -> new OrderBookFrame("BTC").setVisible(true));
    }
}