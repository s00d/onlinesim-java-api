package ru.onlinesim.mock;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * State of the mock backend: the single owner of everything the fake API remembers — balance, operations, rents,
 * the profile's webhook URL and its delivery log, scheduled SMS, the failure queue and the request log. Handlers
 * ({@link Handlers}) read and change it only through this object; {@link MockOnlineSim} serialises access to it.
 *
 * <p>Deterministic: ids and numbers come from a counter, timestamps from a fixed epoch plus that counter. Money is
 * kept in integer thousandths of a USD ("mills") so sums never drift.
 */
final class MockState {
    static final long MOCK_USER_ID = 1;
    /** Seconds an operation stays open, as reported in {@code time}. The mock has no clock, so it never runs down. */
    static final int OPERATION_TIME = 1200;
    /** Minutes an unconfirmed rent waits for its first SMS ({@code stop_time} = start + this). */
    static final int RENT_FIRST_SMS_MINUTES = 20;
    /** Sender of a rent SMS from a prohibited sender: it gets the integer {@code code} 0 and does not confirm the rent. */
    static final String NOTIFY = "notify";

    /** 2026-01-01 00:00:00 UTC. */
    private static final long EPOCH = 1_767_225_600L;

    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
    private static final Pattern SEPARATOR = Pattern.compile("(\\d)[\\h\\v-]+(?=\\d)");
    private static final Pattern CODE = Pattern.compile("(?<!\\d)\\d{4,8}(?!\\d)");

    final String apiKey;
    final int guardInterval;
    final Map<Integer, Map<String, Double>> prices;
    final Map<Integer, MockRentTariff> rentTariffs;
    private final long startBalance;

    /** Total balance incl. the frozen part, mills. */
    long balance;
    /** Frozen by open operations and unconfirmed rents, mills. */
    long frozen;

    final Map<Long, MockOperation> operations = new LinkedHashMap<>();
    final Map<Long, MockRent> rents = new LinkedHashMap<>();
    String webhookUrl;
    final List<JsonObject> webhookLogs = new ArrayList<>();
    final List<MockRequest> requests = new ArrayList<>();
    final List<Failure> failures = new ArrayList<>();
    private final Map<Long, List<Scheduled>> scheduled = new LinkedHashMap<>();
    /** Source of tzids, numbers, message ids and timestamps. */
    private long seq;

    MockState(MockOptions options) {
        this.apiKey = options.apiKey();
        this.guardInterval = options.guardInterval();
        this.prices = options.prices();
        this.rentTariffs = options.rentTariffs();
        this.startBalance = mills(options.balance());
        reset();
    }

    void reset() {
        balance = startBalance;
        frozen = 0;
        operations.clear();
        rents.clear();
        webhookUrl = null;
        webhookLogs.clear();
        requests.clear();
        failures.clear();
        scheduled.clear();
        seq = 0;
    }

    /** Available balance (total minus frozen), mills. */
    long available() {
        return balance - frozen;
    }

    /** The id the next operation/rent will get. */
    long peekId() {
        return seq + 1;
    }

    long nextId() {
        return ++seq;
    }

    long seq() {
        return seq;
    }

    /** Short (country-less) number for an id: deterministic, 9 digits. */
    static String shortNumber(long id) {
        return String.valueOf(900_000_000L + id);
    }

    static long mills(double usd) {
        return Math.round(usd * 1000);
    }

    static double usd(long mills) {
        return mills / 1000.0;
    }

    /** USD with {@code digits} decimals, as the backend sends {@code balance} and rent {@code sum}. */
    static String decimal(long mills, int digits) {
        return BigDecimal.valueOf(mills, 3)
                .setScale(digits, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }

    /** {@code YYYY-MM-DD HH:MM:SS} at {@code seconds} after the fixed mock epoch. */
    static String stamp(long seconds) {
        return STAMP.format(Instant.ofEpochSecond(EPOCH + seconds));
    }

    /**
     * The code rule shared by every SDK mock: dashes/spaces between digits are removed, then the first run of 4–8
     * digits (not part of a longer run) is the code; {@code null} when there is none.
     */
    static String extractCode(String text) {
        String joined = SEPARATOR.matcher(text).replaceAll("$1");
        Matcher m = CODE.matcher(joined);
        return m.find() ? m.group() : null;
    }

    /** Remove and return the first queued failure that applies to {@code path}; {@code null} when none does. */
    Failure takeFailure(String path) {
        for (Iterator<Failure> it = failures.iterator(); it.hasNext(); ) {
            Failure f = it.next();
            if (f.endpoint == null || f.endpoint.equals(path)) {
                it.remove();
                return f;
            }
        }
        return null;
    }

    /** An SMS arrives at operation or rent {@code tzid} now. Returns {@code false} when there is no open one. */
    boolean deliver(long tzid, String text, String service) {
        String code = extractCode(text);
        MockOperation op = operations.get(tzid);
        if (op != null && !op.status.equals("TZ_OVER_OK")) {
            MockSms sms = new MockSms(service != null ? service : op.service, text, code, 0, null);
            op.messages.add(sms);
            op.status = "TZ_NUM_ANSWER";
            // Activation: `+<digits>`, seconds left, the order time.
            webhook("receiving_sms", tzid, op.country, op.number, sms, seq, OPERATION_TIME, stamp(op.tzid));
            return true;
        }
        MockRent rent = rents.get(tzid);
        if (rent != null) {
            long id = nextId();
            MockSms sms = new MockSms(service != null ? service : "other", text, code, id, stamp(id));
            rent.messages.add(sms);
            if (rent.status == 0 && !NOTIFY.equals(sms.service())) {
                // The first real SMS confirms the rent: the reservation is charged.
                frozen -= rent.sumMills;
                balance -= rent.sumMills;
                rent.status = 1;
            }
            // Rent: full number in digits without `+`, minutes left, the SMS time.
            long minutesLeft = rent.status == 1 ? rent.days * 24L * 60 : RENT_FIRST_SMS_MINUTES;
            webhook("rent_sms", tzid, rent.country, rent.country + rent.number, sms, id, minutesLeft, stamp(id));
            return true;
        }
        return false;
    }

    void schedule(long tzid, String text, int polls, String service) {
        scheduled.computeIfAbsent(tzid, k -> new ArrayList<>()).add(new Scheduled(text, service, polls));
    }

    void unschedule(long tzid) {
        scheduled.remove(tzid);
    }

    /** One state poll of {@code tzid}: delivers the scheduled SMS whose polls ran out, counts down the rest. */
    void poll(long tzid) {
        List<Scheduled> list = scheduled.get(tzid);
        if (list == null) return;
        List<Scheduled> due = new ArrayList<>();
        for (Iterator<Scheduled> it = list.iterator(); it.hasNext(); ) {
            Scheduled s = it.next();
            if (s.polls <= 0) {
                due.add(s);
                it.remove();
            } else s.polls--;
        }
        if (list.isEmpty()) scheduled.remove(tzid);
        // Dropped silently when the operation was closed meanwhile: a real SMS would not arrive either.
        for (Scheduled s : due) deliver(tzid, s.text, s.service);
    }

    /** No HTTP call: the mock only records what the backend would have sent. */
    private void webhook(
            String type, long tzid, int country, String number, MockSms sms, long at, long timeLeft, String timeStart) {
        if (webhookUrl == null) return;
        String time = stamp(at);
        JsonObject params = new JsonObject();
        params.addProperty("webhook_type", type);
        params.addProperty("operation_id", tzid);
        params.addProperty("user_id", MOCK_USER_ID);
        params.addProperty("country_code", country);
        params.addProperty("number", number);
        params.addProperty("sender", sms.service());
        params.addProperty("message", sms.text());
        params.addProperty("code", sms.code() != null ? sms.code() : "");
        params.addProperty("time_start", timeStart);
        params.addProperty("time_left", timeLeft);

        JsonObject log = new JsonObject();
        log.addProperty("id", webhookLogs.size() + 1);
        log.addProperty("user_id", MOCK_USER_ID);
        log.addProperty("webhook_url", webhookUrl);
        log.addProperty("type", type);
        log.add("params", params);
        log.addProperty("status", "success");
        log.add("error", com.google.gson.JsonNull.INSTANCE);
        log.addProperty("created_at", time);
        log.addProperty("updated_at", time);
        webhookLogs.add(log);
    }

    /** A queued failure: the next matching request answers {@code {"response": code}}, or a non-JSON HTTP status. */
    static final class Failure {
        final String code;
        final int status;
        final String endpoint;

        Failure(String code, int status, String endpoint) {
            this.code = code;
            this.status = status;
            this.endpoint = endpoint;
        }
    }

    private static final class Scheduled {
        final String text;
        final String service;
        /** getState/getRentState polls of this tzid still to pass before the SMS arrives. */
        int polls;

        Scheduled(String text, String service, int polls) {
            this.text = text;
            this.service = service;
            this.polls = polls;
        }
    }
}
