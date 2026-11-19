package ru.onlinesim;

/** Base class of every exception thrown by the SDK. Unchecked. */
public class OnlineSimException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** @param message the detail message */
    public OnlineSimException(String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the cause
     */
    public OnlineSimException(String message, Throwable cause) {
        super(message, cause);
    }
}
