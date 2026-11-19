# onlinesim-java-api

[![Maven Central](https://img.shields.io/maven-central/v/ru.onlinesim/onlinesim-api.svg)](https://central.sonatype.com/artifact/ru.onlinesim/onlinesim-api)
[![CI](https://github.com/s00d/onlinesim-java-api/actions/workflows/ci.yml/badge.svg)](https://github.com/s00d/onlinesim-java-api/actions/workflows/ci.yml)

Java client for the [OnlineSim](https://onlinesim.io) API: SMS numbers, rent, account and free numbers.
Java 11+, `java.net.http` under the hood, Gson as the only dependency.

```groovy
implementation 'ru.onlinesim:onlinesim-api:2.0.0'
```

```java
import ru.onlinesim.Balance;
import ru.onlinesim.NumberOrder;
import ru.onlinesim.OnlineSim;

OnlineSim client = new OnlineSim(System.getenv("ONLINESIM_KEY"));

Balance b = client.user().balance();
System.out.println("available: " + (b.balance() - b.zbalance()) + " USD");

NumberOrder order = client.numbers().getWithNumber("google", 44);
System.out.println("enter " + order.number() + " in the app");
System.out.println("code: " + client.numbers().waitCode(order.tzid()));
```

Documentation: **https://s00d.github.io/onlinesim-java-api/** · Changes: [CHANGELOG.md](CHANGELOG.md) · License: Apache-2.0
