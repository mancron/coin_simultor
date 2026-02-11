package com.team.coin_simulator.Market_Panel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.team.coin_simulator.DBConnection;

public class AssetDAO {

    // 특정 유자의 보유 자산(KRW 제외) 조회
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
}