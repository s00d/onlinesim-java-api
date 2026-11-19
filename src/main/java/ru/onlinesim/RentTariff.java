package ru.onlinesim;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/** Rent prices of one country. */
public final class RentTariff extends Model {
    private int code;
    private String name;
    private boolean enabled;

    @SerializedName("new")
    private boolean isNew;

    private int position;
    private int extend;
    private Boolean confirm;
    private Map<String, Double> days;
    private Map<String, Integer> count;
    private Map<String, Integer> extCounts;

    private RentTariff() {}

    public int code() {
        return code;
    }

    public String name() {
        return name;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean isNew() {
        return isNew;
    }

    public int position() {
        return position;
    }

    public int extend() {
        return extend;
    }

    public Boolean confirm() {
        return confirm;
    }

    /** @return rent days → price, USD */
    public Map<String, Double> days() {
        return Json.map(days);
    }

    /** @return rent days → numbers available */
    public Map<String, Integer> count() {
        return Json.map(count);
    }

    public Map<String, Integer> extCounts() {
        return Json.map(extCounts);
    }
}
