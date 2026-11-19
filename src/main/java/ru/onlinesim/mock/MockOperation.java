package ru.onlinesim.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A number operation as the mock stores it. {@link MockOnlineSim#operations()} returns snapshots: changing the mock
 * afterwards does not change them.
 */
public final class MockOperation {
    final long tzid;
    final String service;
    final int country;
    final String number;
    final long sumMills;
    String status = "TZ_NUM_WAIT";
    final List<MockSms> messages = new ArrayList<>();
    int cursor;

    MockOperation(long tzid, String service, int country, String number, long sumMills) {
        this.tzid = tzid;
        this.service = service;
        this.country = country;
        this.number = number;
        this.sumMills = sumMills;
    }

    MockOperation snapshot() {
        MockOperation copy = new MockOperation(tzid, service, country, number, sumMills);
        copy.status = status;
        copy.messages.addAll(messages);
        copy.cursor = cursor;
        return copy;
    }

    /** @return operation id */
    public long tzid() {
        return tzid;
    }

    /** @return service slug */
    public String service() {
        return service;
    }

    /** @return country code */
    public int country() {
        return country;
    }

    /** @return full number with {@code +} */
    public String number() {
        return number;
    }

    /** @return price, USD: frozen while open, spent on close after an SMS, refunded on close without one */
    public double sum() {
        return MockState.usd(sumMills);
    }

    /** @return {@code TZ_NUM_WAIT}, {@code TZ_NUM_ANSWER} or {@code TZ_OVER_OK} */
    public String status() {
        return status;
    }

    /** @return SMS received, oldest first (unmodifiable) */
    public List<MockSms> messages() {
        return Collections.unmodifiableList(messages);
    }

    /** @return index of the SMS shown in {@code msg}; {@code next} (setOperationRevise) moves it past the last one */
    public int cursor() {
        return cursor;
    }

    @Override
    public String toString() {
        return "MockOperation{tzid=" + tzid + ", service=" + service + ", country=" + country + ", number=" + number
                + ", status=" + status + ", messages=" + messages + "}";
    }
}
