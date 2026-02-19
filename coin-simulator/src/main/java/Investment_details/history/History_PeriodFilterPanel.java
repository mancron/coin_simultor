package Investment_details.history;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 기간별 조회를 위한 필터 패널
 * 버튼(1주일, 1개월 등)과 날짜 입력 필드 포함
 */
public class History_PeriodFilterPanel extends JPanel {
    
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton searchButton;
    
    private JButton btn1Week, btn1Month, btn3Month, btn6Month;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public History_PeriodFilterPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        setBackground(Color.WHITE);
        
        // 1. 라벨 설정
        JLabel label = new JLabel("기간");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        add(label);
        
        // 2. 기간 선택 버튼들
        btn1Week = createPeriodButton("1주일", 7);
        btn1Month = createPeriodButton("1개월", 30);
        btn3Month = createPeriodButton("3개월", 90);
        btn6Month = createPeriodButton("6개월", 180);
        
        add(btn1Week);
        add(btn1Month);
        add(btn3Month);
        add(btn6Month);
        
        // 3. 간격
        add(new JLabel("  "));
        
        // 4. 날짜 입력 필드 (시작일 ~ 종료일)
        LocalDate today = LocalDate.now();
        LocalDate oneMonthAgo = today.minusMonths(1);
        
        startDateField = new JTextField(oneMonthAgo.format(DATE_FORMAT), 10);
        endDateField = new JTextField(today.format(DATE_FORMAT), 10);
        
        startDateField.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        endDateField.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        add(startDateField);
        add(new JLabel("~"));
        add(endDateField);
        
        // 5. 조회 버튼
        searchButton = new JButton("조회");
        searchButton.setBackground(new Color(52, 152, 219));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        searchButton.setFocusPainted(false);
        add(searchButton);
    }
    
    /**
     * 기간 버튼 생성
     */
    private JButton createPeriodButton(String text, int days) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        // 클릭 시 날짜 필드 자동 설정
        btn.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(days);
            
            startDateField.setText(startDate.format(DATE_FORMAT));
            endDateField.setText(today.format(DATE_FORMAT));
            
            // 조회 버튼 자동 클릭
            searchButton.doClick();
        });
        
        return btn;
    }
    
    /**
     * 조회 버튼 리스너 등록
     */
    public void addSearchListener(ActionListener listener) {
        searchButton.addActionListener(listener);
    }
    
    /**
     * 시작일 가져오기
     */
    public LocalDate getStartDate() {
        try {
            return LocalDate.parse(startDateField.getText(), DATE_FORMAT);
        } catch (Exception e) {
            return LocalDate.now().minusMonths(1);
        }
    }
    
    /**
     * 종료일 가져오기
     */
    public LocalDate getEndDate() {
        try {
            return LocalDate.parse(endDateField.getText(), DATE_FORMAT);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}