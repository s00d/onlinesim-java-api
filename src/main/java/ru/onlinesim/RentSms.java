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

    /**
     * @return sender; {@code notify}: a notice about a prohibited sender (does not confirm the rent), {@code init}: the
     *     record of a rent confirmed at once
     */
    public String service() {
        return service;
    }

    public String text() {
        return text;
    }

    /** @return code found in the text, {@code ""} when none; {@code "0"} for a {@code notify} SMS (the server sends 0) */
    public String code() {
        return code;
    }

    public String createdAt() {
        return createdAt;
    }
}
