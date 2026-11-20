# Webhooks

> Receive SMS as HTTP callbacks and parse them with OnlineSim.parseWebhook.

Set a URL with [`client.user().setWebhookUrl`](/user#setwebhookurl). For each SMS the server POSTs a JSON body to it: `webhook_type` `receiving_sms` for numbers, `rent_sms` for rents. Delivery attempts are listed by [`client.user().webhookLogs`](/user#webhooklogs).

## parseWebhook

Parse an inbound webhook body. A static method: no client needed.

```java
static WebhookPayload parseWebhook(String json)
```

Lenient: numeric fields may arrive as strings, missing or malformed fields become `null`, unknown fields are dropped. It throws `OnlineSimException` only when the input is not a JSON object.

```java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import ru.onlinesim.OnlineSim;
import ru.onlinesim.WebhookPayload;

public class WebhookServer {
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/onlinesim-hook", exchange -> {
      String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      WebhookPayload hook = OnlineSim.parseWebhook(body);
      if ("receiving_sms".equals(hook.webhookType())) System.out.println(hook.operationId() + " " + hook.code());
      exchange.sendResponseHeaders(200, -1);
      exchange.close();
    });
    server.start();
  }
}
```

## Payload fields

<table>
<thead>
  <tr>
    <th>
      Accessor
    </th>
    
    <th>
      Type
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
        webhookType()
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      <code>
        receiving_sms
      </code>
      
      , <code>
        rent_sms
      </code>
      
      , or <code>
        null
      </code>
      
       for anything else.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        operationId()
      </code>
    </td>
    
    <td>
      <code>
        Long
      </code>
    </td>
    
    <td>
      <code>
        tzid
      </code>
      
       of the operation or rent.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        userId()
      </code>
    </td>
    
    <td>
      <code>
        Long
      </code>
    </td>
    
    <td>
      Account id.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        countryCode()
      </code>
    </td>
    
    <td>
      <code>
        Integer
      </code>
    </td>
    
    <td>
      Country code of the number.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        number()
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      Numbers: <code>
        +
      </code>
      
       and the full number (<code>
        +79001234567
      </code>
      
      ), or <code>
        hidden
      </code>
      
      . Rent: the full number in digits without <code>
        +
      </code>
      
       (<code>
        79001234567
      </code>
      
      ).
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        sender()
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      Sender of the SMS, e.g. <code>
        VK
      </code>
      
      .
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        message()
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      Full SMS text.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        code()
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      Code extracted from the SMS.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        timeStart()
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      Numbers: when the operation was ordered. Rent: when the SMS arrived. <code>
        YYYY-MM-DD HH:MM:SS
      </code>
      
      .
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        timeLeft()
      </code>
    </td>
    
    <td>
      <code>
        Long
      </code>
    </td>
    
    <td>
      Time left: seconds for numbers, minutes for rent.
    </td>
  </tr>
</tbody>
</table>

Every accessor returns `null` when the field is missing or malformed.
