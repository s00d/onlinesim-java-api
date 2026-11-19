package ru.onlinesim.mock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * The mock's routes: endpoint path (relative to {@code /api/}) → handler. A handler gets the state and the request
 * and returns the JSON body the real backend would send (HTTP 200). Shapes follow the OpenAPI spec and the live
 * captures: {@code response} as int or string per endpoint, decimal-string money, {@code []} for empty maps. Auth,
 * queued failures, 456 and 404 are the transport's job ({@link MockOnlineSim}), not the handlers'.
 */
final class Handlers {
    /** Endpoints that answer without a key. */
    static final Set<String> PUBLIC_PATHS = Set.of("getNumbersStats.php", "rent/tariffsRent.php", "getFreeList");

    static final Map<String, BiFunction<MockState, MockRequest, JsonElement>> ROUTES;

    static {
        Map<String, BiFunction<MockState, MockRequest, JsonElement>> r = new LinkedHashMap<>();
        r.put("getPrice.php", Handlers::getPrice);
        r.put("getNum.php", Handlers::getNum);
        r.put("getState.php", Handlers::getState);
        r.put("setOperationRevise.php", Handlers::setOperationRevise);
        r.put("setOperationOk.php", Handlers::setOperationOk);
        r.put("getNumbersStats.php", Handlers::getNumbersStats);
        r.put("rent/getRentNum.php", Handlers::getRentNum);
        r.put("rent/getRentState.php", Handlers::getRentState);
        r.put("rent/extendRentState.php", Handlers::extendRentState);
        r.put("rent/closeRentNum.php", Handlers::closeRentNum);
        r.put("rent/tariffsRent.php", Handlers::tariffsRent);
        r.put("getBalance.php", Handlers::getBalance);
        r.put("getProfile.php", Handlers::getProfile);
        r.put("profile.php", Handlers::profile);
        r.put("webhook-logs", Handlers::webhookLogs);
        r.put("getPaymentHistory.php", Handlers::getPaymentHistory);
        r.put("getFreeList", Handlers::getFreeList);
        ROUTES = Collections.unmodifiableMap(r);
    }

    private static final Map<Integer, String[]> COUNTRY_NAMES =
            Map.of(7, new String[] {"russia", "Russia"}, 44, new String[] {"united_kingdom", "United Kingdom"});

    private Handlers() {}

    // ----- helpers -----

    /** JSON object from key/value pairs: numbers, strings, booleans, {@code null} and JSON elements. */
    static JsonObject obj(Object... kv) {
        JsonObject o = new JsonObject();
        for (int i = 0; i < kv.length; i += 2) o.add((String) kv[i], json(kv[i + 1]));
        return o;
    }

    private static JsonElement json(Object v) {
        if (v == null) return JsonNull.INSTANCE;
        if (v instanceof JsonElement) return (JsonElement) v;
        if (v instanceof Number) return new JsonPrimitive((Number) v);
        if (v instanceof Boolean) return new JsonPrimitive((Boolean) v);
        return new JsonPrimitive(v.toString());
    }

    private static JsonObject error(String code) {
        return obj("response", code);
    }

    private static boolean flag(MockRequest req, String key) {
        String v = req.query().get(key);
        return v != null && !v.equals("0");
    }

    private static boolean has(MockRequest req, String key) {
        return req.query().containsKey(key);
    }

    private static long integer(MockRequest req, String key, long fallback) {
        String v = req.query().get(key);
        if (v == null) return fallback;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int int32(MockRequest req, String key, int fallback) {
        long v = integer(req, key, fallback);
        return v == (int) v ? (int) v : fallback;
    }

    private static String[] countryName(int code) {
        String[] name = COUNTRY_NAMES.get(code);
        return name != null ? name : new String[] {"country_" + code, "Country " + code};
    }

    /** Laravel paginator over {@code items}. */
    private static JsonObject paginate(List<? extends JsonElement> items, int page, int perPage, String path) {
        int total = items.size();
        int lastPage = Math.max(1, (total + perPage - 1) / perPage);
        int from = Math.min(Math.max(0, (page - 1) * perPage), total);
        List<? extends JsonElement> slice = items.subList(from, Math.min(total, from + Math.max(0, perPage)));
        JsonArray data = new JsonArray();
        slice.forEach(data::add);
        String base = "https://onlinesim.host/api/" + path;
        return obj(
                "current_page",
                page,
                "data",
                data,
                "first_page_url",
                base + "?page=1",
                "from",
                slice.isEmpty() ? null : (page - 1) * perPage + 1,
                "last_page",
                lastPage,
                "last_page_url",
                base + "?page=" + lastPage,
                "links",
                new JsonArray(),
                "next_page_url",
                page < lastPage ? base + "?page=" + (page + 1) : null,
                "path",
                base,
                "per_page",
                perPage,
                "prev_page_url",
                page > 1 ? base + "?page=" + (page - 1) : null,
                "to",
                slice.isEmpty() ? null : (page - 1) * perPage + slice.size(),
                "total",
                total);
    }

    /** A map's JSON, or {@code []} when it is empty (PHP encodes an empty map as an array). */
    private static JsonElement mapOrArray(JsonObject map) {
        return map.size() == 0 ? new JsonArray() : map;
    }

    // ----- numbers -----

    private static String msgOf(MockSms sms, boolean toCode) {
        return toCode && sms.code() != null ? sms.code() : sms.text();
    }

    private static JsonObject operationView(MockState state, MockOperation op, MockRequest req) {
        boolean toCode = flag(req, "message_to_code");
        boolean open = !op.status.equals("TZ_OVER_OK");
        JsonObject view = obj(
                "country", op.country,
                "sum", MockState.usd(op.sumMills),
                "service", op.service,
                "number", op.number,
                "subscription_package_id", null,
                "webhook_url", state.webhookUrl,
                "response", op.status,
                "tzid", op.tzid,
                "time", open ? MockState.OPERATION_TIME : 0,
                "form", "index");
        if (flag(req, "msg_list")) {
            if (!op.messages.isEmpty()) {
                JsonArray list = new JsonArray();
                for (MockSms s : op.messages) list.add(obj("service", s.service(), "msg", msgOf(s, toCode)));
                view.add("msg", list);
            }
        } else if (op.cursor < op.messages.size()) {
            view.addProperty("msg", msgOf(op.messages.get(op.cursor), toCode));
        }
        if (has(req, "number")) {
            view.addProperty(
                    "guard_interval_remaining_seconds", open && op.messages.isEmpty() ? state.guardInterval : 0);
        }
        return view;
    }

    private static JsonElement getPrice(MockState state, MockRequest req) {
        Map<String, Double> prices = state.prices.get(int32(req, "country", 7));
        if (prices == null) return error("UNDEFINED_COUNTRY");
        return obj("response", "1", "price", prices.get(req.query().getOrDefault("service", "")));
    }

    private static JsonElement getNum(MockState state, MockRequest req) {
        int country = int32(req, "country", 7);
        String service = req.query().getOrDefault("service", "");
        Map<String, Double> prices = state.prices.get(country);
        if (prices == null) return error("UNDEFINED_COUNTRY");
        Double price = prices.get(service);
        if (price == null) return error("ERROR_NO_SERVICE");
        long cost = MockState.mills(price);
        if (state.available() < cost) return error("WARNING_LOW_BALANCE");

        String number = "+" + country + MockState.shortNumber(state.peekId());
        for (String prefix : req.query().getOrDefault("reject", "").split(",")) {
            if (!prefix.isEmpty() && number.startsWith(prefix, 1)) return error("NO_NUMBER");
        }

        long tzid = state.nextId();
        state.frozen += cost;
        state.operations.put(tzid, new MockOperation(tzid, service, country, number, cost));
        if (!has(req, "number")) return obj("response", 1, "tzid", tzid);
        return obj(
                "response", 1,
                "tzid", tzid,
                "number", number,
                "country", country,
                "time", MockState.OPERATION_TIME,
                "service", service);
    }

    private static JsonElement getState(MockState state, MockRequest req) {
        List<MockOperation> ops = new ArrayList<>();
        if (has(req, "tzid")) {
            MockOperation op = state.operations.get(integer(req, "tzid", -1));
            if (op != null) ops.add(op);
        } else ops.addAll(state.operations.values());
        for (MockOperation op : ops) state.poll(op.tzid);
        if (ops.isEmpty()) return error("ERROR_NO_OPERATIONS");
        boolean desc = "desc".equals(req.query().get("orderby"));
        ops.sort((a, b) -> desc ? Long.compare(b.tzid, a.tzid) : Long.compare(a.tzid, b.tzid));
        JsonArray out = new JsonArray();
        for (MockOperation op : ops) out.add(operationView(state, op, req));
        return out;
    }

    private static JsonElement setOperationRevise(MockState state, MockRequest req) {
        long tzid = integer(req, "tzid", 0);
        MockOperation op = state.operations.get(tzid);
        // Always succeeds, even for unknown tzids, like the backend.
        if (op != null && !op.status.equals("TZ_OVER_OK")) {
            op.cursor = op.messages.size();
            op.status = "TZ_NUM_WAIT";
        }
        return obj("response", 1, "tzid", tzid);
    }

    private static JsonElement setOperationOk(MockState state, MockRequest req) {
        long tzid = integer(req, "tzid", 0);
        MockOperation op = state.operations.get(tzid);
        if (op == null || op.status.equals("TZ_OVER_OK")) return error("ERROR_WRONG_TZID");
        boolean received = !op.messages.isEmpty();
        if (flag(req, "ban") && received) return obj("response", "0");
        if (!received && state.guardInterval > 0) return error("NO_COMPLETE_TZID");
        state.frozen -= op.sumMills;
        if (received) state.balance -= op.sumMills;
        op.status = "TZ_OVER_OK";
        return obj("response", 1, "tzid", tzid);
    }

    private static JsonObject countryStat(MockState state, int code) {
        String[] name = countryName(code);
        JsonObject services = new JsonObject();
        int id = 0;
        for (Map.Entry<String, Double> e :
                state.prices.getOrDefault(code, Map.of()).entrySet()) {
            String slug = e.getKey();
            services.add(
                    "service_" + slug,
                    obj(
                            "count",
                            100,
                            "popular",
                            false,
                            "code",
                            code,
                            "price",
                            e.getValue(),
                            "id",
                            ++id,
                            "service",
                            slug,
                            "slug",
                            slug));
        }
        return obj(
                "name",
                name[0],
                "position",
                code,
                "code",
                code,
                "other",
                false,
                "new",
                false,
                "enabled",
                true,
                "services",
                mapOrArray(services),
                "locale_name",
                name[1]);
    }

    private static JsonElement getNumbersStats(MockState state, MockRequest req) {
        if ("all".equals(req.query().get("country"))) {
            JsonObject all = new JsonObject();
            for (int code : state.prices.keySet()) all.add(String.valueOf(code), countryStat(state, code));
            return mapOrArray(all);
        }
        int code = int32(req, "country", 7);
        return state.prices.containsKey(code) ? countryStat(state, code) : error("NO_COUNTRY");
    }

    // ----- rent -----

    private static JsonObject daysMap(Map<Integer, ?> days) {
        JsonObject o = new JsonObject();
        days.forEach((d, v) -> o.add(String.valueOf(d), json(v)));
        return o;
    }

    private static JsonObject rentItem(MockState state, MockRent rent, MockRequest req) {
        MockRentTariff tariff = state.rentTariffs.get(rent.country);
        boolean extendable = rent.extension && tariff != null && tariff.extend();
        long start = rent.tzid;
        JsonArray messages = new JsonArray();
        for (int i = rent.messages.size() - 1; i >= 0; i--) { // newest first
            MockSms m = rent.messages.get(i);
            messages.add(obj(
                    "id", m.id(),
                    "service", m.service(),
                    "text", m.text(),
                    "code", m.code() != null ? m.code() : "",
                    "created_at", m.createdAt()));
        }
        JsonObject item = obj(
                "status",
                1,
                "extension",
                rent.extension ? 1 : 0,
                "messages",
                messages,
                "webhook_url",
                state.webhookUrl,
                "sum",
                MockState.decimal(rent.sumMills, 2),
                "country",
                rent.country,
                "start_time",
                MockState.stamp(start),
                "stop_time",
                MockState.stamp(start + rent.days * 86_400L),
                "checked_time",
                MockState.stamp(start),
                "number",
                rent.number,
                "days_autoextend",
                0,
                "rent",
                1,
                "tzid",
                rent.tzid,
                "time",
                rent.days * 24 * 60,
                "days",
                rent.days,
                "hours",
                rent.days * 24,
                "extend",
                extendable ? daysMap(tariff.days()) : new JsonArray(),
                "checked",
                true,
                "reload",
                null,
                "day_extend",
                extendable ? tariff.days().getOrDefault(1, 0.0) : 0,
                "m_ext",
                false,
                "freeze",
                false);
        if (has(req, "number")) item.addProperty("time_seconds", rent.days * 86_400L);
        return item;
    }

    private static JsonElement getRentNum(MockState state, MockRequest req) {
        int country = int32(req, "country", 7);
        MockRentTariff tariff = state.rentTariffs.get(country);
        if (tariff == null) return error("NO_COUNTRY");
        int days = int32(req, "days", 1);
        Double price = tariff.days().get(days);
        if (price == null) return error("UNDEFINED_DAYS");
        long cost = MockState.mills(price);
        if (state.available() < cost) return error("WARNING_LOW_BALANCE");
        long tzid = state.nextId();
        state.balance -= cost;
        // `extension` defaults to 1 on the server; `0` makes the rent non-extendable for good.
        MockRent rent = new MockRent(
                tzid,
                country,
                MockState.shortNumber(tzid),
                days,
                cost,
                !"0".equals(req.query().get("extension")));
        state.rents.put(tzid, rent);
        return obj("response", 1, "item", rentItem(state, rent, req));
    }

    private static JsonElement getRentState(MockState state, MockRequest req) {
        List<MockRent> rents = new ArrayList<>();
        if (has(req, "tzid")) {
            MockRent rent = state.rents.get(integer(req, "tzid", -1));
            if (rent != null) rents.add(rent);
        } else rents.addAll(state.rents.values());
        for (MockRent rent : rents) state.poll(rent.tzid);
        JsonArray list = new JsonArray();
        for (MockRent rent : rents) list.add(rentItem(state, rent, req));
        return obj("response", 1, "list", list);
    }

    private static JsonElement extendRentState(MockState state, MockRequest req) {
        MockRent rent = state.rents.get(integer(req, "tzid", 0));
        if (rent == null) return error("ERROR_WRONG_TZID");
        MockRentTariff tariff = state.rentTariffs.get(rent.country);
        if (!rent.extension || tariff == null || !tariff.extend()) return error("TRY_AGAIN_LATER");
        int days = int32(req, "days", 1);
        Double price = tariff.days().get(days);
        if (price == null) return error("UNDEFINED_DAYS");
        long cost = MockState.mills(price);
        if (state.available() < cost) return error("WARNING_LOW_BALANCE");
        state.balance -= cost;
        rent.days += days;
        rent.sumMills += cost;
        return obj("response", 1, "item", rentItem(state, rent, req));
    }

    private static JsonElement closeRentNum(MockState state, MockRequest req) {
        long tzid = integer(req, "tzid", 0);
        if (state.rents.remove(tzid) == null) return error("ERROR_WRONG_TZID");
        state.unschedule(tzid);
        return obj("response", "1");
    }

    private static JsonElement rentTariff(MockState state, int code) {
        MockRentTariff t = state.rentTariffs.get(code);
        if (t == null) return new JsonArray();
        JsonObject count = new JsonObject();
        t.days().keySet().forEach(d -> count.addProperty(String.valueOf(d), 100));
        return obj(
                "code",
                code,
                "enabled",
                true,
                "name",
                countryName(code)[0],
                "new",
                false,
                "position",
                code,
                "count",
                count,
                "days",
                daysMap(t.days()),
                "extend",
                t.extend() ? 1 : 0,
                "confirm",
                false,
                "ext_counts",
                count.deepCopy());
    }

    private static JsonElement tariffsRent(MockState state, MockRequest req) {
        if (has(req, "country")) return rentTariff(state, int32(req, "country", 0));
        JsonObject all = new JsonObject();
        for (int code : state.rentTariffs.keySet()) all.add(String.valueOf(code), rentTariff(state, code));
        return mapOrArray(all);
    }

    // ----- user -----

    private static JsonElement getBalance(MockState state, MockRequest req) {
        JsonObject res = obj(
                "response", "1",
                "balance", MockState.decimal(state.balance, 3),
                "zbalance", MockState.usd(state.frozen));
        if (has(req, "income")) {
            res.addProperty("income", 0);
            res.addProperty("income_usd", "0.000");
        }
        return res;
    }

    private static JsonElement getProfile(MockState state, MockRequest req) {
        JsonObject profile = obj(
                "id",
                MockState.MOCK_USER_ID,
                "hash_id",
                "mock",
                "name",
                "mock@example.com",
                "username",
                "mock@example.com",
                "email",
                "mock@example.com",
                "email_verified_at",
                MockState.stamp(0),
                "active",
                "true",
                "apikey",
                state.apiKey,
                "api_access",
                true,
                "is_api",
                true,
                "number",
                null,
                "country",
                null,
                "locale",
                "en",
                "currency",
                "USD",
                "number_region",
                null,
                "number_country",
                null,
                "number_reject",
                new JsonArray(),
                "ugroup",
                1,
                "verify",
                0,
                "block",
                0,
                "resetCount",
                null,
                "created_at",
                MockState.stamp(0),
                "notification_at",
                null,
                "first_login",
                false,
                "sub",
                0,
                "disable",
                false,
                "webhook_url",
                state.webhookUrl,
                "is_project_subscription",
                false);
        return obj("response", "1", "profile", profile);
    }

    private static boolean isHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null
                    && !uri.getHost().isEmpty();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static JsonElement profile(MockState state, MockRequest req) {
        JsonElement body = parse(req.body());
        JsonElement profile =
                body != null && body.isJsonObject() ? body.getAsJsonObject().get("profile") : null;
        if (!req.method().equals("POST") || profile == null || !profile.isJsonObject()) return error("ERROR_PARAMS");
        JsonElement url = profile.getAsJsonObject().get("webhook_url");
        boolean isNull = url == null || url.isJsonNull();
        if (!isNull
                && !(url.isJsonPrimitive() && url.getAsJsonPrimitive().isString() && isHttpUrl(url.getAsString()))) {
            JsonArray messages = new JsonArray();
            messages.add("The webhook url must be a valid URL.");
            return obj("response", obj("webhook_url", messages));
        }
        if (url != null) state.webhookUrl = isNull ? null : url.getAsString();
        return obj("response", "1", "user", obj("id", MockState.MOCK_USER_ID, "webhook_url", state.webhookUrl));
    }

    private static JsonElement parse(String body) {
        if (body == null) return null;
        try {
            return com.google.gson.JsonParser.parseString(body);
        } catch (com.google.gson.JsonParseException e) {
            return null;
        }
    }

    private static JsonElement webhookLogs(MockState state, MockRequest req) {
        List<JsonObject> logs = new ArrayList<>(state.webhookLogs);
        Collections.reverse(logs);
        return obj("response", "1", "data", paginate(logs, int32(req, "page", 1), 10, "webhook-logs"));
    }

    private static JsonElement getPaymentHistory(MockState state, MockRequest req) {
        return obj(
                "response",
                "1",
                "orders",
                paginate(List.of(), int32(req, "page", 1), 10, "getPaymentHistory.php"),
                "paylist",
                new JsonArray(),
                "domains",
                new JsonArray(),
                "currency",
                obj("USD", 1));
    }

    // ----- free -----

    /** Fixed free numbers: country → [name, text, number, (sender, text)...]. */
    private static final Map<Integer, FreeCountry> FREE = new java.util.TreeMap<>(Map.of(
            44,
            new FreeCountry(
                    "united_kingdom",
                    "United Kingdom",
                    "7459608320",
                    "Amazon",
                    "Amazon: Use 195895 to reset your password.",
                    "Microsoft",
                    "Microsoft access code: 2991"),
            7,
            new FreeCountry("russia", "Russia", "9001234567", "Telegram", "Telegram code: 51423")));

    private static final class FreeCountry {
        final String name;
        final String text;
        /** Short number → [sender, text] pairs; one number per country. */
        final Map<String, List<String[]>> numbers = new LinkedHashMap<>();

        FreeCountry(String name, String text, String number, String... messages) {
            this.name = name;
            this.text = text;
            List<String[]> list = new ArrayList<>();
            for (int i = 0; i < messages.length; i += 2) list.add(Arrays.copyOfRange(messages, i, i + 2));
            numbers.put(number, list);
        }
    }

    private static JsonElement getFreeList(MockState state, MockRequest req) {
        int wanted = int32(req, "country", 44);
        int country =
                FREE.containsKey(wanted) ? wanted : FREE.keySet().iterator().next();
        FreeCountry data = FREE.get(country);

        JsonObject numbers = new JsonObject();
        for (String n : data.numbers.keySet()) {
            numbers.add(
                    n,
                    obj(
                            "country",
                            country,
                            "country_original",
                            data.name,
                            "data_humans",
                            "1 day ago",
                            "full_number",
                            country + n,
                            "is_archive",
                            false));
        }
        String asked = req.query().get("number");
        String number = asked != null && data.numbers.containsKey(asked)
                ? asked
                : data.numbers.keySet().iterator().next();
        List<JsonObject> messages = new ArrayList<>();
        List<String[]> sms = data.numbers.get(number);
        for (int i = 0; i < sms.size(); i++) {
            String code = MockState.extractCode(sms.get(i)[1]);
            messages.add(obj(
                    "id",
                    i + 1,
                    "text",
                    sms.get(i)[1],
                    "in_number",
                    sms.get(i)[0],
                    "my_number",
                    number,
                    "created_at",
                    MockState.stamp(i),
                    "data_humans",
                    "1 day ago",
                    "code",
                    code != null ? code : "-"));
        }
        JsonArray countries = new JsonArray();
        FREE.forEach((c, d) -> countries.add(obj("country", c, "country_text", d.text, "country_original", d.name)));
        JsonObject page = paginate(messages, int32(req, "page", 1), int32(req, "count", 10), "getFreeList");
        page.addProperty("number", number);
        page.addProperty("country", country);
        return obj("response", 1, "countries", countries, "numbers", numbers, "messages", page);
    }
}
