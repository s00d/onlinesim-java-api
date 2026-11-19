package ru.onlinesim;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OnlineSim API client. Immutable and thread-safe.
 *
 * <pre>{@code
 * OnlineSim client = new OnlineSim(System.getenv("ONLINESIM_KEY"));
 * Balance balance = client.user().balance();
 * }</pre>
 */
public final class OnlineSim {
    /** Default API base. */
    public static final String DEFAULT_BASE_URL = "https://onlinesim.host/api/";
    /** Default per-request timeout. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private static final int BODY_EXCERPT = 500;

    private final String apiKey;
    private final String baseUrl;
    private final String lang;
    private final String devId;
    private final Duration timeout;
    private final Transport transport;

    private final NumbersApi numbers;
    private final RentApi rent;
    private final UserApi user;
    private final FreeApi free;

    /**
     * Client with default options.
     *
     * @param apiKey API key; {@code null} for the endpoints that need none (tariffs, free numbers)
     */
    public OnlineSim(String apiKey) {
        this(builder().apiKey(apiKey));
    }

    private OnlineSim(Builder b) {
        this.apiKey = b.apiKey == null || b.apiKey.isEmpty() ? null : b.apiKey;
        this.baseUrl = b.baseUrl.endsWith("/") ? b.baseUrl : b.baseUrl + "/";
        this.lang = b.lang;
        this.devId = b.devId;
        this.timeout = b.timeout;
        this.transport = b.transport != null ? b.transport : new HttpTransport();

        Requester request = this::request;
        this.numbers = new NumbersApi(request);
        this.rent = new RentApi(request);
        this.user = new UserApi(request);
        this.free = new FreeApi(request);
    }

    /** @return a builder for a client with custom options */
    public static Builder builder() {
        return new Builder();
    }

    /** @return numbers for receiving SMS */
    public NumbersApi numbers() {
        return numbers;
    }

    /** @return rented numbers */
    public RentApi rent() {
        return rent;
    }

    /** @return account: balance, profile, payments, webhook URL */
    public UserApi user() {
        return user;
    }

    /** @return public free numbers (no key needed) */
    public FreeApi free() {
        return free;
    }

    /**
     * Parses an inbound webhook body ({@code receiving_sms} or {@code rent_sms}). Lenient: numeric fields may arrive
     * as strings, missing or malformed fields become {@code null}, unknown fields are dropped.
     *
     * @param json the request body
     * @return the parsed payload
     * @throws OnlineSimException only when the input is not a JSON object
     */
    public static WebhookPayload parseWebhook(String json) {
        JsonElement data;
        try {
            data = Json.parse(json);
        } catch (JsonParseException e) {
            throw new OnlineSimException("Webhook payload is not valid JSON", e);
        }
        if (data == null || !data.isJsonObject()) throw new OnlineSimException("Webhook payload is not a JSON object");
        return WebhookPayload.of(data.getAsJsonObject());
    }

    /** The one request function every section calls. {@code endpoint} is relative to the base, suffix included. */
    private JsonElement request(String endpoint, Map<String, ?> query, JsonElement body) {
        Map<String, Object> params = new LinkedHashMap<>(query);
        params.put("lang", lang);
        params.put("dev_id", devId);
        String qs = encodeQuery(params);
        URI uri = URI.create(baseUrl + endpoint + (qs.isEmpty() ? "" : "?" + qs));

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        if (apiKey != null) headers.put("Authorization", "Bearer " + apiKey);
        if (body != null) headers.put("Content-Type", "application/json");

        Transport.Response response;
        try {
            // JsonElement.toString() keeps explicit nulls ({"webhook_url":null}).
            response = transport.send(new Transport.Request(
                    body == null ? "GET" : "POST", uri, headers, body == null ? null : body.toString(), timeout));
        } catch (HttpTimeoutException | SocketTimeoutException e) {
            throw new TransportException("Request timed out after " + timeout.toMillis() + " ms", null, "", e);
        } catch (IOException e) {
            throw new TransportException("Network error: " + e.getMessage(), null, "", e);
        }

        int status = response.status();
        String text = response.body();
        String excerpt = text.length() > BODY_EXCERPT ? text.substring(0, BODY_EXCERPT) : text;
        JsonElement data;
        try {
            data = Json.parse(text);
        } catch (JsonParseException e) {
            data = null;
        }
        if (data == null)
            throw new TransportException("HTTP " + status + ": response is not JSON", status, excerpt, null);
        // The body is checked before the status: a wrong key is HTTP 456 with {"response":"ERROR_WRONG_KEY"}.
        if (data.isJsonObject() && data.getAsJsonObject().has("response")) return unwrap(data.getAsJsonObject());
        if (status < 200 || status >= 300) throw new TransportException("HTTP " + status, status, excerpt, null);
        return data;
    }

    /** Success: the payload without {@code response}. Error code: throw. */
    private static JsonObject unwrap(JsonObject data) {
        JsonElement response = data.get("response");
        String code = Json.string(response);
        if ("1".equals(code)) {
            JsonObject rest = data.deepCopy();
            rest.remove("response");
            return rest;
        }
        if (response.isJsonObject()) {
            // profile.php validation errors: {"response":{"webhook_url":["The webhook url must be a valid URL."]}}
            List<String> messages = new ArrayList<>();
            for (Map.Entry<String, JsonElement> field :
                    response.getAsJsonObject().entrySet()) {
                if (field.getValue().isJsonArray())
                    field.getValue().getAsJsonArray().forEach(m -> messages.add(m.getAsString()));
                else messages.add(field.getValue().getAsString());
            }
            throw new ApiException("VALIDATION_ERROR", String.join("; ", messages));
        }
        if (code == null) code = response.toString();
        // Debug backends echo the code as `message`; prefer the readable fallback then.
        String message = Json.string(data.get("message"));
        if (code.equals(message)) message = null;
        if (code.equals("NO_NUMBER") || code.equals("NO_NUMBER_FOR_FORWARD"))
            throw new NoNumberException(code, message);
        throw new ApiException(code, message);
    }

    /** Omits null/empty values; booleans become 1/0, collections a comma list. */
    static String encodeQuery(Map<String, ?> params) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, ?> e : params.entrySet()) {
            String value = encodeValue(e.getValue());
            if (value == null) continue;
            if (out.length() > 0) out.append('&');
            out.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
        return out.toString();
    }

    private static String encodeValue(Object value) {
        if (value instanceof Boolean) return (Boolean) value ? "1" : "0";
        if (value instanceof Collection) {
            value = ((Collection<?>) value).stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        if (value == null || value.toString().isEmpty()) return null;
        return value.toString();
    }

    /** Options of {@link OnlineSim}. */
    public static final class Builder {
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private String lang = "en";
        private String devId;
        private Duration timeout = DEFAULT_TIMEOUT;
        private Transport transport;

        private Builder() {}

        /**
         * @param apiKey sent as {@code Authorization: Bearer <apiKey>}, never in the URL
         * @return this builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * @param baseUrl full API base, e.g. {@code https://onlinesim.host/api/}; trailing slash optional
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            URI.create(Objects.requireNonNull(baseUrl, "baseUrl"));
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * @param lang language of server texts ({@code lang} query parameter), default {@code en}
         * @return this builder
         */
        public Builder lang(String lang) {
            this.lang = Objects.requireNonNull(lang, "lang");
            return this;
        }

        /**
         * @param devId partner developer id ({@code dev_id} query parameter), sent only when set
         * @return this builder
         */
        public Builder devId(String devId) {
            this.devId = devId;
            return this;
        }

        /**
         * @param timeout per-request timeout, default 30 s
         * @return this builder
         */
        public Builder timeout(Duration timeout) {
            if (timeout == null || timeout.isNegative() || timeout.isZero())
                throw new IllegalArgumentException("timeout must be positive");
            this.timeout = timeout;
            return this;
        }

        /**
         * @param transport HTTP transport, default {@link HttpTransport}
         * @return this builder
         */
        public Builder transport(Transport transport) {
            this.transport = transport;
            return this;
        }

        /** @return the client */
        public OnlineSim build() {
            return new OnlineSim(this);
        }
    }
}
