package com.team.coin_simulator.Market_Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class LimitOrder {
    private final DataSource dataSource;
    private final OrderRepository repo = new OrderRepository(); // 공통 저장소 사용

    public LimitOrder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //지정가 주문 접수 및 자산 동결
    public void placeLimitOrder(String userId, int sideIdx, BigDecimal price, BigDecimal qty) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); // 트랜잭션 시작

            try {
                BigDecimal totalCost = price.multiply(qty);
                String currency = (sideIdx == 0) ? "KRW" : "BTC";
                BigDecimal requirement = (sideIdx == 0) ? totalCost : qty;

                // 1. 잔고 확인 (repo 사용)
                if (!repo.hasEnoughBalance(conn, userId, currency, requirement)) {
                    throw new RuntimeException("주문 가능 잔고가 부족합니다.");
                }

                // 2. 자산 동결 (Balance 차감 -> Locked 증가)
                repo.updateBalance(conn, userId, currency, requirement.negate());
                repo.updateLockedAsset(conn, userId, currency, requirement);

                // 3. 대기 주문 생성 (상태: WAIT)
                String orderId = "ORD-" + System.currentTimeMillis();
                repo.saveOrder(conn, orderId, userId, "LIMIT", (sideIdx == 0 ? "BID" : "ASK"), "WAIT", price, qty);

                conn.commit();
                System.out.println("지정가 주문 접수 완료 [WAIT]");

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    //주문 취소 로직

    public void cancelOrder(String userId, String orderId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. 주문 상태 확인 (WAIT 인지 확인)
                String sqlCheck = "SELECT side, price, quantity FROM orders WHERE order_id = ? AND user_id = ? AND status = 'WAIT' FOR UPDATE";
                
                String side;
                BigDecimal price, qty;

                try (var pstmt = conn.prepareStatement(sqlCheck)) {
                    pstmt.setString(1, orderId);
                    pstmt.setString(2, userId);
                    var rs = pstmt.executeQuery();
                    if (!rs.next()) throw new RuntimeException("취소 가능한 주문이 없습니다.");
                    
                    side = rs.getString("side");
                    price = rs.getBigDecimal("price");
                    qty = rs.getBigDecimal("quantity");
                }

                // 2. 상태 변경: WAIT -> CANCEL
                updateStatus(conn, orderId, "CANCEL");

                // 3. 자산 반환 (Locked 차감 -> Balance 증가)
                BigDecimal amountToReturn = "BID".equals(side) ? price.multiply(qty) : qty;
                String currency = "BID".equals(side) ? "KRW" : "BTC";

                repo.updateLockedAsset(conn, userId, currency, amountToReturn.negate());
                repo.updateBalance(conn, userId, currency, amountToReturn);

                conn.commit();
                System.out.println("주문 취소 및 자산 반환 완료");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void updateStatus(Connection conn, String orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, orderId);
            pstmt.executeUpdate();
        }
    }
}