package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.onlinesim.Fake.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RentTest {
    private static JsonArray list() {
        return JsonParser.parseString(Fake.fixture("rent_getRentState"))
                .getAsJsonObject()
                .getAsJsonArray("list");
    }

    private static String withItem(int index) {
        JsonObject o = new JsonObject();
        o.addProperty("response", 1);
        o.add("item", list().get(index));
        return o.toString();
    }

    private static final Map<String, String> DETAIL = map("pagination", "0", "number", "1");

    private static Map<String, String> plusDetail(Map<String, String> q) {
        q.putAll(DETAIL);
        return q;
    }

    @Test
    void getSendsExtensionExplicitlyPaginationAndNumberAndParsesSum() {
        Fake f = Fake.client().json(withItem(0));
        RentItem rent = f.client.rent().get(7, 3);
        assertEquals("/api/rent/getRentNum.php", f.path());
        assertEquals(plusDetail(map("country", "7", "days", "3", "extension", "1")), f.query());
        assertEquals(5.14, rent.sum());
        assertEquals(Map.of("1", 14.7, "3", 25.3, "7", 51.0), rent.extend());
        assertEquals("195895", rent.messages().get(0).code());
        assertEquals(18034680L, rent.timeSeconds());
        assertEquals(1, rent.extension());
    }

    @Test
    void getWithExtensionFalseSendsExtension0() {
        Fake f = Fake.client().json(withItem(1));
        f.client.rent().get(7, 1, false);
        assertEquals("0", f.query().get("extension"));
    }

    @Test
    void getDefaults() {
        Fake f = Fake.client().json(withItem(1));
        f.client.rent().get();
        assertEquals(plusDetail(map("country", "7", "days", "1", "extension", "1")), f.query());
    }

    @Test
    void stateNormalisesSumAndTheEmptyExtendMap() {
        Fake f = Fake.client().json(Fake.fixture("rent_getRentState"));
        List<RentItem> list = f.client.rent().state();
        assertEquals("/api/rent/getRentState.php", f.path());
        assertEquals(DETAIL, f.query());
        assertEquals(5.14, list.get(0).sum());
        assertEquals(0.0, list.get(1).sum());
        assertTrue(list.get(1).extend().isEmpty());
        assertTrue(list.get(1).messages().isEmpty());
    }

    @Test
    void stateOne() {
        JsonObject body = new JsonObject();
        body.addProperty("response", 1);
        JsonArray one = new JsonArray();
        one.add(list().get(0));
        body.add("list", one);
        Fake f = Fake.client().json(body.toString());
        RentItem rent = f.client.rent().stateOne(430);
        assertEquals(430, rent.tzid());
        assertEquals(5.14, rent.sum());
        assertEquals(plusDetail(map("tzid", "430")), f.query());
    }

    @Test
    void stateOneRaisesErrorNoOperationsForAnEmptyList() {
        Fake f = Fake.client().json("{\"response\":1,\"list\":[]}");
        assertEquals(
                "ERROR_NO_OPERATIONS",
                assertThrows(ApiException.class, () -> f.client.rent().stateOne(1))
                        .getCode());
    }

    @Test
    void extend() {
        Fake f = Fake.client().json(withItem(0));
        assertEquals(430, f.client.rent().extend(430, 3).tzid());
        assertEquals("/api/rent/extendRentState.php", f.path());
        assertEquals(plusDetail(map("tzid", "430", "days", "3")), f.query());
    }

    @Test
    void tariffs() {
        Fake f = Fake.client().json(Fake.fixture("rent_tariffsRent"));
        Map<String, RentTariff> tariffs = f.client.rent().tariffs();
        assertEquals("/api/rent/tariffsRent.php", f.path());
        assertEquals(Map.of(), f.query());
        assertEquals(2, tariffs.size());
        assertEquals(9.11, tariffs.get("7").days().get("7"));
        assertEquals(0, tariffs.get("380").count().get("7"));
        assertTrue(tariffs.get("380").extCounts().isEmpty());
    }

    @Test
    void tariffsOne() {
        String ru = JsonParser.parseString(Fake.fixture("rent_tariffsRent"))
                .getAsJsonObject()
                .get("7")
                .toString();
        Fake f = Fake.client().json(ru);
        RentTariff t = f.client.rent().tariffsOne();
        assertEquals(7, t.code());
        assertEquals("russia", t.name());
        assertEquals(5.14, t.days().get("3"));
        assertEquals(map("country", "7"), f.query());
    }

    @Test
    void tariffsOneRaisesNoCountryForTheEmptyArrayAnswer() {
        Fake f = Fake.client().json("[]");
        assertEquals(
                "NO_COUNTRY",
                assertThrows(ApiException.class, () -> f.client.rent().tariffsOne(999))
                        .getCode());
    }

    @Test
    void close() {
        Fake f = Fake.client().json("{\"response\":\"1\"}");
        f.client.rent().close(430);
        assertEquals("/api/rent/closeRentNum.php", f.path());
        assertEquals(map("tzid", "430"), f.query());
    }

    @Test
    void closeSurfacesNoCloseRentUs() {
        Fake f = Fake.client().json("{\"response\":\"NO_CLOSE_RENT_US\"}");
        assertEquals(
                "NO_CLOSE_RENT_US",
                assertThrows(ApiException.class, () -> f.client.rent().close(1)).getCode());
    }
}
