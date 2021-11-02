package ru.onlinesim.response.get_free;

/**
 * Country resp
 */
public class Country {
	private final int country;
	private final String country_text;

	public Country(int country, String country_text) {
		this.country = country;
		this.country_text = country_text;
	}

	public int getCountry() {
		return country;
	}

	public String getCountry_text() {
		return country_text;
	}

	@Override
	public String toString() {
		return "Country{" +
				"country=" + country +
				", country_text=" + country_text +
				'}';
	}
}
