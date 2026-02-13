package com.team.coin_simulator.orderbook;

import okhttp3.*;
import okio.ByteString;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Collections;
import java.util.TreeMap;

public class UpbitOrderBookService extends WebSocketListener {
    private OrderBookPanel panel;
    private String market;

    public UpbitOrderBookService(OrderBookPanel panel, String market) {
        this.panel = panel;
        this.market = market;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        String json = "[{\"ticket\":\"test\"},{\"type\":\"orderbook\",\"codes\":[\"" + market + "\"]}]";
        webSocket.send(json);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        String data = bytes.utf8();
        JSONObject obj = new JSONObject(data);
        JSONArray orderbookUnits = obj.getJSONArray("orderbook_units");
        
        TreeMap<Double, Double> asks = new TreeMap<>();
        TreeMap<Double, Double> bids = new TreeMap<>(Collections.reverseOrder());

        for (int i = 0; i < orderbookUnits.length(); i++) {
            JSONObject unit = orderbookUnits.getJSONObject(i);
            asks.put(unit.getDouble("ask_price"), unit.getDouble("ask_size"));
            bids.put(unit.getDouble("bid_price"), unit.getDouble("bid_size"));
        }

        // 전일 종가는 별도 Ticker API가 필요하므로, 여기선 예시값 전달 (필요시 추가 구현)
        panel.updateData(asks, bids, bids.isEmpty() ? 0 : bids.firstKey()); 
    }
}