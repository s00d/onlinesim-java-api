package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ClientTest {
    @Test
    void sendsTheKeyAsBearerHeaderNeverInTheQueryPlusLang() {
        Fake f = Fake.client().json("{\"response\":\"1\",\"price\":0.36}");
        f.client.numbers().price("vkcom", 44);
        Transport.Request call = f.last();
        assertEquals("GET", call.method());
        assertEquals("Bearer test-key", call.headers().get("Authorization"));
        assertEquals("application/json", call.headers().get("Accept"));
        assertFalse(call.headers().containsKey("Content-Type"));
        assertFalse(Fake.queryOf(call).containsKey("apikey"));
        assertEquals("en", Fake.queryOf(call).get("lang"));
        assertFalse(Fake.queryOf(call).containsKey("dev_id"));
        assertEquals(Duration.ofSeconds(30), call.timeout());
    }

    @Test
    void appliesLangDevIdTimeoutAndABaseUrlWithoutTrailingSlash() {
        Fake f = Fake.client(b ->
                        b.baseUrl("http://api.test/api").lang("ru").devId("42").timeout(Duration.ofSeconds(5)))
                .json("{\"response\":\"1\",\"price\":1}");
        f.client.numbers().price("vkcom");
        Transport.Request call = f.last();
        assertEquals("http://api.test/api/getPrice.php", call.uri().toString().split("\\?")[0]);
        assertEquals("ru", Fake.queryOf(call).get("lang"));
        assertEquals("42", Fake.queryOf(call).get("dev_id"));
        assertEquals(Duration.ofSeconds(5), call.timeout());
    }

    @Test
    void sendsNoAuthorizationWithoutAKey() {
        Fake f = Fake.client(b -> b.apiKey(null)).json("{\"response\":\"1\",\"price\":1}");
        f.client.numbers().price("vkcom");
        assertFalse(f.last().headers().containsKey("Authorization"));
    }

    @Test
    void usesTheOnlineSimHostByDefault() {
        Fake f = Fake.client(b -> b.baseUrl(OnlineSim.DEFAULT_BASE_URL)).json("{\"response\":\"1\",\"price\":1}");
        f.client.numbers().price("vkcom");
        assertTrue(f.last().uri().toString().startsWith("https://onlinesim.host/api/getPrice.php?"));
    }

    @Test
    void mapsNoNumberToNoNumberException() {
        Fake f = Fake.client().json("{\"response\":\"NO_NUMBER\"}").json("{\"response\":\"FORWARD_NOT_SUPPORTED\"}");
        NoNumberException e =
                assertThrows(NoNumberException.class, () -> f.client.numbers().get("google", 44));
        assertInstanceOf(ApiException.class, e);
        assertEquals("NO_NUMBER", e.getCode());
        assertEquals("No numbers available for this service right now", e.getMessage());
        // only NO_NUMBER is a NoNumberException
        ApiException other =
                assertThrows(ApiException.class, () -> f.client.numbers().get("google"));
        assertFalse(other instanceof NoNumberException);
        assertEquals("Number forwarding is no longer available", other.getMessage());
    }

    @Test
    void mapsHttp429TryAgainLaterToApiExceptionNotTransport() {
        Fake f = Fake.client().reply(429, "{\"response\":\"TRY_AGAIN_LATER\"}");
        ApiException e =
                assertThrows(ApiException.class, () -> f.client.numbers().state());
        assertEquals("TRY_AGAIN_LATER", e.getCode());
        assertEquals("Rate limit exceeded or temporary error, try again later", e.getMessage());
    }

    @Test
    void mapsAMissingKeyHttp200WrongKeyToApiException() {
        Fake f = Fake.client(b -> b.apiKey(null)).json("{\"response\":\"ERROR_WRONG_KEY\"}");
        ApiException e = assertThrows(ApiException.class, () -> f.client.user().balance());
        assertEquals("ERROR_WRONG_KEY", e.getCode());
        assertEquals("Wrong or missing API key", e.getMessage());
    }

    @Test
    void theMessageTableFollowsTheSpec() {
        for (String dead :
                new String[] {"ERROR_NO_KEY", "NO_NUMBER_FOR_FORWARD", "ACCOUNT_IDENTIFICATION_REQUIRED", "API_DISABLED"
                }) assertFalse(ApiException.ERROR_MESSAGES.containsKey(dead), dead);
        for (String added :
                new String[] {"0", "ACCOUNT_CLEANED", "USER_NOT_ACTIVATED", "ERROR_UNDEFINED", "FORWARD_NOT_SUPPORTED"})
            assertTrue(ApiException.ERROR_MESSAGES.containsKey(added), added);
    }

    @Test
    void mapsHttp456WrongKeyToApiExceptionNotTransport() {
        Fake f = Fake.client().reply(456, "{\"response\":\"ERROR_WRONG_KEY\"}");
        ApiException e = assertThrows(ApiException.class, () -> f.client.user().balance());
        assertEquals("ERROR_WRONG_KEY", e.getCode());
        assertEquals("Wrong or missing API key", e.getMessage());
    }

    @Test
    void prefersTheServerMessageButNotWhenItEchoesTheCode() {
        Fake f = Fake.client()
                .json("{\"response\":\"TRY_AGAIN_LATER\",\"message\":\"Maintenance until 12:00\"}")
                .json("{\"response\":\"UNDEFINED_COUNTRY\",\"message\":\"UNDEFINED_COUNTRY\"}")
                .json("{\"response\":\"SOMETHING_NEW\"}");
        assertEquals(
                "Maintenance until 12:00",
                assertThrows(ApiException.class, () -> f.client.numbers().get("x"))
                        .getMessage());
        assertEquals(
                "Unknown country",
                assertThrows(ApiException.class, () -> f.client.numbers().get("x"))
                        .getMessage());
        ApiException unknown =
                assertThrows(ApiException.class, () -> f.client.numbers().get("x"));
        assertEquals("SOMETHING_NEW", unknown.getCode());
        assertEquals("SOMETHING_NEW", unknown.getMessage());
    }

    @Test
    void mapsAValidationObjectToValidationError() {
        Fake f = Fake.client()
                .json(
                        "{\"response\":{\"webhook_url\":[\"The webhook url must be a valid URL.\",\"Too long.\"],\"name\":[\"Required.\"]}}");
        ApiException e = assertThrows(ApiException.class, () -> f.client.user().setWebhookUrl("nope"));
        assertEquals("VALIDATION_ERROR", e.getCode());
        assertEquals("The webhook url must be a valid URL.; Too long.; Required.", e.getMessage());
    }

    @Test
    void mapsANonJsonBodyToTransportExceptionWithStatusAndExcerpt() {
        Fake f = Fake.client()
                .reply(502, "<html>Bad gateway</html>")
                .reply(200, "OK")
                .reply(200, "");
        TransportException e =
                assertThrows(TransportException.class, () -> f.client.user().balance());
        assertEquals(502, e.getStatus());
        assertEquals("<html>Bad gateway</html>", e.getBody());
        assertEquals(
                200,
                assertThrows(TransportException.class, () -> f.client.user().balance())
                        .getStatus());
        assertThrows(TransportException.class, () -> f.client.user().balance());
    }

    @Test
    void cutsTheBodyExcerptAt500Chars() {
        Fake f = Fake.client().reply(500, "x".repeat(2000));
        assertEquals(
                500,
                assertThrows(TransportException.class, () -> f.client.user().balance())
                        .getBody()
                        .length());
    }

    @Test
    void mapsAJsonErrorStatusWithoutResponseToTransportException() {
        Fake f = Fake.client().reply(500, "{\"error\":\"boom\"}");
        TransportException e =
                assertThrows(TransportException.class, () -> f.client.user().balance());
        assertEquals(500, e.getStatus());
        assertEquals("HTTP 500", e.getMessage());
    }

    @Test
    void mapsANetworkFailureToTransportExceptionWithoutStatus() {
        Fake f = Fake.client().fail(new ConnectException("refused"));
        TransportException e =
                assertThrows(TransportException.class, () -> f.client.user().balance());
        assertNull(e.getStatus());
        assertEquals("Network error: refused", e.getMessage());
    }

    @Test
    void mapsARequestTimeoutToTransportException() {
        Fake f = Fake.client().fail(new HttpTimeoutException("request timed out"));
        TransportException e =
                assertThrows(TransportException.class, () -> f.client.user().balance());
        assertNull(e.getStatus());
        assertEquals("Request timed out after 30000 ms", e.getMessage());
    }

    @Test
    void returnsBodiesWithoutResponseAsTheyAre() {
        Fake f = Fake.client().json("[]");
        assertTrue(f.client.numbers().state().isEmpty());
    }

    @Test
    void encodesQueryValues() {
        java.util.Map<String, Object> q = new java.util.LinkedHashMap<>();
        q.put("a", true);
        q.put("b", false);
        q.put("c", java.util.List.of("x", "y"));
        q.put("d", java.util.List.of());
        q.put("e", " &");
        q.put("f", "");
        q.put("g", null);
        assertEquals("a=1&b=0&c=x%2Cy&e=+%26", OnlineSim.encodeQuery(q));
    }

    @Test
    void rejectsABadTimeout() {
        assertThrows(IllegalArgumentException.class, () -> OnlineSim.builder().timeout(Duration.ZERO));
    }
}
