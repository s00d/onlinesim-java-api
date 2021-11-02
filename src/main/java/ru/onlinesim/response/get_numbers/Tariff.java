package ru.onlinesim.response.get_numbers;

import java.util.HashMap;



public class Tariff {
	private final HashMap<String, Country> countries;
	private final HashMap<String, Service> favorites;
	private final HashMap<String, Service> services;
	private final int page;
	private final boolean end;

	public Tariff(HashMap<String, Country> countries, HashMap<String, Service> favorites, HashMap<String, Service> services, int page, boolean end) {
		this.countries = countries;
		this.favorites = favorites;
		this.services = services;
		this.page = page;
		this.end = end;
	}

	public HashMap<String, Country> getCountries() {
		return countries;
	}

	public HashMap<String, Service> getFavorites() {
		return favorites;
	}

	public HashMap<String, Service> getServices() {
		return services;
	}

	public int getPage() {
		return page;
	}

	public boolean isEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "Service{" +
				"countries=" + countries +
				", favorites=" + favorites +
				", services=" + services +
				", page=" + page +
				", end=" + end +
				'}';
	}
}
