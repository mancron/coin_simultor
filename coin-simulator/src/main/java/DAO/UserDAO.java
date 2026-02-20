package DAO;

import java.sql.*;

import com.team.coin_simulator.DBConnection;
import DTO.UserDTO;

public class UserDAO {

    // =========================================================
    // 로그인
    // =========================================================
    public static UserDTO loginCheck(String userId, String password) {

        String sql =
                "SELECT user_id, nickname, phone_number, auth_provider, profile_image_path " +
                "FROM users WHERE user_id = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    UserDTO user = new UserDTO();

                    user.setUserId(rs.getString("user_id"));
                    user.setNickname(rs.getString("nickname"));

                    try { user.setPhoneNumber(rs.getString("phone_number")); } catch (Exception ignore) {}
                    try { user.setAuthProvider(rs.getString("auth_provider")); } catch (Exception ignore) {}
                    try { user.setProfileImagePath(rs.getString("profile_image_path")); } catch (Exception ignore) {}

                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =========================================================
    // 아이디 중복 체크
    // =========================================================
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

    // =========================================================
    // 아이디 찾기 (전화번호)
    // =========================================================
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

    // =========================================================
    // 아이디 + 전화번호 확인
    // =========================================================
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

    // =========================================================
    // 비밀번호 변경
    // =========================================================
    public static boolean changePassword(String userId, String currentPw, String newPw) {

        String checkSql = "SELECT 1 FROM users WHERE user_id = ? AND password = ?";
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {

                ps.setString(1, userId);
                ps.setString(2, currentPw);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                }
            }

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

    // =========================================================
    // 임시 비밀번호 재설정
    // =========================================================
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

    // =========================================================
    // 임시 비밀번호 생성
    // =========================================================
    public static String generateTempPassword(int length) {

        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#";

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }

        return sb.toString();
    }

    // =========================================================
    // ⭐ 프로필 관리 (중복 제거된 유일한 세트)
    // =========================================================
    public static class ProfileInfo {

        public final String nickname;
        public final String imagePath;

        public ProfileInfo(String nickname, String imagePath) {
            this.nickname = nickname;
            this.imagePath = imagePath;
        }
    }

    // 프로필 조회
    public static ProfileInfo getProfile(String userId) {

        String sql = "SELECT nickname, profile_image_path FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new ProfileInfo(
                            rs.getString("nickname"),
                            rs.getString("profile_image_path"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ProfileInfo(null, null);
    }

    // 닉네임 변경
    public static boolean updateNickname(String userId, String newNickname) {

        String sql = "UPDATE users SET nickname = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newNickname);
            ps.setString(2, userId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 프로필 사진 경로 변경
    public static boolean updateProfileImagePath(String userId, String path) {

        String sql = "UPDATE users SET profile_image_path = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, path);
            ps.setString(2, userId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // 회원가입 (프로젝트에서 실제 구현된 것 쓰면 됨)
    // =========================================================
    public boolean insertUser(UserDTO user, String phone) {
        return false;
    }
}