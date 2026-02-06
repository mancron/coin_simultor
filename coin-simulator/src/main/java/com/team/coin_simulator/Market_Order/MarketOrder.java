package com.team.coin_simulator.Market_Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.team.coin_simulator.Market_Order.AssetOrderModel.Asset;

public class MarketOrder {
	private final DataSource dataSource;
	private final OrderRepository repo = new OrderRepository();
	
    private final BigDecimal currentMarketPrice = new BigDecimal("50000000"); // 시뮬레이션용 현재가
    private final BigDecimal feeRate = new BigDecimal("0.0005");//수수료 0.05%

    //데이터 받아옴
    public MarketOrder(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
 //시장가 주문 즉시 체결
public void executeMarketOrder(String userId, int sideIdx, BigDecimal amountOrQty) throws Exception {
        
        try (Connection conn = dataSource.getConnection()) {
            	try{
            		conn.setAutoCommit(false); // 트랜잭션 시작 (BEGIN)
            	
            
                if (amountOrQty.compareTo(new BigDecimal("5000")) < 0) {
                        throw new RuntimeException("최소 주문 금액은 5,000원 이상이어야 합니다.");
                    }

                    // 2. 자산 확인 및 잠금 (SELECT FOR UPDATE)
                    if (sideIdx == 0) {//매수 프로세스
                    	if (!repo.hasEnoughBalance(conn, userId, "KRW", amountOrQty)) {
                        throw new RuntimeException("주문 가능 금액(KRW)이 부족합니다.");
                    }

                    // 3. 수수료 및 매수 수량 계산
                    // 실제 매수 투입 금액 = 주문 총액 - 수수료
                    BigDecimal fee = amountOrQty.multiply(feeRate);
                    BigDecimal actualInvestment = amountOrQty.subtract(fee);
                    // 매수 수량 = 실제 투입 금액 / 현재가
                    BigDecimal buyQty = actualInvestment.divide(currentMarketPrice, 8, RoundingMode.DOWN);

                    // 4. 자산 업데이트 (Atomic Update)
                    repo.updateBalance(conn, userId, "KRW", amountOrQty.negate()); // KRW 차감
                    repo.updateBalance(conn, userId, "BTC", buyQty);               // BTC 증가

                    // 5. 기록 저장 (상태: DONE, 타입: MARKET)
                    String orderId = "ORD-"+System.currentTimeMillis();
                    repo.saveOrder(conn, orderId, userId, "MARKET", "BID", "DONE", currentMarketPrice, buyQty);

                } else { //매도 프로세스
                    // 1. 코인 잔고 확인
                    if (!repo.hasEnoughBalance(conn, userId, "BTC", amountOrQty)) {
                        throw new RuntimeException("보유 코인(BTC)이 부족합니다.");
                    }

                    // 2. 금액 및 수수료 계산
                    // 총 매도 금액 = 주문 수량 * 현재가
                    BigDecimal totalSaleAmount = amountOrQty.multiply(currentMarketPrice);
                    BigDecimal fee = totalSaleAmount.multiply(feeRate);
                    // 최종 정산 금액 = 총 매도 금액 - 수수료
                    BigDecimal settleAmount = totalSaleAmount.subtract(fee);

                    // 3. 자산 업데이트
                    repo.updateBalance(conn, userId, "BTC", amountOrQty.negate()); // BTC 차감
                    repo.updateBalance(conn, userId, "KRW", settleAmount);         // KRW 증가

                    // 4. 기록 저장 (상태: DONE)
                    String orderId = "ORD-" + System.currentTimeMillis();
                    repo.saveOrder(conn, orderId, userId, "MARKET", "ASK", "DONE", 
                    		currentMarketPrice, amountOrQty);
                }

                conn.commit(); // 트랜잭션 종료 (COMMIT)
                System.out.println("시장가 주문 체결 및 정산 완료");

            } catch (Exception e) {
                conn.rollback(); // 예외 발생 시 롤백
                throw e;
            	}
        	}
		}
    }