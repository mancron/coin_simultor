package com.team.coin_simulator.chart;

import java.time.LocalDateTime;

public class CandleDTO {
	private String market;                   // 종목 코드 (VARCHAR)
    private LocalDateTime candleDateTimeKst; // 캔들 시간 KST (DATETIME)
    private LocalDateTime candleDateTimeUtc; // 캔들 시간 UTC (DATETIME)
    private double openingPrice;             // 시가 (DECIMAL)
    private double highPrice;                // 고가 (DECIMAL)
    private double lowPrice;                 // 저가 (DECIMAL)
    private double tradePrice;               // 종가 (DECIMAL)
    private long timestamp;                  // 타임 스탬프 (BIGINT)
    private double candleAccTradePrice;      // 누적 거래 대금 (DECIMAL)
    private double candleAccTradeVolume;     // 누적 거래량 (DECIMAL)
    private int unit;						//분 단위
    
    
    public CandleDTO(){    }
    
    
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public LocalDateTime getCandleDateTimeKst() {
		return candleDateTimeKst;
	}
	public void setCandleDateTimeKst(LocalDateTime candleDateTimeKst) {
		this.candleDateTimeKst = candleDateTimeKst;
	}
	public LocalDateTime getCandleDateTimeUtc() {
		return candleDateTimeUtc;
	}
	public void setCandleDateTimeUtc(LocalDateTime candleDateTimeUtc) {
		this.candleDateTimeUtc = candleDateTimeUtc;
	}
	public double getOpeningPrice() {
		return openingPrice;
	}
	public void setOpeningPrice(double openingPrice) {
		this.openingPrice = openingPrice;
	}
	public double getHighPrice() {
		return highPrice;
	}
	public void setHighPrice(double highPrice) {
		this.highPrice = highPrice;
	}
	public double getLowPrice() {
		return lowPrice;
	}
	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}
	public double getTradePrice() {
		return tradePrice;
	}
	public void setTradePrice(double tradePrice) {
		this.tradePrice = tradePrice;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public double getCandleAccTradePrice() {
		return candleAccTradePrice;
	}
	public void setCandleAccTradePrice(double candleAccTradePrice) {
		this.candleAccTradePrice = candleAccTradePrice;
	}
	public double getCandleAccTradeVolume() {
		return candleAccTradeVolume;
	}
	public void setCandleAccTradeVolume(double candleAccTradeVolume) {
		this.candleAccTradeVolume = candleAccTradeVolume;
	}
	public int getUnit() {
		return unit;
	}
	public void setUnit(int unit) {
		this.unit = unit;
	}	
}
