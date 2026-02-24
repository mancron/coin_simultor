package DAO;

import java.sql.*;
import com.team.coin_simulator.DBConnection;
import DTO.UserDTO;
import type.AuthProvider;

public class UserDAO {

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

    public static boolean insertUser(UserDTO user, String phone) {
        String cleanPhone = (phone == null) ? null : phone.replaceAll("[^0-9]", "");

        String sql = "INSERT INTO users (user_id, password, nickname, phone_number, auth_provider, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            pstmt.setString(4, cleanPhone);
            pstmt.setString(5, (user.getAuthProvider() != null)
                    ? user.getAuthProvider().name()
                    : AuthProvider.EMAIL.name());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String findIdByPhone(String phone) {
        String cleanPhone = (phone == null) ? "" : phone.replaceAll("[^0-9]", "");

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

    public static boolean updatePassword(String userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

 // 1) userId로 유저 가져오기 (nickname, profile_image_path 필요)
    public static UserDTO getUserById(String userId) {
        String sql = "SELECT user_id, nickname, profile_image_path FROM users WHERE user_id = ?";
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserDTO u = new UserDTO();
                    u.setUserId(rs.getString("user_id"));
                    u.setNickname(rs.getString("nickname"));
                    // UserDTO에 이 필드/세터 없으면 추가해야 함:
                    u.setProfileImagePath(rs.getString("profile_image_path"));
                    return u;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2) 프로필 이미지 경로 업데이트
    public static boolean updateProfileImagePath(String userId, String path) {
        String sql = "UPDATE users SET profile_image_path = ? WHERE user_id = ?";
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, path);
            ps.setString(2, userId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3) 닉네임 업데이트
    public static boolean updateNickname(String userId, String nickname) {
        String sql = "UPDATE users SET nickname = ? WHERE user_id = ?";
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nickname);
            ps.setString(2, userId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4) 비밀번호 업데이트
    public static boolean updatePassword1(String userId, String newPw) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPw);
            ps.setString(2, userId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static UserDTO loginCheck(String userId, String password) {
        String sql = "SELECT user_id, nickname FROM users WHERE user_id = ? AND password = ?";
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
