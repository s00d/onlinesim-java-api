# Free numbers

> Public numbers and their SMS — client.free(). No API key needed.

## list

Countries, the numbers of the selected country and the SMS of the selected number, in one call.

```java
FreeList list()
FreeList list(FreeListOptions options)
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
        44
      </code>
      
       (server)
    </td>
    
    <td>
      Country code; an unknown one falls back to the first available.
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        number
      </code>
    </td>
    
    <td>
      <code>
        String
      </code>
    </td>
    
    <td>
      first number
    </td>
    
    <td>
      Number <strong>
        without
      </strong>
      
       the country code, as the keys of <code>
        numbers()
      </code>
      
      .
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        page
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
      Page of <code>
        messages()
      </code>
      
      .
    </td>
  </tr>
  
  <tr>
    <td>
      <code>
        count
      </code>
    </td>
    
    <td>
      <code>
        int
      </code>
    </td>
    
    <td>
      server default
    </td>
    
    <td>
      SMS per page, 10–500.
    </td>
  </tr>
</tbody>
</table>

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
