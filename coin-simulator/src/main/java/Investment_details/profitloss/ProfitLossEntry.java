package Investment_details.profitloss;

import java.util.Date;

/**
 * 투자손익 패키지 내부에서 공유하는 데이터 전달 객체
 * 메인패널 -> 각 하위패널로 데이터를 넘길 때 사용
 */
public class ProfitLossEntry {

    private Date    date;            // 날짜
    private long    dailyPnl;        // 일별 손익 (원)
    private double  dailyYield;      // 일별 수익률 (%)
    private long    cumulativePnl;   // 누적 손익 (원)
    private double  cumulativeYield; // 누적 수익률 (%)
    private long    baseAsset;       // 기초 자산
    private long    finalAsset;      // 기말 자산
    private long    deposit;         // 입금
    private long    withdrawal;      // 출금

    // 기본 생성자
    public ProfitLossEntry() {}

    // 편의 생성자
    public ProfitLossEntry(Date date, long dailyPnl, double dailyYield,
                           long cumulativePnl, double cumulativeYield,
                           long baseAsset, long finalAsset,
                           long deposit, long withdrawal) {
        this.date            = date;
        this.dailyPnl        = dailyPnl;
        this.dailyYield      = dailyYield;
        this.cumulativePnl   = cumulativePnl;
        this.cumulativeYield = cumulativeYield;
        this.baseAsset       = baseAsset;
        this.finalAsset      = finalAsset;
        this.deposit         = deposit;
        this.withdrawal      = withdrawal;
    }

    // ---- Getters & Setters ----

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public long getDailyPnl() { return dailyPnl; }
    public void setDailyPnl(long dailyPnl) { this.dailyPnl = dailyPnl; }

    public double getDailyYield() { return dailyYield; }
    public void setDailyYield(double dailyYield) { this.dailyYield = dailyYield; }

    public long getCumulativePnl() { return cumulativePnl; }
    public void setCumulativePnl(long cumulativePnl) { this.cumulativePnl = cumulativePnl; }

    public double getCumulativeYield() { return cumulativeYield; }
    public void setCumulativeYield(double cumulativeYield) { this.cumulativeYield = cumulativeYield; }

    public long getBaseAsset() { return baseAsset; }
    public void setBaseAsset(long baseAsset) { this.baseAsset = baseAsset; }

    public long getFinalAsset() { return finalAsset; }
    public void setFinalAsset(long finalAsset) { this.finalAsset = finalAsset; }

    public long getDeposit() { return deposit; }
    public void setDeposit(long deposit) { this.deposit = deposit; }

    public long getWithdrawal() { return withdrawal; }
    public void setWithdrawal(long withdrawal) { this.withdrawal = withdrawal; }
}
