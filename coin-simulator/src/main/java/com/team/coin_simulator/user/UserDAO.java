package com.team.coin_simulator.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// 1. 팀원 공유 DB 연결 클래스 경로
import com.team.coin_simulator.db.DBConnection; 

// 2. DTO 및 type(Enum) 경로 (이 부분이 누락되면 에러가 납니다)
import DTO.UserDTO; 
import type.AuthProvider; 

public class UserDAO {

    /**
     * 아이디(user_id) 중복 체크
     * @param userId 검사할 아이디
     * @return 존재하면 true, 없으면 false
     */
    public boolean isIdDuplicate(String userId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); 
            }
        } catch (Exception e) {
            System.err.println("ID 중복 체크 에러: " + e.getMessage());
        }
        return false;
    }

    /**
     * 회원 가입 (UserDTO 객체 활용)
     */
    public boolean insertUser(UserDTO user) {
        String sql = "INSERT INTO users (user_id, password, nickname, auth_provider, created_at) "
                   + "VALUES (?, ?, ?, ?, NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            
            // Enum 호환 처리: AuthProvider 객체에서 name(문자열)을 추출하여 DB에 저장
            String providerStr = (user.getAuthProvider() != null) 
                                 ? user.getAuthProvider().name() 
                                 : AuthProvider.EMAIL.name();
            pstmt.setString(4, providerStr);
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("회원 가입 에러: " + e.getMessage());
        }
        return false;
    }

    /**
     * 로그인 체크
     * @return 로그인 성공 시 UserDTO 객체, 실패 시 null
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
                    // DTO 내부의 setAuthProvider(String) 메서드를 이용해 Enum으로 변환
                    user.setAuthProvider(rs.getString("auth_provider"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (Exception e) {
            System.err.println("로그인 체크 에러: " + e.getMessage());
        }
        return null;
    }
}