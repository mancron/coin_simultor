package databasetestdata;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import com.team.coin_simulator.CoinConfig;
import com.team.coin_simulator.DBConnection;

public class DownloadDatabase {

    private static final String UPBIT_URL_MINUTES = "https://api.upbit.com/v1/candles/minutes/";
    
    // 업비트 API 요청 시 날짜 포맷 (yyyy-MM-dd HH:mm:ss)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 등록된 모든 코인의 6개월 치 데이터를 수집합니다.
     * @param unit 분 단위 (예: 1, 3, 5, 15, 30, 60, 240)
     */
    public static void import6MonthsData(int unit) {
        // 1. 수집 종료 시점 설정 (현재로부터 6개월 전)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffDate = now.minusMonths(6); 
        
        System.out.println("=== 과거 데이터 수집 시작 ===");
        System.out.println("목표 기간: " + now.format(FORMATTER) + " ~ " + cutoffDate.format(FORMATTER));
        System.out.println("수집 단위: " + unit + "분봉");

        for (String code : CoinConfig.getCodes()) {
            String market = "KRW-" + code;
            System.out.println("\n[" + market + "] 데이터 수집 시작...");
            
            // 각 코인별로 현재 시간부터 과거로 이동하며 수집
            crawlCoinHistory(market, unit, cutoffDate);
        }
        
        System.out.println("\n=== 모든 코인 데이터 수집 완료 ===");
    }

    private static void crawlCoinHistory(String market, int unit, LocalDateTime cutoffDate) {
        String toDate = ""; // 빈 값이면 가장 최신 데이터 요청
        boolean isFinished = false;
        int totalSaved = 0;

        while (!isFinished) {
            try {
                // 1. API 호출 (200개씩 요청)
                String jsonResponse = fetchCandles(market, unit, 200, toDate);
                JSONArray candles = new JSONArray(jsonResponse);

                if (candles.isEmpty()) {
                    break; // 더 이상 데이터가 없으면 종료
                }

                // 2. 가장 오래된 캔들(마지막 인덱스)의 시간 확인
                JSONObject lastCandle = candles.getJSONObject(candles.length() - 1);
                String lastDateStr = lastCandle.getString("candle_date_time_utc").replace("T", " ");
                LocalDateTime lastDate = LocalDateTime.parse(lastDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // 3. DB 저장
                saveBatch(market, unit, candles);
                totalSaved += candles.length();
                System.out.print("."); // 진행상황 표시 (점 하나당 200개)

                // 4. 종료 조건 검사 (마지막 캔들이 목표 시점보다 과거면 종료)
                if (lastDate.isBefore(cutoffDate)) {
                    isFinished = true;
                } else {
                    // 5. 다음 요청을 위해 'to' 파라미터 업데이트 (마지막 캔들 시간 기준)
                    toDate = lastDateStr;
                }

                // 6. 속도 제한 (초당 10회 미만 유지를 위해 0.12초 대기)
                // 1000ms / 10회 = 100ms이지만, 안전마진을 위해 120ms 설정
                Thread.sleep(120);

            } catch (Exception e) {
                System.err.println("\n[Error] " + market + " 수집 중단: " + e.getMessage());
                // 에러 발생 시 해당 코인은 건너뛰고 다음 코인으로 진행하려면 break;
                // 재시도 로직이 필요하면 여기에 추가
                break; 
            }
        }
        System.out.println(" 완료 (총 " + totalSaved + "개)");
    }

    private static String fetchCandles(String market, int unit, int count, String toDate) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(UPBIT_URL_MINUTES);
        urlBuilder.append(unit)
                  .append("?market=").append(market)
                  .append("&count=").append(count);

        // to 파라미터가 있으면 추가 (없으면 최신 데이터)
        if (toDate != null && !toDate.isEmpty()) {
            urlBuilder.append("&to=").append(toDate.replace(" ", "%20")); // 공백 URL 인코딩
        }

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("HTTP Error: " + conn.getResponseCode()); // 429 Too Many Requests 등 처리
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static void saveBatch(String market, int unit, JSONArray candles) {
        String sql = "INSERT INTO market_candle " +
                     "(market, candle_date_time_utc, candle_date_time_kst, opening_price, high_price, low_price, " +
                     "trade_price, timestamp, candle_acc_trade_price, candle_acc_trade_volume, unit) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "trade_price = VALUES(trade_price)"; // 이미 있으면 업데이트

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < candles.length(); i++) {
                JSONObject c = candles.getJSONObject(i);
                
                String utcStr = c.getString("candle_date_time_utc").replace("T", " ");
                String kstStr = c.getString("candle_date_time_kst").replace("T", " ");

                int idx = 1;
                pstmt.setString(idx++, market);
                pstmt.setTimestamp(idx++, Timestamp.valueOf(utcStr));
                pstmt.setTimestamp(idx++, Timestamp.valueOf(kstStr));
                pstmt.setBigDecimal(idx++, c.getBigDecimal("opening_price"));
                pstmt.setBigDecimal(idx++, c.getBigDecimal("high_price"));
                pstmt.setBigDecimal(idx++, c.getBigDecimal("low_price"));
                pstmt.setBigDecimal(idx++, c.getBigDecimal("trade_price"));
                pstmt.setLong(idx++, c.getLong("timestamp"));
                pstmt.setBigDecimal(idx++, c.getBigDecimal("candle_acc_trade_price"));
                pstmt.setBigDecimal(idx++, c.getBigDecimal("candle_acc_trade_volume"));
                pstmt.setInt(idx++, unit);

                pstmt.addBatch();
            }
            pstmt.executeBatch();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public static void main(String[] args) {
        // DB 연결 확인 (필요 시 initDatabase)
    	System.out.println(">>> 데이터 수집 프로세스 시작 <<<");
        // 6개월 치 데이터 수집 실행
        // unit: 1(1분), 60(1시간), 240(4시간) 권장
        import6MonthsData(240); 
    }
}