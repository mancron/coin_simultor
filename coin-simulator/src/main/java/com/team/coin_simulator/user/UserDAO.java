package com.team.coin_simulator.user;

import com.team.coin_simulator.db.DBConnection;
import java.sql.*;

public class UserDAO {

    public boolean existsByEmail(String email) throws Exception {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        }
    }

    public void save(String email, String passwordHash) throws Exception {
        String sql = "INSERT INTO users (email, password_hash) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
        }
    }

    public User findByEmail(String email) throws Exception {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            return new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("password_hash")
            );
        }
    }
}
