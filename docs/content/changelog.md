---
title: Changelog
description: Release notes of onlinesim-java-api.
navigation:
  order: 7
---

The full history is in [CHANGELOG.md](https://github.com/s00d/onlinesim-java-api/blob/main/CHANGELOG.md).

## 2.1.0

- New: `MockOnlineSim` (`ru.onlinesim.mock`), an offline fake of the API for your tests — see [Testing](/testing).
- Release automation: tags `vX.Y.Z` publish to Maven Central from CI.

## 2.0.0

A rewrite with a new API; v1 code does not compile unchanged.

- Same coordinates as 1.x, new major version: `ru.onlinesim:onlinesim-api:2.0.0`. Java 11+.
- One client, `new OnlineSim(apiKey)` or `OnlineSim.builder()`, with the sections as accessors: `client.numbers()`, `client.rent()`, `client.user()`, `client.free()`.
- HTTP through `java.net.http.HttpClient` behind a `Transport` interface; the only dependency is Gson. No Apache HttpClient, dotenv or Consul.
- The key is sent as a Bearer header, never in the URL; the base URL is configurable and HTTPS by default.
- Unchecked exceptions: `ApiException`, `NoNumberException`, `TransportException`, `WaitTimeoutException`.
- `numbers().waitCode` with `Duration` interval and timeout, `keepOpen`, `since`; retries network errors.
- `rent().get` creates extendable rents by default (`extension = true`).
- New: `user().setWebhookUrl` / `clearWebhookUrl` / `webhookLogs`, `OnlineSim.parseWebhook`.
- Removed: the proxy API, `numbers.repeat`, `numbers.service`, `numbers.serviceNumber`, `rent.portReload`, `user.createEmpty`, the old free-number endpoints (use `free().list`).
