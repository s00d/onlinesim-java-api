package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Fake transport: answers canned replies in order and records every request. */
final class Fake implements Transport {
    final List<Request> calls = new ArrayList<>();
    private final Deque<Object> replies = new ArrayDeque<>();
    final OnlineSim client;

    private Fake(Consumer<OnlineSim.Builder> options) {
        OnlineSim.Builder b = OnlineSim.builder()
                .apiKey("test-key")
                .baseUrl("https://api.test/api/")
                .transport(this);
        options.accept(b);
        client = b.build();
    }

    static Fake client() {
        return new Fake(b -> {});
    }

    static Fake client(Consumer<OnlineSim.Builder> options) {
        return new Fake(options);
    }

    /** Queue a JSON reply with status 200. */
    Fake json(String json) {
        return reply(200, json);
    }

    Fake reply(int status, String body) {
        replies.add(new Response(status, body));
        return this;
    }

    /** Queue a transport failure. */
    Fake fail(IOException error) {
        replies.add(error);
        return this;
    }

    @Override
    public Response send(Request request) throws IOException {
        calls.add(request);
        Object reply = replies.poll();
        if (reply == null) throw new AssertionError("unexpected request " + request.uri());
        if (reply instanceof IOException) throw (IOException) reply;
        return (Response) reply;
    }

    /** The only request made. */
    Request last() {
        assertEquals(1, calls.size(), "number of requests");
        return calls.get(0);
    }

    /** Query of the only request, without the {@code lang} every request carries. */
    Map<String, String> query() {
        Map<String, String> q = queryOf(last());
        q.remove("lang");
        return q;
    }

    String path() {
        return last().uri().getPath();
    }

    static Map<String, String> queryOf(Request request) {
        Map<String, String> q = new LinkedHashMap<>();
        String raw = request.uri().getRawQuery();
        if (raw == null) return q;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            q.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return q;
    }

    static String fixture(String name) {
        try (InputStream in = Fake.class.getResourceAsStream("/fixtures/" + name + ".json")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Map<String, String> map(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i], kv[i + 1]);
        return m;
    }
}
