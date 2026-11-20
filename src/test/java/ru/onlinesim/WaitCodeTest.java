package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WaitCodeTest {
    /** Fake numbers section on a fake clock: sleeping only moves the clock, nothing really waits. */
    private static final class Setup extends Clock implements WaitCode.Target {
        final List<String> log = new ArrayList<>();
        final Deque<Object> steps;
        Instant now = Instant.EPOCH;

        Setup(Object... steps) {
            this.steps = new ArrayDeque<>(Arrays.asList(steps));
        }

        WaitOptions options() {
            return WaitOptions.defaults().clock(this).sleeper(d -> {
                log.add("sleep " + d.toMillis());
                now = now.plus(d);
            });
        }

        @Override
        public Operation stateOne(long tzid, StateOptions options) {
            log.add("state " + tzid + " code=" + options.messageToCode());
            Object step = steps.isEmpty() ? "{}" : steps.poll();
            if (step instanceof RuntimeException) throw (RuntimeException) step;
            com.google.gson.JsonObject op =
                    com.google.gson.JsonParser.parseString((String) step).getAsJsonObject();
            op.addProperty("tzid", tzid);
            if (!op.has("response")) op.addProperty("response", "TZ_NUM_WAIT");
            return Json.convert(op, Operation.class);
        }

        @Override
        public void close(long tzid) {
            log.add("close " + tzid);
        }

        @Override
        public void next(long tzid) {
            log.add("next " + tzid);
        }

        List<String> only(String prefix) {
            return log.stream().filter(l -> l.startsWith(prefix)).collect(Collectors.toList());
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    @Test
    void returnsTheCodeOnceItArrivesAndClosesTheOperation() {
        Setup t = new Setup("{}", "{}", "{\"response\":\"TZ_NUM_ANSWER\",\"msg\":\"195895\"}");
        assertEquals("195895", WaitCode.run(t, 7, t.options()));
        assertEquals(
                List.of(
                        "state 7 code=true",
                        "sleep 5000",
                        "state 7 code=true",
                        "sleep 5000",
                        "state 7 code=true",
                        "close 7"),
                t.log);
    }

    @Test
    void keepOpenCallsNextSinceSkipsThePreviousCodeFullMessageAsksForText() {
        Setup t = new Setup("{\"msg\":\"old\"}", "{\"msg\":\"Your code is 2991\"}");
        WaitOptions o =
                t.options().keepOpen(true).since("old").fullMessage(true).interval(Duration.ofSeconds(1));
        assertEquals("Your code is 2991", WaitCode.run(t, 7, o));
        assertEquals(List.of("state 7 code=false", "sleep 1000", "state 7 code=false", "next 7"), t.log);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TZ_OVER_OK", "ERROR_NO_ITEMS"})
    void raisesApiExceptionAtOnceForAFinishedOperation(String status) {
        Setup t = new Setup("{\"response\":\"" + status + "\"}");
        ApiException e = assertThrows(ApiException.class, () -> WaitCode.run(t, 7, t.options()));
        assertEquals(status, e.getCode());
        assertEquals(List.of("state 7 code=true"), t.log);
    }

    @Test
    void aFinishedOperationThatStillCarriesANewCodeReturnsIt() {
        Setup t = new Setup("{\"response\":\"TZ_OVER_OK\",\"msg\":\"1234\"}");
        assertEquals("1234", WaitCode.run(t, 7, t.options()));
    }

    @Test
    void raisesWaitTimeoutWhenNoCodeArrivesTheLastSleepIsCutToTheDeadline() {
        Setup t = new Setup();
        WaitOptions o = t.options().interval(Duration.ofSeconds(4)).timeout(Duration.ofSeconds(10));
        assertThrows(WaitTimeoutException.class, () -> WaitCode.run(t, 7, o));
        assertEquals(List.of("sleep 4000", "sleep 4000", "sleep 2000"), t.only("sleep"));
        assertEquals(4, t.only("state").size());
        assertEquals(Instant.EPOCH.plusSeconds(10), t.now);
    }

    @Test
    void retriesATransientTransportException() {
        Setup t = new Setup(new TransportException("Network error: reset"), "{\"msg\":\"42\"}");
        assertEquals("42", WaitCode.run(t, 7, t.options()));
        assertEquals(List.of("state 7 code=true", "sleep 5000", "state 7 code=true", "close 7"), t.log);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRY_AGAIN_LATER", "ERROR_UNDEFINED", "INTERVAL_CONCURRENT_REQUESTS_ERROR"})
    void retriesATransientApiExceptionUntilTheCodeArrives(String code) {
        Setup t = new Setup(new ApiException(code), "{\"msg\":\"42\"}");
        assertEquals("42", WaitCode.run(t, 7, t.options()));
        assertEquals(List.of("state 7 code=true", "sleep 5000", "state 7 code=true", "close 7"), t.log);
    }

    @Test
    void aRetryableApiExceptionStillEndsInWaitTimeoutAtTheDeadline() {
        Object[] steps = new Object[10];
        Arrays.fill(steps, new ApiException("TRY_AGAIN_LATER"));
        Setup t = new Setup(steps);
        WaitOptions o = t.options().interval(Duration.ofSeconds(5)).timeout(Duration.ofSeconds(10));
        assertThrows(WaitTimeoutException.class, () -> WaitCode.run(t, 7, o));
        assertEquals(3, t.only("state").size());
    }

    @Test
    void anOperationWithoutResponseKeepsPolling() {
        Setup t = new Setup("{\"response\":null}", "{\"msg\":\"77\"}");
        assertEquals("77", WaitCode.run(t, 7, t.options()));
    }

    @Test
    void raisesAnyOtherApiExceptionAtOnce() {
        Setup t = new Setup(new ApiException("ERROR_NO_OPERATIONS"));
        assertEquals(
                "ERROR_NO_OPERATIONS",
                assertThrows(ApiException.class, () -> WaitCode.run(t, 7, t.options()))
                        .getCode());
        assertEquals(List.of("state 7 code=true"), t.log);
    }

    @Test
    void isExposedAsNumbersWaitCode() {
        Fake f = Fake.client()
                .json("[{\"tzid\":9,\"response\":\"TZ_NUM_ANSWER\",\"msg\":\"555\"}]")
                .json("{\"response\":1,\"tzid\":9}");
        assertEquals(
                "555", f.client.numbers().waitCode(9, WaitOptions.defaults().sleeper(d -> {})));
        assertEquals(
                List.of("/api/getState.php", "/api/setOperationOk.php"),
                f.calls.stream().map(c -> c.uri().getPath()).collect(Collectors.toList()));
    }
}
