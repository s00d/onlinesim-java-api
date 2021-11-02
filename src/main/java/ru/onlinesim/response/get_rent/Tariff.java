package ru.onlinesim.response.get_rent;

import java.util.HashMap;

/**
 * Tariff resp
 */
public class Tariff {
	private final int code;
	private final boolean enabled;
	private final String name;
	private final boolean _new;
	private final int position;
	private final HashMap<String, Integer> count;
	private final HashMap<String, Integer> days;
	private final int extend;

	public Tariff(int code, boolean enabled, String name, boolean aNew, int position, HashMap<String, Integer> count, HashMap<String, Integer> days, int extend) {
		this.code = code;
		this.enabled = enabled;
		this.name = name;
		_new = aNew;
		this.position = position;
		this.count = count;
		this.days = days;
		this.extend = extend;
	}

	public int getCode() {
		return code;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getName() {
		return name;
	}

	public boolean is_new() {
		return _new;
	}

	public int getPosition() {
		return position;
	}

	public HashMap<String, Integer> getCount() {
		return count;
	}

	public HashMap<String, Integer> getDays() {
		return days;
	}

	public int getExtend() {
		return extend;
	}


	@Override
	public String toString() {
		return "Tariff{" +
				"code=" + code +
				", enabled=" + enabled +
				", name=" + name +
				", new=" + _new +
				", position=" + position +
				", count=" + count +
				", days=" + days +
				", extend=" + extend +
				'}';
	}
}
