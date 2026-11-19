---
title: Free numbers
description: Public numbers and their SMS — client.free(). No API key needed.
navigation:
  order: 4
---

## list

Countries, the numbers of the selected country and the SMS of the selected number, in one call.

```java
FreeList list()
FreeList list(FreeListOptions options)
```

| Option | Type | Default | Description |
|---|---|---|---|
| `country` | `int` | `44` (server) | Country code; an unknown one falls back to the first available. |
| `number` | `String` | first number | Number **without** the country code, as the keys of `numbers()`. |
| `page` | `int` | `1` | Page of `messages()`. |
| `count` | `int` | server default | SMS per page, 10–500. |

Unset options are not sent. `FreeList`:

- `countries()`: `List<FreeList.Country>` — `country()`, `countryText()`, `countryOriginal()`
- `numbers()`: `Map<String, FreeList.Phone>` keyed by the short number — `country()`, `countryOriginal()`, `fullNumber()`, `dataHumans()`, `isArchive()`
- `messages()`: `FreeList.Messages`, a `Paginator<FreeList.Message>` plus `number()` and `country()`; a message has `id()`, `text()`, `inNumber()`, `myNumber()`, `code()` (`-` when no code was found), `createdAt()`, `dataHumans()`

```java
FreeList uk = client.free().list(FreeListOptions.defaults().country(44));
Optional<String> first = uk.numbers().keySet().stream().findFirst();
if (first.isPresent()) {
  FreeList page = client.free().list(FreeListOptions.defaults().country(44).number(first.get()).count(20));
  for (FreeList.Message sms : page.messages().data()) {
    System.out.println(sms.inNumber() + " " + sms.code() + " " + sms.text());
  }
}
```
