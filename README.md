# SMSActivateApi

## Начало работы 

Включите библиотеку в maven.

```xml
<dependecy>
  <groupId>ru.onlinesim</groupId>
  <artifactId>OnlineSimApi</artifactId>
  <version>1.0.90</version>
</dependecy>
```

Импортируйте главный класс для взаимодействия с API OnlineSIM.
```java
import ru.onlinesim.OnlineSimApi;
```

Для использования библитеки вам необходим API ключ, его можно получить по ссылкам ниже.

* [OnlineSIM API docs](https://onlinesim.ru/docs/api/ru)

* [OnlineSIM API-Key](https://onlinesim.ru/v2/pages/profile)

<hr/>

### Получение баланса на вашем аккаунте

Для получение текущего баланса на вашем аккаунте используйте метод [**getBalance**](https://github.com/s00d/onlinesim-java-api/wiki/GetUser#balance--docs-ru-docs-en).

`Пример`
```java
import ru.onlinesim.OnlineSimApi;
import ru.onlinesim.error.base.OnlineSimApiBaseException;

import java.math.BigDecimal;

public class Run {
  public static void main(String[] args) {
    try {
      OnlineSimApi onlineSimApi = new OnlineSimApi("API_KEY");

      System.out.println("Your api-key: " + onlineSimApi.getApiKey());

      // request balance
      BigDecimal balance = onlineSimApi.getBalance();

      // print info about score
      System.out.println("Balance: " + balance);
    } catch (OnlineSimApiBaseException e) {
      System.out.println(e.getMessage());
    }
  }
}
```

### *Описание остальных методов находится в wiki [github](https://github.com/s00d/onlinesim-java-api/wiki/).*
