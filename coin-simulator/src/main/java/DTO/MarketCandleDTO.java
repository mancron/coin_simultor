package DTO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.io.Serializable;

/*
 * tradePrice의 의미: 업비트 API 등 대부분의 거래소 API에서 trade_price는 해당 캔들의 
 * **종가(Close Price)**를 의미합니다. 변수명을 closePrice로 바꿔서 쓰기도 하지만, 
 * DB 컬럼명과 일치시키는 것이 매핑 실수를 줄이는 데 유리하여 tradePrice로 유지했습니다.
 * 
 * 
 * unit 필드: DB 스키마에는 unit이 포함되어 있어 1분 봉, 5분 봉 등을 구분합니다. 
 * 이 값이 없으면 PK 중복 에러가 날 수 있으니 꼭 챙겨야 합니다.
 * 
 * 
 * BigDecimal 연산: 편의 메서드(isBullish, getVolatility)를 DTO 안에 넣어두면, 
 * 나중에 JSP나 Swing 화면에서 조건문(if(dto.isBullish()) color = RED;)
 * 을 짤 때 코드가 훨씬 깔끔해집니다.
 * 
 * */

public class MarketCandleDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    // 1. 식별자 (PK 구성 요소)
    private String market;              // market (예: KRW-BTC)
    private Integer unit;               // unit (분 단위: 1, 3, 5, 15, 60...)
    private Timestamp candleDateTimeUtc;// candle_date_time_utc (기준 시간)

    // 2. 시간 정보
    private Timestamp candleDateTimeKst;// candle_date_time_kst (한국 시간)
    private Long timestamp;             // timestamp (Unix Timestamp, 정렬용)

    // 3. OHLC (Open, High, Low, Close) - 가격 데이터
    private BigDecimal openingPrice;    // 시가
    private BigDecimal highPrice;       // 고가
    private BigDecimal lowPrice;        // 저가
    private BigDecimal tradePrice;      // 종가 (현재가)

    // 4. 거래량 데이터
    private BigDecimal candleAccTradePrice;  // 누적 거래 대금 (거래액)
    private BigDecimal candleAccTradeVolume; // 누적 거래량 (볼륨)

    // --- 생성자 (Constructors) ---

    // 기본 생성자
    public MarketCandleDTO() {
    }

    // 필수 데이터 생성자 (업비트 API 등에서 파싱할 때 유용)
    public MarketCandleDTO(String market, Integer unit, Timestamp candleDateTimeUtc, 
                           BigDecimal openingPrice, BigDecimal highPrice, 
                           BigDecimal lowPrice, BigDecimal tradePrice, 
                           BigDecimal candleAccTradeVolume) {
        this.market = market;
        this.unit = unit;
        this.candleDateTimeUtc = candleDateTimeUtc;
        this.openingPrice = openingPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.tradePrice = tradePrice;
        this.candleAccTradeVolume = candleAccTradeVolume;
    }

    // 양봉 여부 확인 (종가가 시가보다 높거나 같으면 true)
    public boolean isBullish() {
        if (tradePrice != null && openingPrice != null) {
            // compareTo 결과가 0 이상이면(크거나 같으면) 양봉 취급
            return tradePrice.compareTo(openingPrice) >= 0;
        }
        return false;
    }
    
	// 변동폭 계산 (고가 - 저가)
    public BigDecimal getVolatility() {
        if (highPrice != null && lowPrice != null) {
            return highPrice.subtract(lowPrice);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "Candle [" + market + " (" + unit + "m) | Time=" + candleDateTimeKst + 
               " | O=" + openingPrice + ", C=" + tradePrice + "]";
    }
    

    // --- Getter & Setter 영역 ---
    public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public Integer getUnit() {
		return unit;
	}

	public void setUnit(Integer unit) {
		this.unit = unit;
	}

	public Timestamp getCandleDateTimeUtc() {
		return candleDateTimeUtc;
	}

	public void setCandleDateTimeUtc(Timestamp candleDateTimeUtc) {
		this.candleDateTimeUtc = candleDateTimeUtc;
	}

	public Timestamp getCandleDateTimeKst() {
		return candleDateTimeKst;
	}

	public void setCandleDateTimeKst(Timestamp candleDateTimeKst) {
		this.candleDateTimeKst = candleDateTimeKst;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public BigDecimal getOpeningPrice() {
		return openingPrice;
	}

	public void setOpeningPrice(BigDecimal openingPrice) {
		this.openingPrice = openingPrice;
	}

	public BigDecimal getHighPrice() {
		return highPrice;
	}

	public void setHighPrice(BigDecimal highPrice) {
		this.highPrice = highPrice;
	}

	public BigDecimal getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(BigDecimal lowPrice) {
		this.lowPrice = lowPrice;
	}

	public BigDecimal getTradePrice() {
		return tradePrice;
	}

	public void setTradePrice(BigDecimal tradePrice) {
		this.tradePrice = tradePrice;
	}

	public BigDecimal getCandleAccTradePrice() {
		return candleAccTradePrice;
	}

	public void setCandleAccTradePrice(BigDecimal candleAccTradePrice) {
		this.candleAccTradePrice = candleAccTradePrice;
	}

	public BigDecimal getCandleAccTradeVolume() {
		return candleAccTradeVolume;
	}

	public void setCandleAccTradeVolume(BigDecimal candleAccTradeVolume) {
		this.candleAccTradeVolume = candleAccTradeVolume;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


}