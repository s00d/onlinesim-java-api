package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WebhookTest {
    @Test
    void parsesAReceivingSmsBody() {
        WebhookPayload p =
                OnlineSim.parseWebhook("{\"webhook_type\":\"receiving_sms\",\"operation_id\":1520,\"user_id\":2,"
                        + "\"country_code\":7,\"number\":\"+79001234567\",\"sender\":\"VK\",\"message\":\"VK: 195895 is your code\","
                        + "\"code\":\"195895\",\"time_start\":\"2026-11-19 10:00:00\",\"time_left\":1100}");
        assertEquals("receiving_sms", p.webhookType());
        assertEquals(1520L, p.operationId());
        assertEquals(2L, p.userId());
        assertEquals(7, p.countryCode());
        assertEquals("+79001234567", p.number());
        assertEquals("VK", p.sender());
        assertEquals("VK: 195895 is your code", p.message());
        assertEquals("195895", p.code());
        assertEquals("2026-11-19 10:00:00", p.timeStart());
        assertEquals(1100L, p.timeLeft());
    }

    @Test
    void isLenientNumericStringsMissingFieldsUnknownFieldsAndTypes() {
        WebhookPayload p = OnlineSim.parseWebhook(
                "{\"webhook_type\":\"rent_sms\",\"operation_id\":\"430\",\"code\":2991,\"time_left\":\"soon\",\"number\":{\"x\":1},\"extra\":true}");
        assertEquals("rent_sms", p.webhookType());
        assertEquals(430L, p.operationId());
        assertEquals("2991", p.code());
        assertNull(p.timeLeft());
        assertNull(p.number());
        assertNull(p.userId());
        assertNull(p.countryCode());
        assertNull(p.sender());
        assertNull(p.message());
        assertNull(p.timeStart());
        assertNull(OnlineSim.parseWebhook("{\"webhook_type\":\"other\"}").webhookType());
    }

    @Test
    void throwsOnlyForInputThatIsNotAJsonObject() {
        assertThrows(OnlineSimException.class, () -> OnlineSim.parseWebhook("not json"));
        assertTrue(assertThrows(OnlineSimException.class, () -> OnlineSim.parseWebhook("[1,2]"))
                .getMessage()
                .contains("not a JSON object"));
        assertThrows(OnlineSimException.class, () -> OnlineSim.parseWebhook("null"));
        assertThrows(OnlineSimException.class, () -> OnlineSim.parseWebhook(""));
    }
}
