package ru.onlinesim;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/** Services, prices and stock of one country ({@code getNumbersStats}). */
public final class CountryStat extends Model {
    private String name;
    private String localeName;
    private int position;
    private int code;
    private Double other;

    @SerializedName("new")
    private boolean isNew;

    private boolean enabled;
    private Map<String, ServiceStat> services;

    private CountryStat() {}

    public String name() {
        return name;
    }

    public String localeName() {
        return localeName;
    }

    public int position() {
        return position;
    }

    public int code() {
        return code;
    }

    /** @return price of other services, or {@code null} when not set */
    public Double other() {
        return other;
    }

    public boolean isNew() {
        return isNew;
    }

    public boolean enabled() {
        return enabled;
    }

    /** @return map {@code service_<slug>} → service; empty when none */
    public Map<String, ServiceStat> services() {
        return Json.map(services);
    }
}
