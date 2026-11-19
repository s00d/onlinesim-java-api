package ru.onlinesim;

import java.time.Duration;
import java.time.Instant;

/** The {@code waitCode} poll loop, on any {@link Target} (the numbers section, or a fake in tests). */
final class WaitCode {
    /** What the loop needs from the numbers section. */
    interface Target {
        Operation stateOne(long tzid, StateOptions options);

        void close(long tzid);

        void next(long tzid);
    }

    private WaitCode() {}

    static String run(Target numbers, long tzid, WaitOptions o) {
        StateOptions state = StateOptions.defaults().messageToCode(!o.fullMessage());
        Instant deadline = o.clock().instant().plus(o.timeout());
        for (; ; ) {
            Operation op = null;
            try {
                op = numbers.stateOne(tzid, state);
            } catch (TransportException e) {
                // transient: retried until the deadline
            }

            if (op != null) {
                String code = op.msg();
                if (code != null && !code.isEmpty() && !code.equals(o.since())) {
                    if (o.keepOpen()) numbers.next(tzid);
                    else numbers.close(tzid);
                    return code;
                }
                if ("TZ_OVER_OK".equals(op.response()) || "ERROR_NO_ITEMS".equals(op.response())) {
                    throw new ApiException(op.response());
                }
            }

            Duration left = Duration.between(o.clock().instant(), deadline);
            if (left.isNegative() || left.isZero()) {
                throw new WaitTimeoutException("No code for operation " + tzid + " within "
                        + o.timeout().toMillis() + " ms");
            }
            try {
                o.sleeper().sleep(left.compareTo(o.interval()) < 0 ? left : o.interval());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OnlineSimException("waitCode interrupted", e);
            }
        }
    }
}
