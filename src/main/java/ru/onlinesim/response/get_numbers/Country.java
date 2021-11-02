package ru.onlinesim.response.get_numbers;

/**
 * Country resp
 */
public class Country {
	private final String name;
	private final String original;
	private final int code;
	private final int pos;
	private final String other;
	private final boolean _new;
	private final boolean enable;

	public Country(String name, String original, int code, int pos, String other, boolean aNew, boolean enable) {
		this.name = name;
		this.original = original;
		this.code = code;
		this.pos = pos;
		this.other = other;
		_new = aNew;
		this.enable = enable;
	}


	public String getName() {
		return name;
	}

	public String getOriginal() {
		return original;
	}

	public int getCode() {
		return code;
	}

	public int getPos() {
		return pos;
	}

	public String getOther() {
		return other;
	}

	public boolean is_new() {
		return _new;
	}

	public boolean isEnable() {
		return enable;
	}

	@Override
	public String toString() {
		return "Country{" +
				"name=" + name +
				", original=" + original +
				", code=" + code +
				", pos=" + pos +
				", other=" + other +
				", new=" + _new +
				", enable=" + enable +
				'}';
	}
}
