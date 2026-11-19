package ru.onlinesim;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;

/** Top-up orders and the available payment methods. */
public final class PaymentHistory extends Model {
    private Paginator<PaymentOrder> orders;
    private Map<String, JsonElement> paylist;
    private List<String> domains;
    private Map<String, String> forms;
    private Map<String, Double> currency;

    private PaymentHistory() {}

    /** @return orders, newest first */
    public Paginator<PaymentOrder> orders() {
        return orders;
    }

    /** @return payment methods by list id, as sent; empty when none */
    public Map<String, JsonElement> paylist() {
        return Json.map(paylist);
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
