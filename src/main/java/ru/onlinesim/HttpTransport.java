package ru.onlinesim;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/** Default {@link Transport} on {@link java.net.http.HttpClient}. */
public final class HttpTransport implements Transport {
    private final HttpClient http;

    /** Transport on a new {@link HttpClient} that follows redirects. */
    public HttpTransport() {
        this(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build());
    }

    /**
     * Transport on your own client: a proxy, an executor, a custom {@link javax.net.ssl.SSLContext}.
     *
     * @param http the client to send requests with
     */
    public HttpTransport(HttpClient http) {
        this.http = http;
    }

    @Override
    public Response send(Request request) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri()).timeout(request.timeout());
        request.headers().forEach(builder::header);
        builder.method(
                request.method(),
                request.body() == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(request.body(), StandardCharsets.UTF_8));
        try {
            HttpResponse<String> response =
                    http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Response(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            InterruptedIOException io = new InterruptedIOException("interrupted");
            io.initCause(e);
            throw io;
        }
    }
}
