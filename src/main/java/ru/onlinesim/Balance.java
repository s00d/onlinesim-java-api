package ru.onlinesim;

/** Balance, USD. {@code balance} includes the frozen {@code zbalance}: available = balance − max(zbalance, 0). */
public final class Balance extends Model {
    private double balance;
    private double zbalance;
    private Double income;

    private Balance() {}

    /** @return total balance (parsed from the server's decimal string) */
    public double balance() {
        return balance;
    }

    /** @return sum frozen by numbers and unconfirmed rents in progress; may be negative */
    public double zbalance() {
        return zbalance;
    }

    /** @return partner income, or {@code null} when not sent */
    public Double income() {
        return income;
    }
}
