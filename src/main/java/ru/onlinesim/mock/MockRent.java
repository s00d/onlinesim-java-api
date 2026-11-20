package ru.onlinesim.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A rent as the mock stores it; closed rents are removed. {@link MockOnlineSim#rents()} returns snapshots: changing
 * the mock afterwards does not change them.
 */
public final class MockRent {
    final long tzid;
    final int country;
    final String number;
    /** Total rent days: the initial period plus extensions. */
    int days;
    /** The initial period ({@code days} of getRentNum). */
    final int initialDays;
    /** 0: reserved, waiting for the first SMS; 1: confirmed by an SMS that is not {@code notify}, price charged. */
    int status;

    long sumMills;
    final boolean extension;
    final List<MockSms> messages = new ArrayList<>();

    MockRent(long tzid, int country, String number, int days, long sumMills, boolean extension) {
        this.tzid = tzid;
        this.country = country;
        this.number = number;
        this.days = days;
        this.initialDays = days;
        this.sumMills = sumMills;
        this.extension = extension;
    }

    MockRent snapshot() {
        MockRent copy = new MockRent(tzid, country, number, initialDays, sumMills, extension);
        copy.days = days;
        copy.status = status;
        copy.messages.addAll(messages);
        return copy;
    }

    /** @return rent id */
    public long tzid() {
        return tzid;
    }

    /** @return country code */
    public int country() {
        return country;
    }

    /** @return number without the country code, as the backend sends it for rents */
    public String number() {
        return number;
    }

    /** @return rent days, extensions included */
    public int days() {
        return days;
    }

    /** @return 0 while reserved (waiting for the first SMS that is not {@code notify}), 1 once confirmed and charged */
    public int status() {
        return status;
    }

    /** @return total cost incl. extensions, USD; frozen while {@code status} is 0 */
    public double sum() {
        return MockState.usd(sumMills);
    }

    /** @return whether the rent can be extended */
    public boolean extension() {
        return extension;
    }

    /** @return SMS received, oldest first (unmodifiable) */
    public List<MockSms> messages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public String toString() {
        return "MockRent{tzid=" + tzid + ", country=" + country + ", number=" + number + ", days=" + days + ", status="
                + status + ", messages=" + messages + "}";
    }
}
