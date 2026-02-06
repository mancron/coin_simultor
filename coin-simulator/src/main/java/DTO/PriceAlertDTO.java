package DTO;



import java.math.BigDecimal;
import java.sql.Timestamp;
import java.io.Serializable;

/*
 * 
 * 
 // DB에 저장할 때 (Insert)
	pstmt.setString(4, AlertType.ABOVE.name()); // "ABOVE"로 저장됨
	
	// 로직에서 비교할 때
	if (AlertType.ABOVE.name().equals(dto.getConditionType())) {
	    // 현재가가 목표가보다 크면 알림 발송!
	    if (currentPrice.compareTo(dto.getTargetPrice()) >= 0) {
	        sendAlert();
	    }
	}
	
 * */



	public class PriceAlertDTO implements Serializable {

	    private static final long serialVersionUID = 1L;

	    // 1. PK & 식별자
	    private Long alertId;           // alert_id
	    private String userId;          // user_id
	    private String market;          // market (KRW-BTC 등)

	    // 2. 알림 조건 (핵심 데이터)
	    private BigDecimal targetPrice; // target_price (목표 가격)
	    
	    // DB에는 "ABOVE", "BELOW" 문자열로 저장됨
	    private String conditionType;   

	    // 3. 상태 및 시간
	    private boolean active;         // is_active (알림 켜짐/꺼짐)
	    private Timestamp createdAt;    // created_at

	    // --- 생성자 (Constructors) ---

	    public PriceAlertDTO() {
	    }

	    // 신규 알림 등록용 생성자 (ID, 날짜는 DB가 자동생성하므로 제외)
	    public PriceAlertDTO(String userId, String market, BigDecimal targetPrice, String conditionType) {
	        this.userId = userId;
	        this.market = market;
	        this.targetPrice = targetPrice;
	        this.conditionType = conditionType;
	        this.active = true; // 생성 시 기본값 true
	    }

	    // --- Getter & Setter ---

	    public Long getAlertId() { return alertId; }
	    public void setAlertId(Long alertId) { this.alertId = alertId; }

	    public String getUserId() { return userId; }
	    public void setUserId(String userId) { this.userId = userId; }

	    public String getMarket() { return market; }
	    public void setMarket(String market) { this.market = market; }

	    public BigDecimal getTargetPrice() { return targetPrice; }
	    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }

	    public String getConditionType() { return conditionType; }
	    public void setConditionType(String conditionType) { this.conditionType = conditionType; }

	    // boolean 타입의 Getter는 'get' 대신 'is'를 쓰는 관례가 있음
	    public boolean isActive() { return active; }
	    public void setActive(boolean active) { this.active = active; }

	    public Timestamp getCreatedAt() { return createdAt; }
	    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

	    // --- toString ---
	    @Override
	    public String toString() {
	        return "PriceAlert [market=" + market + ", target=" + targetPrice + 
	               ", condition=" + conditionType + ", active=" + active + "]";
	    }
	    
	    
	}
