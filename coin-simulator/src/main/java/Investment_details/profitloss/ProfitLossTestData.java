package Investment_details.profitloss;

import com.team.coin_simulator.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * 투자손익 테스트용 더미 데이터 생성 유틸리티
 * 
 * 사용법:
 * ProfitLossTestData.generateTestData("user_01", 30);
 */
public class ProfitLossTestData {
    
    /**
     * 테스트용 거래 데이터를 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param days 생성할 일수
     */
    public static void generateTestData(String userId, int days) {
        Random random = new Random();
        
        // 1. 사용자가 존재하는지 확인 및 생성
        ensureUserExists(userId);
        
        // 2. 초기 자산 설정 (KRW)
        ensureAssetExists(userId, "KRW", new BigDecimal("100000000"));
        
        // 3. 코인 자산 설정 (BTC)
        ensureAssetExists(userId, "BTC", new BigDecimal("0"));
        
        System.out.println("=== 테스트 데이터 생성 시작 ===");
        System.out.println("사용자: " + userId);
        System.out.println("기간: " + days + "일");
        
        // 4. 일별 거래 데이터 생성
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime tradeDate = LocalDateTime.now().minusDays(i);
            
            // 하루에 3~7건의 거래 생성
            int tradesPerDay = 3 + random.nextInt(5);
            
            for (int j = 0; j < tradesPerDay; j++) {
                LocalDateTime tradeTime = tradeDate.plusHours(random.nextInt(24));
                
                // 매수 주문 생성
                long buyOrderId = createOrder(userId, "KRW-BTC", "BID", tradeTime);
                
                // 매수 체결 생성
                BigDecimal buyPrice = new BigDecimal(90000000 + random.nextInt(20000000)); // 9천만 ~ 1.1억
                BigDecimal volume = new BigDecimal("0.001").multiply(
                    new BigDecimal(1 + random.nextInt(10))
                );
                
                createExecution(buyOrderId, "KRW-BTC", "BID", buyPrice, volume, tradeTime);
                
                // 매도 주문 생성 (일부만)
                if (random.nextBoolean()) {
                    LocalDateTime sellTime = tradeTime.plusHours(1 + random.nextInt(5));
                    long sellOrderId = createOrder(userId, "KRW-BTC", "ASK", sellTime);
                    
                    // 매도 체결 생성 (손익 발생)
                    BigDecimal sellPrice = buyPrice.add(
                        new BigDecimal(random.nextInt(10000000) - 5000000) // -500만 ~ +500만
                    );
                    
                    // 실현 손익 계산
                    BigDecimal pnl = sellPrice.subtract(buyPrice).multiply(volume);
                    BigDecimal roi = pnl.divide(buyPrice.multiply(volume), 4, BigDecimal.ROUND_HALF_UP)
                                       .multiply(new BigDecimal(100));
                    
                    createExecutionWithPnl(sellOrderId, "KRW-BTC", "ASK", 
                                          sellPrice, volume, buyPrice, pnl, roi, sellTime);
                }
            }
            
            System.out.print(".");
            if ((days - i) % 10 == 0) {
                System.out.println(" " + (days - i) + "일 완료");
            }
        }
        
        System.out.println("\n=== 테스트 데이터 생성 완료 ===");
    }
    
    private static void ensureUserExists(String userId) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        String insertSql = "INSERT INTO users (user_id, password, nickname) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            // 사용자 존재 확인
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // 이미 존재함
                }
            }
            
            // 사용자 생성
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, "test_password_hash");
                pstmt.setString(3, "테스트유저");
                pstmt.executeUpdate();
                System.out.println("테스트 사용자 생성 완료: " + userId);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void ensureAssetExists(String userId, String currency, BigDecimal balance) {
        String checkSql = "SELECT COUNT(*) FROM assets WHERE user_id = ? AND currency = ?";
        String insertSql = "INSERT INTO assets (user_id, currency, balance, locked, avg_buy_price) " +
                          "VALUES (?, ?, ?, 0, 0)";
        
        try (Connection conn = DBConnection.getConnection()) {
            // 자산 존재 확인
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, currency);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // 이미 존재함
                }
            }
            
            // 자산 생성
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, currency);
                pstmt.setBigDecimal(3, balance);
                pstmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static long createOrder(String userId, String market, String side, LocalDateTime time) {
        String sql = "INSERT INTO orders (user_id, market, side, type, original_price, " +
                    "original_volume, remaining_volume, status, created_at) " +
                    "VALUES (?, ?, ?, 'MARKET', 95000000, 0.001, 0, 'DONE', ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, market);
            pstmt.setString(3, side);
            pstmt.setTimestamp(4, Timestamp.valueOf(time));
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    private static void createExecution(long orderId, String market, String side,
                                       BigDecimal price, BigDecimal volume,
                                       LocalDateTime time) {
        String sql = "INSERT INTO executions (order_id, market, side, price, volume, fee, executed_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            BigDecimal fee = price.multiply(volume).multiply(new BigDecimal("0.0005"));
            
            pstmt.setLong(1, orderId);
            pstmt.setString(2, market);
            pstmt.setString(3, side);
            pstmt.setBigDecimal(4, price);
            pstmt.setBigDecimal(5, volume);
            pstmt.setBigDecimal(6, fee);
            pstmt.setTimestamp(7, Timestamp.valueOf(time));
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void createExecutionWithPnl(long orderId, String market, String side,
                                              BigDecimal price, BigDecimal volume,
                                              BigDecimal buyAvgPrice, BigDecimal pnl,
                                              BigDecimal roi, LocalDateTime time) {
        String sql = "INSERT INTO executions (order_id, market, side, price, volume, fee, " +
                    "buy_avg_price, realized_pnl, roi, executed_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            BigDecimal fee = price.multiply(volume).multiply(new BigDecimal("0.0005"));
            
            pstmt.setLong(1, orderId);
            pstmt.setString(2, market);
            pstmt.setString(3, side);
            pstmt.setBigDecimal(4, price);
            pstmt.setBigDecimal(5, volume);
            pstmt.setBigDecimal(6, fee);
            pstmt.setBigDecimal(7, buyAvgPrice);
            pstmt.setBigDecimal(8, pnl);
            pstmt.setBigDecimal(9, roi);
            pstmt.setTimestamp(10, Timestamp.valueOf(time));
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 메인 메서드 - 독립 실행용
     */
    public static void main(String[] args) {
        // 사용자 user_01의 최근 30일 테스트 데이터 생성
        generateTestData("user_01", 30);
    }
}
