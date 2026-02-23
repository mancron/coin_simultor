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
            System.err.println("[UserDAO] 로그인 체크 실패: " + e.getMessage());
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
            System.err.println("[UserDAO] 중복 체크 실패: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // 회원가입 ⭐ [수정: auth_provider, created_at 명시적 추가]
    // =========================================================
    public static boolean insertUser(UserDTO user, String phone) {
        
        // 전화번호 정제 (하이픈 제거)
        String cleanPhone = (phone == null) ? "" : phone.replaceAll("[^0-9]", "");
        
        // phone_number 필수 체크
        if (cleanPhone.isEmpty()) {
            System.err.println("[UserDAO] 전화번호는 필수입니다.");
            return false;
        }

        String sql =
            "INSERT INTO users (user_id, password, nickname, phone_number, auth_provider, created_at) " +
            "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            pstmt.setString(4, cleanPhone);
            pstmt.setString(5, "EMAIL"); // auth_provider 기본값

            int result = pstmt.executeUpdate();
            
            if (result == 1) {
                System.out.println("[UserDAO] 회원가입 성공: " + user.getUserId());
                
                // 기본 프로필 이미지 설정
                updateProfileImagePath(user.getUserId(), "default");
                
                return true;
            } else {
                System.err.println("[UserDAO] INSERT 실패: 영향받은 행이 0개");
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 회원가입 실패: " + e.getMessage());
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
            System.err.println("[UserDAO] 아이디 찾기 실패: " + e.getMessage());
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
            System.err.println("[UserDAO] 사용자 확인 실패: " + e.getMessage());
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
                    if (!rs.next()) {
                        System.err.println("[UserDAO] 현재 비밀번호 불일치");
                        return false;
                    }
                }
            }

            try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {

                ps2.setString(1, newPw);
                ps2.setString(2, userId);

                int result = ps2.executeUpdate();
                
                if (result == 1) {
                    System.out.println("[UserDAO] 비밀번호 변경 성공: " + userId);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 비밀번호 변경 실패: " + e.getMessage());
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

            int result = pstmt.executeUpdate();
            
            if (result == 1) {
                System.out.println("[UserDAO] 임시 비밀번호 설정 성공: " + userId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 임시 비밀번호 설정 실패: " + e.getMessage());
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
    // ⭐ 프로필 관리
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
                    String nickname = rs.getString("nickname");
                    String imagePath = rs.getString("profile_image_path");
                    
                    // NULL 또는 빈 문자열이면 "default"로 반환
                    if (imagePath == null || imagePath.trim().isEmpty()) {
                        imagePath = "default";
                    }
                    
                    return new ProfileInfo(nickname, imagePath);
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 프로필 조회 실패: " + e.getMessage());
            e.printStackTrace();
        }

        return new ProfileInfo(null, "default");
    }

    // 닉네임 변경
    public static boolean updateNickname(String userId, String newNickname) {

        String sql = "UPDATE users SET nickname = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newNickname);
            ps.setString(2, userId);

            int result = ps.executeUpdate();
            
            if (result == 1) {
                System.out.println("[UserDAO] 닉네임 변경 성공: " + userId + " -> " + newNickname);
                return true;
            } else {
                System.err.println("[UserDAO] 닉네임 변경 실패: 사용자를 찾을 수 없음");
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 닉네임 변경 실패: " + e.getMessage());
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

            int result = ps.executeUpdate();
            
            if (result == 1) {
                System.out.println("[UserDAO] 프로필 이미지 업데이트 성공: " + path);
                return true;
            } else {
                System.err.println("[UserDAO] 프로필 이미지 업데이트 실패: 사용자를 찾을 수 없음");
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] 프로필 이미지 업데이트 실패: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // 오류 문자열을 돌려주는 메서드
    public static String updateNicknameWithError(String userId, String newNickname) {
        String sql = "UPDATE users SET nickname = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newNickname);
            ps.setString(2, userId);

            int updated = ps.executeUpdate();
            if (updated == 1) return null; // 성공
            return "업데이트된 행이 없습니다. user_id가 존재하는지 확인하세요.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "SQL 오류: " + e.getMessage();
        }
    }

    // 오류 문자열을 돌려주는 메서드
    public static String updateProfileImagePathWithError(String userId, String path) {
        String sql = "UPDATE users SET profile_image_path = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, path);
            ps.setString(2, userId);

            int updated = ps.executeUpdate();
            if (updated == 1) return null; // 성공
            return "업데이트된 행이 없습니다. user_id가 존재하는지 확인하세요.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "SQL 오류: " + e.getMessage();
        }
    }
}