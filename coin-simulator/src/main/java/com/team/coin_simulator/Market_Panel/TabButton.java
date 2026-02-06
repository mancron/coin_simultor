package com.team.coin_simulator.Market_Panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

public class TabButton extends JButton {

    private boolean isSelected = false; // 현재 선택된 탭인지 여부
    private boolean isHovered = false;  // 마우스가 올라가 있는지 여부
    
    private Color selectedColor = new Color(52, 152, 219); // 파란색 (선택 시 밑줄)
    private Color textColor = Color.BLACK;                 // 기본 글자색

    public TabButton(String text) {
        super(text);
        
        // 1. 기본 버튼 스타일 제거
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false); // 배경 투명하게
        
        setFont(new Font("맑은 고딕", Font.BOLD, 14));
        setForeground(textColor);

        // 2. 마우스 이벤트 감지
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)); // 손가락 커서
                repaint(); // 다시 그리기 요청
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                repaint();
            }
        });
    }
    
    // 외부에서 이 버튼이 '선택됨' 상태인지 설정하는 메소드
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 글자 그리기 준비
        String text = getText();
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        
        // 글자를 정중앙에 배치하기 위한 좌표 계산
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() + textHeight) / 2 - 2; // -2는 약간의 보정값

        // 2. 글자 색상 설정 (선택되면 파란색, 아니면 검정)
        if (isSelected) {
            g2.setColor(selectedColor);
        } else {
            g2.setColor(isHovered ? selectedColor : Color.BLACK); // 호버 시에도 파랗게 할지 선택
        }
        
        g2.drawString(text, x, y);

        // 3. [기능 1] 마우스 오버 시 글자 밑줄 (Text Underline)
        if (isHovered && !isSelected) { // 선택되지 않았을 때만 호버 효과
            g2.setColor(selectedColor);
            g2.setStroke(new BasicStroke(1)); // 얇은 선
            // 글자 시작점(x)부터 끝점(x+width)까지 선 그리기
            g2.drawLine(x, y + 2, x + textWidth, y + 2); 
        }

        // 4. [기능 2] 선택(클릭) 시 버튼 하단 굵은 밑줄 (Bottom Indicator)
        if (isSelected) {
            g2.setColor(selectedColor);
            int lineThickness = 3; // 밑줄 두께
            // 버튼의 바닥(height) 부분에 사각형 채우기
            g2.fillRect(0, getHeight() - lineThickness, getWidth(), lineThickness);
        }
    }
}