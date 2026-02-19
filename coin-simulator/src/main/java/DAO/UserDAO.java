package DAO;

import java.sql.*;
import com.team.coin_simulator.DBConnection; 
import DTO.UserDTO;
import type.AuthProvider;

public class UserDAO {

    // [중복 확인] 아이디 존재 여부 체크
    public static boolean isIdDuplicate(String userId) {
    	String sql = "SELECT user_id FROM users WHERE REPLACE(REPLACE(phone_number, '-', ''), ' ', '') = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // [회원가입] 휴대폰 번호(phone_number)를 반드시 포함하여 저장
    public static boolean insertUser(UserDTO user, String phone) {
        String sql = "INSERT INTO users (user_id, password, nickname, phone_number, auth_provider, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            pstmt.setString(4, phone); 
            pstmt.setString(5, (user.getAuthProvider() != null) ? user.getAuthProvider().name() : AuthProvider.EMAIL.name());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // [아이디 찾기] 입력된 번호에서 특수문자를 제거하고 DB와 비교
    public static String findIdByPhone(String phone) {
        String cleanPhone = phone.replaceAll("[^0-9]", "").trim();
        String sql = "SELECT user_id FROM users WHERE phone_number = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cleanPhone);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("user_id");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }


    // [비밀번호 변경] 임시 비밀번호를 DB에 반영
    public static boolean updatePassword(String userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // [로그인 체크]
    public static UserDTO loginCheck(String userId, String password) {
        String sql = "SELECT * FROM users WHERE user_id = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = new UserDTO();
                    user.setUserId(rs.getString("user_id"));
                    user.setNickname(rs.getString("nickname"));
                    return user;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}