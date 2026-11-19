package ru.onlinesim;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Map;

/** Account: {@code client.user()}. */
public final class UserApi {
    private final Requester request;

    UserApi(Requester request) {
        this.request = request;
    }

    /** @return balance, USD; {@code balance} includes the frozen {@code zbalance} */
    public Balance balance() {
        return Json.convert(request.get("getBalance.php", "income", 1), Balance.class);
    }

    /** @return the account profile */
    public Profile profile() {
        return Json.convert(request.get("getProfile.php").getAsJsonObject().get("profile"), Profile.class);
    }

    /** @return the first page of top-up orders and the payment methods */
    public PaymentHistory paymentHistory() {
        return paymentHistory(1);
    }

    /**
     * Top-up orders (paginated, newest first) and the available payment methods.
     *
     * @param page page number
     * @return the history
     */
    public PaymentHistory paymentHistory(int page) {
        return Json.convert(request.get("getPaymentHistory.php", "page", page), PaymentHistory.class);
    }

    /**
     * Sets the URL that receives SMS webhooks.
     *
     * @param url the webhook URL
     * @throws ApiException {@code VALIDATION_ERROR} for an invalid URL
     */
    public void setWebhookUrl(String url) {
        postProfile(new JsonPrimitive(url));
    }

    /** Removes the webhook URL (also from operations in progress). */
    public void clearWebhookUrl() {
        postProfile(JsonNull.INSTANCE);
    }

    /** @return the first page of the webhook delivery log */
    public Paginator<WebhookLog> webhookLogs() {
        return webhookLogs(1);
    }

    /**
     * Webhook delivery log, newest first, 10 per page.
     *
     * @param page page number
     * @return the page
     */
    public Paginator<WebhookLog> webhookLogs(int page) {
        JsonElement data =
                request.get("webhook-logs", "page", page).getAsJsonObject().get("data");
        return Json.convert(
                data,
                com.google.gson.reflect.TypeToken.getParameterized(Paginator.class, WebhookLog.class)
                        .getType());
    }

    private void postProfile(JsonElement webhookUrl) {
        JsonObject profile = new JsonObject();
        profile.add("webhook_url", webhookUrl);
        JsonObject body = new JsonObject();
        body.add("profile", profile);
        request.request("profile.php", Map.of(), body);
    }
}
