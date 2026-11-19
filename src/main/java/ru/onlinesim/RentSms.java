package ru.onlinesim;

/** One SMS of a rent. */
public final class RentSms extends Model {
    private long id;
    private String service;
    private String text;
    private String code;
    private String createdAt;

    private RentSms() {}

    public long id() {
        return id;
    }

    public String service() {
        return service;
    }

    public String text() {
        return text;
    }

    public String code() {
        return code;
    }

    public String createdAt() {
        return createdAt;
    }
}
