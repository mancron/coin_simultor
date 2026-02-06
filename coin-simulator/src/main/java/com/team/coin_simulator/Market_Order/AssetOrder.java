package com.team.coin_simulator.Market_Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class AssetOrder {
	
	String[] OrderStatus = {"WAIT", "DONE", "CANCEL"};
	String[] OrderType = {"LIMIT", "MARKET"};//주문 형태: 지정가, 시장가
	String[] Side = {"BID", "ASK"}; //매매 구분: 매수, 매도
	
	class Asset{ //double은 수수료 계산 시 오차가 발생함
		BigDecimal balance; //사용 가능 잔고
		BigDecimal locked; //주문 대기 중 잠긴 잔고
		
		public Asset(BigDecimal balance) { //초기 자산 설정
			this.balance = balance; //초기 잔액
			this.locked = new BigDecimal("0"); //처음엔 주문 없으니까 0
		}
	}
	
	class Order{
		String order_id;
		int status; //만들어둔 인덱스 사용
		int type;
		int side;
		BigDecimal price;
		BigDecimal quantity;
		
		public Order(String order_id, int status, int type,
				int side, BigDecimal price, BigDecimal quantity) {
			this.order_id=order_id;
			this.status=0;
			this.type=type;
			this.side=side;
			this.price=price;
			this.quantity=quantity;
		}
	}
	
	 //주문 가능 수량 계산
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
	
	//시장가 주문, 지정가 주문 처리
	class OrderService{
		//DB연동 시 추후 수정
		Map<String, Asset> userAssets = new HashMap<>();
	    Map<String, Order> orderDatabase = new HashMap<>(); // DB 대용 임시 저장소
	    BigDecimal currentMarketPrice = new BigDecimal("50000000"); // 예시 현재가
	    
	    //시장가 주문
	    public void MarketOrder(String user_id, int side) {
	    	
	    }
	}
	
}
