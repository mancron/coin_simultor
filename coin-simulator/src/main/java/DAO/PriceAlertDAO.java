package DAO;

import com.team.coin_simulator.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PriceAlertDAO {

    // 알림 조건 추가하기 (DB 저장)
    public boolean addPriceAlert(String userId, String market, BigDecimal targetPrice, String condition) {
        String sql = "INSERT INTO price_alerts (user_id, market, target_price, condition_type, is_active) VALUES (?, ?, ?, ?, TRUE)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, market);
            pstmt.setBigDecimal(3, targetPrice);
            pstmt.setString(4, condition); // 'ABOVE' or 'BELOW'
            
            int result = pstmt.executeUpdate();
            return result > 0; // 성공하면 true 반환
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}