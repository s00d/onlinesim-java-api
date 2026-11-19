package ru.onlinesim;

/** One webhook delivery attempt. */
public final class WebhookLog extends Model {
    private long id;
    private long userId;
    private String webhookUrl;
    private String type;
    private WebhookPayload params;
    private String status;
    private String error;
    private String createdAt;
    private String updatedAt;

    private WebhookLog() {}

    public long id() {
        return id;
    }

    public long userId() {
        return userId;
    }

    public String webhookUrl() {
        return webhookUrl;
    }

    /** @return {@code receiving_sms} or {@code rent_sms} */
    public String type() {
        return type;
    }

    /** @return the payload that was sent */
    public WebhookPayload params() {
        return params;
    }

    /** @return {@code success}, {@code fail} or {@code pending} */
    public String status() {
        return status;
    }

    public String error() {
        return error;
    }

    public String createdAt() {
        return createdAt;
    }

    public String updatedAt() {
        return updatedAt;
    }
}
