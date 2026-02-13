package com.team.coin_simulator.orderbook;

import java.awt.*;
import java.util.Collections;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class OrderBookPanel extends JPanel {
    private DefaultTableModel askModel;
    private DefaultTableModel bidModel;
    private JTable askTable;
    private JTable bidTable;
    private JScrollPane scrollPane;
    private boolean isFirstUpdate = true;
    private String coinSymbol;
    private WebSocket webSocket; // 웹소켓 객체 보관

    public OrderBookPanel(String coinSymbol) {
        this.coinSymbol = coinSymbol;
        setLayout(new BorderLayout());
        
        // 1. UI 초기화
        initComponents();
        
        // 2. 상단 헤더 추가 (기존 Frame에서 하던 작업)
        javax.swing.table.JTableHeader tableHeader = askTable.getTableHeader();
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        add(tableHeader, BorderLayout.NORTH);

        // 3. 자체 웹소켓 연결 시작
        connectUpbit();
    }

    private void initComponents() {
        String[] columns = {"Price (Change %)", "Quantity (" + coinSymbol + ")"};

        askModel = new DefaultTableModel(columns, 0);
        askTable = createStyledTable(askModel, new Color(240, 248, 255), new Color(20, 20, 255));
        
        bidModel = new DefaultTableModel(columns, 0);
        bidTable = createStyledTable(bidModel, new Color(255, 245, 245), new Color(255, 20, 20));
        bidTable.setTableHeader(null); 

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.add(askTable);
        container.add(bidTable);

        scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }

    private JTable createStyledTable(DefaultTableModel model, Color bgColor, Color fgColor) {
        JTable table = new JTable(model) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table.setRowHeight(30); // 높이 약간 조절
        table.setBackground(bgColor);
        table.setForeground(fgColor);
        table.setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        return table;
    }

    private void connectUpbit() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("wss://api.upbit.com/websocket/v1").build();
        String market = "KRW-" + coinSymbol;
        
        // 서비스 생성 시 'this'를 넘겨 직접 제어하게 함
        UpbitOrderBookService listener = new UpbitOrderBookService(this, market);
        this.webSocket = client.newWebSocket(request, listener);
    }

    // 패널이 화면에서 제거될 때 소켓을 닫기 위한 메소드
    public void closeConnection() {
        if (webSocket != null) {
            webSocket.close(1000, "Panel Closed");
        }
    }

    public void updateData(TreeMap<Double, Double> asks, TreeMap<Double, Double> bids, double prevClose) {
        SwingUtilities.invokeLater(() -> {
            askModel.setRowCount(0);
            bidModel.setRowCount(0);

            // 매도 (가격 내림차순)
            asks.entrySet().stream()
                .limit(15)
                .sorted(Collections.reverseOrder(java.util.Map.Entry.comparingByKey()))
                .forEach(entry -> addOrderRow(askModel, entry.getKey(), entry.getValue(), prevClose));

            // 매수 (가격 내림차순)
            bids.entrySet().stream()
                .limit(15)
                .forEach(entry -> addOrderRow(bidModel, entry.getKey(), entry.getValue(), prevClose));

            // 초기 위치 설정 로직 개선
            if (isFirstUpdate && askModel.getRowCount() > 0) {
                // 데이터가 로드된 후 UI 레이아웃이 확정될 수 있도록 한 번 더 큐에 넣음
                SwingUtilities.invokeLater(() -> {
                    // 매도 테이블의 전체 높이가 곧 매수/매도 경계선 지점입니다.
                    int boundaryY = askTable.getHeight();
                    
                    // 뷰포트의 절반만큼 위로 올려서 경계선이 중앙에 오게 함
                    int viewportHeight = scrollPane.getViewport().getHeight();
                    int targetScroll = boundaryY - (viewportHeight / 2);
                    
                    scrollPane.getVerticalScrollBar().setValue(targetScroll);
                    isFirstUpdate = false;
                });
            }
        });
    }

    private void addOrderRow(DefaultTableModel model, double price, double volume, double prevClose) {
        double changeRate = ((price - prevClose) / prevClose) * 100;
        String priceStr = String.format("%,.0f (%.2f%%)", price, changeRate);
        model.addRow(new Object[]{priceStr, String.format("%.4f", volume)});
    }
    
}