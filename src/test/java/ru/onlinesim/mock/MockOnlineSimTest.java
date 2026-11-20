package ru.onlinesim.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.onlinesim.ApiException;
import ru.onlinesim.Balance;
import ru.onlinesim.CountryStat;
import ru.onlinesim.FreeList;
import ru.onlinesim.FreeListOptions;
import ru.onlinesim.NoNumberException;
import ru.onlinesim.NumberOrder;
import ru.onlinesim.OnlineSim;
import ru.onlinesim.Operation;
import ru.onlinesim.Paginator;
import ru.onlinesim.PaymentHistory;
import ru.onlinesim.RentItem;
import ru.onlinesim.RentTariff;
import ru.onlinesim.StateOptions;
import ru.onlinesim.Transport;
import ru.onlinesim.TransportException;
import ru.onlinesim.WaitOptions;
import ru.onlinesim.WebhookLog;
import ru.onlinesim.WebhookPayload;

/** MockOnlineSim through the real client: every endpoint, every control helper. */
class MockOnlineSimTest {
    /** waitCode without real waiting: the sleeper is a no-op, the SMS arrives after a number of polls. */
    private static final WaitOptions NO_SLEEP = WaitOptions.defaults().sleeper(d -> {});

    private static String code(Executable call) {
        return assertThrows(ApiException.class, call).getCode();
    }

    private static List<String> paths(MockOnlineSim mock) {
        return mock.requests().stream().map(MockRequest::path).collect(Collectors.toList());
    }

    private static final Map<String, String> AUTH = Map.of("Authorization", "Bearer mock-key");

    /** A raw request to the mock transport, for checking the wire shapes. */
    private static Transport.Response send(MockOnlineSim mock, String pathAndQuery, Map<String, String> headers) {
        try {
            return mock.transport()
                    .send(new Transport.Request(
                            "GET",
                            URI.create("https://x.test/api/" + pathAndQuery),
                            headers,
                            null,
                            Duration.ofSeconds(1)));
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    private static JsonObject wire(MockOnlineSim mock, String pathAndQuery) {
        return JsonParser.parseString(send(mock, pathAndQuery, AUTH).body()).getAsJsonObject();
    }

    private static MockRequest last(MockOnlineSim mock) {
        List<MockRequest> requests = mock.requests();
        return requests.get(requests.size() - 1);
    }

    @Nested
    class AuthAndTransport {
        @Test
        void aWrongKeyIsHttp456AMissingKeyHttp200BothMapToApiException() {
            MockOnlineSim mock = new MockOnlineSim();
            for (String key : new String[] {"nope", ""}) {
                assertEquals(
                        "ERROR_WRONG_KEY",
                        code(() -> mock.client(b -> b.apiKey(key)).user().balance()));
            }
            Transport.Response missing = send(mock, "getBalance.php", Map.of());
            assertEquals(200, missing.status());
            assertEquals("{\"response\":\"ERROR_WRONG_KEY\"}", missing.body());
            Transport.Response wrong = send(mock, "getBalance.php", Map.of("Authorization", "Bearer nope"));
            assertEquals(456, wrong.status());
            assertEquals("{\"response\":\"ERROR_WRONG_KEY\"}", wrong.body());
        }

        @Test
        void tariffsAndFreeNumbersNeedNoKeyButASentWrongKeyIsStillHttp456() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client(b -> b.apiKey(null));
            assertEquals(44, client.numbers().tariffsOne(44).code());
            assertEquals(7, client.rent().tariffsOne(7).code());
            assertEquals(44, client.free().list().messages().country());
            assertEquals(
                    "ERROR_WRONG_KEY",
                    code(() -> mock.client(b -> b.apiKey("nope")).free().list()));
            assertEquals(
                    456,
                    send(mock, "getFreeList", Map.of("Authorization", "Bearer nope"))
                            .status());
        }

        @Test
        void worksAsTheTransportOfAClientBuiltByHandWithAnyBaseUrl() {
            MockOnlineSim mock =
                    new MockOnlineSim(MockOptions.defaults().apiKey("k").balance(5));
            OnlineSim client = OnlineSim.builder()
                    .apiKey("k")
                    .baseUrl("https://anything.test/v2/api/")
                    .transport(mock.transport())
                    .build();
            Balance b = client.user().balance();
            assertEquals(5, b.balance());
            assertEquals(0, b.zbalance());
            assertEquals(0.0, b.income());
            MockRequest req = mock.requests().get(0);
            assertEquals("GET", req.method());
            assertEquals("getBalance.php", req.path());
            assertEquals(Map.of("income", "1", "lang", "en"), req.query());
            assertNull(req.body());
        }

        @Test
        void anUnknownEndpointIsANonJson404() {
            Transport.Response res = send(new MockOnlineSim(), "nope.php", AUTH);
            assertEquals(404, res.status());
            assertTrue(res.body().startsWith("<html>"));
        }
    }

    @Nested
    class Numbers {
        @Test
        void priceNumberNullForAnUnknownServiceUndefinedCountryForAnUnknownCountry() {
            OnlineSim client = new MockOnlineSim().client();
            assertEquals(1.2, client.numbers().price("telegram", 7));
            assertNull(client.numbers().price("nope", 7));
            assertEquals("UNDEFINED_COUNTRY", code(() -> client.numbers().price("google", 999)));
        }

        @Test
        void getFreezesThePriceNumbersAreDeterministicGetWithNumberReturnsTheOrder() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            assertEquals(1, client.numbers().get("google", 44));
            NumberOrder order = client.numbers().getWithNumber("vkcom");
            assertEquals(2, order.tzid());
            assertEquals("+7900000002", order.number());
            assertEquals(7, order.country());
            assertEquals(1200, order.time());
            assertEquals("vkcom", order.service());
            assertEquals("vkcom", order.title());
            assertEquals(0, order.guardIntervalRemainingSeconds());
            assertNull(mock.requests().get(0).query().get("number"));
            assertEquals("1", mock.requests().get(1).query().get("number"));
            Balance b = client.user().balance();
            assertEquals(100, b.balance());
            assertEquals(0.7, b.zbalance());
            List<MockOperation> ops = mock.operations();
            assertEquals(List.of(1L, 2L), ops.stream().map(MockOperation::tzid).collect(Collectors.toList()));
            assertEquals("+44900000001", ops.get(0).number());
            assertEquals("TZ_NUM_WAIT", ops.get(1).status());
        }

        @Test
        void getWithNumberOnTheWireAddsTitleResponseTextAndTheGuardInterval() {
            MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().guardInterval(120));
            JsonObject res = wire(mock, "getNum.php?service=google&country=44&number=1");
            assertEquals("google", res.get("title").getAsString());
            assertEquals("Waiting for SMS", res.get("response_text").getAsString());
            assertEquals(120, res.get("guard_interval_remaining_seconds").getAsInt());
        }

        @Test
        void getErrorsUnknownCountryOrServiceLowBalanceReject() {
            MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().balance(0.25));
            OnlineSim client = mock.client();
            assertEquals("UNDEFINED_COUNTRY", code(() -> client.numbers().get("google", 999)));
            assertEquals("ERROR_NO_SERVICE", code(() -> client.numbers().get("nope", 7)));
            assertEquals("WARNING_LOW_BALANCE", code(() -> client.numbers().get("google", 7)));
            assertThrows(NoNumberException.class, () -> client.numbers().get("google", 44, List.of("4490", "7")));
            assertEquals("4490,7", last(mock).query().get("reject"));
            // a non-matching reject passes; the rejected attempt did not consume a number
            assertEquals(
                    "+44900000001",
                    client.numbers()
                            .getWithNumber("google", 44, List.of("4480"))
                            .number());
            // 0.25 - 0.2 frozen = 0.05 available
            assertEquals("WARNING_LOW_BALANCE", code(() -> client.numbers().get("google", 44)));
        }

        @Test
        void stateEmptyWithoutOperationsListAndOneWaitAnswerOver() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            assertEquals(List.of(), client.numbers().state());
            long a = client.numbers().get("google", 44);
            long b = client.numbers().get("vkcom", 7);
            Operation one = client.numbers().stateOne(a);
            assertEquals("TZ_NUM_WAIT", one.response());
            assertEquals("+44900000001", one.number());
            assertEquals(0.2, one.sum());
            assertEquals(0, one.guardIntervalRemainingSeconds());
            assertNull(one.msg());

            mock.addSms(a, "Your Google code is G-123 456. Do not share it.");
            Operation answered = client.numbers().stateOne(a);
            assertEquals("TZ_NUM_ANSWER", answered.response());
            assertEquals("123456", answered.msg());
            assertEquals(
                    "Your Google code is G-123 456. Do not share it.",
                    client.numbers()
                            .stateOne(a, StateOptions.defaults().messageToCode(false))
                            .msg());
            assertEquals(
                    List.of(b, a),
                    client.numbers().state(StateOptions.defaults().orderBy(StateOptions.Order.DESC)).stream()
                            .map(Operation::tzid)
                            .collect(Collectors.toList()));

            client.numbers().close(a);
            Operation over = client.numbers().stateOne(a);
            assertEquals("TZ_OVER_OK", over.response());
            assertEquals(0, over.time());
            assertEquals("ERROR_NO_OPERATIONS", code(() -> client.numbers().stateOne(999)));
        }

        @Test
        void aTextWithoutACodeGivesTheFullTextNextSwitchesToTheFollowingSmsMsgListListsAll() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            long tzid = client.numbers().get("telegram");
            mock.addSms(tzid, "Welcome!", "telegram");
            assertEquals("Welcome!", client.numbers().stateOne(tzid).msg());
            client.numbers().next(tzid);
            Operation waiting = client.numbers().stateOne(tzid);
            assertEquals("TZ_NUM_WAIT", waiting.response());
            assertNull(waiting.msg());
            mock.addSms(tzid, "code 98765");
            assertEquals("98765", client.numbers().stateOne(tzid).msg());
            Operation all =
                    client.numbers().stateOne(tzid, StateOptions.defaults().msgList(true));
            assertEquals(2, all.msgList().size());
            assertEquals("telegram", all.msgList().get(0).service());
            assertEquals("Welcome!", all.msgList().get(0).msg());
            assertEquals("98765", all.msgList().get(1).msg());
            assertEquals(
                    1,
                    paths(mock).stream()
                            .filter("setOperationRevise.php"::equals)
                            .count());
        }

        @Test
        void nextAnswersAStringTzidWrongTzidForAnUnknownOneNoTzidFor0() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            long tzid = client.numbers().get("google", 44);
            assertEquals(
                    "{\"tzid\":\"" + tzid + "\",\"response\":\"1\"}",
                    send(mock, "setOperationRevise.php?tzid=" + tzid, AUTH).body());
            assertEquals("ERROR_WRONG_TZID", code(() -> client.numbers().next(424242)));
            assertEquals("ERROR_NO_TZID", code(() -> client.numbers().next(0)));
            assertEquals("ERROR_NO_TZID", code(() -> client.numbers().close(0)));
        }

        @Test
        void closeAfterAnSmsSpendsThePriceWithoutOneRefundsItBanAfterAnSmsAnswers0() {
            MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().balance(10));
            OnlineSim client = mock.client();
            long paid = client.numbers().get("telegram");
            long empty = client.numbers().get("telegram");
            mock.addSms(paid, "1111");
            assertEquals("0", code(() -> client.numbers().ban(paid)));
            client.numbers().close(paid);
            client.numbers().close(empty);
            Balance b = client.user().balance();
            assertEquals(8.8, b.balance());
            assertEquals(0, b.zbalance());
            assertEquals(8.8, mock.balance());
            assertEquals("ERROR_WRONG_TZID", code(() -> client.numbers().close(paid)));
            assertEquals("ERROR_WRONG_TZID", code(() -> client.numbers().close(999)));
            long banned = client.numbers().get("google");
            client.numbers().ban(banned);
            assertEquals("TZ_OVER_OK", mock.operations().get(2).status());
        }

        @Test
        void guardIntervalAnOperationWithoutSmsCannotBeClosed() {
            MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().guardInterval(120));
            OnlineSim client = mock.client();
            long tzid = client.numbers().get("google");
            assertEquals(120, client.numbers().stateOne(tzid).guardIntervalRemainingSeconds());
            assertEquals("NO_COMPLETE_TZID", code(() -> client.numbers().close(tzid)));
            mock.addSms(tzid, "5555");
            assertEquals(0, client.numbers().stateOne(tzid).guardIntervalRemainingSeconds());
            client.numbers().close(tzid);
        }

        @Test
        void waitCodeWithAddSmsAfterAndANoOpSleeperNoRealWaiting() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            long tzid = client.numbers().get("google", 44);
            mock.addSmsAfter(tzid, "Your code: 4321", 2);
            assertEquals("4321", client.numbers().waitCode(tzid, NO_SLEEP));
            assertEquals(
                    List.of("getNum.php", "getState.php", "getState.php", "getState.php", "setOperationOk.php"),
                    paths(mock));
            assertEquals("TZ_OVER_OK", mock.operations().get(0).status());
        }

        @Test
        void waitCodeKeepOpenAndSinceWaitForTheNextSmsPollsListingAllOperationsCountToo() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            long tzid = client.numbers().get("google", 44);
            mock.addSmsAfter(tzid, "1111", 0);
            String first = client.numbers().waitCode(tzid, NO_SLEEP.keepOpen(true));
            assertEquals("1111", first);
            mock.addSmsAfter(tzid, "2222", 1);
            client.numbers().state(); // one poll of every operation
            assertEquals("2222", client.numbers().waitCode(tzid, NO_SLEEP.since(first)));
        }

        @Test
        void tariffsFromThePricesServiceSlugKeysCount100NoCountryForAnUnknownOne() {
            OnlineSim client = new MockOnlineSim().client();
            Map<String, CountryStat> all = client.numbers().tariffs();
            assertEquals(List.of("7", "44"), List.copyOf(all.keySet()));
            var vk = all.get("44").services().get("service_vkcom");
            assertEquals(100, vk.count());
            assertEquals(0.36, vk.price());
            assertEquals(2, vk.id());
            assertEquals("vkcom", vk.slug());
            assertEquals("NO_COUNTRY", code(() -> client.numbers().tariffsOne(1)));
            OnlineSim empty = new MockOnlineSim(MockOptions.defaults().prices(Map.of(1, Map.of()))).client();
            assertEquals(Map.of(), empty.numbers().tariffsOne(1).services()); // `[]` on the wire
        }
    }

    @Nested
    class Rent {
        @Test
        void getReservesTheFirstSmsConfirmsAndChargesExtendChargesAtOnceClose() {
            MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().balance(10));
            OnlineSim client = mock.client();
            RentItem rent = client.rent().get(7, 3);
            assertEquals(1, rent.tzid());
            assertEquals("900000001", rent.number());
            assertEquals(0, rent.status());
            assertEquals(false, rent.checked());
            assertEquals(2.5, rent.sum());
            assertEquals(0, rent.days());
            assertEquals(20, rent.time());
            assertEquals(1200L, rent.timeSeconds());
            assertEquals(3, rent.daysAutoextend());
            assertEquals(0, rent.guardIntervalRemainingSeconds());
            assertEquals(1, rent.extension());
            assertEquals(List.of(), rent.messages());
            assertEquals(Map.of("1", 1.0, "3", 2.5, "7", 5.0), rent.extend());
            // reserved: the total balance stays, the price is frozen
            assertEquals(10, mock.balance());
            assertEquals(2.5, client.user().balance().zbalance());
            assertEquals(0, mock.rents().get(0).status());
            assertEquals("TRY_AGAIN_LATER", code(() -> client.rent().extend(rent.tzid()))); // not confirmed yet

            mock.addSms(rent.tzid(), "Bank: 777 888", "bank");
            mock.addSms(rent.tzid(), "no code here");
            RentItem state = client.rent().stateOne(rent.tzid());
            assertEquals(1, state.status());
            assertEquals(true, state.checked());
            assertEquals(3, state.days());
            assertEquals(3 * 1440, state.time());
            assertEquals(3 * 86400L, state.timeSeconds());
            // newest first
            assertEquals("other", state.messages().get(0).service());
            assertEquals("", state.messages().get(0).code());
            assertEquals("bank", state.messages().get(1).service());
            assertEquals("777888", state.messages().get(1).code());
            assertEquals(7.5, mock.balance());
            assertEquals(0, client.user().balance().zbalance());

            RentItem extended = client.rent().extend(rent.tzid(), 1);
            assertEquals(4, extended.days());
            assertEquals(3, extended.daysAutoextend());
            assertEquals(3.5, extended.sum());
            assertEquals(6.5, mock.balance());
            assertEquals("TRY_AGAIN_LATER", code(() -> client.rent().extend(rent.tzid(), 2))); // unknown period
            assertEquals("TRY_AGAIN_LATER", code(() -> client.rent().extend(999))); // unknown tzid

            RentItem fixed = client.rent().get(7, 1, false);
            mock.addSms(fixed.tzid(), "1234");
            RentItem fixedState = client.rent().stateOne(fixed.tzid());
            assertEquals(0, fixedState.extension());
            assertEquals(Map.of(), fixedState.extend());
            assertEquals("TRY_AGAIN_LATER", code(() -> client.rent().extend(fixed.tzid())));
            assertEquals(
                    List.of(rent.tzid(), fixed.tzid()),
                    client.rent().state().stream().map(RentItem::tzid).collect(Collectors.toList()));

            client.rent().close(fixed.tzid()); // confirmed: no refund
            assertEquals(5.5, mock.balance());
            assertEquals(
                    List.of(rent.tzid()),
                    mock.rents().stream().map(MockRent::tzid).collect(Collectors.toList()));
            assertEquals("ERROR_NO_OPERATIONS", code(() -> client.rent().stateOne(fixed.tzid())));
            assertEquals("ERROR_NO_OPERATIONS", code(() -> client.rent().close(fixed.tzid())));
        }

        @Test
        void sumIsANumberInGetRentNumAndExtendAndADecimalStringInGetRentStateOnTheWire() {
            MockOnlineSim mock = new MockOnlineSim();
            JsonObject got = wire(mock, "rent/getRentNum.php?days=3").getAsJsonObject("item");
            assertTrue(got.get("sum").getAsJsonPrimitive().isNumber());
            assertEquals(2.5, got.get("sum").getAsDouble());
            JsonObject listed = wire(mock, "rent/getRentState.php")
                    .getAsJsonArray("list")
                    .get(0)
                    .getAsJsonObject();
            assertEquals("\"2.50\"", listed.get("sum").toString());
            mock.addSms(1, "1234");
            JsonObject extended =
                    wire(mock, "rent/extendRentState.php?tzid=1&days=1").getAsJsonObject("item");
            assertTrue(extended.get("sum").getAsJsonPrimitive().isNumber());
        }

        @Test
        void anUnconfirmedRentCloseReleasesTheReservationNotifySmsHasCode0AndConfirmsNothing() {
            MockOnlineSim mock =
                    new MockOnlineSim(MockOptions.defaults().balance(10).guardInterval(120));
            OnlineSim client = mock.client();
            RentItem rent = client.rent().get(7, 3);
            assertEquals(120, rent.guardIntervalRemainingSeconds());
            assertEquals("NO_COMPLETE_TZID", code(() -> client.rent().close(rent.tzid())));
            mock.addSms(rent.tzid(), "Messages from this sender are not accepted", "notify");
            RentItem state = client.rent().stateOne(rent.tzid());
            assertEquals(0, state.status());
            assertEquals(false, state.checked());
            assertEquals(0, state.guardIntervalRemainingSeconds());
            assertEquals("notify", state.messages().get(0).service());
            assertEquals("0", state.messages().get(0).code());
            JsonObject sms = wire(mock, "rent/getRentState.php")
                    .getAsJsonArray("list")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonArray("messages")
                    .get(0)
                    .getAsJsonObject();
            assertEquals("0", sms.get("code").toString()); // the integer 0 on the wire
            client.rent().close(rent.tzid());
            assertEquals(10, mock.balance()); // released, nothing charged
            assertEquals(0, client.user().balance().zbalance());
        }

        @Test
        void rentErrorsUnknownCountryUnknownDaysLowBalance() {
            OnlineSim client = new MockOnlineSim(MockOptions.defaults().balance(1.5)).client();
            assertEquals("UNDEFINED_COUNTRY", code(() -> client.rent().get(44)));
            assertEquals("UNDEFINED_DAYS", code(() -> client.rent().get(7, 2)));
            assertEquals("WARNING_LOW_BALANCE", code(() -> client.rent().get(7, 3)));
        }

        @Test
        void addSmsAfterWorksForRentsTariffsUnknownCountry() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            long tzid = client.rent().get().tzid();
            mock.addSmsAfter(tzid, "6060", 1);
            assertEquals(List.of(), client.rent().stateOne(tzid).messages());
            RentItem confirmed = client.rent().state().get(0);
            assertEquals(1, confirmed.status());
            var sms = confirmed.messages().get(0);
            assertEquals("6060", sms.code());
            assertEquals("other", sms.service());

            Map<String, RentTariff> tariffs = client.rent().tariffs();
            assertEquals(List.of("7"), List.copyOf(tariffs.keySet()));
            RentTariff ru = tariffs.get("7");
            assertEquals(7, ru.code());
            assertEquals("russia", ru.name());
            assertEquals(Map.of("1", 1.0, "3", 2.5, "7", 5.0), ru.days());
            assertEquals(Map.of("1", 100, "3", 100, "7", 100), ru.count());
            assertEquals(1, ru.extend());
            assertEquals("NO_COUNTRY", code(() -> client.rent().tariffsOne(44)));
        }
    }

    @Nested
    class UserAndFree {
        @Test
        void profileWebhookSetValidateClearLogsRecordedWhenAnSmsArrives() {
            MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().apiKey("secret"));
            OnlineSim client = mock.client();
            assertEquals(1, client.user().profile().id());
            assertEquals("secret", client.user().profile().apikey());
            assertNull(client.user().profile().webhookUrl());
            ApiException invalid =
                    assertThrows(ApiException.class, () -> client.user().setWebhookUrl("not a url"));
            assertEquals("VALIDATION_ERROR", invalid.getCode());
            assertEquals("The webhook url must be a valid URL.", invalid.getMessage());
            client.user().setWebhookUrl("ftp://x"); // any scheme://… URL
            client.user().setWebhookUrl("https://example.com/hook");
            MockRequest post = last(mock);
            assertEquals("POST", post.method());
            assertEquals("profile.php", post.path());
            assertEquals("{\"profile\":{\"webhook_url\":\"https://example.com/hook\"}}", post.body());
            assertEquals("https://example.com/hook", client.user().profile().webhookUrl());

            long tzid = client.numbers().get("google", 44);
            mock.addSms(tzid, "code 1234");
            RentItem rent = client.rent().get();
            mock.addSms(rent.tzid(), "code 5678");
            mock.addSms(rent.tzid(), "no code");
            Paginator<WebhookLog> logs = client.user().webhookLogs();
            assertEquals(3, logs.total());
            assertEquals(1, logs.currentPage());
            assertEquals(10, logs.perPage());
            assertEquals("rent_sms", logs.data().get(0).type());
            assertEquals("", logs.data().get(0).params().code());
            assertEquals("rent_sms", logs.data().get(1).type());
            assertEquals(rent.tzid(), logs.data().get(1).params().operationId());
            assertEquals("5678", logs.data().get(1).params().code());
            assertEquals("receiving_sms", logs.data().get(2).type());
            assertEquals(tzid, logs.data().get(2).params().operationId());
            assertEquals("1234", logs.data().get(2).params().code());
            // activation: +number, seconds left, order time; rent: digits without +, minutes left, SMS time
            WebhookPayload activation = logs.data().get(2).params();
            assertEquals("+44900000001", activation.number());
            assertEquals(1200L, activation.timeLeft());
            assertEquals("2026-01-01 00:00:01", activation.timeStart());
            WebhookPayload rentSms = logs.data().get(1).params();
            assertEquals("7900000002", rentSms.number());
            assertEquals(1440L, rentSms.timeLeft());
            assertEquals("2026-01-01 00:00:03", rentSms.timeStart());

            client.user().clearWebhookUrl();
            assertEquals("{\"profile\":{\"webhook_url\":null}}", last(mock).body());
            assertNull(client.user().profile().webhookUrl());
        }

        @Test
        void balanceIncludesTheFrozenPartPaymentHistoryIsEmpty() {
            MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().balance(3));
            OnlineSim client = mock.client();
            client.numbers().get("telegram");
            Balance b = client.user().balance();
            assertEquals(3, b.balance());
            assertEquals(1.2, b.zbalance());
            PaymentHistory history = client.user().paymentHistory();
            assertEquals(Map.of(), history.paylist());
            assertEquals(Map.of("1", "Card"), history.forms());
            assertEquals(List.of("onlinesim.io"), history.domains());
            assertEquals(
                    "{\"USD\":[5,10,20]}",
                    wire(mock, "getPaymentHistory.php").get("buttons").toString());
            assertEquals(List.of(), history.orders().data());
            assertEquals(0, history.orders().total());
            assertEquals(1, history.orders().currentPage());
        }

        @Test
        void freeListCountriesNumbersKeyedByTheShortNumberMessagesOfTheSelectedNumber() {
            OnlineSim client = new MockOnlineSim().client();
            FreeList uk = client.free().list();
            assertEquals(
                    List.of(44, 7),
                    uk.countries().stream().map(c -> c.country()).collect(Collectors.toList()));
            assertEquals(List.of("7459608320"), List.copyOf(uk.numbers().keySet()));
            assertEquals("7459608320", uk.messages().number());
            assertEquals(44, uk.messages().country());
            assertEquals(2, uk.messages().total());
            assertEquals(10, uk.messages().perPage());
            assertEquals("195895", uk.messages().data().get(0).code());
            assertEquals(
                    "2026-01-01T00:00:00.000000Z", uk.messages().data().get(0).createdAt());
            // count clamped to 10..500 (0 used to divide by zero)
            assertEquals(
                    10,
                    client.free()
                            .list(FreeListOptions.defaults().count(0))
                            .messages()
                            .perPage());
            assertEquals(
                    10,
                    client.free()
                            .list(FreeListOptions.defaults().count(-5))
                            .messages()
                            .perPage());
            assertEquals(
                    500,
                    client.free()
                            .list(FreeListOptions.defaults().count(9999))
                            .messages()
                            .perPage());
            // unknown country → the first one
            assertEquals(
                    44,
                    client.free()
                            .list(FreeListOptions.defaults().country(999))
                            .messages()
                            .country());
            JsonObject raw = JsonParser.parseString(
                            send(new MockOnlineSim(), "getFreeList", Map.of()).body())
                    .getAsJsonObject();
            assertTrue(raw.get("content").isJsonNull());
            assertTrue(raw.get("ignore").getAsJsonPrimitive().isString());
            FreeList ru = client.free().list(FreeListOptions.defaults().country(7));
            assertEquals("51423", ru.messages().data().get(0).code());
            assertEquals("9001234567", ru.messages().data().get(0).myNumber());
        }
    }

    @Nested
    class ControlApi {
        @Test
        void failNextAnswersAnErrorCodeOnceOptionallyOnlyForOneEndpoint() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            mock.failNext("NO_NUMBER", "getNum.php");
            assertEquals(100, client.user().balance().balance()); // other endpoint: untouched
            assertThrows(NoNumberException.class, () -> client.numbers().get("google"));
            assertEquals(1, client.numbers().get("google"));
            mock.failNext("TRY_AGAIN_LATER");
            assertEquals("TRY_AGAIN_LATER", code(() -> client.user().profile()));
        }

        @Test
        void failNextTransportFailsOnceWithANonJsonBodyWaitCodeRetriesIt() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            mock.failNextTransport();
            TransportException error =
                    assertThrows(TransportException.class, () -> client.user().balance());
            assertEquals(502, error.getStatus());

            long tzid = client.numbers().get("google");
            mock.addSms(tzid, "3030");
            mock.failNextTransport(503, "getState.php");
            assertEquals("3030", client.numbers().waitCode(tzid, NO_SLEEP));
            assertEquals(2, paths(mock).stream().filter("getState.php"::equals).count());
        }

        @Test
        void addSmsRejectsUnknownIdsSetBalanceRequestsLogReset() {
            MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().balance(1));
            OnlineSim client = mock.client(b -> b.lang("ru"));
            IllegalStateException unknown = assertThrows(IllegalStateException.class, () -> mock.addSms(5, "x"));
            assertTrue(unknown.getMessage().contains("no open operation or rent 5"));
            IllegalStateException unknownLater =
                    assertThrows(IllegalStateException.class, () -> mock.addSmsAfter(5, "x", 1));
            assertTrue(unknownLater.getMessage().contains("no operation or rent 5"));
            mock.setBalance(50);
            assertEquals(50, mock.balance());
            long tzid = client.numbers().get("telegram");
            client.numbers().close(tzid);
            assertThrows(IllegalStateException.class, () -> mock.addSms(tzid, "late")); // closed
            MockRequest first = mock.requests().get(0);
            assertEquals("GET", first.method());
            assertEquals("getNum.php", first.path());
            assertEquals(Map.of("service", "telegram", "country", "7", "lang", "ru"), first.query());

            mock.failNext("NO_NUMBER");
            mock.reset();
            assertEquals(1, mock.balance());
            assertEquals(List.of(), mock.operations());
            assertEquals(List.of(), mock.rents());
            assertEquals(List.of(), mock.requests());
            assertEquals(1, client.numbers().get("google", 44)); // ids restart, failure queue cleared
        }

        @Test
        void operationsAndRentsAreSnapshots() {
            MockOnlineSim mock = new MockOnlineSim();
            OnlineSim client = mock.client();
            long tzid = client.numbers().get("google");
            MockOperation before = mock.operations().get(0);
            mock.addSms(tzid, "1234");
            assertEquals("TZ_NUM_WAIT", before.status());
            assertEquals(List.of(), before.messages());
            assertEquals("TZ_NUM_ANSWER", mock.operations().get(0).status());
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> mock.operations().get(0).messages().clear());
        }

        @Test
        void extractCodeIsTheSharedRule() {
            assertEquals("123456", MockState.extractCode("G-123 456"));
            assertEquals("123456", MockState.extractCode("code 123-456 ok"));
            assertEquals("4321", MockState.extractCode("Your code: 4321"));
            assertNull(MockState.extractCode("call 123"));
            assertNull(MockState.extractCode("id 123456789")); // longer than 8 digits
            assertEquals("12345678", MockState.extractCode("x 12345678 y"));
        }
    }

    @Test
    void clientOptionsCannotReplaceTheMockTransport() {
        MockOnlineSim mock = new MockOnlineSim();
        OnlineSim client = mock.client(b -> b.transport(r -> {
            throw new AssertionError("real transport used");
        }));
        assertEquals(100, client.user().balance().balance());
        assertInstanceOf(MockRequest.class, mock.requests().get(0));
    }
}
