# Changelog

## 2.0.0

A rewrite. The public API changed; see the [docs](https://s00d.github.io/onlinesim-java-api/).

### Removed

- The proxy API (`proxy()`, deprecated by the backend).
- `numbers.repeat` (getNumRepeat), `numbers.service` (getService), `numbers.serviceNumber` (getServiceNumber): no backend route.
- `rent.portReload`: deprecated no-op on the backend.
- `user.createEmpty` (pay/createEmpty).
- `free.countries`, `free.numbers`, `free.messages`: the backend returns empty data for them; use `free().list`.
- Dependencies on Apache HttpClient 4.x, `java-dotenv`, `org.jetbrains:annotations`; test-only Consul, Mockito, Hamcrest leftovers.
- The bogus `gradlePlugin` publication (`id "ru.onlinesim.OnlineSimApi"`), `jcenter`, `apply plugin: 'maven'`, `profile-release.gradle`, `pub.sh`, committed `apidocs/` and `local.properties`, `.travis.yml`.
- The `ru.onlinesim.apis`, `ru.onlinesim.response.*`, `ru.onlinesim.transport`, `ru.onlinesim.exceptions` packages, `UrlParameters`, `SingleUrlParameters`, `Utils`.

### Changed

- Same coordinates as 1.x (`ru.onlinesim:onlinesim-api`) and package `ru.onlinesim`, new major version. Java 11+ (was Java 8).
- One client: `new OnlineSim(apiKey)` or `OnlineSim.builder().apiKey(..).baseUrl(..).lang(..).devId(..).timeout(Duration).transport(..).build()`; sections are accessors (`client.numbers()`, `client.rent()`, `client.user()`, `client.free()`) and are created once, not per call. Immutable, thread-safe.
- HTTP: `java.net.http.HttpClient` behind the `Transport` interface (`HttpTransport` by default; pass your own `HttpClient` for a proxy or TLS). JSON: Gson 2.15.
- The API key is sent as `Authorization: Bearer <key>`, never in the query string.
- Unchecked exceptions: `OnlineSimException` → `ApiException` (`getCode()`, `getMessage()`), `NoNumberException` (subclass of `ApiException`), `TransportException` (`getStatus()`, `getBody()`), `WaitTimeoutException`. Replace `RequestException`, `OperationException`, `NoNumberException` (old package), `BaseTypeError`.
- Responses are immutable classes with record-style accessors (`order.tzid()`, `rent.sum()`); money (`balance`, rent `sum`) is parsed to `double`.
- `numbers().waitCode(tzid, WaitOptions)` with `Duration` interval/timeout, `keepOpen`, `fullMessage`, `since`, injectable `Sleeper` and `Clock`.
- `numbers().state(StateOptions)` / `stateOne(tzid, StateOptions)` take booleans and an `Order` enum sent lowercase.
- `rent().get(country, days, extension)`: `extension` defaults to `true` and is always sent.
- `user().paymentHistory(page)`, `free().list(FreeListOptions)`; `next`/`close`/`ban`/`rent().close` return `void` (errors are exceptions).
- Build: Gradle 9.9 wrapper (needs JDK 17+ to build; the jar is compiled with `--release 11`), `java-library`, Maven Central publishing via `com.vanniktech.maven.publish` 0.37.0, Spotless with palantir-java-format; tests on JUnit 6.

### Fixed

- `price()` sent no parameters; it now sends `service` and `country`.
- `stateOne` checked the response the wrong way round; it now returns the operation or raises `ERROR_NO_OPERATIONS`.
- `NO_NUMBER` / `NO_NUMBER_FOR_FORWARD` were never mapped; they raise `NoNumberException`.
- Unknown error codes produced an empty message; the message now falls back to a code table, then to the code.
- The scheme was forced to `http` and the host was package-private; the base URL is configurable and defaults to `https://onlinesim.host/api/`.
- `dev_id=0` was always sent; `dev_id` is sent only when set.
- Booleans were sent as `true`/`false`; they are sent as `1`/`0`. Unset parameters are omitted.
- `reject` was serialized as `[]`; it is a comma list (`reject=4470,4471`).
- The read timeout was 10 minutes; requests time out after 30 s by default (configurable), as `TransportException`.
- An HTTP 456 wrong-key answer is an `ApiException`, not a transport error; non-JSON answers raise `TransportException` with the status and a body excerpt.
- `state()` returns an empty list instead of throwing when there are no operations.
- Empty maps sent by PHP as `[]` (rent `extend`, `paylist`, free `numbers`, country `services`) are returned as empty maps.
- Rent requests always send `pagination=0`, so `messages` is a flat list.

### Added

- `user().setWebhookUrl(url)`, `user().clearWebhookUrl()`, `user().webhookLogs(page)`.
- `OnlineSim.parseWebhook(json)` for inbound `receiving_sms` / `rent_sms` webhooks.
- Injectable `Transport` and per-request `timeout`.
- Offline test suite (JUnit 6, fake transport, fake clock for `waitCode`), docs examples compiled in tests, opt-in integration test, CI on JDK 17, 21 and 25.
- Documentation site (Nuxt Content).
