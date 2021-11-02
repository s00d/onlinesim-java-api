package ru.onlinesim.response.get_proxy;

import java.util.HashMap;


/**
 * TariffTraffic resp
 */
public class TariffTraffic {
	private final HashMap<String, TariffTrafficConfig> config;
	private final HashMap<String, Integer> price;

	public TariffTraffic(HashMap<String, TariffTrafficConfig> config, HashMap<String, Integer> price) {
		this.config = config;
		this.price = price;
	}

	public HashMap<String, TariffTrafficConfig> getConfig() {
		return config;
	}

	public HashMap<String, Integer> getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return "Traffic{" +
				"config=" + config +
				", price=" + price +
				'}';
	}
}
