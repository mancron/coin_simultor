package com.ream.coin_simulator.orderbook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OrderBookFrame extends JFrame {
    private OrderBookPanel orderBookPanel;
    private JLabel priceLabel;
    private double prevClose = 100000000.0; // 전일 종가 기준

    public OrderBookFrame() {
        setTitle("Real-time Exchange - BTC/KRW");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 850);
        setLocationRelativeTo(null);

        // 상단 헤더
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(Color.WHITE);
        priceLabel = new JLabel("연결 중...");
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        headerPanel.add(new JLabel("BTC (Bitcoin)"), BorderLayout.NORTH);
        headerPanel.add(priceLabel, BorderLayout.CENTER);

        orderBookPanel = new OrderBookPanel();
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(orderBookPanel, BorderLayout.CENTER);
        

        connectUpbit();
    }
    
    private void connectUpbit() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("wss://api.upbit.com/websocket/v1")
                .build();

        // "KRW-BTC" 코인의 실시간 호가 연결
        UpbitOrderBookService listener = new UpbitOrderBookService(this, orderBookPanel, "KRW-BTC");
        client.newWebSocket(request, listener);
    }
    
    public void updatePrice(double currentPrice) {
        SwingUtilities.invokeLater(() -> {
            double change = ((currentPrice - prevClose) / prevClose) * 100;
            priceLabel.setText(String.format("%,.0f KRW (%.2f%%)", currentPrice, change));
            
            // 상승 시 빨강, 하락 시 파랑 색상 변경
            if (currentPrice > prevClose) priceLabel.setForeground(Color.RED);
            else if (currentPrice < prevClose) priceLabel.setForeground(Color.BLUE);
            else priceLabel.setForeground(Color.BLACK);
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new OrderBookFrame().setVisible(true));
    }
}