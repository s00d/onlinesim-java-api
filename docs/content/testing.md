---
title: Testing
description: Test your integration offline with MockOnlineSim — no network, key or money.
navigation:
  order: 6
---

`MockOnlineSim` (package `ru.onlinesim.mock`) is an in-process fake of the OnlineSim API that ships in the main jar. It is a `Transport` that answers like the real backend — same shapes, same error codes, HTTP 456 for a wrong key — and keeps a balance, operations and rents. Your code talks to a normal `OnlineSim` client, so nothing in it changes for tests. It is deterministic: no randomness, no clock, numbers and ids come from a counter. Thread-safe: requests and test helpers are serialised.

## Setup

```java
import ru.onlinesim.OnlineSim;
import ru.onlinesim.mock.MockOnlineSim;
import ru.onlinesim.mock.MockOptions;

MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().balance(10));
OnlineSim client = mock.client(); // an OnlineSim wired to the mock
```

```java
new MockOnlineSim()
new MockOnlineSim(MockOptions options)

MockOptions.defaults()
    .apiKey(String apiKey)                                   // default "mock-key"
    .balance(double usd)                                     // default 100
    .prices(Map<Integer, Map<String, Double>> prices)        // country → service → USD
    .rentTariffs(Map<Integer, MockRentTariff> rentTariffs)   // country → new MockRentTariff(days → USD, extend)
    .guardInterval(int seconds)                              // default 0
```

| Option | Default | Description |
|---|---|---|
| `apiKey` | `"mock-key"` | Key the mock accepts. `mock.client()` uses it; `mock.client(b -> b.apiKey("x"))` tests a wrong key. |
| `balance` | `100` | Starting balance, USD. |
| `prices` | `{7: {vkcom: 0.5, telegram: 1.2, google: 0.3}, 44: {google: 0.2, vkcom: 0.36}}` | Number prices; replaces the defaults. Also feeds `numbers().tariffs()`. |
| `rentTariffs` | `{7: MockRentTariff({1: 1.0, 3: 2.5, 7: 5.0}, extend = true)}` | Rent prices; `extend = false` makes rents of that country non-extendable. |
| `guardInterval` | `0` | `> 0`: an operation without SMS cannot be closed (`NO_COMPLETE_TZID`). |

`mock.client(Consumer<OnlineSim.Builder>)` takes the usual [client options](/#client-options); the transport is always the mock's. To build the client yourself, pass `mock.transport()`; any `baseUrl` works:

```java
import ru.onlinesim.OnlineSim;
import ru.onlinesim.mock.MockOnlineSim;

MockOnlineSim mock = new MockOnlineSim();
OnlineSim client = OnlineSim.builder().apiKey("mock-key").transport(mock.transport()).build();
```

## A full example

Order a number, let an SMS arrive while `waitCode` polls, check the code and the money. `addSmsAfter` delivers the SMS after a number of polls instead of after some time, so `waitCode` runs with a no-op sleeper and the test takes milliseconds.

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.onlinesim.NumberOrder;
import ru.onlinesim.OnlineSim;
import ru.onlinesim.WaitOptions;
import ru.onlinesim.mock.MockOnlineSim;
import ru.onlinesim.mock.MockOptions;

class ReceiveCodeTest {
    @Test
    void receivesTheCode() {
        MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().balance(10));
        OnlineSim client = mock.client();

        NumberOrder order = client.numbers().getWithNumber("google", 44);
        assertEquals("+44900000001", order.number());

        mock.addSmsAfter(order.tzid(), "Your Google code: 123-456", 2); // the third poll sees it
        String code = client.numbers().waitCode(order.tzid(), WaitOptions.defaults().sleeper(d -> {}));

        assertEquals("123456", code);
        assertEquals("TZ_OVER_OK", mock.operations().get(0).status()); // waitCode closed it
        assertEquals(9.8, mock.balance()); // the price is spent once an SMS arrived
    }
}
```

The `msg` of an operation is the code by default: the first run of 4–8 digits, with spaces and dashes between digits removed (`123-456` → `123456`); a text without such a run comes back whole. `messageToCode(false)` (or `fullMessage(true)` in `waitCode`) gives the full text.

## Errors

`failNext(code)` makes the next request answer `{"response": code}` — the client turns it into `ApiException` (or `NoNumberException`) exactly as for the real server. Pass an endpoint to fail only that endpoint. `failNextTransport()` makes the next request fail with a non-JSON HTTP 502 (`TransportException`).

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import ru.onlinesim.NoNumberException;
import ru.onlinesim.OnlineSim;
import ru.onlinesim.TransportException;
import ru.onlinesim.mock.MockOnlineSim;

class FailuresTest {
    @Test
    void handlesAMissingNumberAndANetworkHiccup() {
        MockOnlineSim mock = new MockOnlineSim();
        OnlineSim client = mock.client();

        mock.failNext("NO_NUMBER", "getNum.php");
        assertThrows(NoNumberException.class, () -> client.numbers().get("google"));

        mock.failNextTransport(503);
        assertThrows(TransportException.class, () -> client.user().balance());

        assertEquals(100, client.user().balance().balance()); // one-shot
    }
}
```

The state itself produces the real error codes too: `WARNING_LOW_BALANCE` when the balance is short, `NO_NUMBER` when `reject` matches the next number, `UNDEFINED_COUNTRY` / `ERROR_NO_SERVICE` for unknown prices, `ERROR_WRONG_TZID` for an unknown or closed operation, `TRY_AGAIN_LATER` when extending a non-extendable rent, `ERROR_WRONG_KEY` (HTTP 456) for a wrong key.

## Inspecting requests

`mock.requests()` lists every request received as `MockRequest`: `method()`, `path()` (relative to `/api/`), `query()` (decoded, in the order sent) and `body()` (the JSON of a POST as sent, `null` for a GET).

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.onlinesim.mock.MockOnlineSim;
import ru.onlinesim.mock.MockRequest;

class RejectTest {
    @Test
    void sendsRejectAsACommaList() {
        MockOnlineSim mock = new MockOnlineSim();
        mock.client().numbers().get("google", 44, List.of("4470", "4471"));

        MockRequest req = mock.requests().get(0);
        assertEquals("getNum.php", req.path());
        assertEquals(Map.of("service", "google", "country", "44", "reject", "4470,4471", "lang", "en"), req.query());
    }
}
```

## Control API

| Member | Description |
|---|---|
| `client()` / `client(Consumer<OnlineSim.Builder>)` | A normal `OnlineSim` wired to the mock. |
| `transport()` | The fake `Transport`. |
| `addSms(tzid, text)` / `addSms(tzid, text, service)` | An SMS arrives now at a number operation or a rent (detected by `tzid`). Throws `IllegalStateException` for an unknown or closed one. |
| `addSmsAfter(tzid, text, polls)` / `addSmsAfter(tzid, text, polls, service)` | The SMS arrives after `polls` state requests for `tzid` (`numbers().state()`/`stateOne`, `rent().state()`/`stateOne`). |
| `failNext(code)` / `failNext(code, endpoint)` | The next request (to `endpoint` only, e.g. `"getNum.php"`) answers `{"response": code}`. |
| `failNextTransport()` / `failNextTransport(status)` / `failNextTransport(status, endpoint)` | The next request fails with a non-JSON body and HTTP `status` (default `502`). |
| `setBalance(usd)` / `balance()` | Set / read the total balance, USD (it includes the part frozen by open operations, as `user().balance()` reports it). |
| `operations()` / `rents()` | Snapshots of the current number operations (`MockOperation`) / open rents (`MockRent`), for assertions. |
| `requests()` | Log of the requests received (`MockRequest`). |
| `reset()` | Back to the constructor's state. |

## What the mock does

- **Numbers**: `get` freezes the price; `close` after an SMS spends it, `close` without one refunds it; `ban` after an SMS answers `"0"`, like the server. Numbers are `+<country>9000000NN`, ids count from 1. `next` switches to the following SMS. The operation's `time` stays at 1200 s — there is no clock.
- **Rent**: `get` charges at once and keeps the `extension` flag; `extend` charges again; `close` removes the rent. SMS appear in `messages()`, newest first.
- **User**: `balance` (frozen part included), a fixed profile, `setWebhookUrl` validates `http(s)` URLs (`VALIDATION_ERROR` otherwise), `webhookLogs` lists the deliveries the mock *recorded* when an SMS arrived while a webhook URL was set — it makes no HTTP call. `paymentHistory` is empty.
- **Free**: `free().list()` returns two fixed countries with one number and a few SMS each.
- Not imitated: the server's 5 s lock after `setOperationOk` (`INTERVAL_CONCURRENT_REQUESTS_ERROR`) and anything else that needs real time.
