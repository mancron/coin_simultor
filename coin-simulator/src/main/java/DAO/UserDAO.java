package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.team.coin_simulator.DBConnection;
import DTO.UserDTO;

public class UserDAO {

    // =========================
    // 1) 아이디(이메일) 중복 체크
    // =========================
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

    // =========================
    // 2) 회원가입 (users 테이블)
    // - auth_provider는 프로젝트마다 enum/패키지 충돌이 잦아서 "EMAIL" 고정
    // =========================
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

            // ✅ 컴파일 오류 방지: enum 의존 제거
            pstmt.setString(5, "EMAIL");

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================
    // 3) 휴대폰 번호로 아이디 찾기
    // =========================
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

    // =========================
    // 4) 로그인 체크
    // - ProfileDialog/PasswordChangeDialog에서도 사용
    // =========================
    public static UserDTO loginCheck(String userId, String password) {
        String sql = "SELECT user_id, nickname, profile_image_path FROM users WHERE user_id = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = new UserDTO();
                    user.setUserId(rs.getString("user_id"));
                    user.setNickname(rs.getString("nickname"));
                    // ✅ UserDTO에 profileImagePath 필드/세터 있어야 함
                    user.setProfileImagePath(rs.getString("profile_image_path"));
                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================
    // 5) userId로 유저 조회 (프로필/닉네임 표시용)
    // =========================
    public static UserDTO getUserById(String userId) {
        String sql = "SELECT user_id, nickname, profile_image_path FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserDTO u = new UserDTO();
                    u.setUserId(rs.getString("user_id"));
                    u.setNickname(rs.getString("nickname"));
                    // ✅ UserDTO에 profileImagePath 필드/세터 있어야 함
                    u.setProfileImagePath(rs.getString("profile_image_path"));
                    return u;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================
    // 6) 프로필 이미지 경로 업데이트
    // =========================
    public static boolean updateProfileImagePath(String userId, String path) {
        String sql = "UPDATE users SET profile_image_path = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, path);
            ps.setString(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================
    // 7) 닉네임 업데이트
    // =========================
    public static boolean updateNickname(String userId, String nickname) {
        String sql = "UPDATE users SET nickname = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nickname);
            ps.setString(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================
    // 8) 비밀번호 업데이트 (중복 제거: updatePassword1 삭제)
    // =========================
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
}