package com.team.coin_simulator;

import java.math.BigDecimal;
import java.sql.Timestamp;

// lombok을 안 쓴다면 getter/setter/생성자를 직접 구현

public class CoinDTO {
    
    private String code;            // 코인 코드 (예: KRW-BTC)
    private String name;            // 코인 이름 (예: 비트코인)
    private BigDecimal price;       // 현재가 (금융 계산은 BigDecimal 필수)
    private double changeRate;      // 등락률 (퍼센트는 double 가능)
    private BigDecimal volume;      // 거래량
    private Timestamp tradeTime;    // 체결 시간

    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public double getChangeRate() {
		return changeRate;
	}

	public void setChangeRate(double changeRate) {
		this.changeRate = changeRate;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public void setVolume(BigDecimal volume) {
		this.volume = volume;
	}

	public Timestamp getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(Timestamp tradeTime) {
		this.tradeTime = tradeTime;
	}

	// 기본 생성자
    public CoinDTO() {}

    // 전체 생성자
    public CoinDTO(String code, String name, BigDecimal price, double changeRate) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.changeRate = changeRate;
    }

    
    
    
    @Override
    public String toString() {
        return "CoinDTO [name=" + name + ", price=" + price + "]";
    }
}