package ru.onlinesim.mock;

import com.google.gson.JsonElement;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import ru.onlinesim.OnlineSim;
import ru.onlinesim.Transport;

/**
 * In-process fake OnlineSim API for tests: no network, no key, no money, no clock. It is a {@link Transport} ({@link
 * #transport()}) that answers like the real backend; {@link #client()} returns a normal {@link OnlineSim} wired to
 * it. Test helpers add SMS, queue failures and expose the state. Thread-safe: requests and helpers are serialised.
 *
 * <pre>{@code
 * MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().balance(10));
 * OnlineSim client = mock.client();
 * long tzid = client.numbers().get("google", 44);
 * mock.addSmsAfter(tzid, "Your code: 123-456", 2);
 * client.numbers().waitCode(tzid, WaitOptions.defaults().sleeper(d -> {})); // "123456"
 * }</pre>
 */
public final class MockOnlineSim {
    /** Base URL of clients made by {@link #client()}. Never contacted: the mock answers in-process. */
    public static final String MOCK_BASE_URL = "https://mock.onlinesim.invalid/api/";

    private final MockState state;
    private final Transport transport = this::handle;

    /** Mock with {@link MockOptions#defaults()}. */
    public MockOnlineSim() {
        this(MockOptions.defaults());
    }

    /**
     * @param options key, balance, prices, rent tariffs, guard interval
     */
    public MockOnlineSim(MockOptions options) {
        this.state = new MockState(Objects.requireNonNull(options, "options"));
    }

    /**
     * The fake transport; pass it to {@link OnlineSim.Builder#transport(Transport)} when you build the client
     * yourself. Any base URL works.
     *
     * @return the transport
     */
    public Transport transport() {
        return transport;
    }

    /** @return a normal client wired to the mock, with the mock's key */
    public OnlineSim client() {
        return client(b -> {});
    }

    /**
     * A normal client wired to the mock. The builder starts with the mock's key and {@link #MOCK_BASE_URL}; set
     * {@code apiKey} to test a wrong key, or any other client option. The transport is always the mock's.
     *
     * @param options changes to the client builder
     * @return the client
     */
    public OnlineSim client(Consumer<OnlineSim.Builder> options) {
        OnlineSim.Builder b = OnlineSim.builder().apiKey(state.apiKey).baseUrl(MOCK_BASE_URL);
        options.accept(b);
        return b.transport(transport).build();
    }

    /**
     * An SMS arrives now at number operation or rent {@code tzid}; the service is the operation's ({@code other} for
     * a rent).
     *
     * @param tzid operation or rent id
     * @param text SMS text
     * @throws IllegalStateException when there is no open operation or rent with that id
     */
    public void addSms(long tzid, String text) {
        addSms(tzid, text, null);
    }

    /**
     * An SMS arrives now at number operation or rent {@code tzid}.
     *
     * @param tzid    operation or rent id
     * @param text    SMS text
     * @param service sender service; {@code null} for the default
     * @throws IllegalStateException when there is no open operation or rent with that id
     */
    public synchronized void addSms(long tzid, String text, String service) {
        if (!state.deliver(tzid, Objects.requireNonNull(text, "text"), service)) {
            throw new IllegalStateException("MockOnlineSim: no open operation or rent " + tzid);
        }
    }

    /**
     * The SMS arrives after {@code polls} state requests for {@code tzid} ({@code getState} / rent {@code
     * getRentState}, listing it alone or with others): with {@code polls = 2} the third poll sees it. Lets
     * {@code waitCode} run with a no-op sleeper.
     *
     * @param tzid  operation or rent id
     * @param text  SMS text
     * @param polls state requests to pass first
     * @throws IllegalStateException when there is no operation or rent with that id
     */
    public void addSmsAfter(long tzid, String text, int polls) {
        addSmsAfter(tzid, text, polls, null);
    }

    /**
     * Like {@link #addSmsAfter(long, String, int)} with a sender service.
     *
     * @param tzid    operation or rent id
     * @param text    SMS text
     * @param polls   state requests to pass first
     * @param service sender service; {@code null} for the default
     * @throws IllegalStateException when there is no operation or rent with that id
     */
    public synchronized void addSmsAfter(long tzid, String text, int polls, String service) {
        if (!state.operations.containsKey(tzid) && !state.rents.containsKey(tzid)) {
            throw new IllegalStateException("MockOnlineSim: no operation or rent " + tzid);
        }
        state.schedule(tzid, Objects.requireNonNull(text, "text"), polls, service);
    }

    /**
     * The next request answers {@code {"response": code}} ({@code NO_NUMBER}, {@code TRY_AGAIN_LATER}, …).
     *
     * @param code the error code
     */
    public void failNext(String code) {
        failNext(code, null);
    }

    /**
     * The next request to {@code endpoint} answers {@code {"response": code}}.
     *
     * @param code     the error code
     * @param endpoint path relative to {@code /api/}, e.g. {@code getNum.php}; {@code null} for any request
     */
    public synchronized void failNext(String code, String endpoint) {
        state.failures.add(new MockState.Failure(Objects.requireNonNull(code, "code"), 0, endpoint));
    }

    /** The next request fails at transport level: a non-JSON body with HTTP 502. */
    public void failNextTransport() {
        failNextTransport(502, null);
    }

    /**
     * The next request fails at transport level: a non-JSON body with HTTP {@code status}.
     *
     * @param status HTTP status
     */
    public void failNextTransport(int status) {
        failNextTransport(status, null);
    }

    /**
     * The next request to {@code endpoint} fails at transport level: a non-JSON body with HTTP {@code status}.
     *
     * @param status   HTTP status
     * @param endpoint path relative to {@code /api/}; {@code null} for any request
     */
    public synchronized void failNextTransport(int status, String endpoint) {
        state.failures.add(new MockState.Failure(null, status, endpoint));
    }

    /**
     * Sets the total balance.
     *
     * @param usd balance, USD
     */
    public synchronized void setBalance(double usd) {
        state.balance = MockState.mills(usd);
    }

    /** @return total balance, USD, including the part frozen by open operations and unconfirmed rents (as {@code getBalance} reports it) */
    public synchronized double balance() {
        return MockState.usd(state.balance);
    }

    /** @return snapshots of the number operations, oldest first */
    public synchronized List<MockOperation> operations() {
        List<MockOperation> out = new ArrayList<>();
        state.operations.values().forEach(op -> out.add(op.snapshot()));
        return List.copyOf(out);
    }

    /** @return snapshots of the open rents, oldest first */
    public synchronized List<MockRent> rents() {
        List<MockRent> out = new ArrayList<>();
        state.rents.values().forEach(rent -> out.add(rent.snapshot()));
        return List.copyOf(out);
    }

    /** @return every request received, in order (a copy) */
    public synchronized List<MockRequest> requests() {
        return List.copyOf(state.requests);
    }

    /** Back to the constructor's state: balance, no operations, rents, logs, schedules or failures. */
    public synchronized void reset() {
        state.reset();
    }

    private synchronized Transport.Response handle(Transport.Request request) {
        URI uri = request.uri();
        String path = uri.getPath() == null ? "" : uri.getPath();
        int at = path.indexOf("/api/");
        MockRequest req = new MockRequest(
                request.method().toUpperCase(Locale.ROOT),
                at < 0 ? path.replaceFirst("^/", "") : path.substring(at + 5),
                query(uri.getRawQuery()),
                request.body());
        state.requests.add(req);

        MockState.Failure failure = state.takeFailure(req.path());
        if (failure != null) {
            return failure.code != null
                    ? json(Handlers.obj("response", failure.code), 200)
                    : html(failure.status, "Mock transport failure");
        }

        BiFunction<MockState, MockRequest, JsonElement> handler = Handlers.ROUTES.get(req.path());
        if (handler == null) return html(404, "Not Found");
        // A wrong key is HTTP 456 (public endpoints check a sent key too); no key at all is HTTP 200.
        String auth = header(request, "Authorization");
        if (auth != null && !auth.equals("Bearer " + state.apiKey))
            return json(Handlers.obj("response", "ERROR_WRONG_KEY"), 456);
        if (auth == null && !Handlers.PUBLIC_PATHS.contains(req.path()))
            return json(Handlers.obj("response", "ERROR_WRONG_KEY"), 200);
        return json(handler.apply(state, req), 200);
    }

    private static Map<String, String> query(String raw) {
        Map<String, String> q = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) return q;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            q.put(decode(kv[0]), kv.length > 1 ? decode(kv[1]) : "");
        }
        return q;
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static String header(Transport.Request request, String name) {
        for (Map.Entry<String, String> h : request.headers().entrySet()) {
            if (h.getKey().equalsIgnoreCase(name)) return h.getValue();
        }
        return null;
    }

    /** {@code JsonElement.toString()} keeps explicit nulls, like the backend's JSON. */
    private static Transport.Response json(JsonElement body, int status) {
        return new Transport.Response(status, body.toString());
    }

    private static Transport.Response html(int status, String text) {
        return new Transport.Response(status, "<html><body>" + status + " " + text + "</body></html>");
    }
}
