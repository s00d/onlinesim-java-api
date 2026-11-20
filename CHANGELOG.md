# Changelog

## 2.1.1

Aligned with the regenerated backend API spec.

### Fixed

- Models: `Profile.number()` is a `Long` and `Profile.country()` a `String` (they were swapped; both `null` when unset); `Operation.response()` may be `null` (absent when no number could be issued); `RentSms.code()` normalises the integer `0` of a `notify` SMS to `"0"`; nullable `FreeList` `countryText`/`countryOriginal` and `PaymentOrder` `currency`/`formText` documented.
- `user().paymentHistory().paylist()` no longer drops a paylist that the server sends as a JSON array (ids 0, 1, 2…): its items are keyed by index.
- `numbers().waitCode` retries `ApiException` `TRY_AGAIN_LATER` (HTTP 429 rate limit), `ERROR_UNDEFINED` and `INTERVAL_CONCURRENT_REQUESTS_ERROR` until its timeout instead of throwing at once.
- Error messages: `"0"` (ban not applied, the operation already has an SMS), `ACCOUNT_CLEANED`, `USER_NOT_ACTIVATED`, `ERROR_UNDEFINED`, `FORWARD_NOT_SUPPORTED` added; `ERROR_NO_KEY`, `NO_NUMBER_FOR_FORWARD`, `ACCOUNT_IDENTIFICATION_REQUIRED`, `API_DISABLED` (no longer sent) removed; `NoNumberException` only for `NO_NUMBER`; `ERROR_WRONG_KEY` reads "Wrong or missing API key".
- `MockOnlineSim` follows the backend: `setOperationRevise` answers `{"tzid":"N","response":"1"}` and `ERROR_WRONG_TZID` / `ERROR_NO_TZID`; `setOperationOk` tzid `0` → `ERROR_NO_TZID`; a missing key is HTTP 200 `ERROR_WRONG_KEY` (456 only for a wrong key, also on public endpoints); `getNum` with `number` adds `title`, `response_text`, `guard_interval_remaining_seconds`. Rent: `getRentNum` reserves the price (`status` 0) and the first non-`notify` SMS confirms and charges it (`MockRent.status()`), unknown country → `UNDEFINED_COUNTRY`, `sum` a number in getRentNum/extendRentState and a string in getRentState, `guard_interval_remaining_seconds` with `number`; `extendRentState` refusals are all `TRY_AGAIN_LATER`; `closeRentNum` unknown tzid → `ERROR_NO_OPERATIONS`, releases the reservation of an unconfirmed rent. Webhook-log params: `code` `""` without a code, rent `number` in full digits, rent `time_left` in minutes, `time_start` per type. `profile.php` accepts any `scheme://…` URL. `getPaymentHistory` adds `forms`/`buttons`/`domains`; `getFreeList` lists 44 first, clamps `count` to 10..500 (a `count` below 1 used to throw `ArithmeticException`), sends ISO-8601 `created_at` and `ignore`/`content`.
- Docs: rent reservation and `sum`, available = balance − max(zbalance, 0), `ban` answer `"0"`, `Profile.apiAccess()` may be `false`, `rent().extend` needs a confirmed rent, `Operation.response()` may be `null`, webhook `number`/`timeLeft`/`timeStart`, the `waitCode` retry policy, the mock's behaviour.

## 2.1.0

### Added

- `MockOnlineSim` (package `ru.onlinesim.mock`): a built-in, in-process fake of the OnlineSim API for testing integrations offline — no network, key, money or clock. `mock.client()` returns a normal `OnlineSim` wired to it (`mock.transport()` is the fake `Transport`). It answers every endpoint the SDK calls like the real backend (HTTP 456 for a wrong key, string/int `response`, decimal-string money, `[]` for empty maps) and keeps balance, operations and rents. Options via `MockOptions` (`apiKey`, `balance`, `prices`, `rentTariffs`, `guardInterval`). Test helpers: `addSms`, `addSmsAfter` (lets `waitCode` run with a no-op sleeper), `failNext`, `failNextTransport`, `setBalance`, `balance`, `operations`, `rents`, `requests`, `reset`. Ships in the main jar, no extra dependency. See the [testing guide](https://s00d.github.io/onlinesim-java-api/testing).
- Packaging checks in CI: a smoke test that publishes the artifact to a local repository inside `build/`, checks the POM (name, description, url, licenses, scm, developers, Gson as the only dependency), the sources/javadoc jars and class files (major 55), then resolves it from an empty Gradle project and runs the README "Testing" example on Java 11.
- `.github/workflows/release.yml`: on a `vX.Y.Z` tag runs CI, checks the tag against the version, publishes to Maven Central and creates the GitHub release from this changelog.

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
