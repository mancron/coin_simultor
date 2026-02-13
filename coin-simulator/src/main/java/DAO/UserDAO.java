package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 1. 보내주신 DBConnection 및 DTO 패키지 경로에 맞게 임포트
import com.team.coin_simulator.DBConnection; 
import DTO.UserDTO;
import type.AuthProvider;

public class UserDAO {

    /**
     * [중복 확인] 아이디(user_id)가 이미 존재하는지 확인합니다.
     * @param userId 검사할 아이디
     * @return 존재하면 true, 없으면 false
     */
    public boolean isIdDuplicate(String userId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        
        // DBConnection.getConnection() 메서드가 static이므로 바로 호출
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // 레코드가 존재하면 중복(true)
            }
        } catch (SQLException e) {
            System.err.println("아이디 중복 체크 중 오류: " + e.getMessage());
        }
        return false;
    }

    /**
     * [회원가입] 새로운 사용자를 DB에 저장합니다.
     * @param user 저장할 사용자 정보가 담긴 DTO
     * @return 성공 시 true, 실패 시 false
     */
    public boolean insertUser(UserDTO user) {
        String sql = "INSERT INTO users (user_id, password, nickname, auth_provider, created_at) "
                   + "VALUES (?, ?, ?, ?, NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            
            // Enum을 문자열로 변환하여 저장 (기본값 EMAIL 처리)
            String providerStr = (user.getAuthProvider() != null) 
                                 ? user.getAuthProvider().name() 
                                 : AuthProvider.EMAIL.name();
            pstmt.setString(4, providerStr);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("회원 가입 처리 중 오류: " + e.getMessage());
        }
        return false;
    }

    /**
     * [로그인] 아이디와 비밀번호가 일치하는 유저 정보를 가져옵니다.
     * @param userId 입력한 아이디
     * @param password 입력한 비밀번호
     * @return 성공 시 유저 정보 DTO, 실패 시 null
     */
    public UserDTO loginCheck(String userId, String password) {
        String sql = "SELECT user_id, nickname, auth_provider, created_at FROM users "
                   + "WHERE user_id = ? AND password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = new UserDTO();
                    user.setUserId(rs.getString("user_id"));
                    user.setNickname(rs.getString("nickname"));
                    
                    // DTO에 있는 편의 메서드 setAuthProvider(String)를 사용하여 Enum으로 자동 변환
                    user.setAuthProvider(rs.getString("auth_provider"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("로그인 처리 중 오류: " + e.getMessage());
        }
        return null;
    }
}