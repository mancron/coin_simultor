package com.team.coin_simulator.Market_Panel;


import java.util.concurrent.*;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class CoinPriceScheduler {
    private static final List<String> COINS = Arrays.asList(
        "BTC", "ETH", "XRP", "SOL", "ADA", "DOGE", "TRX", "LINK",
        "DOT", "MATIC", "LTC", "BCH", "SHIB", "AVAX", "DAI", "UNI"
    );

    // 설정값 상수
    private static final long NORMAL_INTERVAL_MS = 200;  // 정상: 0.2초 (5 req/s)
    private static final long COOLDOWN_MS = 5000;        // 차단 시: 5초 대기

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public void start() {
        System.out.println("=== 동적 스케줄러 시작 ===");
        scheduleNext(0); // 즉시 시작
    }

    // 다음 작업을 예약하는 메서드 (핵심 로직)
    private void scheduleNext(long delayMs) {
        scheduler.schedule(this::executeTask, delayMs, TimeUnit.MILLISECONDS);
    }

    private void executeTask() {
        // 1. 코인 선택
        int index = currentIndex.getAndUpdate(i -> (i + 1) % COINS.size());
        String targetCoin = COINS.get(index);
        
        long nextDelay = NORMAL_INTERVAL_MS; // 기본값 200ms

        try {
            // 2. API 요청 시도
            int responseCode = mockApiRequest(targetCoin);

            // 3. 응답 코드 분석
            if (responseCode == 429) { // Too Many Requests
                System.err.println("!! 429 에러 발생 (Rate Limit) !! -> 5초 쿨다운 진입");
                nextDelay = COOLDOWN_MS; 
                // 실패한 코인을 다시 시도하고 싶다면 인덱스를 되돌리는 로직 추가 가능
                // currentIndex.decrementAndGet(); 
            } else if (responseCode == 200) {
                System.out.println("정상 수신: " + targetCoin);
            }

        } catch (Exception e) {
            System.err.println("네트워크 오류 -> 쿨다운 적용");
            nextDelay = COOLDOWN_MS;
        }

        // 4. 다음 작업 예약 (재귀 호출)
        scheduleNext(nextDelay);
    }

    // 테스트용 모의 API (랜덤하게 429 에러 발생)
    private int mockApiRequest(String coin) {
        // 약 5% 확률로 429 에러 시뮬레이션
        if (Math.random() < 0.05) return 429;
        return 200;
    }

    public static void main(String[] args) {
        new CoinPriceScheduler().start();
    }
}