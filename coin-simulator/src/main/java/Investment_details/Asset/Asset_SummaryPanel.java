package Investment_details.Asset;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class Asset_SummaryPanel extends JPanel {

    private JLabel lblTotalAsset; // 총 보유자산
    private JLabel lblTotalBuy;   // 총 매수금액
    private JLabel lblTotalPnl;   // 총 평가손익
    private JLabel lblYield;      // 총 수익률

    public Asset_SummaryPanel() {
        setLayout(new GridLayout(1, 4, 10, 0)); // 1행 4열, 가로 간격 10
        setBackground(Color.WHITE);
        setBorder(new TitledBorder("자산 현황 요약"));

        // 초기 라벨 생성 (0원으로 초기화)
        lblTotalAsset = createInfoLabel("총 보유자산", "0 KRW");
        lblTotalBuy = createInfoLabel("총 매수금액", "0 KRW");
        lblTotalPnl = createInfoLabel("총 평가손익", "0 KRW");
        lblYield = createInfoLabel("총 수익률", "0.00 %");
    }

    // 라벨 생성 헬퍼 메서드
    private JLabel createInfoLabel(String title, String initVal) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblVal = new JLabel(initVal, SwingConstants.CENTER);
        lblVal.setFont(new Font("맑은 고딕", Font.BOLD, 15));

        p.add(lblTitle);
        p.add(lblVal);
        add(p);
        
        return lblVal;
    }

    // 외부에서 데이터를 받아 화면을 갱신하는 메서드
    public void updateSummary(BigDecimal totalAsset, BigDecimal totalBuy, BigDecimal totalPnl, double yield) {
        // 총 보유자산
        lblTotalAsset.setText(String.format("%,.0f KRW", totalAsset));
        
        // 총 매수금액
        lblTotalBuy.setText(String.format("%,.0f KRW", totalBuy));
        
        // 총 평가손익 (양수면 +, 색상 변경)
        String pnlStr = String.format("%,.0f KRW", totalPnl);
        lblTotalPnl.setText(totalPnl.compareTo(BigDecimal.ZERO) > 0 ? "+" + pnlStr : pnlStr);
        lblTotalPnl.setForeground(getColor(totalPnl.doubleValue()));

        // 총 수익률
        String yieldStr = String.format("%.2f %%", yield);
        lblYield.setText(yield > 0 ? "+" + yieldStr : yieldStr);
        lblYield.setForeground(getColor(yield));
    }

    // 값에 따라 빨강(상승)/파랑(하락)/검정(보합) 반환
    private Color getColor(double val) {
        if (val > 0) return Color.RED;
        if (val < 0) return Color.BLUE;
        return Color.BLACK;
    }
}