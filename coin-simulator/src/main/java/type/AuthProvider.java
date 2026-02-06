package type;

public enum AuthProvider {
    EMAIL("이메일"),
    GOOGLE("구글"),
    KAKAO("카카오");

    private final String description;

    AuthProvider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}