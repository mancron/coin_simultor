package com.team.coin_simulator.chart;

import com.team.coin_simulator.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CandleDAO {

    // 2. 특정 종목의 캔들 데이터 조회 (최신순)
    public List<CandleDTO> getCandles(String market, int unit, int limit) {
        List<CandleDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM market_candle WHERE market = ? AND unit = ? " +
                     "ORDER BY candle_date_time_kst DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, market);
            pstmt.setInt(2, unit);
            pstmt.setInt(3, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CandleDTO dto = new CandleDTO();
                    dto.setMarket(rs.getString("market"));
                    
                    // 수정: getTimestamp() 대신 getObject()를 사용하여 DB에 저장된 값을 타임존 변환 없이 그대로 가져옴
                    dto.setCandleDateTimeKst(rs.getObject("candle_date_time_kst", LocalDateTime.class));
                    dto.setCandleDateTimeUtc(rs.getObject("candle_date_time_utc", LocalDateTime.class));
                    
                    dto.setOpeningPrice(rs.getDouble("opening_price"));
                    dto.setHighPrice(rs.getDouble("high_price"));
                    dto.setLowPrice(rs.getDouble("low_price"));
                    dto.setTradePrice(rs.getDouble("trade_price"));
                    dto.setTimestamp(rs.getLong("timestamp"));
                    dto.setCandleAccTradePrice(rs.getDouble("candle_acc_trade_price"));
                    dto.setCandleAccTradeVolume(rs.getDouble("candle_acc_trade_volume"));
                    dto.setUnit(rs.getInt("unit"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // 특정 시점(targetTime) 이전의 데이터를 가져오는 메서드
    public List<CandleDTO> getHistoricalCandles(String market, int unit, java.time.LocalDateTime targetTime, int limit) {
        List<CandleDTO> list = new ArrayList<>();
        // market_candle 테이블에서 targetTime보다 작거나 같은(과거) 데이터만 최신순으로 조회
        String sql = "SELECT * FROM market_candle WHERE market = ? AND unit = ? " +
                     "AND candle_date_time_kst <= ? " + 
                     "ORDER BY candle_date_time_kst DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, market);
            pstmt.setInt(2, unit);
            pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(targetTime));
            pstmt.setInt(4, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CandleDTO dto = new CandleDTO();
                    dto.setMarket(rs.getString("market"));
                    
                    // 수정: getTimestamp() 대신 getObject()를 사용하여 DB에 저장된 값을 타임존 변환 없이 그대로 가져옴
                    dto.setCandleDateTimeKst(rs.getObject("candle_date_time_kst", LocalDateTime.class));
                    dto.setCandleDateTimeUtc(rs.getObject("candle_date_time_utc", LocalDateTime.class));
                    
                    dto.setOpeningPrice(rs.getDouble("opening_price"));
                    dto.setHighPrice(rs.getDouble("high_price"));
                    dto.setLowPrice(rs.getDouble("low_price"));
                    dto.setTradePrice(rs.getDouble("trade_price"));
                    dto.setTimestamp(rs.getLong("timestamp"));
                    dto.setCandleAccTradePrice(rs.getDouble("candle_acc_trade_price"));
                    dto.setCandleAccTradeVolume(rs.getDouble("candle_acc_trade_volume"));
                    dto.setUnit(rs.getInt("unit"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}