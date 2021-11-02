package ru.onlinesim.response.get_free;

import java.math.BigInteger;

/**
 * Number resp
 */
public class Number {
	private final BigInteger number;
	private final int country;
	private final String updated_at;
	private final String full_number;
	private final String country_text;

	public Number(BigInteger number, int country, String country_text, String updated_at, String full_number) {
		this.number = number;
		this.country = country;
		this.country_text = country_text;
		this.updated_at = updated_at;
		this.full_number = full_number;
	}

	public BigInteger getNumber() {
		return number;
	}

	public int getCountry() {
		return country;
	}

	/**
	 * @return String
	 */
	public String getUpdated_at() {
		return updated_at;
	}

	public String getFull_number() {
		return full_number;
	}

	public String getCountry_text() {
		return country_text;
	}

	@Override
	public String toString() {
		return "Number{" +
				"number=" + number +
				", country=" + country +
				", updated_at=" + updated_at +
				", full_number=" + full_number +
				", country_text=" + country_text +
				'}';
	}
}
