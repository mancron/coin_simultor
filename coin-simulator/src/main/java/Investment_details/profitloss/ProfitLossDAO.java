package Investment_details.profitloss;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.team.coin_simulator.DBConnection;

import DTO.ExecutionDTO;

/**
 * 투자손익 데이터 조회 DAO
 * executions 테이블에서 데이터를 조회합니다.
 */
public class ProfitLossDAO {
    
    /**
     * 특정 사용자의 최근 N일간 모든 체결 내역을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @param days 조회할 일수
     * @return 체결 내역 리스트 (최신순)
     */
    public List<ExecutionDTO> getExecutions(String userId, int days) {
        List<ExecutionDTO> resultList = new ArrayList<>();
        
        String sql = 
            "SELECT e.* " +
            "FROM executions e " +
            "INNER JOIN orders o ON e.order_id = o.order_id " +
            "WHERE o.user_id = ? " +
            "  AND e.executed_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
            "ORDER BY e.executed_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setInt(2, days);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ExecutionDTO dto = new ExecutionDTO();
                    
                    // PK & FK
                    dto.setExecutionId(rs.getLong("execution_id"));
                    dto.setOrderId(rs.getLong("order_id"));
                    
                    // 체결 기본 정보
                    dto.setMarket(rs.getString("market"));
                    dto.setSide(rs.getString("side"));
                    
                    // 체결 수치
                    dto.setPrice(rs.getBigDecimal("price"));
                    dto.setVolume(rs.getBigDecimal("volume"));
                    dto.setFee(rs.getBigDecimal("fee"));
                    
                    // 손익 분석 데이터
                    dto.setBuyAvgPrice(rs.getBigDecimal("buy_avg_price"));
                    dto.setRealizedPnl(rs.getBigDecimal("realized_pnl"));
                    dto.setRoi(rs.getBigDecimal("roi"));
                    
                    // 시간
                    dto.setExecutedAt(rs.getTimestamp("executed_at"));
                    
                    resultList.add(dto);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return resultList;
    }
    
    /**
     * 매도(ASK) 체결만 조회 (실현 손익이 있는 거래만)
     * 
     * @param userId 사용자 ID
     * @param days 조회할 일수
     * @return 매도 체결 내역 리스트
     */
    public List<ExecutionDTO> getSellExecutions(String userId, int days) {
        List<ExecutionDTO> resultList = new ArrayList<>();
        
        String sql = 
            "SELECT e.* " +
            "FROM executions e " +
            "INNER JOIN orders o ON e.order_id = o.order_id " +
            "WHERE o.user_id = ? " +
            "  AND e.side = 'ASK' " +
            "  AND e.executed_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
            "ORDER BY e.executed_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setInt(2, days);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
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
                    
                    resultList.add(dto);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return resultList;
    }
    
    /**
     * 일별로 그룹화된 손익 집계 (SQL 집계 사용)
     * 
     * @param userId 사용자 ID
     * @param days 조회할 일수
     * @return 날짜별 손익 맵 (key: 날짜, value: 총 실현손익)
     */
    public Map<Date, BigDecimal> getDailyPnlSummary(String userId, int days) {
        Map<Date, BigDecimal> result = new LinkedHashMap<>();
        
        String sql = 
            "SELECT " +
            "    DATE(e.executed_at) AS trade_date, " +
            "    SUM(CASE WHEN e.side = 'ASK' THEN e.realized_pnl ELSE 0 END) AS daily_pnl " +
            "FROM executions e " +
            "INNER JOIN orders o ON e.order_id = o.order_id " +
            "WHERE o.user_id = ? " +
            "  AND e.executed_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
            "GROUP BY DATE(e.executed_at) " +
            "ORDER BY trade_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setInt(2, days);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Date tradeDate = rs.getDate("trade_date");
                    BigDecimal dailyPnl = rs.getBigDecimal("daily_pnl");
                    result.put(tradeDate, dailyPnl);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 특정 날짜의 체결 내역 조회
     * 
     * @param userId 사용자 ID
     * @param date 조회할 날짜
     * @return 해당 날짜의 체결 내역 리스트
     */
    public List<ExecutionDTO> getExecutionsByDate(String userId, Date date) {
        List<ExecutionDTO> resultList = new ArrayList<>();
        
        String sql = 
            "SELECT e.* " +
            "FROM executions e " +
            "INNER JOIN orders o ON e.order_id = o.order_id " +
            "WHERE o.user_id = ? " +
            "  AND DATE(e.executed_at) = DATE(?) " +
            "ORDER BY e.executed_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setDate(2, new java.sql.Date(date.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
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
                    
                    resultList.add(dto);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return resultList;
    }
    
    /**
     * 사용자의 총 실현 손익 조회
     * 
     * @param userId 사용자 ID
     * @return 총 실현 손익
     */
    public BigDecimal getTotalRealizedPnl(String userId) {
        String sql = 
            "SELECT SUM(e.realized_pnl) AS total_pnl " +
            "FROM executions e " +
            "INNER JOIN orders o ON e.order_id = o.order_id " +
            "WHERE o.user_id = ? " +
            "  AND e.side = 'ASK'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal totalPnl = rs.getBigDecimal("total_pnl");
                    return totalPnl != null ? totalPnl : BigDecimal.ZERO;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 사용자의 초기 자본금을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 초기 자본금
     */
    public long getInitialSeedMoney(String userId) {
        String sql = 
            "SELECT initial_seed_money " +
            "FROM simulation_sessions " +
            "WHERE user_id = ? " +
            "  AND is_active = TRUE " +
            "ORDER BY created_at DESC " +
            "LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal seedMoney = rs.getBigDecimal("initial_seed_money");
                    return seedMoney != null ? seedMoney.longValue() : 100000000L;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 100000000L; // 기본값 1억
    }
}