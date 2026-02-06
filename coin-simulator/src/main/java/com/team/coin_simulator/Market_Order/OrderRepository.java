package com.team.coin_simulator.Market_Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderRepository {
	    
	    // 1. 잔고 확인 및 행 잠금 (FOR UPDATE)
	    public boolean hasEnoughBalance(Connection conn, String userId, String currency, BigDecimal req) throws SQLException {
	        String sql = "SELECT balance FROM assets WHERE user_id = ? AND currency = ? FOR UPDATE";
	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setString(1, userId);
	            pstmt.setString(2, currency);
	            try (ResultSet rs = pstmt.executeQuery()) {
	                return rs.next() && rs.getBigDecimal("balance").compareTo(req) >= 0;
	            }
	        }
	    }

	    // 2. 가용 자산(Balance) 업데이트
	    public void updateBalance(Connection conn, String userId, String currency, BigDecimal amount) throws SQLException {
	        String sql = "UPDATE assets SET balance = balance + ? WHERE user_id = ? AND currency = ?";
	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setBigDecimal(1, amount);
	            pstmt.setString(2, userId);
	            pstmt.setString(3, currency);
	            pstmt.executeUpdate();
	        }
	    }

	    // 3. 동결 자산(Locked) 업데이트
	    public void updateLockedAsset(Connection conn, String userId, String currency, BigDecimal amount) throws SQLException {
	        String sql = "UPDATE assets SET locked = locked + ? WHERE user_id = ? AND currency = ?";
	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setBigDecimal(1, amount);
	            pstmt.setString(2, userId);
	            pstmt.setString(3, currency);
	            pstmt.executeUpdate();
	        }
	    }

	    // 4. 주문 기록 저장
	    public void saveOrder(Connection conn, String orderId, String userId, String type, String side, String status, BigDecimal price, BigDecimal qty) throws SQLException {
	        String sql = "INSERT INTO orders (order_id, user_id, type, side, status, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)";
	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setString(1, orderId);
	            pstmt.setString(2, userId);
	            pstmt.setString(3, type);
	            pstmt.setString(4, side);
	            pstmt.setString(5, status);
	            pstmt.setBigDecimal(6, price);
	            pstmt.setBigDecimal(7, qty);
	            pstmt.executeUpdate();
	        }
	    }
	}