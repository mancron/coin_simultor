package com.team.coin_simulator.Market_Order;
import java.math.BigDecimal;
import java.math.RoundingMode;

class OrderCalc{
		private static final BigDecimal FeeRate = new BigDecimal("0.0005"); //수수료 0.05%
		private static final int Scale = 8; //소수점 8자리까지 계산
		
		//매수 가능한 최대 수량(현금/(가격*(1+수수료)))
		public static BigDecimal getMaxBuyVolume(BigDecimal KRWBalance, BigDecimal price) {
			//0원 이하 있으면 0개 반환
			if(price.compareTo(BigDecimal.ZERO)<=0) return BigDecimal.ZERO;
			//코인 1개 당 드는 비용=가격(1+수수료)
			BigDecimal costPerUnit = price.multiply(BigDecimal.ONE.add(FeeRate));
			return KRWBalance.divide(costPerUnit, Scale, RoundingMode.DOWN);
		}
		
		//매수 시 필요한 총 금액(시장가용: 수수료 포함 금액) 입력금액(=amount)(1+수수료)
		public static BigDecimal calcTotalBuyCost(BigDecimal amount) {
			return amount.multiply
					(BigDecimal.ONE.add(FeeRate)).setScale(0,RoundingMode.UP);
		}
	}