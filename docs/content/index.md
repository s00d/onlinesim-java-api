---
title: onlinesim-java-api
description: Java 11+ client for the OnlineSim API — SMS numbers, rent, account and free numbers.
---

## Install

Java 11 or newer. HTTP goes through the JDK's `java.net.http.HttpClient`; the only dependency is Gson.

::code-group
```groovy [Gradle]
implementation 'ru.onlinesim:onlinesim-api:2.1.0'
```

```kotlin [Gradle (Kotlin)]
implementation("ru.onlinesim:onlinesim-api:2.1.0")
```

```xml [Maven]
<dependency>
  <groupId>ru.onlinesim</groupId>
  <artifactId>onlinesim-api</artifactId>
  <version>2.1.0</version>
</dependency>
```
::

## Quick start

Check the balance, order a number, wait for the SMS code.

```java
import ru.onlinesim.Balance;
import ru.onlinesim.NumberOrder;
import ru.onlinesim.OnlineSim;

public class QuickStart {
  public static void main(String[] args) {
    OnlineSim client = new OnlineSim(System.getenv("ONLINESIM_KEY"));

    Balance balance = client.user().balance();
    System.out.println("available: " + (balance.balance() - balance.zbalance()) + " USD");

    NumberOrder order = client.numbers().getWithNumber("google", 44);
    System.out.println("enter " + order.number() + " in the app");

    // Polls every 5 s for up to 10 min, then closes the operation.
    String code = client.numbers().waitCode(order.tzid());
    System.out.println("code: " + code);
  }
}
```

## Client options

```java
import java.time.Duration;
import ru.onlinesim.OnlineSim;

OnlineSim client = OnlineSim.builder()
    .apiKey("your-api-key")
    .lang("en")
    .timeout(Duration.ofSeconds(15))
    .build();
```

| Option | Type | Default | Description |
|---|---|---|---|
| `apiKey` | `String` | — | Sent as `Authorization: Bearer <apiKey>`, never in the URL. Not needed for tariffs and free numbers. |
| `baseUrl` | `String` | `https://onlinesim.host/api/` | API base; trailing slash optional. |
| `lang` | `String` | `en` | Language of server texts (`lang` query parameter). |
| `devId` | `String` | — | Partner developer id (`dev_id`), sent only when set. |
| `timeout` | `Duration` | 30 s | Per-request timeout. |
| `transport` | `Transport` | `HttpTransport` | HTTP transport: your own `HttpClient` (proxy, TLS), or a fake in tests. |

`new OnlineSim(apiKey)` uses the defaults. The client is immutable and thread-safe; share one instance.

Sections: [`client.numbers()`](/numbers), [`client.rent()`](/rent), [`client.user()`](/user), [`client.free()`](/free). Webhook bodies are parsed by the static [`OnlineSim.parseWebhook`](/webhooks).

Durations (`timeout`, the `waitCode` interval) are `java.time.Duration`. Response objects are immutable, with record-style accessors named after the server's fields (`webhook_url` → `webhookUrl()`). Two things are normalised: money that the server sends as a decimal string (`balance`, rent `sum`) becomes a `double`, and maps that arrive as an empty PHP array `[]` become empty maps. Collections are never `null`.

A proxy or a custom TLS setup goes through your own `HttpClient`:

```java
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import ru.onlinesim.HttpTransport;
import ru.onlinesim.OnlineSim;

HttpClient http = HttpClient.newBuilder()
    .proxy(ProxySelector.of(new InetSocketAddress("proxy.local", 3128)))
    .build();
OnlineSim client = OnlineSim.builder().apiKey("your-api-key").transport(new HttpTransport(http)).build();
```

## Error handling

Every exception is unchecked and extends `OnlineSimException`.

| Class | When | Fields |
|---|---|---|
| `ApiException` | The API answered with an error code (also a wrong key, HTTP 456). | `getCode()`, `getMessage()` |
| `NoNumberException` | `NO_NUMBER` / `NO_NUMBER_FOR_FORWARD`: no number right now. Subclass of `ApiException`. | `getCode()`, `getMessage()` |
| `TransportException` | Network error, request timeout, or a body that is not JSON. | `getStatus()` (`null` without a response), `getBody()` (first 500 chars) |
| `WaitTimeoutException` | `waitCode` got no code within its `timeout`. | — |

```java
import ru.onlinesim.ApiException;
import ru.onlinesim.NoNumberException;
import ru.onlinesim.TransportException;

try {
  client.numbers().get("google", 44);
} catch (NoNumberException e) {
  // try another country or later
} catch (ApiException e) {
  System.err.println(e.getCode() + ": " + e.getMessage()); // e.g. WARNING_LOW_BALANCE
} catch (TransportException e) {
  System.err.println("network " + e.getStatus() + " " + e.getBody());
}
```

`getMessage()` is the server's text when it sends one, otherwise a short English description of the code (`ApiException.ERROR_MESSAGES`), otherwise the code itself. Profile validation failures become `ApiException` with code `VALIDATION_ERROR`.
