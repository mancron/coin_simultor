package Investment_details.Asset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import DTO.MyAssetStatusDTO;

public class Assets_TablePanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    // 테이블 헤더 정의
    private String[] headers = {"코인명", "보유수량", "매수평균가", "현재가", "평가금액", "수익률(%)"};

    public Assets_TablePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. 테이블 모델 설정 (셀 수정 불가)
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 2. 테이블 생성
        table = new JTable(tableModel);
        table.setRowHeight(30); // 행 높이 설정
        table.setShowGrid(false); // 그리드 숨김 (깔끔한 디자인)
        table.getTableHeader().setReorderingAllowed(false); // 컬럼 이동 방지
        
        // 3. 셀 렌더러 설정 (정렬 및 색상)
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT); // 우측 정렬
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // 중앙 정렬

        // 코인명(0번)은 중앙 정렬
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // 나머지 숫자 컬럼들(1~4번)은 우측 정렬
        for(int i=1; i<5; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        // 4. 수익률 컬럼(5번) 커스텀 렌더러 (색상 처리)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String valStr = value.toString().replace("%", "").trim();
                try {
                    double val = Double.parseDouble(valStr);
                    if (val > 0) c.setForeground(Color.RED);       // 수익
                    else if (val < 0) c.setForeground(Color.BLUE); // 손실
                    else c.setForeground(Color.BLACK);             // 보합
                } catch (Exception e) {
                    c.setForeground(Color.BLACK);
                }
                
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        // 스크롤 페인에 추가
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    // 데이터 갱신 메서드
    public void updateTable(List<MyAssetStatusDTO> list) {
        tableModel.setRowCount(0); // 기존 데이터 삭제
        
        if (list == null) return;

        for (MyAssetStatusDTO dto : list) {
            Object[] row = {
                dto.getCurrency(),
                String.format("%,.4f", dto.getBalance()),     // 보유수량 (소수점 4자리)
                String.format("%,.0f", dto.getAvgPrice()),    // 평단가 (정수)
                String.format("%,.0f", dto.getCurrentPrice()),// 현재가
                String.format("%,.0f", dto.getTotalValue()),  // 평가금액
                String.format("%.2f", dto.getProfitRate())    // 수익률
            };
            tableModel.addRow(row);
        }
    }
}