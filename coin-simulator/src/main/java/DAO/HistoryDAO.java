package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.team.coin_simulator.DBConnection;

import DTO.ExecutionDTO;

/**
 * 거래내역 조회 DAO
 * executions 테이블에서 사용자의 거래 내역을 조회합니다.
 */
public class HistoryDAO {
    
    /**
     * 사용자의 모든 거래 내역 조회 (기간 필터)
     * 
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 거래 내역 리스트
     */
    public List<ExecutionDTO> getExecutionHistory(String userId, Date startDate, Date endDate) {
        List<ExecutionDTO> list = new ArrayList<>();
        
        String sql = 
            "SELECT e.* " +
            "FROM executions e " +
            "INNER JOIN orders o ON e.order_id = o.order_id " +
            "WHERE o.user_id = ? " +
            "  AND DATE(e.executed_at) BETWEEN ? AND ? " +
            "ORDER BY e.executed_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setDate(2, startDate);
            pstmt.setDate(3, endDate);
            
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
    
    /**
     * 거래 종류별 필터링 조회
     * 
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @param side 거래 종류 ("BID", "ASK", null=전체)
     * @return 거래 내역 리스트
     */
    public List<ExecutionDTO> getExecutionHistoryBySide(
            String userId, Date startDate, Date endDate, String side) {
        
        List<ExecutionDTO> list = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
            "SELECT e.* " +
            "FROM executions e " +
            "INNER JOIN orders o ON e.order_id = o.order_id " +
            "WHERE o.user_id = ? " +
            "  AND DATE(e.executed_at) BETWEEN ? AND ? ");
        
        if (side != null && !side.isEmpty()) {
            sql.append("  AND e.side = ? ");
        }
        
        sql.append("ORDER BY e.executed_at DESC");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            pstmt.setString(idx++, userId);
            pstmt.setDate(idx++, startDate);
            pstmt.setDate(idx++, endDate);
            
            if (side != null && !side.isEmpty()) {
                pstmt.setString(idx++, side);
            }
            
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
    
    /**
     * 코인별 + 거래종류별 필터링 조회
     * 
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @param market 마켓 코드 (예: "KRW-BTC", null=전체)
     * @param side 거래 종류 ("BID", "ASK", null=전체)
     * @return 거래 내역 리스트
     */
    public List<ExecutionDTO> getExecutionHistoryFiltered(
            String userId, Date startDate, Date endDate, String market, String side) {
        
        List<ExecutionDTO> list = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
            "SELECT e.* " +
            "FROM executions e " +
            "INNER JOIN orders o ON e.order_id = o.order_id " +
            "WHERE o.user_id = ? " +
            "  AND DATE(e.executed_at) BETWEEN ? AND ? ");
        
        if (market != null && !market.isEmpty()) {
            sql.append("  AND e.market = ? ");
        }
        
        if (side != null && !side.isEmpty()) {
            sql.append("  AND e.side = ? ");
        }
        
        sql.append("ORDER BY e.executed_at DESC");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            pstmt.setString(idx++, userId);
            pstmt.setDate(idx++, startDate);
            pstmt.setDate(idx++, endDate);
            
            if (market != null && !market.isEmpty()) {
                pstmt.setString(idx++, market);
            }
            
            if (side != null && !side.isEmpty()) {
                pstmt.setString(idx++, side);
            }
            
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
    
    /**
     * ResultSet을 ExecutionDTO로 매핑
     */
    private ExecutionDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        ExecutionDTO dto = new ExecutionDTO();
        
        dto.setExecutionId(rs.getLong("execution_id"));
        dto.setOrderId(rs.getLong("order_id"));
        dto.setMarket(rs.getString("market"));
        dto.setSide(rs.getString("side"));
        dto.setPrice(rs.getBigDecimal("price"));
        dto.setVolume(rs.getBigDecimal("volume"));
        dto.setFee(rs.getBigDecimal("fee"));
        dto.setBuyAvgPrice(rs.getBigDecimal("buy_avg_price"));
        dto.setRealizedPnl(rs.getBigDecimal("realized_pnl"));
        dto.setRoi(rs.getBigDecimal("roi"));
        dto.setExecutedAt(rs.getTimestamp("executed_at"));
        
        return dto;
    }
}