package com.team.coin_simulator;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CoinConfig {
    // [1] 코인 코드와 한글 이름을 매핑 (순서 유지 위해 LinkedHashMap 사용)
    public static final Map<String, String> COIN_INFO;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("BTC", "비트코인");
        map.put("ETH", "이더리움");
        map.put("USDT", "테더");
        map.put("SOL", "솔라나");
        map.put("XRP", "리플");
        map.put("DOGE", "도지코인");
        map.put("AXS", "엑시인피니티");
        map.put("ADA", "에이다");
        map.put("SUI", "수이");
        map.put("AERGO", "아르고");
        map.put("POKT", "포켓네트워크");
        map.put("SHIB", "시바이누");
        map.put("ONDO", "온도파이낸스");
        map.put("XLM", "스텔라루멘");
        map.put("VIRTUAL", "버추털프로토콜");
        map.put("BCH", "비트코인캐시");
        map.put("AWE", "에이더블유이");
        map.put("LINK", "체인링크");
        map.put("STX", "스택스");
        map.put("HBAR", "헤데라");
        
        // 수정 불가한 맵으로 포장
        COIN_INFO = Collections.unmodifiableMap(map);
    }
    
    // [2] API 요청용으로 코드 리스트(KeySet)만 필요할 때 사용
    public static Set<String> getCodes() {
        return COIN_INFO.keySet();
    }
}