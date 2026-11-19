package ru.onlinesim;

/** Result of {@link NumbersApi#getWithNumber(String, int)}. */
public final class NumberOrder extends Model {
    private long tzid;
    private String number;
    private int country;
    private int time;
    private String service;
    private String title;
    private Integer guardIntervalRemainingSeconds;

    private NumberOrder() {}

    public long tzid() {
        return tzid;
    }

    /** @return full number with {@code +}, e.g. {@code +447459608320} */
    public String number() {
        return number;
    }

    public int country() {
        return country;
    }

    /** @return seconds left */
    public int time() {
        return time;
    }

    public String service() {
        return service;
    }

    public String title() {
        return title;
    }

    public Integer guardIntervalRemainingSeconds() {
        return guardIntervalRemainingSeconds;
    }
}
