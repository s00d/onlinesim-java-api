package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.onlinesim.Fake.map;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NumbersTest {
    @Test
    void price() {
        Fake f = Fake.client().json("{\"response\":\"1\",\"price\":0.36}");
        assertEquals(0.36, f.client.numbers().price("vkcom", 44));
        assertEquals("/api/getPrice.php", f.path());
        assertEquals(map("service", "vkcom", "country", "44"), f.query());
    }

    @Test
    void priceIsNullWhenTheServerHasNone() {
        Fake f = Fake.client().json("{\"response\":\"1\",\"price\":null}");
        assertNull(f.client.numbers().price("vkcom"));
    }

    @Test
    void getSendsNoNumberFlagAndRejectAsACommaList() {
        Fake f = Fake.client().json("{\"response\":1,\"tzid\":1520}");
        assertEquals(1520, f.client.numbers().get("google", 44, List.of("4470", "4471")));
        assertEquals("/api/getNum.php", f.path());
        assertEquals(map("service", "google", "country", "44", "reject", "4470,4471"), f.query());
    }

    @Test
    void getDefaultsToCountry7AndOmitsAnEmptyReject() {
        Fake f = Fake.client().json("{\"response\":1,\"tzid\":1}");
        f.client.numbers().get("google", 7, List.of());
        assertEquals(map("service", "google", "country", "7"), f.query());
        Fake g = Fake.client().json("{\"response\":1,\"tzid\":1}");
        g.client.numbers().get("google");
        assertEquals(map("service", "google", "country", "7"), g.query());
    }

    @Test
    void getWithNumberSendsNumber1AndReturnsTheOrder() {
        Fake f = Fake.client()
                .json(
                        "{\"response\":1,\"tzid\":1520,\"number\":\"+447459608320\",\"country\":44,\"time\":1200,\"service\":\"google\"}");
        NumberOrder order = f.client.numbers().getWithNumber("google", 44);
        assertEquals(1520, order.tzid());
        assertEquals("+447459608320", order.number());
        assertEquals(44, order.country());
        assertEquals(1200, order.time());
        assertEquals("google", order.service());
        assertEquals(map("service", "google", "country", "44", "number", "1"), f.query());
    }

    @Test
    void stateSendsBooleansAs10LowercaseOrderbyAndTheNumberFlag() {
        Fake f = Fake.client().json(Fake.fixture("getState"));
        List<Operation> ops = f.client
                .numbers()
                .state(StateOptions.defaults().orderBy(StateOptions.Order.DESC).msgList(true));
        assertEquals(2, ops.size());
        assertEquals(594, ops.get(0).tzid());
        assertEquals("TZ_NUM_ANSWER", ops.get(0).response());
        assertEquals("195895", ops.get(0).msg());
        assertEquals(0.3, ops.get(0).sum());
        assertNull(ops.get(1).msg());
        assertEquals(120, ops.get(1).guardIntervalRemainingSeconds());
        assertEquals("/api/getState.php", f.path());
        assertEquals(map("message_to_code", "1", "orderby", "desc", "msg_list", "1", "number", "1"), f.query());
    }

    @Test
    void stateDefaults() {
        Fake f = Fake.client().json("[]");
        f.client.numbers().state();
        assertEquals(map("message_to_code", "1", "orderby", "asc", "msg_list", "0", "number", "1"), f.query());
    }

    @Test
    void stateReturnsEmptyOnErrorNoOperations() {
        Fake f = Fake.client().json("{\"response\":\"ERROR_NO_OPERATIONS\"}");
        assertTrue(f.client.numbers().state().isEmpty());
    }

    @Test
    void stateOneReturnsTheOperation() {
        Fake f = Fake.client().json("[{\"tzid\":594,\"response\":\"TZ_NUM_ANSWER\",\"msg\":\"VK: 195895\"}]");
        Operation op = f.client.numbers().stateOne(594, StateOptions.defaults().messageToCode(false));
        assertEquals("VK: 195895", op.msg());
        assertEquals(map("tzid", "594", "message_to_code", "0", "msg_list", "0", "number", "1"), f.query());
    }

    @Test
    void stateOneWithMsgList() {
        Fake f = Fake.client()
                .json("[{\"tzid\":5,\"response\":\"TZ_NUM_ANSWER\",\"msg\":[{\"service\":\"vk\",\"msg\":\"1234\"}]}]");
        Operation op = f.client.numbers().stateOne(5, StateOptions.defaults().msgList(true));
        assertNull(op.msg());
        assertEquals("1234", op.msgList().get(0).msg());
        assertEquals("vk", op.msgList().get(0).service());
    }

    @Test
    void stateOneRaisesErrorNoOperations() {
        Fake f = Fake.client().json("{\"response\":\"ERROR_NO_OPERATIONS\"}").json("[]");
        assertEquals(
                "ERROR_NO_OPERATIONS",
                assertThrows(ApiException.class, () -> f.client.numbers().stateOne(1))
                        .getCode());
        assertEquals(
                "ERROR_NO_OPERATIONS",
                assertThrows(ApiException.class, () -> f.client.numbers().stateOne(1))
                        .getCode());
    }

    @Test
    void next() {
        Fake f = Fake.client().json("{\"response\":\"1\",\"tzid\":\"1520\"}");
        f.client.numbers().next(1520);
        assertEquals("/api/setOperationRevise.php", f.path());
        assertEquals(map("tzid", "1520"), f.query());
    }

    @Test
    void close() {
        Fake f = Fake.client().json("{\"response\":1,\"tzid\":1520}");
        f.client.numbers().close(1520);
        assertEquals("/api/setOperationOk.php", f.path());
        assertEquals(map("tzid", "1520"), f.query());
    }

    @Test
    void ban() {
        Fake f = Fake.client().json("{\"response\":1,\"tzid\":1520}");
        f.client.numbers().ban(1520);
        assertEquals("/api/setOperationOk.php", f.path());
        assertEquals(map("tzid", "1520", "ban", "1"), f.query());
    }

    @Test
    void closeSurfacesNoCompleteTzid() {
        Fake f = Fake.client().json("{\"response\":\"NO_COMPLETE_TZID\"}");
        assertEquals(
                "NO_COMPLETE_TZID",
                assertThrows(ApiException.class, () -> f.client.numbers().close(1))
                        .getCode());
    }

    @Test
    void tariffsRequestsCountryAllAndNormalisesEmptyServices() {
        String uk = Fake.fixture("getNumbersStats_44");
        Fake f = Fake.client()
                .json("{\"44\":" + uk + ",\"7\":{\"name\":\"russia\",\"code\":7,\"other\":false,\"services\":[]}}");
        Map<String, CountryStat> all = f.client.numbers().tariffs();
        assertEquals("/api/getNumbersStats.php", f.path());
        assertEquals(map("country", "all"), f.query());
        assertEquals("vkcom", all.get("44").services().get("service_vkcom").slug());
        assertTrue(all.get("7").services().isEmpty());
        assertNull(all.get("7").other());
    }

    @Test
    void tariffsOne() {
        Fake f = Fake.client().json(Fake.fixture("getNumbersStats_44"));
        CountryStat uk = f.client.numbers().tariffsOne(44);
        assertEquals(map("country", "44"), f.query());
        assertEquals(44, uk.code());
        assertEquals("united_kingdom", uk.name());
        assertEquals("United Kingdom", uk.localeName());
        assertEquals(0.63, uk.other());
        assertTrue(uk.enabled());
        ServiceStat vk = uk.services().get("service_vkcom");
        assertEquals(363, vk.count());
        assertEquals(0.36, vk.price());
        assertEquals("VK", vk.service());
    }

    @Test
    void serviceCountQuestionMarkIsNull() {
        Fake f = Fake.client()
                .json("{\"code\":44,\"services\":{\"service_x\":{\"count\":\"?\",\"price\":\"1.5\",\"slug\":\"x\"}}}");
        ServiceStat x = f.client.numbers().tariffsOne(44).services().get("service_x");
        assertNull(x.count());
        assertEquals(1.5, x.price());
    }

    @Test
    void tariffsOneRaisesNoCountry() {
        Fake f = Fake.client().json("{\"response\":\"NO_COUNTRY\"}");
        assertEquals(
                "NO_COUNTRY",
                assertThrows(ApiException.class, () -> f.client.numbers().tariffsOne(999))
                        .getCode());
    }
}
