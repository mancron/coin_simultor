package Investment_details.profitloss;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 투자손익 하단 상세 테이블 패널
 * 수정됨: 입금, 출금 컬럼 제거
 */
public class ProfitLoss_DetailTablePanel extends JPanel {

    // [수정 1] COLUMNS 배열에서 "입금", "출금" 제거
    private static final String[] COLUMNS = {
            "일자", "일일 손익", "일일 수익률", "누적 손익", "누적 수익률",
            "기초 자산", "기말 자산"
    };

    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final Color COLOR_PROFIT = new Color(220, 60, 60);
    private static final Color COLOR_LOSS   = new Color(70, 100, 220);
    private static final Color COLOR_EVEN   = new Color(30, 30, 30);

    private final SimpleDateFormat sdf = new SimpleDateFormat("MM.dd");

    public ProfitLoss_DetailTablePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("투자손익 상세");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 6, 0));
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(240, 245, 255));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        styleHeader();
        styleColumns();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleHeader() {
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(248, 248, 248));
        header.setForeground(Color.GRAY);
        header.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        header.setPreferredSize(new Dimension(0, 32));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(248, 248, 248));
        headerRenderer.setForeground(Color.GRAY);
        headerRenderer.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        headerRenderer.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        for (int i = 0; i < COLUMNS.length; i++) {
            headerRenderer.setHorizontalAlignment(i == 0 ? SwingConstants.CENTER : SwingConstants.RIGHT);
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }

    private void styleColumns() {
        // 일자 - 중앙
        table.getColumnModel().getColumn(0).setCellRenderer(buildCenterRenderer());
        
        // 일일/누적 손익/수익률 (인덱스 1~4)
        table.getColumnModel().getColumn(1).setCellRenderer(buildColorRenderer(true));
        table.getColumnModel().getColumn(3).setCellRenderer(buildColorRenderer(true));
        table.getColumnModel().getColumn(2).setCellRenderer(buildColorRenderer(false));
        table.getColumnModel().getColumn(4).setCellRenderer(buildColorRenderer(false));

        // [수정 2] 기초자산(5), 기말자산(6)만 우측 정렬 (입출금 인덱스 7,8 제거됨)
        DefaultTableCellRenderer rightRenderer = buildRightRenderer();
        for (int i = 5; i <= 6; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        // [수정 3] 컬럼 너비 배열 수정 (마지막 70, 70 제거 및 전체 비율 조정)
        // 기존 너비 합이 대략 테이블 꽉 채우게 조정
        int[] widths = {60, 100, 90, 100, 90, 110, 110}; 
        
        for (int i = 0; i < widths.length; i++) {
            if (i < table.getColumnCount()) { // 안전장치
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
        }
    }

    private DefaultTableCellRenderer buildCenterRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        r.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        return r;
    }

    private DefaultTableCellRenderer buildRightRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        r.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        r.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        return r;
    }

    private DefaultTableCellRenderer buildColorRenderer(boolean isKrw) {
        return new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
            }

            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (value == null) return this;

                String txt = value.toString().replaceAll("[+%,\\s원KRW]", "").trim();
                try {
                    double v = Double.parseDouble(txt);
                    setForeground(v > 0 ? COLOR_PROFIT : v < 0 ? COLOR_LOSS : COLOR_EVEN);
                } catch (NumberFormatException ignore) {
                    setForeground(COLOR_EVEN);
                }
                return this;
            }
        };
    }

    public void updateTable(List<ExecutionDTO> entries) {
        tableModel.setRowCount(0);
        if (entries == null) return;

        for (ExecutionDTO e : entries) {
            String dateStr = sdf.format(e.getDate());

            // 일일 손익
            long dp = e.getDailyPnl();
            String dpStr = (dp >= 0 ? "+" : "") + String.format("%,d", dp);

            // 일일 수익률
            double dy = e.getDailyYield();
            String dyStr = (dy >= 0 ? "+" : "") + String.format("%.2f%%", dy);

            // 누적 손익
            long cp = e.getCumulativePnl();
            String cpStr = (cp >= 0 ? "+" : "") + String.format("%,d", cp);

            // 누적 수익률
            double cy = e.getCumulativeYield();
            String cyStr = (cy >= 0 ? "+" : "") + String.format("%.2f%%", cy);

            // [수정 4] addRow에서 입금, 출금 데이터 제외 (총 7개 컬럼)
            tableModel.addRow(new Object[]{
                    dateStr,
                    dpStr,
                    dyStr,
                    cpStr,
                    cyStr,
                    String.format("%,d", e.getBaseAsset()),
                    String.format("%,d", e.getFinalAsset())
            });
        }
    }
}