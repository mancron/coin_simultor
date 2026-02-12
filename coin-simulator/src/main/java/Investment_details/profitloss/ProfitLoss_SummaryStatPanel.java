package Investment_details.profitloss;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * 투자손익 상단 요약 패널
 * 이미지 기준: 기간 누적 손익 / 기간 누적 수익률 / 기간 평균 투자금액
 * ProfitLoss_MainPanel 에서 호출, ChartAreaPanel 상단에 배치
 */
public class ProfitLoss_SummaryStatPanel extends JPanel {

    // ---- 표시 라벨 ----
    private final JLabel lblPnlValue;      // 기간 누적 손익 숫자
    private final JLabel lblYieldValue;    // 기간 누적 수익률 숫자
    private final JLabel lblAvgInvValue;   // 기간 평균 투자금액 숫자

    private static final Color COLOR_PROFIT = new Color(220, 60, 60);
    private static final Color COLOR_LOSS   = new Color(70, 100, 220);
    private static final Color COLOR_EVEN   = new Color(30, 30, 30);

    public ProfitLoss_SummaryStatPanel() {
        setLayout(new GridLayout(1, 3, 1, 0));
        setBackground(new Color(250, 250, 250));
        setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        setPreferredSize(new Dimension(0, 75));

        // 각 항목 패널 생성
        lblPnlValue    = new JLabel("0 KRW", SwingConstants.LEFT);
        lblYieldValue  = new JLabel("0.00 %", SwingConstants.LEFT);
        lblAvgInvValue = new JLabel("0 KRW", SwingConstants.LEFT);

        add(buildStatCell("기간 누적 손익",    lblPnlValue,    true));
        add(buildStatCell("기간 누적 수익률",  lblYieldValue,  false));
        add(buildStatCell("기간 평균 투자금액", lblAvgInvValue, false));
    }

    /** 각 통계 셀 (제목 + 값 라벨) 생성 */
    private JPanel buildStatCell(String title, JLabel valueLabel, boolean hasDivider) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(new Color(250, 250, 250));
        cell.setBorder(new EmptyBorder(10, 20, 10, 20));

        if (hasDivider) {
            // 오른쪽 구분선
            cell.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
                    new EmptyBorder(10, 20, 10, 20)));
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 17));
        valueLabel.setForeground(COLOR_EVEN);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cell.add(Box.createVerticalGlue());
        cell.add(titleLabel);
        cell.add(Box.createVerticalStrut(4));
        cell.add(valueLabel);
        cell.add(Box.createVerticalGlue());

        return cell;
    }

    /**
     * 메인패널에서 집계 결과를 받아 화면 갱신
     *
     * @param cumulativePnl    기간 누적 손익 (원)
     * @param cumulativeYield  기간 누적 수익률 (%)
     * @param avgInvestment    기간 평균 투자금액 (원)
     */
    public void updateSummary(long cumulativePnl, double cumulativeYield, long avgInvestment) {
        // 누적 손익
        String pnlStr = String.format("%,d KRW", cumulativePnl);
        if (cumulativePnl > 0) {
            lblPnlValue.setText("+" + pnlStr);
            lblPnlValue.setForeground(COLOR_PROFIT);
        } else if (cumulativePnl < 0) {
            lblPnlValue.setText(pnlStr);
            lblPnlValue.setForeground(COLOR_LOSS);
        } else {
            lblPnlValue.setText(pnlStr);
            lblPnlValue.setForeground(COLOR_EVEN);
        }

        // 누적 수익률
        String yieldStr = String.format("%.2f %%", cumulativeYield);
        if (cumulativeYield > 0) {
            lblYieldValue.setText("+" + yieldStr);
            lblYieldValue.setForeground(COLOR_PROFIT);
        } else if (cumulativeYield < 0) {
            lblYieldValue.setText(yieldStr);
            lblYieldValue.setForeground(COLOR_LOSS);
        } else {
            lblYieldValue.setText(yieldStr);
            lblYieldValue.setForeground(COLOR_EVEN);
        }

        // 평균 투자금액
        lblAvgInvValue.setText(String.format("%,d KRW", avgInvestment));
        lblAvgInvValue.setForeground(COLOR_EVEN);
    }
}
