# onlinesim-java-api

[![Maven Central](https://img.shields.io/maven-central/v/ru.onlinesim/onlinesim-api.svg)](https://central.sonatype.com/artifact/ru.onlinesim/onlinesim-api)
[![CI](https://github.com/s00d/onlinesim-java-api/actions/workflows/ci.yml/badge.svg)](https://github.com/s00d/onlinesim-java-api/actions/workflows/ci.yml)

Java client for the [OnlineSim](https://onlinesim.io) API: SMS numbers, rent, account and free numbers.
Java 11+, `java.net.http` under the hood, Gson as the only dependency.

```groovy
implementation 'ru.onlinesim:onlinesim-api:2.1.1'
```

```java
import ru.onlinesim.Balance;
import ru.onlinesim.NumberOrder;
import ru.onlinesim.OnlineSim;

OnlineSim client = new OnlineSim(System.getenv("ONLINESIM_KEY"));

Balance b = client.user().balance();
System.out.println("available: " + (b.balance() - Math.max(b.zbalance(), 0)) + " USD");

NumberOrder order = client.numbers().getWithNumber("google", 44);
System.out.println("enter " + order.number() + " in the app");
System.out.println("code: " + client.numbers().waitCode(order.tzid()));
```

## Testing

`MockOnlineSim` is a built-in offline fake of the API: test your integration without network, key or money.
More in the [testing guide](https://s00d.github.io/onlinesim-java-api/testing).

```java
import ru.onlinesim.OnlineSim;
import ru.onlinesim.WaitOptions;
import ru.onlinesim.mock.MockOnlineSim;
import ru.onlinesim.mock.MockOptions;

public class MockExample {
    public static void main(String[] args) {
        MockOnlineSim mock = new MockOnlineSim(MockOptions.defaults().balance(10));
        OnlineSim client = mock.client();

        long tzid = client.numbers().get("google", 44);
        mock.addSmsAfter(tzid, "Your code: 123-456", 2); // arrives on the third poll
        String code = client.numbers().waitCode(tzid, WaitOptions.defaults().sleeper(d -> {}));

        if (!code.equals("123456") || mock.balance() != 9.8) throw new AssertionError(code + " " + mock.balance());
    }
}
```

Documentation: **https://s00d.github.io/onlinesim-java-api/** · Changes: [CHANGELOG.md](CHANGELOG.md) · License: Apache-2.0
