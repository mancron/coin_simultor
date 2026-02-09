package com.team.coin_simulator.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// 1. DB 연결 클래스 임포트 (상대방 소스 경로에 맞춤)
import com.team.coin_simulator.db.DBConnection; 

// 2. DTO 클래스 임포트 (DTO 패키지에 있다면 반드시 필요)
import DTO.UserDTO; 

public class UserDAO {

    /**
     * 아이디 중복 체크 (user_id 기준)
     */
    public boolean isIdDuplicate(String userId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        
        // 상대방 소스 방식인 DBConnection.getConnection() 사용
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // 존재하면 true
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
                   + "VALUES (?, ?, ?, 'EMAIL', NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}