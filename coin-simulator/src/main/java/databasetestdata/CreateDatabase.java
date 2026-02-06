package databasetestdata;

import java.sql.Connection;
import java.sql.DriverManager; // 직접 연결을 위해 추가
import java.sql.Statement;
import java.sql.SQLException;
import com.team.coin_simulator.DBConnection; 

public class CreateDatabase {

    public static void initDatabase() {
        // [핵심] DB 생성 단계에서는 DBConnection(HikariCP) 대신
        // 맨땅(Root)에 헤딩하는 임시 연결을 직접 만듭니다.
        String rootUrl = "jdbc:mysql://localhost:3306/?characterEncoding=UTF-8&serverTimezone=UTC";
        String user = "root";
        String password = "1234"; // 본인 비밀번호 확인

        String createSchema = "CREATE DATABASE IF NOT EXISTS coin_simulator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
        String useSchema = "USE coin_simulator";
        
        String[] queries = {
            "CREATE TABLE IF NOT EXISTS users (" +
            "    user_id VARCHAR(50) PRIMARY KEY," +
            "    password VARCHAR(64)," +
            "    nickname VARCHAR(30) NOT NULL," +
            "    auth_provider VARCHAR(10) DEFAULT 'EMAIL'," +
            "    created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")",
            // ... (나머지 테이블 쿼리는 동일하므로 생략하지 않고 그대로 두셔도 됩니다)
            "CREATE TABLE IF NOT EXISTS market_candle (" +
            "    market VARCHAR(10) NOT NULL," +
            "    candle_date_time_utc DATETIME NOT NULL," +
            "    candle_date_time_kst DATETIME," +
            "    opening_price DECIMAL(20, 8)," +
            "    high_price DECIMAL(20, 8)," +
            "    low_price DECIMAL(20, 8)," +
            "    trade_price DECIMAL(20, 8)," +
            "    timestamp BIGINT," +
            "    candle_acc_trade_price DECIMAL(30, 8)," +
            "    candle_acc_trade_volume DECIMAL(30, 8)," + // 30으로 수정됨 확인
            "    unit INT NOT NULL," +
            "    PRIMARY KEY (market, unit, candle_date_time_utc)" +
            ")",
            "CREATE TABLE IF NOT EXISTS watchlists (" +
            "    user_id VARCHAR(50) NOT NULL," +
            "    market VARCHAR(10) NOT NULL," +
            "    created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "    PRIMARY KEY (user_id, market)," +
            "    FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")",
            "CREATE TABLE IF NOT EXISTS simulation_sessions (" +
            "    session_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "    user_id VARCHAR(50) NOT NULL," +
            "    session_name VARCHAR(50)," +
            "    session_type VARCHAR(10) DEFAULT 'REALTIME'," +
            "    start_sim_time DATETIME," +
            "    current_sim_time DATETIME," +
            "    initial_seed_money DECIMAL(20, 0) DEFAULT 100000000," +
            "    is_active BOOLEAN DEFAULT TRUE," +
            "    created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")",
            "CREATE TABLE IF NOT EXISTS assets (" +
            "    user_id VARCHAR(50) NOT NULL," +
            "    currency VARCHAR(10) NOT NULL," +
            "    session_id BIGINT DEFAULT 0," +
            "    balance DECIMAL(20, 8) DEFAULT 0," +
            "    locked DECIMAL(20, 8) DEFAULT 0," +
            "    avg_buy_price DECIMAL(20, 8) DEFAULT 0," +
            "    PRIMARY KEY (user_id, currency)," +
            "    FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")",
            "CREATE TABLE IF NOT EXISTS orders (" +
            "    order_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "    user_id VARCHAR(50) NOT NULL," +
            "    session_id BIGINT DEFAULT 0," +
            "    market VARCHAR(10) NOT NULL," +
            "    side VARCHAR(4) NOT NULL," +
            "    type VARCHAR(10) DEFAULT 'LIMIT'," +
            "    original_price DECIMAL(20, 8) NOT NULL," +
            "    original_volume DECIMAL(20, 8) NOT NULL," +
            "    remaining_volume DECIMAL(20, 8) NOT NULL," +
            "    status VARCHAR(10) DEFAULT 'WAIT'," +
            "    created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (user_id) REFERENCES users(user_id)," +
            "    INDEX idx_user_status (user_id, session_id, status)" +
            ")",
            "CREATE TABLE IF NOT EXISTS executions (" +
            "    execution_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "    order_id BIGINT NOT NULL," +
            "    market VARCHAR(10) NOT NULL," +
            "    side VARCHAR(4) NOT NULL," +
            "    price DECIMAL(20, 8) NOT NULL," +
            "    volume DECIMAL(20, 8) NOT NULL," +
            "    fee DECIMAL(20, 8) DEFAULT 0," +
            "    buy_avg_price DECIMAL(20, 8)," +
            "    realized_pnl DECIMAL(20, 8)," +
            "    roi DECIMAL(10, 2)," +
            "    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
            "    INDEX idx_executed_at (executed_at)" +
            ")",
            "CREATE TABLE IF NOT EXISTS price_alerts (" +
            "    alert_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "    user_id VARCHAR(50) NOT NULL," +
            "    market VARCHAR(10) NOT NULL," +
            "    target_price DECIMAL(20, 8) NOT NULL," +
            "    condition_type VARCHAR(10) NOT NULL," +
            "    is_active BOOLEAN DEFAULT TRUE," +
            "    created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
            ")"
        };

        // 여기서는 DBConnection.getConnection()을 쓰지 않고 직접 연결합니다.
        try (Connection conn = DriverManager.getConnection(rootUrl, user, password);
             Statement stmt = conn.createStatement()) {
           
           // 1. DB 생성
           stmt.execute(createSchema);
           // 2. 해당 DB 선택
           stmt.execute(useSchema);
           
           // 3. 테이블 생성
           for (String sql : queries) {
               stmt.execute(sql);
           }
           System.out.println("[DB] 스키마 및 테이블 초기화 완료");
           
       } catch (SQLException e) {
           e.printStackTrace();
           System.err.println("DB 연결 또는 생성 실패: 비밀번호나 URL을 확인하세요.");
       }
    }
    
    public static void main(String[] args) {
        initDatabase();
    }
}