package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.team.coin_simulator.DBConnection;
import DTO.UserDTO;

public class UserDAO {

    // 로그인 체크
    public static UserDTO loginCheck(String userId, String password) {
        String sql = "SELECT user_id, nickname, auth_provider, phone_number FROM users WHERE user_id = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = new UserDTO();
                    // DTO에 맞게 수정 필요할 수 있음
                    user.setUserId(rs.getString("user_id"));
                    user.setNickname(rs.getString("nickname"));
                    try {
                        user.setAuthProvider(rs.getString("auth_provider"));
                    } catch (Exception ignore) { }
                    try {
                        user.setPhoneNumber(rs.getString("phone_number"));
                    } catch (Exception ignore) { }
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean changePassword(String userId, String currentPw, String newPw) {
        String checkSql = "SELECT 1 FROM users WHERE user_id = ? AND password = ?";
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            // 1) 현재 비밀번호 확인
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, userId);
                ps.setString(2, currentPw);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                }
            }

            // 2) 새 비밀번호로 업데이트
            try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {
                ps2.setString(1, newPw);
                ps2.setString(2, userId);
                return ps2.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 아이디 중복 체크 (아이디=이메일)
    public static boolean isIdDuplicate(String userId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 휴대폰 번호로 아이디 찾기
    public static String findIdByPhone(String phone) {
        String sql = "SELECT user_id FROM users WHERE phone_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 아이디+휴대폰번호 일치 확인
    public static boolean verifyUserByIdAndPhone(String userId, String phone) {
        String sql = "SELECT 1 FROM users WHERE user_id = ? AND phone_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 비밀번호 업데이트 (연습용: 평문 저장)
    public static boolean updatePassword(String userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 임시 비밀번호 생성
    public static String generateTempPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#";
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }

	public boolean insertUser(UserDTO user, String phone) {
		// TODO Auto-generated method stub
		return false;
	}
}
