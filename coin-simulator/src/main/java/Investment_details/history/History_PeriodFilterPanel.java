package Investment_details.history;

import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;

/**
 * 기간별 조회를 위한 필터 패널입니다.
 * 버튼(1주일, 1개월 등)과 날짜 입력 필드를 포함합니다.
 */
public class History_PeriodFilterPanel extends JPanel {

    public History_PeriodFilterPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        setBackground(Color.WHITE);

        // 1. 라벨 설정
        JLabel label = new JLabel("기간");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        add(label);

        // 2. 기간 선택 버튼들 (사진과 유사한 구성)
        String[] periods = {"1주일", "1개월", "3개월", "6개월"};
        for (String p : periods) {
            JButton btn = new JButton(p);
            btn.setBackground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            add(btn);
        }

        // 3. 날짜 입력 필드 (시작일 ~ 종료일)
        JTextField startDate = new JTextField(8);
        JTextField endDate = new JTextField(8);
        startDate.setText("2024-01-01"); // 예시 데이터
        endDate.setText("2024-01-31");   // 예시 데이터

        add(new JLabel(" ")); // 간격 조절
        add(startDate);
        add(new JLabel("~"));
        add(endDate);

        // 4. 조회 버튼
        JButton searchBtn = new JButton("조회");
        searchBtn.setBackground(new Color(0, 102, 204)); // 파란색 계열
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        add(searchBtn);
    }
}
