package com.team.coin_simulator.Market_Panel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerDto {

	private String code;      		// 종목 구분
    private double trade_price;  		// 현재가
    private double signed_change_rate;  // 전일 종가 대비 가격 변화율
    private double acc_trade_price;		// 총 거래대금 
    
    
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public double getTrade_price() {
		return trade_price;
	}
	public void setTrade_price(double trade_price) {
		this.trade_price = trade_price;
	}
	public double getSigned_change_rate() {
		return signed_change_rate;
	}
	public void setSigned_change_rate(double signed_change_rate) {
		this.signed_change_rate = signed_change_rate;
	}
	public double getAcc_trade_price() {
		return acc_trade_price;
	}
	public void setAcc_trade_price(double acc_trade_price) {
		this.acc_trade_price = acc_trade_price;
	}
    

	

}
