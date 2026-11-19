package ru.onlinesim;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

/** Options of {@link NumbersApi#waitCode(long, WaitOptions)}. Immutable: every setter returns a copy. */
public final class WaitOptions {
    /** Blocks the calling thread; injectable so tests never really wait. */
    @FunctionalInterface
    public interface Sleeper {
        /**
         * @param duration how long to sleep
         * @throws InterruptedException when the thread is interrupted
         */
        void sleep(Duration duration) throws InterruptedException;
    }

    private static final WaitOptions DEFAULTS = new WaitOptions(
            Duration.ofSeconds(5),
            Duration.ofMinutes(10),
            false,
            false,
            null,
            d -> Thread.sleep(d.toMillis()),
            Clock.systemUTC());

    private final Duration interval;
    private final Duration timeout;
    private final boolean keepOpen;
    private final boolean fullMessage;
    private final String since;
    private final Sleeper sleeper;
    private final Clock clock;

    private WaitOptions(
            Duration interval,
            Duration timeout,
            boolean keepOpen,
            boolean fullMessage,
            String since,
            Sleeper sleeper,
            Clock clock) {
        this.interval = interval;
        this.timeout = timeout;
        this.keepOpen = keepOpen;
        this.fullMessage = fullMessage;
        this.since = since;
        this.sleeper = sleeper;
        this.clock = clock;
    }

    /** @return interval 5 s, timeout 10 min, close after the code, code only, real sleep and clock */
    public static WaitOptions defaults() {
        return DEFAULTS;
    }

    /**
     * @param interval poll interval, default 5 s
     * @return a copy with the option set
     */
    public WaitOptions interval(Duration interval) {
        return new WaitOptions(positive(interval, "interval"), timeout, keepOpen, fullMessage, since, sleeper, clock);
    }

    /**
     * @param timeout give up after this long, default 10 min
     * @return a copy with the option set
     */
    public WaitOptions timeout(Duration timeout) {
        return new WaitOptions(interval, positive(timeout, "timeout"), keepOpen, fullMessage, since, sleeper, clock);
    }

    /**
     * @param keepOpen call {@code next(tzid)} instead of {@code close(tzid)} after the code, to wait for another SMS
     * @return a copy with the option set
     */
    public WaitOptions keepOpen(boolean keepOpen) {
        return new WaitOptions(interval, timeout, keepOpen, fullMessage, since, sleeper, clock);
    }

    /**
     * @param fullMessage return the full SMS text instead of the code
     * @return a copy with the option set
     */
    public WaitOptions fullMessage(boolean fullMessage) {
        return new WaitOptions(interval, timeout, keepOpen, fullMessage, since, sleeper, clock);
    }

    /**
     * @param since ignore this value (the previous code when waiting for the next one); {@code null} for none
     * @return a copy with the option set
     */
    public WaitOptions since(String since) {
        return new WaitOptions(interval, timeout, keepOpen, fullMessage, since, sleeper, clock);
    }

    /**
     * @param sleeper sleep implementation (tests)
     * @return a copy with the option set
     */
    public WaitOptions sleeper(Sleeper sleeper) {
        return new WaitOptions(interval, timeout, keepOpen, fullMessage, since, Objects.requireNonNull(sleeper), clock);
    }

    /**
     * @param clock clock for the deadline (tests)
     * @return a copy with the option set
     */
    public WaitOptions clock(Clock clock) {
        return new WaitOptions(interval, timeout, keepOpen, fullMessage, since, sleeper, Objects.requireNonNull(clock));
    }

    /** @return poll interval */
    public Duration interval() {
        return interval;
    }

    /** @return overall timeout */
    public Duration timeout() {
        return timeout;
    }

    /** @return whether {@code next} is called instead of {@code close} */
    public boolean keepOpen() {
        return keepOpen;
    }

    /** @return whether the full SMS text is returned */
    public boolean fullMessage() {
        return fullMessage;
    }

    /** @return the ignored value, or {@code null} */
    public String since() {
        return since;
    }

    /** @return the sleep implementation */
    public Sleeper sleeper() {
        return sleeper;
    }

    /** @return the clock */
    public Clock clock() {
        return clock;
    }

    private static Duration positive(Duration d, String name) {
        if (d == null || d.isNegative() || d.isZero()) throw new IllegalArgumentException(name + " must be positive");
        return d;
    }
}
