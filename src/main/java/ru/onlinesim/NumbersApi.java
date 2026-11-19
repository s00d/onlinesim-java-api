package ru.onlinesim;

import com.google.gson.JsonElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Numbers for receiving SMS: {@code client.numbers()}. */
public final class NumbersApi implements WaitCode.Target {
    private final Requester request;

    NumbersApi(Requester request) {
        this.request = request;
    }

    /**
     * Price of one number in country 7.
     *
     * @param service service slug
     * @return price, USD; {@code null} when the country has no price for the service
     */
    public Double price(String service) {
        return price(service, 7);
    }

    /**
     * Price of one number.
     *
     * @param service service slug
     * @param country country code
     * @return price, USD; {@code null} when the country has no price for the service
     */
    public Double price(String service, int country) {
        JsonElement res = request.get("getPrice.php", "service", service, "country", country);
        return Json.convert(res.getAsJsonObject().get("price"), Double.class);
    }

    /**
     * Orders a number in country 7.
     *
     * @param service service slug
     * @return the operation id ({@code tzid})
     */
    public long get(String service) {
        return get(service, 7, null);
    }

    /**
     * Orders a number.
     *
     * @param service service slug
     * @param country country code
     * @return the operation id ({@code tzid})
     */
    public long get(String service, int country) {
        return get(service, country, null);
    }

    /**
     * Orders a number, skipping numbers with the given prefixes.
     *
     * @param service service slug
     * @param country country code
     * @param reject  number prefixes to skip, e.g. {@code List.of("4470")}; {@code null} or empty for the profile list
     * @return the operation id ({@code tzid})
     */
    public long get(String service, int country, Collection<String> reject) {
        // `number` is a presence flag on getNum: never sent here.
        JsonElement res = request.get("getNum.php", "service", service, "country", country, "reject", reject);
        return Json.convert(res.getAsJsonObject().get("tzid"), long.class);
    }

    /**
     * Orders a number in country 7 and returns it right away.
     *
     * @param service service slug
     * @return the order
     */
    public NumberOrder getWithNumber(String service) {
        return getWithNumber(service, 7, null);
    }

    /**
     * Orders a number and returns it right away.
     *
     * @param service service slug
     * @param country country code
     * @return the order
     */
    public NumberOrder getWithNumber(String service, int country) {
        return getWithNumber(service, country, null);
    }

    /**
     * Orders a number and returns it right away, skipping numbers with the given prefixes.
     *
     * @param service service slug
     * @param country country code
     * @param reject  number prefixes to skip; {@code null} or empty for the profile list
     * @return the order
     */
    public NumberOrder getWithNumber(String service, int country, Collection<String> reject) {
        JsonElement res =
                request.get("getNum.php", "service", service, "country", country, "reject", reject, "number", 1);
        return Json.convert(res, NumberOrder.class);
    }

    /** @return all operations of the account; empty when there are none */
    public List<Operation> state() {
        return state(StateOptions.defaults());
    }

    /**
     * All operations of the account.
     *
     * @param options code or text, order, SMS list
     * @return the operations; empty when there are none
     */
    public List<Operation> state(StateOptions options) {
        try {
            JsonElement res = request.get(
                    "getState.php",
                    "message_to_code",
                    options.messageToCode(),
                    "orderby",
                    options.orderBy().name().toLowerCase(java.util.Locale.ROOT),
                    "msg_list",
                    options.msgList(),
                    "number",
                    1);
            return operations(res);
        } catch (ApiException e) {
            if (e.getCode().equals("ERROR_NO_OPERATIONS")) return List.of();
            throw e;
        }
    }

    /**
     * One operation.
     *
     * @param tzid operation id
     * @return the operation
     * @throws ApiException {@code ERROR_NO_OPERATIONS} when it does not exist (any more)
     */
    public Operation stateOne(long tzid) {
        return stateOne(tzid, StateOptions.defaults());
    }

    /**
     * One operation.
     *
     * @param tzid    operation id
     * @param options code or text, SMS list ({@code orderBy} is ignored)
     * @return the operation
     * @throws ApiException {@code ERROR_NO_OPERATIONS} when it does not exist (any more)
     */
    @Override
    public Operation stateOne(long tzid, StateOptions options) {
        JsonElement res = request.get(
                "getState.php",
                "tzid",
                tzid,
                "message_to_code",
                options.messageToCode(),
                "msg_list",
                options.msgList(),
                "number",
                1);
        List<Operation> list = operations(res);
        if (list.isEmpty()) throw new ApiException("ERROR_NO_OPERATIONS");
        return list.get(0);
    }

    /**
     * Switches the operation to its next SMS and keeps it open.
     *
     * @param tzid operation id
     */
    @Override
    public void next(long tzid) {
        request.get("setOperationRevise.php", "tzid", tzid);
    }

    /**
     * Finishes the operation. Without SMS it can be closed only after its guard interval.
     *
     * @param tzid operation id
     * @throws ApiException e.g. {@code NO_COMPLETE_TZID}, {@code INTERVAL_CONCURRENT_REQUESTS_ERROR}
     */
    @Override
    public void close(long tzid) {
        request.get("setOperationOk.php", "tzid", tzid);
    }

    /**
     * Finishes the operation and reports the number as bad (only before any SMS arrived).
     *
     * @param tzid operation id
     */
    public void ban(long tzid) {
        request.get("setOperationOk.php", "tzid", tzid, "ban", true);
    }

    /** @return services, prices and stock of every country, keyed by country code. No key needed. */
    public Map<String, CountryStat> tariffs() {
        JsonElement res = request.get("getNumbersStats.php", "country", "all");
        Map<String, CountryStat> all = Json.convert(res, Json.mapOf(CountryStat.class));
        return Json.map(all);
    }

    /** @return services, prices and stock of country 7. No key needed. */
    public CountryStat tariffsOne() {
        return tariffsOne(7);
    }

    /**
     * Services, prices and stock of one country. No key needed.
     *
     * @param country country code
     * @return the country
     * @throws ApiException {@code NO_COUNTRY}
     */
    public CountryStat tariffsOne(int country) {
        return Json.convert(request.get("getNumbersStats.php", "country", country), CountryStat.class);
    }

    /**
     * Polls the operation every 5 s for up to 10 min until a code arrives, then closes it.
     *
     * @param tzid operation id
     * @return the code
     * @throws WaitTimeoutException when no code arrived in time
     */
    public String waitCode(long tzid) {
        return waitCode(tzid, WaitOptions.defaults());
    }

    /**
     * Polls the operation until a code different from {@code since} arrives, then closes it (or calls {@code next}
     * with {@code keepOpen}). A transient {@link TransportException} is retried until the timeout; any
     * {@link ApiException} is thrown at once.
     *
     * @param tzid    operation id
     * @param options interval, timeout, keepOpen, fullMessage, since
     * @return the code (or the full text with {@code fullMessage})
     * @throws WaitTimeoutException when no code arrived in time
     * @throws ApiException         {@code TZ_OVER_OK} / {@code ERROR_NO_ITEMS} when the operation ended without a new
     *                              code, or any other API error
     */
    public String waitCode(long tzid, WaitOptions options) {
        return WaitCode.run(this, tzid, options);
    }

    private static List<Operation> operations(JsonElement res) {
        if (!res.isJsonArray()) return List.of();
        List<Operation> list = Json.convert(res, Json.listOf(Operation.class));
        return list.stream().collect(Collectors.toUnmodifiableList());
    }
}
