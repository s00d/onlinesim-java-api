package ru.onlinesim.mock;

/** One SMS received by a number operation or a rent. Immutable. */
public final class MockSms {
    private final String service;
    private final String text;
    private final String code;
    private final long id;
    private final String createdAt;

    MockSms(String service, String text, String code, long id, String createdAt) {
        this.service = service;
        this.text = text;
        this.code = code;
        this.id = id;
        this.createdAt = createdAt;
    }

    /** @return sender service */
    public String service() {
        return service;
    }

    /** @return full text */
    public String text() {
        return text;
    }

    /** @return code extracted from the text (first run of 4–8 digits), {@code null} when there is none */
    public String code() {
        return code;
    }

    /** @return message id of a rent SMS; 0 for a number operation SMS */
    public long id() {
        return id;
    }

    /** @return {@code YYYY-MM-DD HH:MM:SS} of a rent SMS; {@code null} for a number operation SMS */
    public String createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "MockSms{service=" + service + ", text=" + text + ", code=" + code + "}";
    }
}
