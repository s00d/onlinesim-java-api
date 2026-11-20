package ru.onlinesim;

import java.util.HashMap;
import java.util.Map;

/**
 * The API answered with an error code in {@code response} (also a wrong key, HTTP 456, and the rate limit, HTTP 429
 * {@code TRY_AGAIN_LATER}).
 */
public class ApiException extends OnlineSimException {
    private static final long serialVersionUID = 1L;

    /** Fallback messages for error codes the server sends without a {@code message}. */
    public static final Map<String, String> ERROR_MESSAGES;

    static {
        Map<String, String> m = new HashMap<>();
        m.put("ACCOUNT_BLOCKED", "Account is blocked");
        m.put("ACCOUNT_CLEANED", "Account was deleted");
        m.put("USER_NOT_ACTIVATED", "Account email is not confirmed");
        m.put("ERROR_WRONG_KEY", "Wrong or missing API key");
        m.put("ERROR_NO_SERVICE", "Service is not specified");
        m.put("API_ACCESS_DISABLED", "API access is disabled in the profile");
        m.put("API_ACCESS_IP", "API access from this IP is not allowed in the profile");
        m.put("ERROR_NO_TZID", "tzid is not specified");
        m.put("ERROR_WRONG_TZID", "Wrong tzid");
        m.put("ERROR_NO_OPERATIONS", "No operations");
        m.put("ERROR_UNDEFINED", "Unexpected server error, try again");
        m.put("NO_NUMBER", "No numbers available for this service right now");
        m.put("NO_COUNTRY", "Country not found");
        m.put("UNDEFINED_COUNTRY", "Unknown country");
        m.put("WARNING_LOW_BALANCE", "Not enough balance");
        m.put("EXCEEDED_CONCURRENT_OPERATIONS", "Too many concurrent operations for this account");
        m.put("INTERVAL_CONCURRENT_REQUESTS_ERROR", "Too many concurrent requests, try again in a few seconds");
        m.put("TRY_AGAIN_LATER", "Rate limit exceeded or temporary error, try again later");
        m.put("FORWARD_NOT_SUPPORTED", "Number forwarding is no longer available");
        m.put("ERROR_PARAMS", "Wrong parameters");
        m.put("NO_COMPLETE_TZID", "The operation cannot be closed yet");
        m.put("NO_CLOSE_RENT_US", "US rent numbers cannot be closed early");
        m.put("UNDEFINED_DAYS", "Unknown rent period");
        m.put("VALIDATION_ERROR", "Validation failed");
        m.put("0", "Ban not applied: the operation already has an SMS");
        m.put("TZ_OVER_OK", "Operation finished without a new code");
        m.put("ERROR_NO_ITEMS", "Operation failed");
        ERROR_MESSAGES = java.util.Collections.unmodifiableMap(m);
    }

    private final String code;

    /**
     * @param code    raw error code, e.g. {@code ERROR_WRONG_KEY}
     * @param message server message; {@code null} or empty falls back to {@link #ERROR_MESSAGES}, then to the code
     */
    public ApiException(String code, String message) {
        super(message != null && !message.isEmpty() ? message : ERROR_MESSAGES.getOrDefault(code, code));
        this.code = code;
    }

    /** @param code raw error code; the message comes from {@link #ERROR_MESSAGES} */
    public ApiException(String code) {
        this(code, null);
    }

    /** @return raw error code, e.g. {@code ERROR_WRONG_KEY} */
    public String getCode() {
        return code;
    }
}
