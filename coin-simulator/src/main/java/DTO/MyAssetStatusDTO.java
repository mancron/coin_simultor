package DTO;

import java.math.BigDecimal;

//assets 테이블 + 현재가 정보 + 수익률 계산 결과
public class MyAssetStatusDTO {
	 private String currency;        // 코인명 (BTC)
	 private BigDecimal balance;     // 보유 수량 (from assets)
	 private BigDecimal avgPrice;    // 평단가 (from assets)
	 private BigDecimal currentPrice;// 현재가 (from market_candle or API)
	 private BigDecimal totalValue;  // 평가 금액 (balance * currentPrice)
	 private double profitRate;      // 수익률 ((current - avg) / avg * 100)
	 
	 public String getCurrency() {
		 return currency;
	 }
	 public void setCurrency(String currency) {
		 this.currency = currency;
	 }
	 public BigDecimal getBalance() {
		 return balance;
	 }
	 public void setBalance(BigDecimal balance) {
		 this.balance = balance;
	 }
	 public BigDecimal getAvgPrice() {
		 return avgPrice;
	 }
	 public void setAvgPrice(BigDecimal avgPrice) {
		 this.avgPrice = avgPrice;
	 }
	 public BigDecimal getCurrentPrice() {
		 return currentPrice;
	 }
	 public void setCurrentPrice(BigDecimal currentPrice) {
		 this.currentPrice = currentPrice;
	 }
	 public BigDecimal getTotalValue() {
		 return totalValue;
	 }
	 public void setTotalValue(BigDecimal totalValue) {
		 this.totalValue = totalValue;
	 }
	 public double getProfitRate() {
		 return profitRate;
	 }
	 public void setProfitRate(double profitRate) {
		 this.profitRate = profitRate;
	 }
}