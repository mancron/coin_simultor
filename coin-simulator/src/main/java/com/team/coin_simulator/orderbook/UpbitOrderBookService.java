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
    private double currentPrevClose = 0;

    public UpbitOrderBookService(OrderBookPanel panel, String market) {
        this.panel = panel;
        this.market = market;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        String json = "[{\"ticket\":\"test\"},{\"type\":\"ticker\",\"codes\":[\"" + market + "\"]},{\"type\":\"orderbook\",\"codes\":[\"" + market + "\"]}]";
        webSocket.send(json);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        String data = bytes.utf8();
        JSONObject obj = new JSONObject(data);
        String type = obj.optString("type");

        if ("ticker".equals(type)) {
            // ticker 정보에서 전일 종가(prev_closing_price) 추출
            currentPrevClose = obj.getDouble("prev_closing_price");
        } 
        else if ("orderbook".equals(type)) {
            JSONArray orderbookUnits = obj.getJSONArray("orderbook_units");
            
            TreeMap<Double, Double> asks = new TreeMap<>();
            TreeMap<Double, Double> bids = new TreeMap<>(Collections.reverseOrder());

            for (int i = 0; i < orderbookUnits.length(); i++) {
                JSONObject unit = orderbookUnits.getJSONObject(i);
                asks.put(unit.getDouble("ask_price"), unit.getDouble("ask_size"));
                bids.put(unit.getDouble("bid_price"), unit.getDouble("bid_size"));
            }

            // 확보된 전일 종가(currentPrevClose)를 사용하여 데이터 갱신
            panel.updateData(asks, bids, currentPrevClose);
        }
    }
}