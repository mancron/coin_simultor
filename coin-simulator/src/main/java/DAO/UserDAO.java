package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// 1. 본인의 DBconnection 패키지 경로를 정확히 적어주세요.
// 만약 DBconnection이 com.team.coin_simulator.db에 있다면 아래처럼 수정
import com.team.coin_simulator.db.DBconnection; 
import DTO.UserDTO;
import type.AuthProvider;

public class UserDAO {

    /**
     * 아이디 중복 체크
     */
    public boolean isIdDuplicate(String userId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        
        // 본인 방식: DBconnection.getInstance().getConnection() 사용
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 회원 가입
     */
    public boolean insertUser(UserDTO user) {
        String sql = "INSERT INTO users (user_id, password, nickname, auth_provider, created_at) "
                   + "VALUES (?, ?, ?, ?, NOW())";
        
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            
            // Enum 처리
            String provider = (user.getAuthProvider() != null) 
                              ? user.getAuthProvider().name() 
                              : AuthProvider.EMAIL.name();
            pstmt.setString(4, provider);
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}