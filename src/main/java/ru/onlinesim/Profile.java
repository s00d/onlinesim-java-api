package ru.onlinesim;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;

/** Account profile. */
public final class Profile extends Model {
    private long id;
    private String hashId;
    private String name;
    private String username;
    private String email;
    private String emailVerifiedAt;
    private String apikey;
    private JsonElement apiAccess;
    private Boolean isApi;
    private String locale;
    private String currency;
    private Integer numberCountry;
    private JsonElement numberReject;
    private Integer numberRegion;
    private String webhookUrl;
    private String number;
    private Integer country;
    private String createdAt;

    private Profile() {}

    public long id() {
        return id;
    }

    public String hashId() {
        return hashId;
    }

    public String name() {
        return name;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public String emailVerifiedAt() {
        return emailVerifiedAt;
    }

    public String apikey() {
        return apikey;
    }

    /** @return {@code true}, or a map of allowed IPs, as sent */
    public JsonElement apiAccess() {
        return apiAccess;
    }

    public Boolean isApi() {
        return isApi;
    }

    public String locale() {
        return locale;
    }

    public String currency() {
        return currency;
    }

    public Integer numberCountry() {
        return numberCountry;
    }

    /** @return number prefixes skipped by default (the server sends a list or a map) */
    public List<String> numberReject() {
        List<String> out = new ArrayList<>();
        if (numberReject != null && numberReject.isJsonArray())
            numberReject.getAsJsonArray().forEach(e -> add(out, e));
        if (numberReject != null && numberReject.isJsonObject())
            numberReject.getAsJsonObject().entrySet().forEach(e -> add(out, e.getValue()));
        return Json.list(out);
    }

    public Integer numberRegion() {
        return numberRegion;
    }

    public String webhookUrl() {
        return webhookUrl;
    }

    public String number() {
        return number;
    }

    public Integer country() {
        return country;
    }

    public String createdAt() {
        return createdAt;
    }

    private static void add(List<String> out, JsonElement e) {
        String s = Json.string(e);
        if (s != null) out.add(s);
    }
}
