---
title: Rent
description: Rent a number for days and receive every SMS sent to it — client.rent().
navigation:
  order: 2
---

A rent keeps a number for `days` days. Its SMS come in `RentItem.messages()`, newest first.

`RentItem`: `tzid()`, `status()`, `number()`, `country()`, `sum()`, `days()`, `hours()`, `time()`, `timeSeconds()`, `extension()`, `extend()`, `messages()`, `startTime()`, `stopTime()`, `dayExtend()`, … —
`sum()` is the price paid in USD (a `double`; the server sends a decimal string), `time()` minutes left, `timeSeconds()` seconds left,
`extension()` `0` not extendable / `1` extendable / `2` auto-extension, `extend()` maps days to the extension price (empty when not extendable),
`messages()` is `List<RentSms>`: `id()`, `service()`, `text()`, `code()`, `createdAt()`.

## get

Rent a number and charge the balance.

```java
RentItem get()
RentItem get(int country)
RentItem get(int country, int days)
RentItem get(int country, int days, boolean extension)
```

| Param | Type | Default | Description |
|---|---|---|---|
| `country` | `int` | `7` | Country code. |
| `days` | `int` | `1` | One of the tariff's `days()` keys. |
| `extension` | `boolean` | `true` | `false` makes the rent non-extendable for good. |

```java
RentItem rent = client.rent().get(7, 7);
System.out.println(rent.tzid() + " " + rent.number() + " " + rent.sum());
```

Errors: `NoNumberException`; `ApiException` `UNDEFINED_DAYS`, `UNDEFINED_COUNTRY`, `WARNING_LOW_BALANCE`, `INTERVAL_CONCURRENT_REQUESTS_ERROR`, `TRY_AGAIN_LATER`.

## state

All rents of the account.

```java
List<RentItem> state()
```

```java
for (RentItem rent : client.rent().state()) {
  System.out.println(rent.number() + " " + (rent.messages().isEmpty() ? "-" : rent.messages().get(0).code()));
}
```

## stateOne

One rent.

```java
RentItem stateOne(long tzid)
```

```java
List<RentSms> messages = client.rent().stateOne(tzid).messages();
```

Errors: `ApiException` `ERROR_NO_OPERATIONS` for an unknown tzid.

## extend

Extend a rent by `days` days and charge the balance. Only rents created with `extension = true`.

```java
RentItem extend(long tzid)
RentItem extend(long tzid, int days)
```

```java
RentItem rent = client.rent().extend(tzid, 3);
System.out.println(rent.stopTime());
```

Errors: `ApiException` `TRY_AGAIN_LATER` (not extendable, unknown tzid or period, a repeat within 10 s), `WARNING_LOW_BALANCE`.

## close

Return the number before the period ends. A rent that is not confirmed yet and has no SMS can be closed only after 120 s; US numbers cannot be closed early.

```java
void close(long tzid)
```

```java
client.rent().close(tzid);
```

Errors: `ApiException` `ERROR_NO_OPERATIONS`, `NO_COMPLETE_TZID`, `NO_CLOSE_RENT_US`, `TRY_AGAIN_LATER`.

## tariffs

Rent prices of every country, keyed by country code. No API key needed.

```java
Map<String, RentTariff> tariffs()
```

`RentTariff`: `code()`, `name()`, `enabled()`, `isNew()`, `position()`, `extend()`, `confirm()`, `days()`, `count()`, `extCounts()` — `days()` maps rent days to the price in USD, `count()` to the numbers available.

```java
Map<String, RentTariff> tariffs = client.rent().tariffs();
```

## tariffsOne

Rent prices of one country.

```java
RentTariff tariffsOne()
RentTariff tariffsOne(int country)
```

```java
Map<String, Double> days = client.rent().tariffsOne(7).days();
System.out.println(days.get("7")); // price of 7 days
```

Errors: `ApiException` `NO_COUNTRY` for a country without rent.
