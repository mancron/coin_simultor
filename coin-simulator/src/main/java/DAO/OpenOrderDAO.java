package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.team.coin_simulator.DBConnection;
import DTO.OrderDTO;

/**
 * 미체결 주문 조회 및 관리 DAO
 */
public class OpenOrderDAO {
    
    // 1. 전체 조회 (세션 ID 추가)
    public List<OrderDTO> getOpenOrders(String userId, long sessionId) {
        List<OrderDTO> list = new ArrayList<>();
        
        String sql = 
            "SELECT * FROM orders " +
            "WHERE user_id = ? AND session_id = ? AND status = 'WAIT' " + // 💡 조건 추가
            "ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setLong(2, sessionId); // 💡 파라미터 매핑
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDTO(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // 2. 특정 마켓 미체결 조회 (세션 ID 추가)
    public List<OrderDTO> getOpenOrdersByMarket(String userId, long sessionId, String market) {
        List<OrderDTO> list = new ArrayList<>();
        
        String sql = 
            "SELECT * FROM orders " +
            "WHERE user_id = ? AND session_id = ? AND market = ? AND status = 'WAIT' " + // 💡 조건 추가
            "ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setLong(2, sessionId); // 💡 파라미터 매핑
            pstmt.setString(3, market);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDTO(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    public boolean cancelOrder(long orderId) {
        String sql = "UPDATE orders SET status = 'CANCEL' WHERE order_id = ? AND status = 'WAIT'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, orderId);
            int affected = pstmt.executeUpdate();
            return affected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //3. 일괄 취소 (세션 ID 추가)
    public int cancelAllOrders(String userId, long sessionId) {
        String sql = "UPDATE orders SET status = 'CANCEL' WHERE user_id = ? AND session_id = ? AND status = 'WAIT'"; // 💡 조건 추가
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setLong(2, sessionId); // 💡 파라미터 매핑
            return pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    private OrderDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        OrderDTO dto = new OrderDTO();
        
        dto.setOrderId(rs.getLong("order_id"));
        dto.setUserId(rs.getString("user_id"));
        dto.setSessionId(rs.getLong("session_id"));
        dto.setMarket(rs.getString("market"));
        dto.setSide(rs.getString("side"));
        dto.setType(rs.getString("type"));
        dto.setOriginalPrice(rs.getBigDecimal("original_price"));
        dto.setOriginalVolume(rs.getBigDecimal("original_volume"));
        dto.setRemainingVolume(rs.getBigDecimal("remaining_volume"));
        dto.setStatus(rs.getString("status"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        
        return dto;
    }
}