package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.team.coin_simulator.DBConnection;

import DTO.AssetDTO;

public class AssetDAO {

    // 1. 기존 메서드: 코인만 조회 (테이블 리스트용 - KRW 제외)
    public List<AssetDTO> getUserAssets(String userId) {
        List<AssetDTO> list = new ArrayList<>();
        // KRW는 목록에서 제외하고 코인만 조회
        String sql = "SELECT * FROM assets WHERE user_id = ? AND currency != 'KRW' AND (balance > 0 OR locked > 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AssetDTO dto = new AssetDTO();
                    dto.setUserId(rs.getString("user_id"));
                    dto.setCurrency(rs.getString("currency"));
                    dto.setBalance(rs.getBigDecimal("balance"));
                    dto.setLocked(rs.getBigDecimal("locked"));
                    dto.setAvgBuyPrice(rs.getBigDecimal("avg_buy_price"));
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. [추가된 메서드] 모든 자산 조회 (차트 및 요약용 - KRW 포함)
    public List<AssetDTO> getAllAssets(String userId) {
        List<AssetDTO> list = new ArrayList<>();
        // KRW 포함 모든 자산 조회 (currency != 'KRW' 조건 제거)
        String sql = "SELECT * FROM assets WHERE user_id = ? AND (balance > 0 OR locked > 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AssetDTO dto = new AssetDTO();
                    dto.setUserId(rs.getString("user_id"));
                    dto.setCurrency(rs.getString("currency"));
                    dto.setBalance(rs.getBigDecimal("balance"));
                    dto.setLocked(rs.getBigDecimal("locked"));
                    dto.setAvgBuyPrice(rs.getBigDecimal("avg_buy_price"));
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}