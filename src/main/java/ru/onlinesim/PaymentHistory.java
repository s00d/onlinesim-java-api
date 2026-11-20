package ru.onlinesim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Top-up orders and the available payment methods. */
public final class PaymentHistory extends Model {
    private Paginator<PaymentOrder> orders;
    /** A map by group id, or a JSON array when the ids are 0, 1, 2… */
    private JsonElement paylist;

    private List<String> domains;
    private Map<String, String> forms;
    private Map<String, Double> currency;

    private PaymentHistory() {}

    /** @return orders, newest first */
    public Paginator<PaymentOrder> orders() {
        return orders;
    }

    /**
     * @return payment methods by group id; empty when none. The server sends a JSON array when the ids are 0, 1, 2…:
     *     its items are keyed by their index then ({@code "0"}, {@code "1"}, …)
     */
    public Map<String, JsonElement> paylist() {
        Map<String, JsonElement> out = new LinkedHashMap<>();
        if (paylist != null && paylist.isJsonObject())
            paylist.getAsJsonObject().entrySet().forEach(e -> out.put(e.getKey(), e.getValue()));
        if (paylist != null && paylist.isJsonArray()) {
            JsonArray list = paylist.getAsJsonArray();
            for (int i = 0; i < list.size(); i++) out.put(String.valueOf(i), list.get(i));
        }
        return Json.map(out);
    }

    public List<String> domains() {
        return Json.list(domains);
    }

    public Map<String, String> forms() {
        return Json.map(forms);
    }

    /** @return currency → rate */
    public Map<String, Double> currency() {
        return Json.map(currency);
    }
}
