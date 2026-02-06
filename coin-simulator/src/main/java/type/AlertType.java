package type;

public enum AlertType {
    ABOVE("이상"), 
    BELOW("이하");

    private final String label;

    AlertType(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}