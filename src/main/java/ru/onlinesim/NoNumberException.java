package ru.onlinesim;

/** No number is available right now ({@code NO_NUMBER}). Retry later or pick another country. */
public class NoNumberException extends ApiException {
    private static final long serialVersionUID = 1L;

    /**
     * @param code    {@code NO_NUMBER} or {@code NO_NUMBER_FOR_FORWARD}
     * @param message server message, or {@code null} for the fallback
     */
    public NoNumberException(String code, String message) {
        super(code, message);
    }
}
