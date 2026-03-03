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
import java.math.RoundingMode;
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
    public void updateTable(List<ExecutionDTO> executions, String userId, long sessionId) {
        tableModel.setRowCount(0);
        if (executions == null || executions.isEmpty()) return;

        long initialSeedMoney = dao.getInitialSeedMoney(userId, sessionId);

        // ── 1. 오름차순 정렬 (과거 → 최신, 상태 누적용) ─────────────────
        List<ExecutionDTO> sorted = new ArrayList<>(executions);
        sorted.sort(Comparator.comparing(ExecutionDTO::getExecutedAt));

        // ── 2. 날짜별 그룹화 ────────────────────────────────────────────
        // LinkedHashMap → 오름차순 유지
        Map<Date, List<ExecutionDTO>> dailyMap = new LinkedHashMap<>();
        for (ExecutionDTO exec : sorted) {
            Date dateOnly = truncateToDay(exec.getExecutedAt());
            dailyMap.computeIfAbsent(dateOnly, k -> new ArrayList<>()).add(exec);
        }

        // ── 3. 상태 추적용 변수 ─────────────────────────────────────────
        // KRW 잔고 (초기자본으로 시작)
        BigDecimal krwBalance = new BigDecimal(initialSeedMoney);

        // 코인별 보유수량 & 평단가
        Map<String, BigDecimal> coinQty = new HashMap<>();
        Map<String, BigDecimal> coinAvg = new HashMap<>();

        BigDecimal cumulativePnl = BigDecimal.ZERO;

        // 최신순 표시를 위해 행을 별도 리스트에 모은 뒤 역순 삽입
        List<Object[]> rows = new ArrayList<>();

        // ── 4. 날짜별 반복 ─────────────────────────────────────────────
        for (Map.Entry<Date, List<ExecutionDTO>> entry : dailyMap.entrySet()) {
            Date date = entry.getKey();
            List<ExecutionDTO> dayExecs = entry.getValue();

            // 하루 시작 시점의 자산 스냅샷 (기초자산 계산용)
            BigDecimal baseKrw = krwBalance;
            Map<String, BigDecimal> baseCoinQty = new HashMap<>(coinQty);
            Map<String, BigDecimal> baseCoinAvg = new HashMap<>(coinAvg);

            BigDecimal dailyPnl = BigDecimal.ZERO;

            // ── 4-1. 당일 체결 처리 ────────────────────────────────────
            for (ExecutionDTO exec : dayExecs) {
                String coin = exec.getMarket() != null
                        ? exec.getMarket().replace("KRW-", "") : "UNKNOWN";
                BigDecimal fee  = exec.getFee()    != null ? exec.getFee()    : BigDecimal.ZERO;
                BigDecimal price  = exec.getPrice()  != null ? exec.getPrice()  : BigDecimal.ZERO;
                BigDecimal volume = exec.getVolume() != null ? exec.getVolume() : BigDecimal.ZERO;

                if ("BID".equals(exec.getSide())) {
                    // 매수: KRW 차감(원금 + 수수료), 코인 증가, 평단가 갱신
                    BigDecimal totalCost = price.multiply(volume).add(fee);
                    krwBalance = krwBalance.subtract(totalCost);

                    BigDecimal prevQty = coinQty.getOrDefault(coin, BigDecimal.ZERO);
                    BigDecimal prevAvg = coinAvg.getOrDefault(coin, BigDecimal.ZERO);
                    BigDecimal newQty  = prevQty.add(volume);

                    if (newQty.compareTo(BigDecimal.ZERO) > 0) {
                        // 가중평균 평단가
                        BigDecimal newAvg = prevQty.multiply(prevAvg)
                                .add(volume.multiply(price))
                                .divide(newQty, 8, RoundingMode.HALF_UP);
                        coinAvg.put(coin, newAvg);
                    }
                    coinQty.put(coin, newQty);

                    // [Bug 1 수정] 매수 수수료만 일손익에 반영 (realized_pnl 없음)
                    dailyPnl = dailyPnl.subtract(fee);

                } else if ("ASK".equals(exec.getSide())) {
                    // 매도: KRW 증가(매도대금 - 수수료), 코인 감소
                    BigDecimal proceeds = price.multiply(volume).subtract(fee);
                    krwBalance = krwBalance.add(proceeds);

                    BigDecimal prevQty = coinQty.getOrDefault(coin, BigDecimal.ZERO);
                    BigDecimal newQty  = prevQty.subtract(volume);
                    coinQty.put(coin, newQty.compareTo(BigDecimal.ZERO) < 0
                            ? BigDecimal.ZERO : newQty);

                    // 전량 매도 시 평단가 초기화
                    if (coinQty.get(coin).compareTo(BigDecimal.ZERO) <= 0) {
                        coinAvg.put(coin, BigDecimal.ZERO);
                    }

                    // [Bug 1 수정] realized_pnl에 이미 매도 수수료가 포함되어 있으므로
                    // fee를 별도 차감하지 않음
                    if (exec.getRealizedPnl() != null) {
                        dailyPnl = dailyPnl.add(exec.getRealizedPnl());
                    }
                }
            }

            cumulativePnl = cumulativePnl.add(dailyPnl);

            // ── 4-2. [Bug 2 수정] 기초·기말 자산 = KRW + 보유코인 평가금액 ──

            // 기초자산: 하루 시작 KRW + 하루 시작 코인 보유 × 평단가
            BigDecimal baseAsset = baseKrw.add(evalCoins(baseCoinQty, baseCoinAvg));

            // 기말자산: 하루 끝 KRW + 하루 끝 코인 보유 × 평단가
            BigDecimal endAsset  = krwBalance.add(evalCoins(coinQty, coinAvg));

            // ── 4-3. 수익률 계산 ────────────────────────────────────────
            long dailyPnlLong  = dailyPnl.setScale(0, RoundingMode.HALF_UP).longValue();
            long cumPnlLong    = cumulativePnl.setScale(0, RoundingMode.HALF_UP).longValue();
            long baseAssetLong = baseAsset.setScale(0, RoundingMode.HALF_UP).longValue();
            long endAssetLong  = endAsset.setScale(0, RoundingMode.HALF_UP).longValue();

            double dailyYield = baseAssetLong > 0
                    ? ((double) dailyPnlLong / baseAssetLong) * 100 : 0.0;
            double cumYield   = initialSeedMoney > 0
                    ? ((double) cumPnlLong / initialSeedMoney) * 100 : 0.0;

            // ── 4-4. 포맷팅 ─────────────────────────────────────────────
            String dpStr = (dailyPnlLong >= 0 ? "+" : "") + String.format("%,d", dailyPnlLong);
            String dyStr = (dailyYield  >= 0 ? "+" : "") + String.format("%.2f%%", dailyYield);
            String cpStr = (cumPnlLong  >= 0 ? "+" : "") + String.format("%,d", cumPnlLong);
            String cyStr = (cumYield    >= 0 ? "+" : "") + String.format("%.2f%%", cumYield);

            rows.add(new Object[]{
                    sdf.format(date),
                    dpStr, dyStr,
                    cpStr, cyStr,
                    String.format("%,d", baseAssetLong),
                    String.format("%,d", endAssetLong)
            });
        }

        // ── 5. 최신순(역순)으로 테이블에 삽입 ─────────────────────────
        for (int i = rows.size() - 1; i >= 0; i--) {
            tableModel.addRow(rows.get(i));
        }
    }

    // ── 헬퍼: 날짜 시각 절사 ─────────────────────────────────────────────
    private Date truncateToDay(java.sql.Timestamp ts) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ts);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // ── 헬퍼: 코인 포지션 평가금액 합산 ─────────────────────────────────
    private BigDecimal evalCoins(Map<String, BigDecimal> qtys, Map<String, BigDecimal> avgs) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> e : qtys.entrySet()) {
            BigDecimal qty = e.getValue();
            BigDecimal avg = avgs.getOrDefault(e.getKey(), BigDecimal.ZERO);
            if (qty.compareTo(BigDecimal.ZERO) > 0 && avg.compareTo(BigDecimal.ZERO) > 0) {
                total = total.add(qty.multiply(avg));
            }
        }
        return total;
    }
    
    
    
    
    
}