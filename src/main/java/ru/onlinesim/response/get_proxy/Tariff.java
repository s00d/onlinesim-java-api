package ru.onlinesim.response.get_proxy;

/**
 * Tariff resp
 */
public class Tariff {
	private final TariffDays days;
	private final TariffTraffic traffic;

	public Tariff(TariffDays days, TariffTraffic traffic) {
		this.days = days;
		this.traffic = traffic;
	}

	public TariffDays getDays() {
		return days;
	}

	public TariffTraffic getTraffic() {
		return traffic;
	}

	@Override
	public String toString() {
		return "Traffic{" +
				"days=" + days +
				", traffic=" + traffic +
				'}';
	}
}
