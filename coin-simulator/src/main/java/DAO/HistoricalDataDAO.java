package DAO;

import com.team.coin_simulator.DBConnection;
import DTO.TickerDto;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 백테스팅용 과거 시세 데이터 조회 DAO
 * - 특정 시점의 캔들 데이터를 조회하여 실시간 시세처럼 반환
 * - TimeController와 연동하여 시뮬레이션 시간에 맞는 데이터 제공
 */
public class HistoricalDataDAO {
    
    /**
     * 특정 시점의 모든 코인 현재가 조회
     * @param targetTime 조회할 시점
     * @return Map<코인심볼, TickerDto>
     */
    public Map<String, TickerDto> getTickersAtTime(LocalDateTime targetTime) {
        Map<String, TickerDto> tickers = new HashMap<>();
        
        String sql = "SELECT market, trade_price, opening_price, high_price, low_price, " +
                    "candle_acc_trade_price, candle_acc_trade_volume " +
                    "FROM market_candle " +
                    "WHERE candle_date_time_kst <= ? " +
                    "  AND unit = 240 " + // 4시간 봉 기준
                    "ORDER BY candle_date_time_kst DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(targetTime));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                // 각 마켓별로 가장 최근(첫 번째) 데이터만 사용
                while (rs.next()) {
                    String market = rs.getString("market"); // "KRW-BTC"
                    String symbol = market.replace("KRW-", ""); // "BTC"
                    
                    // 이미 처리된 심볼이면 스킵 (최신 데이터 우선)
                    if (tickers.containsKey(symbol)) {
                        continue;
                    }
                    
                    TickerDto ticker = new TickerDto();
                    ticker.setCode(market);
                    ticker.setTrade_price(rs.getDouble("trade_price"));
                    
                    // 등락률 계산 (현재가 vs 시가)
                    double openPrice = rs.getDouble("opening_price");
                    if (openPrice > 0) {
                        double changeRate = (ticker.getTrade_price() - openPrice) / openPrice;
                        ticker.setSigned_change_rate(changeRate);
                    }
                    
                    ticker.setAcc_trade_price(rs.getDouble("candle_acc_trade_price"));
                    
                    tickers.put(symbol, ticker);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return tickers;
    }
    
    /**
     * 특정 시점의 특정 코인 현재가 조회
     */
    public BigDecimal getPriceAtTime(String market, LocalDateTime targetTime) {
        String sql = "SELECT trade_price FROM market_candle " +
                    "WHERE market = ? AND candle_date_time_kst <= ? " +
                    "ORDER BY candle_date_time_kst DESC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, market);
            pstmt.setTimestamp(2, Timestamp.valueOf(targetTime));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("trade_price");
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 특정 기간의 캔들 데이터 조회 (차트용)
     * @param market 마켓 코드 (KRW-BTC)
     * @param unit 분 단위 (240 = 4시간)
     * @param endTime 종료 시점
     * @param count 조회할 캔들 개수
     */
    public java.util.List<DTO.MarketCandleDTO> getCandlesUntil(
            String market, int unit, LocalDateTime endTime, int count) {
        
        java.util.List<DTO.MarketCandleDTO> candles = new java.util.ArrayList<>();
        
        String sql = "SELECT * FROM market_candle " +
                    "WHERE market = ? AND unit = ? AND candle_date_time_kst <= ? " +
                    "ORDER BY candle_date_time_kst DESC LIMIT ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, market);
            pstmt.setInt(2, unit);
            pstmt.setTimestamp(3, Timestamp.valueOf(endTime));
            pstmt.setInt(4, count);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DTO.MarketCandleDTO dto = new DTO.MarketCandleDTO();
                    dto.setMarket(rs.getString("market"));
                    dto.setUnit(rs.getInt("unit"));
                    dto.setCandleDateTimeKst(rs.getTimestamp("candle_date_time_kst"));
                    dto.setCandleDateTimeUtc(rs.getTimestamp("candle_date_time_utc"));
                    dto.setOpeningPrice(rs.getBigDecimal("opening_price"));
                    dto.setHighPrice(rs.getBigDecimal("high_price"));
                    dto.setLowPrice(rs.getBigDecimal("low_price"));
                    dto.setTradePrice(rs.getBigDecimal("trade_price"));
                    dto.setTimestamp(rs.getLong("timestamp"));
                    dto.setCandleAccTradePrice(rs.getBigDecimal("candle_acc_trade_price"));
                    dto.setCandleAccTradeVolume(rs.getBigDecimal("candle_acc_trade_volume"));
                    candles.add(dto);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return candles;
    }
}