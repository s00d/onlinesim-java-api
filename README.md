# Onlinesim JAVA API

Wrapper for automatic reception of SMS-messages by onlinesim.ru

## Installation


Require this package:

[Maven](https://mvnrepository.com/artifact/ru.onlinesim/onlinesim-api/latest)

```xml
<dependency>
  <groupId>ru.onlinesim</groupId>
  <artifactId>onlinesim-api</artifactId>
  <version>1.0.3</version>
</dependency>
```

[Gradle](https://plugins.gradle.org/plugin/ru.onlinesim.OnlineSimApi)

Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):

```gradle
plugins {
  id "ru.onlinesim.OnlineSimApi" version "1.0.3"
}
```

Using [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application):

```gradle
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "ru.onlinesim:onlinesim-api:1.0.3"
  }
}

apply plugin: "ru.onlinesim.OnlineSimApi"
```

Require this package in your APP:

```java
import ru.onlinesim.OnlineSimApi;
```

## Documentation

All documentation is in the wiki of this project - **[Documentation](https://github.com/s00d/onlinesim-java-api/wiki)**

## Responses

All responses from the server are described in the folder Responses

## Bugs

If you have any problems, please create Issues [here](https://github.com/s00d/onlinesim-java-api/issues)

<hr/>

### Example
`
```java
import ru.onlinesim.OnlineSimApi;
import ru.onlinesim.error.base.OnlineSimApiBaseException;

public class Run {
  public static void main(String[] args) {
    try {
	OnlineSimApi loader = new OnlineSimApi(APIKEY);
	GetUser user = loader.user();
	Balance balance = user.balance();
	// print info about score
	System.out.println(balance.toString());
    } catch (BaseException e) {
	System.out.println(e.getMessage());
    }
  }
}
```
