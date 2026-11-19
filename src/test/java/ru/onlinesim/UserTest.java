package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.onlinesim.Fake.map;

import java.util.List;
import org.junit.jupiter.api.Test;

class UserTest {
    @Test
    void balanceSendsIncome1AndParsesTheDecimalString() {
        Fake f = Fake.client().json(Fake.fixture("getBalance"));
        Balance b = f.client.user().balance();
        assertEquals(1442.33, b.balance());
        assertEquals(1023.41, b.zbalance());
        assertEquals(9.78, b.income());
        assertEquals("/api/getBalance.php", f.path());
        assertEquals(map("income", "1"), f.query());
    }

    @Test
    void balanceWithoutIncome() {
        Fake f = Fake.client().json("{\"response\":\"1\",\"balance\":\"0.000\",\"zbalance\":0}");
        Balance b = f.client.user().balance();
        assertEquals(0.0, b.balance());
        assertNull(b.income());
    }

    @Test
    void profile() {
        Fake f = Fake.client().json(Fake.fixture("getProfile"));
        Profile p = f.client.user().profile();
        assertEquals("/api/getProfile.php", f.path());
        assertEquals(map(), f.query());
        assertEquals(1, p.id());
        assertEquals("https://example.com/hook", p.webhookUrl());
        assertEquals(List.of("333"), p.numberReject());
        assertTrue(p.apiAccess().getAsBoolean());
        assertEquals(1, p.numberCountry());
    }

    @Test
    void profileNumberRejectAsAMap() {
        Fake f = Fake.client()
                .json("{\"response\":\"1\",\"profile\":{\"id\":2,\"number_reject\":{\"0\":\"4470\",\"1\":\"4471\"}}}");
        assertEquals(List.of("4470", "4471"), f.client.user().profile().numberReject());
    }

    @Test
    void paymentHistoryNormalisesTheEmptyPaylist() {
        Fake f = Fake.client().json(Fake.fixture("getPaymentHistory"));
        PaymentHistory h = f.client.user().paymentHistory(2);
        assertEquals("/api/getPaymentHistory.php", f.path());
        assertEquals(map("page", "2"), f.query());
        assertTrue(h.paylist().isEmpty());
        assertEquals(9462, h.orders().data().get(0).id());
        assertEquals("89.00", h.orders().data().get(0).sum());
        assertEquals(106, h.orders().total());
        assertEquals(1.0, h.currency().get("USD"));
    }

    @Test
    void setWebhookUrlPostsJson() {
        Fake f = Fake.client().json("{\"response\":\"1\",\"user\":{\"id\":2}}");
        f.client.user().setWebhookUrl("https://example.com/hook");
        Transport.Request call = f.last();
        assertEquals("POST", call.method());
        assertEquals("/api/profile.php", call.uri().getPath());
        assertEquals("application/json", call.headers().get("Content-Type"));
        assertEquals("Bearer test-key", call.headers().get("Authorization"));
        assertEquals("{\"profile\":{\"webhook_url\":\"https://example.com/hook\"}}", call.body());
    }

    @Test
    void clearWebhookUrlPostsWebhookUrlNull() {
        Fake f = Fake.client().json("{\"response\":\"1\",\"user\":{\"id\":2}}");
        f.client.user().clearWebhookUrl();
        assertEquals("POST", f.last().method());
        assertEquals("{\"profile\":{\"webhook_url\":null}}", f.last().body());
    }

    @Test
    void webhookLogsReturnsThePaginator() {
        String page =
                "{\"current_page\":1,\"data\":[{\"id\":7,\"user_id\":2,\"webhook_url\":\"https://example.com/hook\","
                        + "\"type\":\"receiving_sms\",\"params\":{\"webhook_type\":\"receiving_sms\",\"operation_id\":1520,\"code\":\"195895\"},"
                        + "\"status\":\"success\",\"error\":null,\"created_at\":\"2026-11-19 10:00:01\",\"updated_at\":\"2026-11-19 10:00:01\"}],"
                        + "\"from\":1,\"to\":1,\"last_page\":1,\"per_page\":10,\"total\":1}";
        Fake f = Fake.client().json("{\"response\":\"1\",\"data\":" + page + "}");
        Paginator<WebhookLog> logs = f.client.user().webhookLogs();
        assertEquals("/api/webhook-logs", f.path());
        assertEquals(map("page", "1"), f.query());
        assertEquals(1, logs.total());
        WebhookLog log = logs.data().get(0);
        assertEquals(7, log.id());
        assertEquals("success", log.status());
        assertEquals(1520L, log.params().operationId());
        assertEquals("195895", log.params().code());
    }
}
