package com.team.coin_simulator.Alerts;

import DAO.*;
import com.team.coin_simulator.*;
import com.team.coin_simulator.Alerts.NotificationUtil;
import javax.swing.JFrame;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

public class PriceAlertService implements UpbitWebSocketDao.TickerListener {
    
    private JFrame mainFrame;
    private String currentUser = "test_user";

    public PriceAlertService(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        // 웹소켓 리스너로 자신을 등록하여 실시간 가격 수신
        UpbitWebSocketDao.getInstance().addListener(this);
    }

    @Override
    public void onTickerUpdate(String symbol, String priceStr, String flucStr, String accPriceStr) {
        // 1. 현재가 파싱
        String cleanPrice = priceStr.replace(",", "").replace(" KRW", "").trim();
        if (cleanPrice.isEmpty() || cleanPrice.equals("연결중...")) return;
        BigDecimal currentPrice = new BigDecimal(cleanPrice);

        // 2. 백그라운드 스레드에서 DB 조회 (메인 화면 버벅임 방지 - 기획안 1.2 비동기 처리 적용)
        new Thread(() -> checkPriceAlerts(symbol, currentPrice)).start();
        //급등락 로직
        checkVolatility(symbol, currentPrice);
    }

    private void checkPriceAlerts(String market, BigDecimal currentPrice) {
        String selectSql = "SELECT alert_id, target_price, condition_type FROM price_alerts " +
                           "WHERE user_id = ? AND market = ? AND is_active = TRUE";
        String updateSql = "UPDATE price_alerts SET is_active = FALSE WHERE alert_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            
            selectStmt.setString(1, currentUser);
            selectStmt.setString(2, market); // 예: "KRW-BTC"
            
            try (ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    long alertId = rs.getLong("alert_id");
                    BigDecimal targetPrice = rs.getBigDecimal("target_price");
                    String condition = rs.getString("condition_type");

                    boolean isTriggered = false;
                    
                    // 기획안 1.1 조건 검사
                    if (condition.equals("ABOVE") && currentPrice.compareTo(targetPrice) >= 0) {
                        isTriggered = true;
                    } else if (condition.equals("BELOW") && currentPrice.compareTo(targetPrice) <= 0) {
                        isTriggered = true;
                    }

                    if (isTriggered) {
                        // 1. 즉시 비활성화 (중복 알림 방지)
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setLong(1, alertId);
                            updateStmt.executeUpdate();
                        }

                        // 2. 알림 발송 (UI 스레드에서 Toast 띄우기)
                        final String msg = String.format("🔔 [가격 알림] %s가 %,.0f원에 도달했습니다!", market, targetPrice);
                        SwingUtilities.invokeLater(() -> NotificationUtil.showToast(mainFrame, msg));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
 // 코인별 가격 기록 (최근 180초 데이터)
 // Key: "KRW-BTC", Value: 시간순 가격 리스트 (LinkedList 사용)
 private Map<String, LinkedList<BigDecimal>> priceHistory = new ConcurrentHashMap<>();

 public void checkVolatility(String symbol, BigDecimal currentPrice) {
     LinkedList<BigDecimal> history = priceHistory.computeIfAbsent(symbol, k -> new LinkedList<>());
     
     // 1. 현재 가격 추가
     history.add(currentPrice);
     
     // 2. 데이터 개수 체크
     if (history.size() > 180) {
         BigDecimal oldPrice = history.poll(); // n초 전 가격 꺼내기
         
         // [안전장치] 0으로 나누기 오류 방지
         if (oldPrice.compareTo(BigDecimal.ZERO) == 0) return;

         // 3. 급등락 계산
         BigDecimal diff = currentPrice.subtract(oldPrice);
         BigDecimal rate = diff.multiply(new BigDecimal("100"))
                 .divide(oldPrice, 6, RoundingMode.HALF_UP);
         
         // 4. 절대값이 3% 이상이면 알림 발송
         if (rate.abs().compareTo(new BigDecimal("3.0")) >= 0) {
             
             String type = rate.compareTo(BigDecimal.ZERO) > 0 ? "급등" : "급락";
             String msg = String.format("⚠️ [%s] %s 발생! (%.2f%% 변동)", symbol, type, rate);
             javax.swing.SwingUtilities.invokeLater(() -> 
                 com.team.coin_simulator.Alerts.NotificationUtil.showToast(mainFrame, msg)
             );
             
             history.clear(); // 알림 폭탄 방지 (기록 초기화)
         }
     }
 }
}