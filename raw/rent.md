# Rent

> Rent a number for days and receive every SMS sent to it — client.rent().

A rent keeps a number for `days` days. Its SMS come in `RentItem.messages()`, newest first.

`get` reserves the price (it shows in `zbalance`): the rent starts with `status()` `0` and `checked()` `false` and waits up to 20 minutes for its first SMS. The first SMS that is not a `notify` notice confirms it (`status()` `1`) and charges the price; without one the rent closes and the reservation is released. Some countries (`confirm` in `tariffs`, e.g. US numbers) are confirmed and charged at once.

`RentItem`: `tzid()`, `status()`, `number()`, `country()`, `sum()`, `days()`, `hours()`, `time()`, `timeSeconds()`, `extension()`, `extend()`, `messages()`, `startTime()`, `stopTime()`, `dayExtend()`, … —
`sum()` is the cost in USD incl. extensions (a `double`; the server sends a number, or a decimal string in `state`), `time()` minutes left, `timeSeconds()` seconds left,
`extension()` `0` not extendable / `1` extendable / `2` auto-extension, `extend()` maps days to the extension price (empty when not extendable),
`messages()` is `List<RentSms>`: `id()`, `service()`, `text()`, `code()`, `createdAt()` — `code()` is `""` when no code was found and `"0"` for a `notify` SMS (a prohibited sender; the server sends the integer `0`).

## get

Rent a number. The price is reserved; the first SMS charges it.

```java
RentItem get()
RentItem get(int country)
RentItem get(int country, int days)
RentItem get(int country, int days, boolean extension)
```

<table>
<thead>
  <tr>
    <th>
      Param
    </th>
    
    <th>
      Type
    </th>
    
    <th>
      Default
    </th>
    
    <th>
      Description
    </th>
  </tr>
</thead>

<tbody>
  <tr>
    <td>
      <code>
        country
      </code>
    </td>
    
    <td>
      <code>
        int
      </code>
    </td>
    
    <td>
      <code>
        7
      </code>
    </td>
    
    <td>
      Country code.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        days
      </code>
    </td>
    
    <td>
      <code>
        int
      </code>
    </td>
    
    <td>
      <code>
        1
      </code>
    </td>
    
    <td>
      One of the tariff's <code>
        days()
      </code>
      
       keys.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        extension
      </code>
    </td>
    
    <td>
      <code>
        boolean
      </code>
    </td>
    
    <td>
      <code>
        true
      </code>
    </td>
    
    <td>
      <code>
        false
      </code>
      
       makes the rent non-extendable for good.
    </td>
  </tr>
</tbody>
</table>

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

Extend a rent by `days` days and charge the balance at once. Only confirmed rents (`status()` `1`) created with `extension = true`.

```java
RentItem extend(long tzid)
RentItem extend(long tzid, int days)
```

```java
RentItem rent = client.rent().extend(tzid, 3);
System.out.println(rent.stopTime());
```

Errors: `ApiException` `TRY_AGAIN_LATER` (unknown tzid, not confirmed yet or closed, not extendable, unknown period, a repeat within 10 s), `WARNING_LOW_BALANCE`.

## close

Return the number before the period ends. A rent that is not confirmed yet (`status()` `0`) and has no SMS can be closed only after 120 s; then, or with only `notify` SMS, the reservation is released. With a real SMS the rent is confirmed (charged) and closed. A confirmed rent closes without a refund. US numbers cannot be closed early.

```java
void close(long tzid)
```

```java
client.rent().close(tzid);
```

Errors: `ApiException` `ERROR_NO_OPERATIONS` (unknown tzid), `NO_COMPLETE_TZID`, `NO_CLOSE_RENT_US`, `TRY_AGAIN_LATER`.

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
