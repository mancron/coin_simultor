package DAO;

import java.sql.*;

import com.team.coin_simulator.DBConnection;
import DTO.UserDTO;

public class SignUpService {

    // 초기 시드머니 (원하면 DB 기본값(1억)과 통일)
    private static final long INITIAL_KRW = 100_000_000L;

    /**
     * 회원가입 3단계 트랜잭션:
     * 1) users insert
     * 2) simulation_sessions 기본 REALTIME 세션 생성 (session_id 반환)
     * 3) assets에 해당 session_id로 KRW 지급
     */
    public static boolean register(UserDTO user, String phoneDigitsOnly, long initialKRW) {

        String insertUserSql =
            "INSERT INTO users (user_id, password, nickname, phone_number, profile_image_path, auth_provider) " +
            "VALUES (?, ?, ?, ?, NULL, 'EMAIL')";

        String insertSessionSql =
            "INSERT INTO simulation_sessions (user_id, session_name, session_type) " +
            "VALUES (?, 'DEFAULT', 'REALTIME')";

        String insertAssetSql =
            "INSERT INTO assets (session_id, user_id, currency, balance, locked, avg_buy_price) " +
            "VALUES (?, ?, 'KRW', ?, 0, 0)";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) users
            try (PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
                ps.setString(1, user.getUserId());
                ps.setString(2, user.getPassword());   // 이미 해시된 값이면 그대로
                ps.setString(3, user.getNickname());
                ps.setString(4, phoneDigitsOnly);
                ps.executeUpdate();
            }

            // 2) simulation_sessions (generated session_id)
            long sessionId;
            try (PreparedStatement ps = conn.prepareStatement(insertSessionSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUserId());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("session_id 생성 실패 (generatedKeys 없음)");
                    }
                    sessionId = rs.getLong(1);
                }
            }

            // 3) assets (KRW 지급)
            try (PreparedStatement ps = conn.prepareStatement(insertAssetSql)) {
                ps.setLong(1, sessionId);
                ps.setString(2, user.getUserId());
                ps.setLong(3, INITIAL_KRW);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }
}