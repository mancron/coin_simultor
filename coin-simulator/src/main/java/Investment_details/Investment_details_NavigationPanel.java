package Investment_details;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import com.team.coin_simulator.Market_Panel.TabButton;

/**
 * 투자내역 탭 네비게이션 패널
 * 업비트 스타일: 보유자산 | 투자손익 | 거래내역 | 미체결
 */
public class Investment_details_NavigationPanel extends JPanel {
    
    private List<TabButton> tabButtons = new ArrayList<>();
    private TabButton btnAsset, btnProfitLoss, btnHistory, btnOpenOrder;
    
    public Investment_details_NavigationPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        setPreferredSize(new Dimension(0, 50));
        
        // 탭 버튼 생성
        btnAsset = createTabButton("보유자산");
        btnProfitLoss = createTabButton("투자손익");
        btnHistory = createTabButton("거래내역");
        btnOpenOrder = createTabButton("미체결");
        
        // 버튼 목록에 추가
        tabButtons.add(btnAsset);
        tabButtons.add(btnProfitLoss);
        tabButtons.add(btnHistory);
        tabButtons.add(btnOpenOrder);
        
        // 패널에 추가
        add(btnAsset);
        add(btnProfitLoss);
        add(btnHistory);
        add(btnOpenOrder);
        
        // 기본 선택: 보유자산
        btnAsset.setSelected(true);
    }
    
    /**
     * 탭 버튼 생성
     */
    private TabButton createTabButton(String text) {
        TabButton button = new TabButton(text);
        button.setPreferredSize(new Dimension(120, 50));
        return button;
    }
    
    /**
     * 탭 선택 상태 업데이트
     */
    private void updateSelection(TabButton selected) {
        for (TabButton btn : tabButtons) {
            btn.setSelected(btn == selected);
        }
    }
    
    /**
     * 보유자산 탭 리스너
     */
    public void addAssetTabListener(ActionListener listener) {
        btnAsset.addActionListener(e -> {
            updateSelection(btnAsset);
            listener.actionPerformed(e);
        });
    }
    
    /**
     * 투자손익 탭 리스너
     */
    public void addProfitLossTabListener(ActionListener listener) {
        btnProfitLoss.addActionListener(e -> {
            updateSelection(btnProfitLoss);
            listener.actionPerformed(e);
        });
    }
    
    /**
     * 거래내역 탭 리스너
     */
    public void addHistoryTabListener(ActionListener listener) {
        btnHistory.addActionListener(e -> {
            updateSelection(btnHistory);
            listener.actionPerformed(e);
        });
    }
    
    /**
     * 미체결 탭 리스너
     */
    public void addOpenOrderTabListener(ActionListener listener) {
        btnOpenOrder.addActionListener(e -> {
            updateSelection(btnOpenOrder);
            listener.actionPerformed(e);
        });
    }
}