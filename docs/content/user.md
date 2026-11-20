---
title: User
description: Balance, profile, payments and the webhook URL — client.user().
navigation:
  order: 3
---

## balance

Balance in USD.

```java
Balance balance()
```

`Balance`: `balance()` includes the sum frozen by numbers and unconfirmed rents in progress (`zbalance()`, which may be negative): available = `balance() − max(zbalance(), 0)`. `income()` is the partner income (`null` when not sent). The server caches the answer for 60 s.

```java
Balance b = client.user().balance();
double available = b.balance() - Math.max(b.zbalance(), 0);
```

Errors: `ApiException` `ERROR_WRONG_KEY`, `ACCOUNT_BLOCKED`, `API_ACCESS_DISABLED`, `API_ACCESS_IP`.

## profile

The account profile.

```java
Profile profile()
```

`Profile`: `id()`, `name()`, `username()`, `email()`, `locale()`, `currency()`, `numberCountry()`, `numberReject()` (`List<String>`), `webhookUrl()`, `apiAccess()` (`true`, `false` when API access is disabled in the profile, or a map of allowed IPs, as a Gson `JsonElement`), `number()` (`Long`, `null` when no phone is linked), `country()` (`String` or `null`), ….

```java
Profile p = client.user().profile();
System.out.println(p.email() + " " + p.webhookUrl());
```

## paymentHistory

Top-up orders (paginated, newest first) and the available payment methods.

```java
PaymentHistory paymentHistory()
PaymentHistory paymentHistory(int page)
```

`PaymentHistory`: `orders()` (`Paginator<PaymentOrder>`), `paylist()`, `domains()`, `forms()`, `currency()`. `PaymentOrder.sum()` stays a decimal string (`"89.00"`); `currency()` and `formText()` may be `null`. `paylist()` maps a group id to its payment methods; when the ids are 0, 1, 2… the server sends a JSON array, whose items are keyed `"0"`, `"1"`, ….
`Paginator<T>`: `data()`, `currentPage()`, `lastPage()`, `perPage()`, `total()`, `from()`, `to()`, `nextPageUrl()`, ….

```java
Paginator<PaymentOrder> orders = client.user().paymentHistory().orders();
System.out.println(orders.total() + " orders, page " + orders.currentPage() + "/" + orders.lastPage());
```

## setWebhookUrl

Set the URL that receives SMS webhooks (see [Webhooks](/webhooks)).

```java
void setWebhookUrl(String url)
```

```java
client.user().setWebhookUrl("https://example.com/onlinesim-hook");
```

Errors: `ApiException` `VALIDATION_ERROR` for an invalid URL; `getMessage()` holds the server's field messages.

## clearWebhookUrl

Remove the webhook URL, also from operations in progress.

```java
void clearWebhookUrl()
```

```java
client.user().clearWebhookUrl();
```

## webhookLogs

Webhook delivery log, newest first, 10 per page.

```java
Paginator<WebhookLog> webhookLogs()
Paginator<WebhookLog> webhookLogs(int page)
```

`WebhookLog`: `id()`, `userId()`, `webhookUrl()`, `type()`, `params()`, `status()`, `error()`, `createdAt()`, `updatedAt()` — `type()` is `receiving_sms` or `rent_sms`, `status()` is `success`, `fail` or `pending`, `params()` the payload that was sent (`WebhookPayload`).

```java
for (WebhookLog log : client.user().webhookLogs().data()) {
  System.out.println(log.createdAt() + " " + log.status() + " " + log.error());
}
```
