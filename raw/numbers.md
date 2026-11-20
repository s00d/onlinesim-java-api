# Numbers

> Order a number, read its SMS, close it — client.numbers().

An operation (`tzid`) is one ordered number. Order it with `get`/`getWithNumber`, read the SMS with `stateOne` or `waitCode`, finish it with `close` (or `ban`). Countries are phone codes (`7`, `44`, …), services are slugs (`google`, `vkcom`, …) as returned by `tariffs`. Every `country` parameter defaults to `7` in the shorter overloads.

## price

Price of one number, USD; `null` when the country has no price for the service.

```java
Double price(String service)
Double price(String service, int country)
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
        service
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      —
    </td>
    
    <td>
      Service slug.
    </td>
  </tr>
  
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
</tbody>
</table>

```java
Double usd = client.numbers().price("google", 44);
```

Errors: `ApiException` `ERROR_NO_SERVICE`, `UNDEFINED_COUNTRY`.

## get

Order a number; returns the operation id. Freezes the price on the balance.

```java
long get(String service)
long get(String service, int country)
long get(String service, int country, Collection<String> reject)
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
        service
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      —
    </td>
    
    <td>
      Service slug.
    </td>
  </tr>
  
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
        reject
      </code>
    </td>
    
    <td>
      <code>
        Collection<String>
      </code>
    </td>
    
    <td>
      profile list
    </td>
    
    <td>
      Number prefixes to skip, e.g. <code>
        List.of("4470")
      </code>
      
      .
    </td>
  </tr>
</tbody>
</table>

```java
long tzid = client.numbers().get("google", 44, List.of("4470"));
```

Errors: `NoNumberException`; `ApiException` `WARNING_LOW_BALANCE`, `ERROR_NO_SERVICE`, `UNDEFINED_COUNTRY`, `EXCEEDED_CONCURRENT_OPERATIONS`, `INTERVAL_CONCURRENT_REQUESTS_ERROR`, `TRY_AGAIN_LATER`.

## getWithNumber

Same as `get`, but returns the number right away.

```java
NumberOrder getWithNumber(String service)
NumberOrder getWithNumber(String service, int country)
NumberOrder getWithNumber(String service, int country, Collection<String> reject)
```

`NumberOrder`: `tzid()`, `number()` (with `+`), `country()`, `time()` (seconds left), `service()`, `title()`, `guardIntervalRemainingSeconds()` (seconds until the operation may be closed).

```java
NumberOrder order = client.numbers().getWithNumber("google", 44);
System.out.println(order.tzid() + " " + order.number());
```

Errors: as `get`.

## state

All operations of the account; an empty list when there are none.

```java
List<Operation> state()
List<Operation> state(StateOptions options)
```

<table>
<thead>
  <tr>
    <th>
      Option
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
        messageToCode
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
        msg()
      </code>
      
       is the code only; <code>
        false
      </code>
      
       — the full SMS text.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        orderBy
      </code>
    </td>
    
    <td>
      <code>
        StateOptions.Order
      </code>
    </td>
    
    <td>
      <code>
        ASC
      </code>
    </td>
    
    <td>
      Order of operations.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        msgList
      </code>
    </td>
    
    <td>
      <code>
        boolean
      </code>
    </td>
    
    <td>
      <code>
        false
      </code>
    </td>
    
    <td>
      All SMS in <code>
        msgList()
      </code>
      
       (<code>
        List<OperationSms>
      </code>
      
      ) instead of <code>
        msg()
      </code>
      
      .
    </td>
  </tr>
</tbody>
</table>

`Operation`: `tzid()`, `response()`, `number()`, `service()`, `country()`, `sum()`, `time()`, `msg()`, `msgList()`, `guardIntervalRemainingSeconds()`, ….
`response()` is the state: `TZ_INPOOL` (number being assigned), `TZ_NUM_WAIT` (waiting for SMS), `TZ_NUM_ANSWER` (SMS arrived), `TZ_OVER_OK` (finished), `ERROR_NO_ITEMS` (failed), or `null` when the server sent no state (no number could be issued). `msg()` is `null` until the first SMS. The server caches answers for a few seconds.

```java
StateOptions newestFirst = StateOptions.defaults().orderBy(StateOptions.Order.DESC);
for (Operation op : client.numbers().state(newestFirst)) {
  System.out.println(op.tzid() + " " + op.response() + " " + op.msg());
}
```

## stateOne

One operation. Takes the same `StateOptions`; `orderBy` is ignored.

```java
Operation stateOne(long tzid)
Operation stateOne(long tzid, StateOptions options)
```

```java
Operation op = client.numbers().stateOne(tzid);
if ("TZ_NUM_ANSWER".equals(op.response())) System.out.println(op.msg()); // response() may be null
```

Errors: `ApiException` `ERROR_NO_OPERATIONS` when the operation does not exist (any more).

## waitCode

Poll the operation until a code arrives, then close it and return the code. Blocks the calling thread.

```java
String waitCode(long tzid)
String waitCode(long tzid, WaitOptions options)
```

<table>
<thead>
  <tr>
    <th>
      Option
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
        interval
      </code>
    </td>
    
    <td>
      <code>
        Duration
      </code>
    </td>
    
    <td>
      5 s
    </td>
    
    <td>
      Poll interval.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        timeout
      </code>
    </td>
    
    <td>
      <code>
        Duration
      </code>
    </td>
    
    <td>
      10 min
    </td>
    
    <td>
      Give up after.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        keepOpen
      </code>
    </td>
    
    <td>
      <code>
        boolean
      </code>
    </td>
    
    <td>
      <code>
        false
      </code>
    </td>
    
    <td>
      Call <code>
        next(tzid)
      </code>
      
       instead of <code>
        close(tzid)
      </code>
      
      , to wait for another SMS.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        fullMessage
      </code>
    </td>
    
    <td>
      <code>
        boolean
      </code>
    </td>
    
    <td>
      <code>
        false
      </code>
    </td>
    
    <td>
      Return the full SMS text instead of the code.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        since
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      <code>
        null
      </code>
    </td>
    
    <td>
      Ignore this value (the previous code).
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        sleeper
      </code>
      
      , <code>
        clock
      </code>
    </td>
    
    <td>
      <code>
        WaitOptions.Sleeper
      </code>
      
      , <code>
        Clock
      </code>
    </td>
    
    <td>
      real
    </td>
    
    <td>
      Injectable for tests.
    </td>
  </tr>
</tbody>
</table>

Transient errors are retried until the timeout: `TransportException`, and `ApiException` `TRY_AGAIN_LATER` (the HTTP 429 rate limit), `ERROR_UNDEFINED` and `INTERVAL_CONCURRENT_REQUESTS_ERROR`. Any other `ApiException` is thrown at once. An interrupt stops the wait with `OnlineSimException` and keeps the thread's interrupt flag.

```java
String first = client.numbers().waitCode(tzid, WaitOptions.defaults().keepOpen(true));
String second = client.numbers().waitCode(tzid, WaitOptions.defaults().since(first).timeout(Duration.ofMinutes(5)));
```

Errors: `WaitTimeoutException`; `ApiException` with code `TZ_OVER_OK` or `ERROR_NO_ITEMS` when the operation ended without a new code; any other `ApiException` from `stateOne`/`close`/`next`.

## next

Switch the operation to its next SMS and keep it open (for services that send several SMS).

```java
void next(long tzid)
```

```java
client.numbers().next(tzid);
```

Errors: `ApiException` `ERROR_WRONG_TZID` (not an operation of your account), `ERROR_NO_TZID`.

## close

Finish the operation. An operation without SMS can be closed only after its guard interval (`guardIntervalRemainingSeconds()`); every attempt locks the tzid for 5 s.

```java
void close(long tzid)
```

```java
client.numbers().close(tzid);
```

Errors: `ApiException` `NO_COMPLETE_TZID`, `INTERVAL_CONCURRENT_REQUESTS_ERROR`, `ERROR_WRONG_TZID`.

## ban

Finish the operation and report the number as bad. Only before any SMS arrived.

```java
void ban(long tzid)
```

```java
client.numbers().ban(tzid);
```

Errors: as `close`; `ApiException` with code `"0"` when the operation already has an SMS — nothing is changed then.

## tariffs

Services, prices and stock of every country, keyed by country code. No API key needed. The answer is large (a few MB); prefer `tariffsOne`.

```java
Map<String, CountryStat> tariffs()
```

`CountryStat`: `name()`, `localeName()`, `position()`, `code()`, `other()`, `isNew()`, `enabled()`, `services()`; `services()` maps `service_<slug>` to `ServiceStat`: `count()` (`null` when unknown), `popular()`, `code()`, `price()`, `id()`, `service()`, `slug()`.

```java
Map<String, CountryStat> all = client.numbers().tariffs();
System.out.println(all.size() + " countries");
```

## tariffsOne

Services, prices and stock of one country.

```java
CountryStat tariffsOne()
CountryStat tariffsOne(int country)
```

```java
CountryStat uk = client.numbers().tariffsOne(44);
for (ServiceStat s : uk.services().values()) System.out.println(s.slug() + " " + s.price() + " " + s.count());
```

Errors: `ApiException` `NO_COUNTRY`.
