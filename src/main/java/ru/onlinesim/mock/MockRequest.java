package ru.onlinesim.mock;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** One request the mock received. Immutable. */
public final class MockRequest {
    private final String method;
    private final String path;
    private final Map<String, String> query;
    private final String body;

    MockRequest(String method, String path, Map<String, String> query, String body) {
        this.method = method;
        this.path = path;
        this.query = Collections.unmodifiableMap(new LinkedHashMap<>(query));
        this.body = body;
    }

    /** @return {@code GET} or {@code POST} */
    public String method() {
        return method;
    }

    /** @return path relative to {@code /api/}, e.g. {@code getNum.php}, {@code rent/getRentNum.php}, {@code getFreeList} */
    public String path() {
        return path;
    }

    /** @return decoded query parameters in the order sent (unmodifiable) */
    public Map<String, String> query() {
        return query;
    }

    /** @return JSON body of a POST as sent, {@code null} for a GET */
    public String body() {
        return body;
    }

    @Override
    public String toString() {
        return method + " " + path + " " + query + (body == null ? "" : " " + body);
    }
}
