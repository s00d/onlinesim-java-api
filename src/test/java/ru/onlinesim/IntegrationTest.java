package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Opt-in integration test against a LOCAL backend. Skipped unless both are set:
 * {@code ONLINESIM_IT_BASE=https://on.test/api/ ONLINESIM_IT_KEY=<apikey>}. With {@code ONLINESIM_IT_MOCK=1} it also runs
 * the number lifecycle on the provider-free {@code test} driver ({@code spec/verify/backend_mock.php setup} first, a key
 * of a user without webhook_url, teardown after).
 */
@EnabledIfEnvironmentVariable(named = "ONLINESIM_IT_BASE", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ONLINESIM_IT_KEY", matches = ".+")
class IntegrationTest {
    static final String BASE = System.getenv("ONLINESIM_IT_BASE");
    static final String KEY = System.getenv("ONLINESIM_IT_KEY");
    static Transport transport;
    static OnlineSim client;

    @BeforeAll
    static void setUp() throws Exception {
        String host = URI.create(BASE).getHost();
        if (!(host.equals("localhost")
                || host.equals("127.0.0.1")
                || host.endsWith(".test")
                || host.endsWith(".localhost"))) {
            throw new IllegalStateException("refusing non-local integration host " + host);
        }
        transport =
                new HttpTransport(HttpClient.newBuilder().sslContext(trustAll()).build());
        client = OnlineSim.builder()
                .apiKey(KEY)
                .baseUrl(BASE)
                .transport(transport)
                .build();
    }

    /** Test-only: local backends use a self-signed certificate. Never part of the SDK API. */
    private static SSLContext trustAll() throws Exception {
        TrustManager trustAll = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] {trustAll}, new SecureRandom());
        return ctx;
    }

    @Test
    void balance() {
        Balance b = client.user().balance();
        assertTrue(Double.isFinite(b.balance()) && Double.isFinite(b.zbalance()));
    }

    @Test
    void profile() {
        assertTrue(client.user().profile().id() > 0);
    }

    @Test
    void numbersTariffsOne44() {
        CountryStat uk = client.numbers().tariffsOne(44);
        assertEquals(44, uk.code());
        assertNotNull(uk.services());
    }

    @Test
    void numbersState() {
        assertNotNull(client.numbers().state());
    }

    @Test
    void rentTariffsOne7() {
        assertEquals(7, client.rent().tariffsOne(7).code());
    }

    @Test
    void rentState() {
        for (RentItem r : client.rent().state()) {
            assertNotNull(r.messages());
            assertNotNull(r.extend());
        }
    }

    @Test
    void paymentHistory() {
        PaymentHistory h = client.user().paymentHistory();
        assertNotNull(h.orders().data());
        assertNotNull(h.paylist());
    }

    @Test
    void webhookLogs() {
        assertNotNull(client.user().webhookLogs().data());
    }

    @Test
    void freeList() {
        FreeList f = client.free().list();
        assertNotNull(f.countries());
        assertNotNull(f.numbers());
    }

    @Test
    void wrongKeyIsApiException() {
        OnlineSim bad = OnlineSim.builder()
                .apiKey("wrong-key")
                .baseUrl(BASE)
                .transport(transport)
                .build();
        assertEquals(
                "ERROR_WRONG_KEY",
                assertThrows(ApiException.class, () -> bad.user().balance()).getCode());
    }

    @Nested
    @EnabledIfEnvironmentVariable(named = "ONLINESIM_IT_MOCK", matches = "1")
    class Lifecycle {
        static final int COUNTRY = 44;
        static final String SERVICE = "google";

        /** Injects an SMS through the test driver callback (local backends only, no auth). */
        private void injectSms(long tzid, String text) throws Exception {
            String body = "{\"id\":" + tzid + ",\"phoneFrom\":\"VK\",\"text\":\"" + text + "\"}";
            Transport.Response res = transport.send(new Transport.Request(
                    "POST",
                    URI.create(BASE).resolve("sms_in_getaways/test"),
                    Map.of("Content-Type", "application/json", "Accept", "application/json"),
                    body,
                    Duration.ofSeconds(30)));
            assertEquals(200, res.status());
        }

        @Test
        void priceGetGetWithNumberStateSmsWaitCodeClose() throws Exception {
            assertNotNull(client.numbers().price(SERVICE, COUNTRY));

            long tzid = client.numbers().get(SERVICE, COUNTRY);
            NumberOrder order = client.numbers().getWithNumber(SERVICE, COUNTRY);
            assertTrue(order.number().matches("^\\+?44\\d+$"));
            assertEquals(COUNTRY, order.country());

            List<Long> ids =
                    client.numbers().state().stream().map(Operation::tzid).collect(Collectors.toList());
            assertTrue(ids.contains(tzid) && ids.contains(order.tzid()));
            assertTrue(client.numbers().stateOne(tzid).response().startsWith("TZ_"));

            // The test driver answers NO_NUMBER to any reject list: proves `reject` is sent.
            assertThrows(NoNumberException.class, () -> client.numbers().get(SERVICE, COUNTRY, List.of("4470")));

            // No SMS yet: the guard interval keeps it open.
            assertEquals(
                    "NO_COMPLETE_TZID",
                    assertThrows(ApiException.class, () -> client.numbers().close(order.tzid()))
                            .getCode());

            injectSms(tzid, "VK: 195895 is your code");
            assertEquals(
                    "195895",
                    client.numbers()
                            .waitCode(
                                    tzid,
                                    WaitOptions.defaults()
                                            .interval(Duration.ofSeconds(1))
                                            .timeout(Duration.ofSeconds(30))));
        }

        @Test
        void setWebhookUrlValidationErrorClearWebhookUrl() {
            client.user().setWebhookUrl("https://example.com/hook");
            assertEquals("https://example.com/hook", client.user().profile().webhookUrl());
            ApiException e =
                    assertThrows(ApiException.class, () -> client.user().setWebhookUrl("not a url"));
            assertEquals("VALIDATION_ERROR", e.getCode());
            client.user().clearWebhookUrl();
            assertNull(client.user().profile().webhookUrl());
        }
    }
}
