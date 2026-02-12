package com.team.coin_simulator.orderbook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

class OrderBookPanel extends JPanel {
    private DefaultTableModel askModel;
    private DefaultTableModel bidModel;
    private JTable askTable;
    private JTable bidTable;
    private JScrollPane scrollPane;
    private boolean isFirstUpdate = true;
    private String coinSymbol; // 추가

    public OrderBookPanel(String coinSymbol) {
        this.coinSymbol = coinSymbol; // 전달받은 심볼 저장
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Quantity 옆의 괄호에 동적으로 코인 심볼 삽입
        String[] columns = {"Price (Change %)", "Quantity (" + coinSymbol + ")"};

        // 1. 테이블 설정
        askModel = new DefaultTableModel(columns, 0);
        askTable = createStyledTable(askModel, new Color(240, 248, 255), Color.BLUE);
        
        bidModel = new DefaultTableModel(columns, 0);
        bidTable = createStyledTable(bidModel, new Color(255, 245, 245), Color.RED);
        bidTable.setTableHeader(null); 

        // 2. 컨테이너 설정
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.add(askTable);
        container.add(bidTable);

        scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // 휠 스크롤 속도 조절
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    // Frame에서 헤더를 가져갈 수 있도록 public 메소드 추가
    public javax.swing.table.JTableHeader getTableHeader() {
        return askTable.getTableHeader();
    }

    private JTable createStyledTable(DefaultTableModel model, Color bgColor, Color fgColor) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table.setRowHeight(35);
        table.setBackground(bgColor);
        table.setForeground(fgColor);
        table.setFont(new Font("Monospaced", Font.BOLD, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        return table;
    }

    public void updateData(TreeMap<Double, Double> asks, TreeMap<Double, Double> bids, double prevClose) {
        SwingUtilities.invokeLater(() -> {
            // 1. 기존 데이터 초기화
            askModel.setRowCount(0);
            bidModel.setRowCount(0);

            // 매도 데이터 20개 제한 및 추가 (높은 가격 -> 낮은 가격 순서로 아래로 쌓임)
            asks.entrySet().stream()
                .limit(20)
                .sorted(Collections.reverseOrder(java.util.Map.Entry.comparingByKey()))
                .forEach(entry -> addOrderRow(askModel, entry.getKey(), entry.getValue(), prevClose));

            // 매수 데이터 20개 제한 및 추가 (현재가와 가까운 높은 가격부터 아래로 쌓임)
            bids.entrySet().stream()
                .limit(20)
                .forEach(entry -> addOrderRow(bidModel, entry.getKey(), entry.getValue(), prevClose));

            // 2. 중앙 집중 고정: 11번째 행(인덱스 10)을 화면 최상단으로 올리기
            // 결과적으로 화면에는 매도 하위 10개와 매수 상위 10개가 보입니다.
            //처음 업데이트될 때만 중앙 위치(11번째 행)로 고정
            if (isFirstUpdate && askModel.getRowCount() >= 11) {
                Rectangle rect = askTable.getCellRect(10, 0, true);
                scrollPane.getViewport().setViewPosition(new Point(0, rect.y));
                
                // 한 번 실행했으니 false로 변경하여 다음부터는 사용자 스크롤을 방해하지 않음
                isFirstUpdate = false; 
            }
        });
    }

    // 데이터 추가 반복 로직 분리
    private void addOrderRow(DefaultTableModel model, double price, double volume, double prevClose) {
        double changeRate = ((price - prevClose) / prevClose) * 100;
        String priceStr = String.format("%,.0f (%.2f%%)", price, changeRate);
        model.addRow(new Object[]{priceStr, String.format("%.4f", volume)});
    }
}