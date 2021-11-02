package ru.onlinesim.response.get_proxy;

import java.util.HashMap;

/**
 * TariffTrafficConfig resp
 */
public class TariffTrafficConfig {
	private final String name;
	private final HashMap<String, String> cities;
	private final HashMap<String, String> operators;

	public TariffTrafficConfig(String name, HashMap<String, String> cities, HashMap<String, String> operators) {
		this.name = name;
		this.cities = cities;
		this.operators = operators;
	}

	public String getName() {
		return name;
	}

	public HashMap<String, String> getCities() {
		return cities;
	}

	public HashMap<String, String> getOperators() {
		return operators;
	}

	@Override
	public String toString() {
		return "TrafficConfig{" +
				"name=" + name +
				", cities=" + cities +
				", operators=" + operators +
				'}';
	}
}
