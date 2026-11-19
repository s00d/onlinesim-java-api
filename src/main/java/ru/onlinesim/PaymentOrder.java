package ru.onlinesim;

/** One top-up order of {@link PaymentHistory#orders()}. */
public final class PaymentOrder extends Model {
    private long id;
    private String sum;
    private String currency;
    private int stat;
    private String formText;
    private String statusText;
    private String createdAt;

    private PaymentOrder() {}

    public long id() {
        return id;
    }

    /** @return decimal string as sent, e.g. {@code "89.00"} */
    public String sum() {
        return sum;
    }

    public String currency() {
        return currency;
    }

    public int stat() {
        return stat;
    }

    public String formText() {
        return formText;
    }

    public String statusText() {
        return statusText;
    }

    public String createdAt() {
        return createdAt;
    }
}
