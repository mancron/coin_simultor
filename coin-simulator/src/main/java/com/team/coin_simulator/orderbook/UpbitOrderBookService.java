package com.team.coin_simulator.orderbook;

import okhttp3.*;
import java.util.Collections;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class UpbitOrderBookService extends WebSocketListener {
	
	private OrderBookFrame frame;
    private OrderBookPanel panel;
    private String market; // 예: "KRW-BTC"

    public UpbitOrderBookService(OrderBookFrame frame, OrderBookPanel panel, String market) {
    	
    	this.frame = frame;
        this.panel = panel;
        this.market = market;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        // 업비트 형식에 맞는 구독 요청 메시지 (호가: orderbook)
        String json = "[{\"ticket\":\"test\"},{\"type\":\"orderbook\",\"codes\":[\"" + market + "\"]}]";
        webSocket.send(json);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        String data = bytes.utf8();
        JSONObject obj = new JSONObject(data);
        
        // 업비트에서 받은 실시간 호가 리스트 추출
        JSONArray orderbookUnits = obj.getJSONArray("orderbook_units");
        
        TreeMap<Double, Double> asks = new TreeMap<>();
        TreeMap<Double, Double> bids = new TreeMap<>(Collections.reverseOrder());

        for (int i = 0; i < orderbookUnits.length(); i++) {
            JSONObject unit = orderbookUnits.getJSONObject(i);
            asks.put(unit.getDouble("ask_price"), unit.getDouble("ask_size"));
            bids.put(unit.getDouble("bid_price"), unit.getDouble("bid_size"));
        }

        // UI 업데이트 (기존 prevClose는 현재가나 전일 종가 데이터 연동 필요)
        panel.updateData(asks, bids, 100000000.0); 
        
        // 2. 현재가 레이블 업데이트 (추가된 부분)
        // 업비트 orderbook 응답에는 보통 가장 낮은 매도호가와 가장 높은 매수호가 사이의 중간값 또는 
        // 체결가 정보가 포함될 수 있으나, 가장 간단하게는 매수 1호가(bids.firstKey())를 현재가로 표시합니다.
        if (!bids.isEmpty()) {
            double currentPrice = bids.firstKey(); // 가장 높은 매수 호가
            frame.updatePrice(currentPrice);
        }
    }
}