package com.team.coin_simulator.Market_Panel;

import java.math.BigDecimal;

public class AssetDTO {

	// 직렬화 ID (선택사항이지만 권장됨)
    private static final long serialVersionUID = 1L;

    // 1. PK & 식별자
    private String userId;      // user_id
    private String currency;    // currency (KRW, BTC, ETH ...)
    private Long sessionId;     // session_id (실전/백테스트 구분)

    // 2. 자산 데이터 (금융 계산 정밀도 유지를 위해 BigDecimal 사용)
    private BigDecimal balance;     // balance (주문 가능 잔고)
    private BigDecimal locked;      // locked (미체결 주문으로 묶인 돈)
    private BigDecimal avgBuyPrice; // avg_buy_price (평단가)

    // --- 생성자 (Constructor) ---

    // 기본 생성자 (필수)
    public AssetDTO() {}

    // 전체 필드 생성자 (테스트 및 객체 생성용)
    public AssetDTO(String userId, String currency, Long sessionId, 
                    BigDecimal balance, BigDecimal locked, BigDecimal avgBuyPrice) {
        this.userId = userId;
        this.currency = currency;
        this.sessionId = sessionId;
        this.balance = balance;
        this.locked = locked;
        this.avgBuyPrice = avgBuyPrice;
    }

    // --- Getter & Setter ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getLocked() {
        return locked;
    }

    public void setLocked(BigDecimal locked) {
        this.locked = locked;
    }

    public BigDecimal getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(BigDecimal avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }

    // --- 편의 메서드 및 toString ---

    // 총 보유 수량 (사용 가능 + 묶인 돈) 반환 - 로직 계산용
    public BigDecimal getTotalAmount() {
        // null 방지 처리 후 더하기
        BigDecimal bal = (balance == null) ? BigDecimal.ZERO : balance;
        BigDecimal lock = (locked == null) ? BigDecimal.ZERO : locked;
        return bal.add(lock);
    }

    @Override
    public String toString() {
        return "AssetDTO [userId=" + userId + ", currency=" + currency + 
               ", balance=" + balance + ", locked=" + locked + 
               ", avgBuyPrice=" + avgBuyPrice + "]";
    }

}
