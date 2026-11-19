package ru.onlinesim;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;

/** Rented numbers: {@code client.rent()}. */
public final class RentApi {
    private final Requester request;

    RentApi(Requester request) {
        this.request = request;
    }

    /** @return a rent of one day in country 7, extendable */
    public RentItem get() {
        return get(7, 1, true);
    }

    /**
     * Rents a number for one day, extendable.
     *
     * @param country country code
     * @return the rent
     */
    public RentItem get(int country) {
        return get(country, 1, true);
    }

    /**
     * Rents a number, extendable.
     *
     * @param country country code
     * @param days    one of the tariff's {@code days} keys
     * @return the rent
     */
    public RentItem get(int country, int days) {
        return get(country, days, true);
    }

    /**
     * Rents a number and charges the balance.
     *
     * @param country   country code
     * @param days      one of the tariff's {@code days} keys
     * @param extension {@code false} makes the rent non-extendable for good
     * @return the rent
     * @throws NoNumberException when no number is available
     */
    public RentItem get(int country, int days, boolean extension) {
        JsonElement res = request.get(
                "rent/getRentNum.php",
                "country",
                country,
                "days",
                days,
                "extension",
                extension,
                "pagination",
                0,
                "number",
                1);
        return Json.convert(res.getAsJsonObject().get("item"), RentItem.class);
    }

    /** @return all rents of the account */
    public List<RentItem> state() {
        return list(request.get("rent/getRentState.php", "pagination", 0, "number", 1));
    }

    /**
     * One rent.
     *
     * @param tzid rent id
     * @return the rent
     * @throws ApiException {@code ERROR_NO_OPERATIONS} for an unknown tzid
     */
    public RentItem stateOne(long tzid) {
        List<RentItem> list = list(request.get("rent/getRentState.php", "tzid", tzid, "pagination", 0, "number", 1));
        if (list.isEmpty()) throw new ApiException("ERROR_NO_OPERATIONS");
        return list.get(0);
    }

    /**
     * Extends a rent by one day.
     *
     * @param tzid rent id
     * @return the rent
     */
    public RentItem extend(long tzid) {
        return extend(tzid, 1);
    }

    /**
     * Extends a rent and charges the balance (only rents created with {@code extension = true}).
     *
     * @param tzid rent id
     * @param days days to add
     * @return the rent
     */
    public RentItem extend(long tzid, int days) {
        JsonElement res =
                request.get("rent/extendRentState.php", "tzid", tzid, "days", days, "pagination", 0, "number", 1);
        return Json.convert(res.getAsJsonObject().get("item"), RentItem.class);
    }

    /** @return rent prices of every country, keyed by country code. No key needed. */
    public Map<String, RentTariff> tariffs() {
        Map<String, RentTariff> all = Json.convert(request.get("rent/tariffsRent.php"), Json.mapOf(RentTariff.class));
        return Json.map(all);
    }

    /** @return rent prices of country 7. No key needed. */
    public RentTariff tariffsOne() {
        return tariffsOne(7);
    }

    /**
     * Rent prices of one country. No key needed.
     *
     * @param country country code
     * @return the tariff
     * @throws ApiException {@code NO_COUNTRY} for a country without rent
     */
    public RentTariff tariffsOne(int country) {
        JsonElement res = request.get("rent/tariffsRent.php", "country", country);
        // An unknown country answers `[]`.
        if (!res.isJsonObject() || !res.getAsJsonObject().has("code")) throw new ApiException("NO_COUNTRY");
        return Json.convert(res, RentTariff.class);
    }

    /**
     * Returns the number before the rent period ends.
     *
     * @param tzid rent id
     * @throws ApiException e.g. {@code NO_COMPLETE_TZID}, {@code NO_CLOSE_RENT_US}
     */
    public void close(long tzid) {
        request.get("rent/closeRentNum.php", "tzid", tzid);
    }

    private static List<RentItem> list(JsonElement res) {
        return Json.list(Json.convert(res.getAsJsonObject().get("list"), Json.listOf(RentItem.class)));
    }
}
