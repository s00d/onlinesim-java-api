package ru.onlinesim;

import com.google.gson.JsonObject;

/** Inbound webhook body as parsed by {@link OnlineSim#parseWebhook(String)}: missing or malformed fields are {@code null}. */
public final class WebhookPayload extends Model {
    private String webhookType;
    private Long operationId;
    private Long userId;
    private Integer countryCode;
    private String number;
    private String sender;
    private String message;
    private String code;
    private String timeStart;
    private Long timeLeft;

    private WebhookPayload() {}

    static WebhookPayload of(JsonObject data) {
        WebhookPayload p = Json.convert(data, WebhookPayload.class);
        if (!"receiving_sms".equals(p.webhookType) && !"rent_sms".equals(p.webhookType)) p.webhookType = null;
        return p;
    }

    /** @return {@code receiving_sms}, {@code rent_sms}, or {@code null} for anything else */
    public String webhookType() {
        return webhookType;
    }

    /** @return {@code tzid} of the operation or rent */
    public Long operationId() {
        return operationId;
    }

    public Long userId() {
        return userId;
    }

    public Integer countryCode() {
        return countryCode;
    }

    /** @return the number, e.g. {@code +79001234567} */
    public String number() {
        return number;
    }

    /** @return sender of the SMS, e.g. {@code VK} */
    public String sender() {
        return sender;
    }

    /** @return full SMS text */
    public String message() {
        return message;
    }

    /** @return code extracted from the SMS */
    public String code() {
        return code;
    }

    public String timeStart() {
        return timeStart;
    }

    public Long timeLeft() {
        return timeLeft;
    }
}
