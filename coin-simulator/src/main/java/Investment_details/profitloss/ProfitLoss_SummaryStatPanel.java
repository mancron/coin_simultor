package Investment_details.profitloss;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class ProfitLoss_SummaryStatPanel extends JPanel {

    private final JLabel lblPnlValue;      
    private final JLabel lblYieldValue;    
    private final JLabel lblAvgInvValue;   
    private final JLabel lblFeeValue;      // 수수료 라벨 추가

    private static final Color COLOR_PROFIT = new Color(220, 60, 60);
    private static final Color COLOR_LOSS   = new Color(70, 100, 220);
    private static final Color COLOR_EVEN   = new Color(30, 30, 30);

    public ProfitLoss_SummaryStatPanel() {
        setLayout(new GridLayout(1, 4, 1, 0)); // 1x4 레이아웃으로 변경
        setBackground(new Color(250, 250, 250));
        setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        setPreferredSize(new Dimension(0, 75));

        lblPnlValue    = new JLabel("0 KRW", SwingConstants.LEFT);
        lblYieldValue  = new JLabel("0.00 %", SwingConstants.LEFT);
        lblFeeValue    = new JLabel("0 KRW", SwingConstants.LEFT); // 추가
        lblAvgInvValue = new JLabel("0 KRW", SwingConstants.LEFT);

        add(buildStatCell("기간 누적 순손익",    lblPnlValue,    true)); // 명칭 변경
        add(buildStatCell("기간 누적 수익률",  lblYieldValue,  true));
        add(buildStatCell("기간 총 수수료",    lblFeeValue,    true)); // 추가
        add(buildStatCell("기간 평균 투자금액", lblAvgInvValue, false));
    }

    private JPanel buildStatCell(String title, JLabel valueLabel, boolean hasDivider) {
        // 기존 메서드 내용 동일 유지
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(new Color(250, 250, 250));
        cell.setBorder(new EmptyBorder(10, 20, 10, 20));

        if (hasDivider) {
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
     * totalFee 매개변수 추가
     */
    public void updateSummary(long cumulativePnl, double cumulativeYield, long avgInvestment, long totalFee) {
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

        // 총 수수료 표시
        lblFeeValue.setText(String.format("%,d KRW", totalFee));
        lblFeeValue.setForeground(COLOR_PROFIT); // 수수료는 지출이므로 필요시 색상 변경 가능

        // 평균 투자금액
        lblAvgInvValue.setText(String.format("%,d KRW", avgInvestment));
        lblAvgInvValue.setForeground(COLOR_EVEN);
    }
}