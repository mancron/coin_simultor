package com.team.coin_simulator.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// 1. 팀원 파일인 DBConnection 위치 확인 (현재 경로: .db 패키지 안)
import com.team.coin_simulator.db.DBConnection; 

// 2. DTO 클래스 임포트 (패키지명이 대문자 'DTO'인 경우)
import DTO.UserDTO; 

public class UserDAO {

    /**
     * 아이디(user_id) 중복 체크
     */
    public boolean isIdDuplicate(String userId) {
        // SQL 문을 UserDTO의 필드명인 user_id에 맞춤
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // 레코드가 있으면 중복(true)
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
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            
            // AuthProvider Enum 처리
            String provider = (user.getAuthProvider() != null) 
                              ? user.getAuthProvider().name() 
                              : "EMAIL";
            pstmt.setString(4, provider);
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}