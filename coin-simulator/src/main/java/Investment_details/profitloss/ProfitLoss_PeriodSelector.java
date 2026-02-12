package Investment_details.profitloss;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 투자손익 기간 선택 패널
 * 주/월/년 단위로 조회 기간을 변경할 수 있는 버튼 제공
 */
public class ProfitLoss_PeriodSelector extends JPanel {
    
    private JButton btn1Week;
    private JButton btn1Month;
    private JButton btn3Month;
    private JButton btn6Month;
    private JButton btn1Year;
    private JButton btnAll;
    
    private JButton selectedButton;
    
    private static final Color COLOR_SELECTED = new Color(52, 152, 219);
    private static final Color COLOR_DEFAULT = new Color(245, 245, 245);
    private static final Color COLOR_HOVER = new Color(230, 240, 250);
    private static final Color TEXT_SELECTED = Color.WHITE;
    private static final Color TEXT_DEFAULT = new Color(80, 80, 80);
    
    public ProfitLoss_PeriodSelector() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // 라벨
        JLabel label = new JLabel("조회 기간:");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        label.setForeground(new Color(60, 60, 60));
        add(label);
        
        // 버튼 생성
        btn1Week = createPeriodButton("1주일", 7);
        btn1Month = createPeriodButton("1개월", 30);
        btn3Month = createPeriodButton("3개월", 90);
        btn6Month = createPeriodButton("6개월", 180);
        btn1Year = createPeriodButton("1년", 365);
        btnAll = createPeriodButton("전체", -1);
        
        add(btn1Week);
        add(btn1Month);
        add(btn3Month);
        add(btn6Month);
        add(btn1Year);
        add(btnAll);
        
        // 기본 선택: 1개월
        selectButton(btn1Month);
    }
    
    /**
     * 기간 버튼 생성
     */
    private JButton createPeriodButton(String text, int days) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        button.setPreferredSize(new Dimension(70, 32));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 초기 스타일
        button.setBackground(COLOR_DEFAULT);
        button.setForeground(TEXT_DEFAULT);
        button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        // 마우스 호버 효과
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(COLOR_HOVER);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(COLOR_DEFAULT);
                }
            }
        });
        
        return button;
    }
    
    /**
     * 버튼 선택 상태로 변경
     */
    private void selectButton(JButton button) {
        // 이전 선택 버튼 초기화
        if (selectedButton != null) {
            selectedButton.setBackground(COLOR_DEFAULT);
            selectedButton.setForeground(TEXT_DEFAULT);
            selectedButton.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        }
        
        // 새 버튼 선택
        selectedButton = button;
        button.setBackground(COLOR_SELECTED);
        button.setForeground(TEXT_SELECTED);
        button.setBorder(BorderFactory.createLineBorder(COLOR_SELECTED, 1));
    }
    
    /**
     * 기간 변경 리스너 등록
     * 
     * @param listener 기간이 변경될 때 호출될 리스너 (int days를 파라미터로 받음)
     */
    public void addPeriodChangeListener(PeriodChangeListener listener) {
        btn1Week.addActionListener(e -> {
            selectButton(btn1Week);
            listener.onPeriodChanged(7);
        });
        
        btn1Month.addActionListener(e -> {
            selectButton(btn1Month);
            listener.onPeriodChanged(30);
        });
        
        btn3Month.addActionListener(e -> {
            selectButton(btn3Month);
            listener.onPeriodChanged(90);
        });
        
        btn6Month.addActionListener(e -> {
            selectButton(btn6Month);
            listener.onPeriodChanged(180);
        });
        
        btn1Year.addActionListener(e -> {
            selectButton(btn1Year);
            listener.onPeriodChanged(365);
        });
        
        btnAll.addActionListener(e -> {
            selectButton(btnAll);
            listener.onPeriodChanged(9999); // 충분히 큰 값 (전체 조회)
        });
    }
    
    /**
     * 기간 변경 이벤트 리스너 인터페이스
     */
    public interface PeriodChangeListener {
        void onPeriodChanged(int days);
    }
}