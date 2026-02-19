package com.team.coin_simulator.Market_Order;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderCalc {
    // 수수료율 0.05% (업비트 등 국내 거래소 기준)
    private static final BigDecimal FEE_RATE = new BigDecimal("0.0005"); 
    private static final int SCALE_BTC = 8; // 코인 수량 소수점
    private static final int SCALE_KRW = 0; // 원화 금액 소수점 (정수 처리)

    //[매수] 현재 잔고로 살 수 있는 최대 수량 계산
    public static String getAvailableVolumeString(String priceText, BigDecimal krwBalance) {
        try {
            if (priceText == null || priceText.replace(",", "").trim().isEmpty()) return "0.00000000";
            
            BigDecimal price = new BigDecimal(priceText.replace(",", "").trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) return "0.00000000";

            // 1단위 구매 시 드는 총 비용 = 가격 + 수수료(가격 * 0.0005)
            BigDecimal costPerUnit = price.multiply(BigDecimal.ONE.add(FEE_RATE));
            
            // 최대 수량 = 잔고 / 단위당 비용
            BigDecimal maxBTC = krwBalance.divide(costPerUnit, SCALE_BTC, RoundingMode.DOWN);
            return String.format("%.8f", maxBTC);
        } catch (Exception e) {
            return "0.00000000";
        }
    }

    //[매도] 매도 시 수수료를 제외하고 실제로 받게 될 원화 계산
    public static String getExpectedSellKRWString(String priceText, String qtyText) {
        try {
            if (priceText == null || qtyText == null) return "0";
            
            BigDecimal price = new BigDecimal(priceText.replace(",", "").trim());
            BigDecimal qty = new BigDecimal(qtyText.replace(",", "").trim());
            
            if (price.compareTo(BigDecimal.ZERO) <= 0 || qty.compareTo(BigDecimal.ZERO) <= 0) return "0";

            // 총 매도 대금 = 가격 * 수량
            BigDecimal totalSellAmount = price.multiply(qty);
            // 수수료 = 총 매도 대금 * 0.0005
            BigDecimal fee = totalSellAmount.multiply(FEE_RATE);
            // 실 수령액 = 총 매도 대금 - 수수료
            BigDecimal netProceeds = totalSellAmount.subtract(fee);

            // 원화는 소수점 없이 콤마만 찍어서 반환
            return String.format("%,.0f", netProceeds.setScale(SCALE_KRW, RoundingMode.DOWN));
        } catch (Exception e) {
            return "0";
        }
    }

    //[공통] 단순 총액 계산 (수수료 제외 가격 * 수량)

    public static BigDecimal calcTotalCost(BigDecimal price, BigDecimal qty) {
        if (price == null || qty == null) return BigDecimal.ZERO;
        return price.multiply(qty).setScale(SCALE_KRW, RoundingMode.DOWN);
    }
 // [시장가 매수] 입력한 총액(KRW)으로 살 수 있는 코인 수량 계산 (수수료 반영)
    public static BigDecimal calculateMarketBuyQuantity(BigDecimal totalAmountKRW, BigDecimal currentPrice) {
        if (totalAmountKRW == null || currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // 1 코인당 실제 구매 비용 = 현재가 + 수수료(현재가 * 0.0005)
        BigDecimal costPerUnit = currentPrice.multiply(BigDecimal.ONE.add(FEE_RATE));
        
        // 살 수 있는 수량 = 내가 낸 돈 / 1 코인당 구매 비용 (소수점 8자리 내림)
        return totalAmountKRW.divide(costPerUnit, SCALE_BTC, RoundingMode.DOWN);
    }
}