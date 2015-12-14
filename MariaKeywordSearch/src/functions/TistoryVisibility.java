package functions;

public enum TistoryVisibility {
    PRIVATE(0), PROTECT(1), PUBLIC(2), PUBLISH(3);
    private int value;

    TistoryVisibility(int value) {
        this.value = value;
    }
};

