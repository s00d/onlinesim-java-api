package ru.onlinesim;

/** {@link NumbersApi#waitCode(long, WaitOptions)} got no code within its timeout. */
public class WaitTimeoutException extends OnlineSimException {
    private static final long serialVersionUID = 1L;

    /** @param message the detail message */
    public WaitTimeoutException(String message) {
        super(message);
    }
}
