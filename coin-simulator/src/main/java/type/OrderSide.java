package type;

public enum OrderSide {
    BID("매수"), 
    ASK("매도");

    private final String label;
    
    OrderSide(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}