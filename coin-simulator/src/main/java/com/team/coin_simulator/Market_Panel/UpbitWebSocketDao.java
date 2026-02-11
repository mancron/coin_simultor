package com.team.coin_simulator.Market_Panel;

import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.coin_simulator.CoinConfig;

import DTO.TickerDto;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class UpbitWebSocketDao extends WebSocketListener {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HistoryPanel historyPanel; // UI를 갱신하기 위해 추가

    // 생성자에서 HistoryPanel을 주입받음
    public UpbitWebSocketDao(HistoryPanel historyPanel) {
        this.historyPanel = historyPanel;
    }

    private String getSubscriptionMessage() {
        // CoinConfig.getCodes()를 스트림으로 변환
        String codes = CoinConfig.getCodes().stream()
                .map(coin -> "\"KRW-" + coin + "\"")
                .collect(Collectors.joining(","));
        
        return String.format("[{\"ticket\":\"test\"},{\"type\":\"ticker\",\"codes\":[%s]}]", codes);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send(getSubscriptionMessage());
    }
    
    

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        try {
            String json = bytes.utf8();
            TickerDto data = objectMapper.readValue(json, TickerDto.class);
            
            // [핵심] Swing UI 업데이트는 반드시 별도의 이벤트 스레드에서 실행
            SwingUtilities.invokeLater(() -> {
                // 1. 심볼 변환 (KRW-BTC -> BTC)
                if (data.getCode() == null) return;	
                String symbol = data.getCode().replace("KRW-", "");
                
                // 2. 가격 포맷 (천단위 콤마)
                double price = data.getTrade_price();
                String priceStr;
                
                double accPrice = data.getAcc_trade_price();
                String accPriceStr;

                if (accPrice >= 100_000_000) {
                    // 1억 원 이상일 경우 '억' 단위 표시 (선택 사항)
                    accPriceStr = String.format("%,.0f백만", accPrice / 1_000_000);
                } else {
                    accPriceStr = String.format("%,.0f", accPrice);
                }
                
                if (price < 1) {
                    // 1원 미만 (예: 0.0005) -> 소수점 4자리 표시
                    priceStr = String.format("%,.5f", price);
                } else if (price < 100) {
                    // 100원 미만 (예: 12.50) -> 소수점 2자리 표시
                    priceStr = String.format("%,.2f", price);
                } else {
                    // 100원 이상 -> 소수점 없이 정수만 표시
                    priceStr = String.format("%,.0f", price);
                }
                
                // 3. 등락률 계산 및 포맷 (signed_change_rate는 소수점이므로 * 100)
                String flucStr = String.format("%.2f", data.getSigned_change_rate() * 100);

                
                // 4. HistoryPanel의 메소드 호출
                historyPanel.updateCoinPrice(symbol, priceStr, flucStr,accPriceStr);
                
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}