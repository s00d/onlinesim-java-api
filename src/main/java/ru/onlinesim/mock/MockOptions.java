package ru.onlinesim.mock;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Options of {@link MockOnlineSim}. Immutable: every setter returns a copy.
 *
 * <pre>{@code
 * new MockOnlineSim(MockOptions.defaults().balance(10).guardInterval(120));
 * }</pre>
 */
public final class MockOptions {
    private static final MockOptions DEFAULTS = new MockOptions(
            "mock-key",
            100,
            copyPrices(Map.of(
                    7, ordered("vkcom", 0.5, "telegram", 1.2, "google", 0.3),
                    44, ordered("google", 0.2, "vkcom", 0.36))),
            copyTariffs(Map.of(7, new MockRentTariff(Map.of(1, 1.0, 3, 2.5, 7, 5.0), true))),
            0);

    private final String apiKey;
    private final double balance;
    private final Map<Integer, Map<String, Double>> prices;
    private final Map<Integer, MockRentTariff> rentTariffs;
    private final int guardInterval;

    private MockOptions(
            String apiKey,
            double balance,
            Map<Integer, Map<String, Double>> prices,
            Map<Integer, MockRentTariff> rentTariffs,
            int guardInterval) {
        this.apiKey = apiKey;
        this.balance = balance;
        this.prices = prices;
        this.rentTariffs = rentTariffs;
        this.guardInterval = guardInterval;
    }

    /**
     * Key {@code mock-key}, balance 100 USD, prices {@code {7: {vkcom: 0.5, telegram: 1.2, google: 0.3}, 44: {google:
     * 0.2, vkcom: 0.36}}}, rent tariffs {@code {7: {days: {1: 1.0, 3: 2.5, 7: 5.0}, extend: true}}}, guard interval 0.
     *
     * @return the default options
     */
    public static MockOptions defaults() {
        return DEFAULTS;
    }

    /**
     * @param apiKey key the mock accepts as {@code Authorization: Bearer <apiKey>}, default {@code mock-key}
     * @return a copy with the option set
     */
    public MockOptions apiKey(String apiKey) {
        return new MockOptions(Objects.requireNonNull(apiKey, "apiKey"), balance, prices, rentTariffs, guardInterval);
    }

    /**
     * @param balance starting balance, USD, default 100
     * @return a copy with the option set
     */
    public MockOptions balance(double balance) {
        return new MockOptions(apiKey, balance, prices, rentTariffs, guardInterval);
    }

    /**
     * @param prices number prices, country code → service slug → USD; replaces the defaults (also feeds {@code
     *     numbers().tariffs()})
     * @return a copy with the option set
     */
    public MockOptions prices(Map<Integer, Map<String, Double>> prices) {
        return new MockOptions(apiKey, balance, copyPrices(prices), rentTariffs, guardInterval);
    }

    /**
     * @param rentTariffs rent tariffs, country code → tariff; replaces the defaults
     * @return a copy with the option set
     */
    public MockOptions rentTariffs(Map<Integer, MockRentTariff> rentTariffs) {
        return new MockOptions(apiKey, balance, prices, copyTariffs(rentTariffs), guardInterval);
    }

    /**
     * @param seconds guard interval of new operations; {@code > 0}: an operation without SMS cannot be closed
     *     ({@code NO_COMPLETE_TZID}). Default 0.
     * @return a copy with the option set
     */
    public MockOptions guardInterval(int seconds) {
        if (seconds < 0) throw new IllegalArgumentException("guardInterval must not be negative");
        return new MockOptions(apiKey, balance, prices, rentTariffs, seconds);
    }

    /** @return the accepted key */
    public String apiKey() {
        return apiKey;
    }

    /** @return the starting balance, USD */
    public double balance() {
        return balance;
    }

    /** @return country code (ascending) → service slug → USD (unmodifiable) */
    public Map<Integer, Map<String, Double>> prices() {
        return prices;
    }

    /** @return country code (ascending) → tariff (unmodifiable) */
    public Map<Integer, MockRentTariff> rentTariffs() {
        return rentTariffs;
    }

    /** @return the guard interval, seconds */
    public int guardInterval() {
        return guardInterval;
    }

    private static Map<String, Double> ordered(Object... kv) {
        Map<String, Double> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], (Double) kv[i + 1]);
        return m;
    }

    /** Countries ascending (as the backend lists them); services keep the caller's order. */
    private static Map<Integer, Map<String, Double>> copyPrices(Map<Integer, Map<String, Double>> prices) {
        Map<Integer, Map<String, Double>> copy = new TreeMap<>();
        Objects.requireNonNull(prices, "prices")
                .forEach((country, services) ->
                        copy.put(country, Collections.unmodifiableMap(new LinkedHashMap<>(services))));
        return Collections.unmodifiableMap(copy);
    }

    private static Map<Integer, MockRentTariff> copyTariffs(Map<Integer, MockRentTariff> tariffs) {
        return Collections.unmodifiableMap(new TreeMap<>(Objects.requireNonNull(tariffs, "rentTariffs")));
    }
}
