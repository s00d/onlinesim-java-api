package ru.onlinesim;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sends one HTTP request. The default is {@link HttpTransport} ({@code java.net.http}); pass your own to
 * {@link OnlineSim.Builder#transport(Transport)} for a proxy, a custom TLS setup, or a fake in tests.
 *
 * <p>Throw {@link java.net.http.HttpTimeoutException} (or {@link java.net.SocketTimeoutException}) when
 * {@link Request#timeout()} runs out and any other {@link IOException} for network failures: the client maps
 * both to {@link TransportException}.
 */
@FunctionalInterface
public interface Transport {
    /**
     * Sends the request and returns the response, whatever its HTTP status.
     *
     * @param request the request to send
     * @return the response
     * @throws IOException on network failure or timeout
     */
    Response send(Request request) throws IOException;

    /** An HTTP request built by the client. */
    final class Request {
        private final String method;
        private final URI uri;
        private final Map<String, String> headers;
        private final String body;
        private final Duration timeout;

        /**
         * @param method  {@code GET} or {@code POST}
         * @param uri     full URI with the query string
         * @param headers request headers
         * @param body    JSON body, or {@code null} for none
         * @param timeout per-request timeout
         */
        public Request(String method, URI uri, Map<String, String> headers, String body, Duration timeout) {
            this.method = method;
            this.uri = uri;
            this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
            this.body = body;
            this.timeout = timeout;
        }

        /** @return {@code GET} or {@code POST} */
        public String method() {
            return method;
        }

        /** @return full URI with the query string */
        public URI uri() {
            return uri;
        }

        /** @return request headers (unmodifiable) */
        public Map<String, String> headers() {
            return headers;
        }

        /** @return JSON body, or {@code null} for none */
        public String body() {
            return body;
        }

        /** @return per-request timeout */
        public Duration timeout() {
            return timeout;
        }
    }

    /** An HTTP response: status and body text. */
    final class Response {
        private final int status;
        private final String body;

        /**
         * @param status HTTP status code
         * @param body   response body as text ({@code null} is treated as empty)
         */
        public Response(int status, String body) {
            this.status = status;
            this.body = body == null ? "" : body;
        }

        /** @return HTTP status code */
        public int status() {
            return status;
        }

        /** @return response body, never {@code null} */
        public String body() {
            return body;
        }
    }
}
