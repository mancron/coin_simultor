package com.team.coin_simulator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 시간 제어 UI 패널
 * - 실시간/백테스팅 모드 전환
 * - 시간 이동 버튼 (1시간, 1일, 1주일 단위)
 * - 현재 시뮬레이션 시간 표시
 */
public class TimeControlPanel extends JPanel implements TimeController.TimeChangeListener {
    
    private JLabel lblCurrentTime;
    private JLabel lblModeIndicator;
    private JButton btnRealtime, btnBacktest;
    private JButton btnBack1h, btnBack1d, btnBack1w;
    private JButton btnForward1h, btnForward1d, btnForward1w;
    private JButton btnJumpToDate;
    
    private TimeController timeController;
    
    private static final Color COLOR_REALTIME = new Color(34, 139, 34);
    private static final Color COLOR_BACKTEST = new Color(255, 140, 0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public TimeControlPanel() {
        timeController = TimeController.getInstance();
        timeController.addTimeChangeListener(this);
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        setPreferredSize(new Dimension(0, 60));
        
        // 왼쪽: 모드 전환 버튼
        JPanel leftPanel = createModePanel();
        
        // 중앙: 시간 표시 및 이동 버튼
        JPanel centerPanel = createTimeControlPanel();
        
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        
        // 초기 상태 업데이트
        updateUI(null, true);
    }
    
    private JPanel createModePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel("모드:");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        
        btnRealtime = new JButton("실시간 거래");
        btnBacktest = new JButton("백테스팅");
        
        styleButton(btnRealtime);
        styleButton(btnBacktest);
        
        btnRealtime.addActionListener(e -> {
            timeController.switchToRealtimeMode();
        });
        
        btnBacktest.addActionListener(e -> {
            showBacktestDialog();
        });
        
        panel.add(label);
        panel.add(btnRealtime);
        panel.add(btnBacktest);
        
        return panel;
    }
    
    private JPanel createTimeControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        panel.setBackground(Color.WHITE);
        
        // 현재 시간 표시
        lblModeIndicator = new JLabel("● 실시간");
        lblModeIndicator.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        lblModeIndicator.setForeground(COLOR_REALTIME);
        
        lblCurrentTime = new JLabel("현재 시각");
        lblCurrentTime.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        // 시간 이동 버튼 (백테스팅 모드에서만 활성화)
        btnBack1h = new JButton("◀ 1시간");
        btnBack1d = new JButton("◀ 1일");
        btnBack1w = new JButton("◀ 1주");
        
        btnForward1h = new JButton("1시간 ▶");
        btnForward1d = new JButton("1일 ▶");
        btnForward1w = new JButton("1주 ▶");
        
        btnJumpToDate = new JButton("날짜 선택");
        
        JButton[] timeButtons = {btnBack1h, btnBack1d, btnBack1w, 
                                btnForward1h, btnForward1d, btnForward1w, btnJumpToDate};
        for (JButton btn : timeButtons) {
            styleButton(btn);
            btn.setEnabled(false); // 초기에는 비활성화
        }
        
        // 이벤트 리스너
        btnBack1h.addActionListener(e -> timeController.moveTime(-60));
        btnBack1d.addActionListener(e -> timeController.moveTime(-60 * 24));
        btnBack1w.addActionListener(e -> timeController.moveTime(-60 * 24 * 7));
        
        btnForward1h.addActionListener(e -> timeController.moveTime(60));
        btnForward1d.addActionListener(e -> timeController.moveTime(60 * 24));
        btnForward1w.addActionListener(e -> timeController.moveTime(60 * 24 * 7));
        
        btnJumpToDate.addActionListener(e -> showDatePickerDialog());
        
        // 레이아웃
        panel.add(lblModeIndicator);
        panel.add(lblCurrentTime);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnBack1w);
        panel.add(btnBack1d);
        panel.add(btnBack1h);
        panel.add(btnForward1h);
        panel.add(btnForward1d);
        panel.add(btnForward1w);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(btnJumpToDate);
        
        return panel;
    }
    
    private void styleButton(JButton btn) {
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    /**
     * 백테스팅 시작 다이얼로그
     */
    private void showBacktestDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        
        JLabel infoLabel = new JLabel("<html>백테스팅 모드는 최소 2개월 전부터 시작할 수 있습니다.<br>" +
                                      "시작 시점을 선택하세요:</html>");
        
        JComboBox<String> monthCombo = new JComboBox<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 2; i <= 12; i++) {
            LocalDateTime time = now.minusMonths(i);
            monthCombo.addItem(i + "개월 전 (" + time.format(FORMATTER) + ")");
        }
        
        panel.add(infoLabel);
        panel.add(monthCombo);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "백테스팅 시작", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            int selectedMonths = monthCombo.getSelectedIndex() + 2;
            LocalDateTime startTime = now.minusMonths(selectedMonths);
            timeController.switchToBacktestMode(startTime);
        }
    }
    
    /**
     * 날짜 선택 다이얼로그
     */
    private void showDatePickerDialog() {
        // 간단한 날짜 입력 다이얼로그
        String input = JOptionPane.showInputDialog(this, 
            "이동할 날짜와 시간을 입력하세요 (yyyy-MM-dd HH:mm):", 
            timeController.getCurrentSimTime().format(FORMATTER));
        
        if (input != null && !input.trim().isEmpty()) {
            try {
                LocalDateTime targetTime = LocalDateTime.parse(input, FORMATTER);
                timeController.jumpToTime(targetTime);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "잘못된 날짜 형식입니다.\n형식: yyyy-MM-dd HH:mm", 
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public void onTimeChanged(LocalDateTime newTime, boolean isRealtime) {
        updateUI(newTime, isRealtime);
    }
    
    /**
     * UI 상태 업데이트
     */
    private void updateUI(LocalDateTime time, boolean isRealtime) {
        SwingUtilities.invokeLater(() -> {
            if (isRealtime) {
                // 실시간 모드
                lblModeIndicator.setText("● 실시간");
                lblModeIndicator.setForeground(COLOR_REALTIME);
                lblCurrentTime.setText("현재 시각");
                
                btnRealtime.setBackground(new Color(220, 255, 220));
                btnBacktest.setBackground(Color.WHITE);
                
                // 시간 이동 버튼 비활성화
                JButton[] timeButtons = {btnBack1h, btnBack1d, btnBack1w, 
                                        btnForward1h, btnForward1d, btnForward1w, btnJumpToDate};
                for (JButton btn : timeButtons) {
                    btn.setEnabled(false);
                }
                
            } else {
                // 백테스팅 모드
                lblModeIndicator.setText("● 백테스팅");
                lblModeIndicator.setForeground(COLOR_BACKTEST);
                lblCurrentTime.setText(time.format(FORMATTER));
                
                btnRealtime.setBackground(Color.WHITE);
                btnBacktest.setBackground(new Color(255, 240, 220));
                
                // 시간 이동 버튼 활성화
                JButton[] timeButtons = {btnBack1h, btnBack1d, btnBack1w, 
                                        btnForward1h, btnForward1d, btnForward1w, btnJumpToDate};
                for (JButton btn : timeButtons) {
                    btn.setEnabled(true);
                }
            }
        });
    }
}