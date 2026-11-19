package ru.onlinesim;

/** Network failure, request timeout, or a response that is not JSON (or an HTTP error without an API code). */
public class TransportException extends OnlineSimException {
    private static final long serialVersionUID = 1L;

    private final Integer status;
    private final String body;

    /**
     * @param message the detail message
     * @param status  HTTP status, or {@code null} when no response arrived
     * @param body    start of the response body, empty when none
     * @param cause   the cause, or {@code null}
     */
    public TransportException(String message, Integer status, String body, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.body = body == null ? "" : body;
    }

    /** @param message the detail message */
    public TransportException(String message) {
        this(message, null, "", null);
    }

    /** @return HTTP status, or {@code null} when no response arrived (network error, timeout) */
    public Integer getStatus() {
        return status;
    }

    /** @return start of the response body (up to 500 chars), empty when none */
    public String getBody() {
        return body;
    }
}
