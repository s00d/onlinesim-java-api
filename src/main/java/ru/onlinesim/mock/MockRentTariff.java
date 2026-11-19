package ru.onlinesim.mock;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Rent tariff of one country in {@link MockOptions#rentTariffs(Map)}. Immutable. */
public final class MockRentTariff {
    private final Map<Integer, Double> days;
    private final boolean extend;

    /**
     * @param days   rent days → price, USD (also the price of extending by that many days)
     * @param extend whether rents of this country can be extended
     */
    public MockRentTariff(Map<Integer, Double> days, boolean extend) {
        this.days = Collections.unmodifiableMap(new TreeMap<>(Objects.requireNonNull(days, "days")));
        this.extend = extend;
    }

    /** @return rent days (ascending) → price, USD */
    public Map<Integer, Double> days() {
        return days;
    }

    /** @return whether rents of this country can be extended */
    public boolean extend() {
        return extend;
    }
}
