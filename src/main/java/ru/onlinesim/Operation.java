package ru.onlinesim;

import com.google.gson.JsonElement;
import java.util.List;

/** Number operation (one {@code getState} item). */
public final class Operation extends Model {
    private long tzid;
    private String response;
    private String number;
    private String service;
    private Integer country;
    private Double sum;
    private Integer time;
    private String form;
    private String webhookUrl;
    private JsonElement msg;
    private Integer extend;
    private Integer guardIntervalRemainingSeconds;

    private Operation() {}

    public long tzid() {
        return tzid;
    }

    /**
     * @return state: {@code TZ_INPOOL}, {@code TZ_NUM_WAIT}, {@code TZ_NUM_ANSWER}, {@code TZ_OVER_OK} or
     *     {@code ERROR_NO_ITEMS}
     */
    public String response() {
        return response;
    }

    /** @return full number with {@code +}, or {@code null} while it is being assigned */
    public String number() {
        return number;
    }

    public String service() {
        return service;
    }

    public Integer country() {
        return country;
    }

    /** @return price, USD */
    public Double sum() {
        return sum;
    }

    /** @return seconds left */
    public Integer time() {
        return time;
    }

    public String form() {
        return form;
    }

    public String webhookUrl() {
        return webhookUrl;
    }

    /**
     * @return the code (or the full text with {@code messageToCode = false}); {@code null} before the first SMS or
     *     with {@code msgList = true}
     */
    public String msg() {
        return Json.string(msg);
    }

    /** @return all SMS when requested with {@code msgList = true}; empty otherwise */
    public List<OperationSms> msgList() {
        if (msg == null || !msg.isJsonArray()) return List.of();
        return Json.list(Json.convert(msg, Json.listOf(OperationSms.class)));
    }

    public Integer extend() {
        return extend;
    }

    /** @return seconds until the operation may be closed */
    public Integer guardIntervalRemainingSeconds() {
        return guardIntervalRemainingSeconds;
    }
}
