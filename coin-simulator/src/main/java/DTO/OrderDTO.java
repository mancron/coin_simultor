package DTO;

import java.math.BigDecimal;

import com.google.protobuf.Timestamp;

public class OrderDTO {


	// 1. PK
    private Long orderId;

    // 2. 식별자
    private String userId;
    private Long sessionId;

    // 3. 주문 정보
    private String market;          // KRW-BTC
    private String side;            // BID(매수), ASK(매도) -> String 대신 enum 추천
    private String type;            // LIMIT, MARKET -> String 대신 enum 추천

    // 4. 가격/수량 (돈 계산은 무조건 BigDecimal)
    private BigDecimal originalPrice;
    private BigDecimal originalVolume;
    private BigDecimal remainingVolume;

    // 5. 상태 및 시간
    private String status;          // WAIT, DONE, CANCEL
    private Timestamp createdAt;

    // 기본 생성자
    public OrderDTO() {}

    // Getter, Setter 생략 (Lombok @Data 쓰면 편함)
}
