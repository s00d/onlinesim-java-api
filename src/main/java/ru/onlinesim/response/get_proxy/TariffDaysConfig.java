package ru.onlinesim.response.get_proxy;

import java.util.HashMap;

/**
 * TariffDaysConfig resp
 */
public class TariffDaysConfig {
	private final String type;
	private final Boolean enabled;
	private final HashMap<Integer, Integer> days;

	public TariffDaysConfig(String type, Boolean enabled, HashMap<Integer, Integer> days) {
		this.type = type;
		this.enabled = enabled;
		this.days = days;
	}

	public String getType() {
		return type;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public HashMap<Integer, Integer> getDays() {
		return days;
	}

	@Override
	public String toString() {
		return "DaysConfig{" +
				"type=" + type +
				", enabled=" + enabled +
				", days=" + days +
				'}';
	}
}
