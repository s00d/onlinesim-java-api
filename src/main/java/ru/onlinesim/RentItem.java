package ru.onlinesim;

import java.util.List;
import java.util.Map;

/** Rent operation. */
public final class RentItem extends Model {
    private long tzid;
    private int status;
    private String number;
    private int country;
    private double sum;
    private int days;
    private int hours;
    private long time;
    private Long timeSeconds;
    private Integer guardIntervalRemainingSeconds;
    private int extension;
    private Integer daysAutoextend;
    private String startTime;
    private String stopTime;
    private String checkedTime;
    private Boolean checked;
    private Boolean freeze;
    private Double dayExtend;
    private Map<String, Double> extend;
    private String webhookUrl;
    private List<RentSms> messages;

    private RentItem() {}

    public long tzid() {
        return tzid;
    }

    public int status() {
        return status;
    }

    public String number() {
        return number;
    }

    public int country() {
        return country;
    }

    /** @return price paid, USD (parsed from the server's decimal string) */
    public double sum() {
        return sum;
    }

    public int days() {
        return days;
    }

    public int hours() {
        return hours;
    }

    /** @return minutes left */
    public long time() {
        return time;
    }

    /** @return seconds left */
    public Long timeSeconds() {
        return timeSeconds;
    }

    public Integer guardIntervalRemainingSeconds() {
        return guardIntervalRemainingSeconds;
    }

    /** @return 0 not extendable, 1 extendable, 2 auto-extension */
    public int extension() {
        return extension;
    }

    public Integer daysAutoextend() {
        return daysAutoextend;
    }

    public String startTime() {
        return startTime;
    }

    public String stopTime() {
        return stopTime;
    }

    public String checkedTime() {
        return checkedTime;
    }

    public Boolean checked() {
        return checked;
    }

    public Boolean freeze() {
        return freeze;
    }

    /** @return price of one day of extension */
    public Double dayExtend() {
        return dayExtend;
    }

    /** @return days → extension price, USD; empty when not extendable */
    public Map<String, Double> extend() {
        return Json.map(extend);
    }

    public String webhookUrl() {
        return webhookUrl;
    }

    /** @return SMS, newest first */
    public List<RentSms> messages() {
        return Json.list(messages);
    }
}
