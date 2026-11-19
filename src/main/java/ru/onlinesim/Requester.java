package ru.onlinesim;

import com.google.gson.JsonElement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Handle to the client's single request function, given to every section. */
@FunctionalInterface
interface Requester {
    /**
     * @param endpoint relative to the base, suffix included ({@code getNum.php})
     * @param query    parameters; null/empty values are omitted
     * @param body     JSON body (switches to POST), or {@code null}
     * @return the payload: the object without {@code response}, or the bare JSON value
     */
    JsonElement request(String endpoint, Map<String, ?> query, JsonElement body);

    default JsonElement get(String endpoint, Object... keyValues) {
        return request(endpoint, query(keyValues), null);
    }

    /** Ordered map from key/value pairs; values may be {@code null}. */
    static Map<String, Object> query(Object... keyValues) {
        if (keyValues.length == 0) return Collections.emptyMap();
        Map<String, Object> q = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) q.put((String) keyValues[i], keyValues[i + 1]);
        return q;
    }
}
