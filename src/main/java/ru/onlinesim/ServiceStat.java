package ru.onlinesim;

/** One service of a {@link CountryStat}. */
public final class ServiceStat extends Model {
    private Integer count;
    private boolean popular;
    private int code;
    private double price;
    private long id;
    private String service;
    private String slug;

    private ServiceStat() {}

    /** @return numbers in stock, or {@code null} when unknown (the server sends {@code "?"}) */
    public Integer count() {
        return count;
    }

    public boolean popular() {
        return popular;
    }

    /** @return country code */
    public int code() {
        return code;
    }

    /** @return price, USD */
    public double price() {
        return price;
    }

    public long id() {
        return id;
    }

    /** @return display name */
    public String service() {
        return service;
    }

    /** @return service id for {@link NumbersApi#get(String, int)} and {@link NumbersApi#price(String, int)} */
    public String slug() {
        return slug;
    }
}
