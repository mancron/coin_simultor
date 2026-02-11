package com.team.coin_simulator.Market_Panel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.team.coin_simulator.DBConnection; // DB 연결 클래스가 있다고 가정

public class WatchListDAO {

    public List<WatchlistDTO> getWatchlistByUser(String userId) {
        List<WatchlistDTO> list = new ArrayList<>();
        String sql = "SELECT user_id, market, created_at FROM watchlists WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WatchlistDTO dto = new WatchlistDTO();
                    dto.setUser(rs.getString("user_id"));
                    dto.setMarket(rs.getString("market"));
                    dto.setCreate_at(rs.getTimestamp("created_at"));
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}