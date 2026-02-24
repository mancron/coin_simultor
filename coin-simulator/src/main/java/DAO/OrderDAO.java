package DAO;

import com.team.coin_simulator.DBConnection;
import DTO.OrderDTO;
import java.sql.*;
import java.math.BigDecimal;

public class OrderDAO {
    
    //지정가 주문 (자산 잠금까지 완벽 처리)
    public boolean insertOrder(DTO.OrderDTO order) {
        String insertOrderSql = "INSERT INTO orders (order_id, user_id, session_id, market, side, type, original_price, original_volume, remaining_volume, status) " +
                                "VALUES (?, ?, ?, ?, ?, 'LIMIT', ?, ?, ?, 'WAIT')";
        
        // 지정가 주문 시 자산을 미리 묶어둠(Locked)
        String lockAssetSql = "INSERT INTO assets (user_id, session_id, currency, balance, locked) VALUES (?, ?, ?, ?, ?) " +
                              "ON DUPLICATE KEY UPDATE balance = balance - ?, locked = locked + ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 
             
            //주문 내역 저장
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderSql)) {
                pstmt.setLong(1, order.getOrderId());
                pstmt.setString(2, order.getUserId());
                pstmt.setLong(3, order.getSessionId()); 
                pstmt.setString(4, order.getMarket());
                pstmt.setString(5, order.getSide());
                pstmt.setBigDecimal(6, order.getOriginalPrice());
                pstmt.setBigDecimal(7, order.getOriginalVolume());
                pstmt.setBigDecimal(8, order.getRemainingVolume());
                pstmt.executeUpdate();
            }

            //자산 잠금 (KRW 또는 코인)
            String currency = order.getSide().equals("BID") ? "KRW" : order.getMarket().replace("KRW-", "");
            BigDecimal requiredAmt = order.getSide().equals("BID") ? order.getOriginalPrice().multiply(order.getOriginalVolume()) : order.getOriginalVolume();

            try (PreparedStatement pstmt = conn.prepareStatement(lockAssetSql)) {
                pstmt.setString(1, order.getUserId());
                pstmt.setLong(2, order.getSessionId());
                pstmt.setString(3, currency);
                pstmt.setBigDecimal(4, requiredAmt.negate());
                pstmt.setBigDecimal(5, requiredAmt);
                pstmt.setBigDecimal(6, requiredAmt); // update 차감용
                pstmt.setBigDecimal(7, requiredAmt); // update 증가용
                pstmt.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch(SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
    }
    
    //주문 취소 처리 (어떤 코인이든 알아서 환불)
    public boolean cancelOrder(long orderId, String userId, String side, BigDecimal amount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            //취소할 주문의 session_id와 market(코인 종류) 조회
            long sessionId = 0;
            String market = "";
            String fetchSql = "SELECT session_id, market FROM orders WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(fetchSql)) {
                pstmt.setLong(1, orderId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        sessionId = rs.getLong("session_id");
                        market = rs.getString("market");
                    } else {
                        throw new SQLException("주문 정보를 찾을 수 없습니다.");
                    }
                }
            }

            //주문 상태 변경
            String updateOrderSql = "UPDATE orders SET status = 'CANCEL' WHERE order_id = ? AND user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateOrderSql)) {
                pstmt.setLong(1, orderId);
                pstmt.setString(2, userId);
                if (pstmt.executeUpdate() == 0) throw new SQLException("취소할 수 없는 주문입니다.");
            }

            //자산 복구 (동적 코인 할당 및 세션 격리 적용)
            String currency = side.equals("BID") ? "KRW" : market.replace("KRW-", "");
            String refundAssetSql = "UPDATE assets SET balance = balance + ?, locked = locked - ? WHERE user_id = ? AND session_id = ? AND currency = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(refundAssetSql)) {
                pstmt.setBigDecimal(1, amount);
                pstmt.setBigDecimal(2, amount); 
                pstmt.setString(3, userId);
                pstmt.setLong(4, sessionId);
                pstmt.setString(5, currency);
                
                // 💡 [핵심 수정] 0건 업데이트 시 조용히 넘어가지 못하게 철퇴!
                int updatedRows = pstmt.executeUpdate(); 
                if (updatedRows == 0) {
                    System.err.println(">> [에러] 환불할 계좌를 찾지 못했습니다! (user:" + userId + ", session:" + sessionId + ", currency:" + currency + ")");
                    throw new SQLException("자산을 돌려줄 계좌(세션)를 DB에서 찾지 못했습니다.");
                }
            }

            conn.commit(); 
            System.out.println(">> [DB] 주문 취소 및 자산 복구 완벽 처리 성공! (session: " + sessionId + ")");
            return true;
            
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch(SQLException ex) {}
            System.err.println("취소 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
    }

    //주문 정정 처리
    public boolean modifyOrder(long orderId, String userId, String side, BigDecimal oldAmount, BigDecimal newAmount, BigDecimal newPrice, BigDecimal newQty) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // [1] 정정할 주문의 session_id와 market 조회
            long sessionId = 0;
            String market = "";
            String fetchSql = "SELECT session_id, market FROM orders WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(fetchSql)) {
                pstmt.setLong(1, orderId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        sessionId = rs.getLong("session_id");
                        market = rs.getString("market");
                    } else {
                        throw new SQLException("주문 정보를 찾을 수 없습니다.");
                    }
                }
            }

            BigDecimal diff = oldAmount.subtract(newAmount); 
            String currency = side.equals("BID") ? "KRW" : market.replace("KRW-", "");

            // [2] 자산 변경 (차액만큼 알아서 더하고 빼기)
            String updateAssetSql = "UPDATE assets SET balance = balance + ?, locked = locked - ? WHERE user_id = ? AND session_id = ? AND currency = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateAssetSql)) {
                pstmt.setBigDecimal(1, diff);
                pstmt.setBigDecimal(2, diff); 
                pstmt.setString(3, userId);
                pstmt.setLong(4, sessionId);
                pstmt.setString(5, currency);
                
                // 💡 [핵심 수정] 0건 업데이트 방어
                if (pstmt.executeUpdate() == 0) {
                    throw new SQLException("정정할 자산 계좌를 찾지 못했습니다.");
                }
            }

            // [3] 주문 정보 수정
            String updateOrderSql = "UPDATE orders SET original_price = ?, original_volume = ?, remaining_volume = ? WHERE order_id = ? AND user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateOrderSql)) {
                pstmt.setBigDecimal(1, newPrice);
                pstmt.setBigDecimal(2, newQty);
                pstmt.setBigDecimal(3, newQty);
                pstmt.setLong(4, orderId); 
                pstmt.setString(5, userId);
                if (pstmt.executeUpdate() == 0) throw new SQLException("정정할 주문이 없습니다.");
            }

            conn.commit();
            System.out.println(">> [DB] 주문 정정 완료");
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch(SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
    }
    
    //시장가 주문 (즉시 체결)
    public boolean executeMarketOrder(OrderDTO order, String userId, BigDecimal tradePrice, BigDecimal tradeVolume, BigDecimal tradeTotalAmt) {
        String insertOrderSql = "INSERT INTO orders (order_id, user_id, session_id, market, side, type, original_price, original_volume, remaining_volume, status) " +
                                "VALUES (?, ?, ?, ?, ?, 'MARKET', ?, ?, ?, 'DONE')";
        
        String insertExecSql = "INSERT INTO executions (order_id, price, volume, fee, market, side, user_id, total_price) " +
                               "VALUES (?, ?, ?, 0, ?, ?, ?, ?)"; 
        
        // UPSERT 구문으로 해당 세션의 자산이 없으면 생성하고, 있으면 더함
        String updateAssetSql = "INSERT INTO assets (user_id, session_id, currency, balance, locked) VALUES (?, ?, ?, ?, 0) " +
                                "ON DUPLICATE KEY UPDATE balance = balance + ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // [1] 주문 내역 저장
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderSql)) {
                pstmt.setLong(1, order.getOrderId());
                pstmt.setString(2, userId);
                pstmt.setLong(3, order.getSessionId()); 
                pstmt.setString(4, order.getMarket()); 
                pstmt.setString(5, order.getSide());
                pstmt.setBigDecimal(6, tradePrice);
                pstmt.setBigDecimal(7, tradeVolume);
                pstmt.setBigDecimal(8, BigDecimal.ZERO);
                pstmt.executeUpdate();
            }

            // [2] 체결 내역 저장
            try (PreparedStatement pstmt = conn.prepareStatement(insertExecSql)) {
                pstmt.setLong(1, order.getOrderId());     
                pstmt.setBigDecimal(2, tradePrice);       
                pstmt.setBigDecimal(3, tradeVolume);      
                pstmt.setString(4, order.getMarket());     
                pstmt.setString(5, order.getSide());      
                pstmt.setString(6, userId);               
                pstmt.setBigDecimal(7, tradeTotalAmt);    
                pstmt.executeUpdate();
            }

            // [3] 자산 업데이트 (KRW와 코인 각각 처리)
            String symbol = order.getMarket().replace("KRW-", ""); 
            
            if (order.getSide().equals("BID")) { // 매수
                // KRW 차감
                try (PreparedStatement pstmt = conn.prepareStatement(updateAssetSql)) {
                    pstmt.setString(1, userId);
                    pstmt.setLong(2, order.getSessionId());
                    pstmt.setString(3, "KRW");
                    pstmt.setBigDecimal(4, tradeTotalAmt.negate());
                    pstmt.setBigDecimal(5, tradeTotalAmt.negate());
                    pstmt.executeUpdate();
                }
                // 코인 획득
                try (PreparedStatement pstmt = conn.prepareStatement(updateAssetSql)) {
                    pstmt.setString(1, userId);
                    pstmt.setLong(2, order.getSessionId());
                    pstmt.setString(3, symbol);
                    pstmt.setBigDecimal(4, tradeVolume);
                    pstmt.setBigDecimal(5, tradeVolume);
                    pstmt.executeUpdate();
                }
            } else { // 매도
                // 코인 차감
                try (PreparedStatement pstmt = conn.prepareStatement(updateAssetSql)) {
                    pstmt.setString(1, userId);
                    pstmt.setLong(2, order.getSessionId());
                    pstmt.setString(3, symbol);
                    pstmt.setBigDecimal(4, tradeVolume.negate());
                    pstmt.setBigDecimal(5, tradeVolume.negate());
                    pstmt.executeUpdate();
                }
                // KRW 획득
                try (PreparedStatement pstmt = conn.prepareStatement(updateAssetSql)) {
                    pstmt.setString(1, userId);
                    pstmt.setLong(2, order.getSessionId());
                    pstmt.setString(3, "KRW");
                    pstmt.setBigDecimal(4, tradeTotalAmt);
                    pstmt.setBigDecimal(5, tradeTotalAmt);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch(SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
    }
}