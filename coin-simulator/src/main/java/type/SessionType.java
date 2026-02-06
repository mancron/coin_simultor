package type;

public enum SessionType {
    REALTIME("실시간 모의투자"), 
    BACKTEST("과거 백테스팅");

    private final String description;

    SessionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}