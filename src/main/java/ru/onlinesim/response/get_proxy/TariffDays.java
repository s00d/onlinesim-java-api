package ru.onlinesim.response.get_proxy;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * TariffDays resp
 */
public class TariffDays {
	private final HashMap<String, TariffDaysConfig> config;
	private final ArrayList<String> operators;
	private final ArrayList<String> connect;

	public TariffDays(HashMap<String, TariffDaysConfig> config, ArrayList<String> operators, ArrayList<String> connect) {
		this.config = config;
		this.operators = operators;
		this.connect = connect;
	}

	public HashMap<String, TariffDaysConfig> getConfig() {
		return config;
	}

	public ArrayList<String> getOperators() {
		return operators;
	}

	public ArrayList<String> getConnect() {
		return connect;
	}

	@Override
	public String toString() {
		return "Days{" +
				"config=" + config +
				", operators=" + operators +
				", connect=" + connect +
				'}';
	}
}
