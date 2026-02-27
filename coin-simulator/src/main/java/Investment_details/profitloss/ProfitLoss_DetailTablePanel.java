package Investment_details.profitloss;

import DTO.ExecutionDTO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import DAO.ProfitLossDAO;

import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 투자손익 하단 상세 테이블 패널 (ExecutionDTO 사용)
 */
public class ProfitLoss_DetailTablePanel extends JPanel {

    private static final String[] COLUMNS = {
            "일자", "일일 손익", "일일 수익률", "누적 손익", "누적 수익률",
            "기초 자산", "기말 자산"
    };

    private final DefaultTableModel tableModel;
    private final JTable table;
    private ProfitLossDAO dao;

    private static final Color COLOR_PROFIT = new Color(220, 60, 60);
    private static final Color COLOR_LOSS   = new Color(70, 100, 220);
    private static final Color COLOR_EVEN   = new Color(30, 30, 30);

    private final SimpleDateFormat sdf = new SimpleDateFormat("MM.dd");

    public ProfitLoss_DetailTablePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        dao = new ProfitLossDAO();

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
        
        // 전체 좌측 정렬 및 좌측 여백 추가
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(0, 12, 0, 0)));

        for (int i = 0; i < COLUMNS.length; i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }

    private void styleColumns() {
        DefaultTableCellRenderer leftRenderer = buildLeftRenderer();
        
        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        
        table.getColumnModel().getColumn(1).setCellRenderer(buildColorRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(buildColorRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(buildColorRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(buildColorRenderer());

        for (int i = 5; i <= 6; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }

        int[] widths = {60, 100, 90, 100, 90, 110, 110}; 
        for (int i = 0; i < widths.length; i++) {
            if (i < table.getColumnCount()) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
        }
    }

    private DefaultTableCellRenderer buildLeftRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.LEFT); // 좌측 정렬
        r.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        r.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0)); // 좌측 여백
        return r;
    }

    private DefaultTableCellRenderer buildColorRenderer() {
        return new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.LEFT); // 좌측 정렬
                setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0)); // 좌측 여백
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

    /**
     * ExecutionDTO 리스트로 테이블 업데이트
     */
    public void updateTable(List<ExecutionDTO> executions, String userId) {
        tableModel.setRowCount(0);
        if (executions == null || executions.isEmpty()) return;

        // 초기 자본금 조회
        long initialSeedMoney = dao.getInitialSeedMoney(userId);

        // 날짜별로 그룹화
        Map<Date, BigDecimal> dailyPnlMap = new TreeMap<>(Collections.reverseOrder());
        
        for (ExecutionDTO exec : executions) {
            Date date = new Date(exec.getExecutedAt().getTime());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dateOnly = cal.getTime();
            
            BigDecimal netPnl = BigDecimal.ZERO;
            
            // 1. 매도인 경우 실현 손익 추가
            if ("ASK".equals(exec.getSide()) && exec.getRealizedPnl() != null) {
                netPnl = netPnl.add(exec.getRealizedPnl());
            }
            
            // 2. 수수료 차감 (매수, 매도 모두 적용)
            if (exec.getFee() != null) {
                netPnl = netPnl.subtract(exec.getFee());
            }
            
            dailyPnlMap.merge(dateOnly, netPnl, BigDecimal::add);
        }

        // 테이블 행 생성
        BigDecimal cumulativePnl = BigDecimal.ZERO;
        long currentAsset = initialSeedMoney;

        for (Map.Entry<Date, BigDecimal> entry : dailyPnlMap.entrySet()) {
            String dateStr = sdf.format(entry.getKey());
            BigDecimal dailyPnl = entry.getValue();
            
            long baseAsset = currentAsset; // 기초자산
            cumulativePnl = cumulativePnl.add(dailyPnl);
            currentAsset = initialSeedMoney + cumulativePnl.longValue(); // 기말자산

            // 일일 수익률
            double dailyYield = baseAsset > 0 
                ? (dailyPnl.doubleValue() / baseAsset) * 100 
                : 0.0;

            // 누적 수익률
            double cumulativeYield = initialSeedMoney > 0
                ? (cumulativePnl.doubleValue() / initialSeedMoney) * 100
                : 0.0;

            // 포맷팅
            String dpStr = (dailyPnl.longValue() > 0 ? "+" : "") + 
                          String.format("%,d", dailyPnl.longValue());
            String dyStr = (dailyYield > 0 ? "+" : "") + 
                          String.format("%.2f%%", dailyYield);
            String cpStr = (cumulativePnl.longValue() > 0 ? "+" : "") + 
                          String.format("%,d", cumulativePnl.longValue());
            String cyStr = (cumulativeYield > 0 ? "+" : "") + 
                          String.format("%.2f%%", cumulativeYield);

            tableModel.addRow(new Object[]{
                    dateStr,
                    dpStr,
                    dyStr,
                    cpStr,
                    cyStr,
                    String.format("%,d", baseAsset),
                    String.format("%,d", currentAsset)
            });
        }
    }
}