package com.team.coin_simulator.Market_Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class AssetOrderModel {
	
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
}
