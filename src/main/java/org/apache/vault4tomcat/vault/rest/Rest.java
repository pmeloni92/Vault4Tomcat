package org.apache.vault4tomcat.vault.rest;

import org.apache.vault4tomcat.vault.json.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

/**
 * Minimal REST client for Vault4Tomcat.
 * Supports GET requests with configurable headers and timeouts.
 */
public class Rest {

    private final Map<String, String> headers = new TreeMap<>();
    private String url;
    private int connectTimeoutSeconds = 10;
    private int readTimeoutSeconds = 30;

    /**
     * Sets the full URL to be used for the request.
     *
     * @param url the target URL
     * @return this Rest instance for chaining
     */
    public Rest url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Adds a header to the request.
     *
     * @param name  the header name
     * @param value the header value
     * @return this Rest instance for chaining
     */
    public Rest header(final String name, final String value) {
        if (value != null && !value.isEmpty()) {
            this.headers.put(name, value);
        }
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * @param seconds timeout in seconds
     * @return this Rest instance
     */
    public Rest connectTimeoutSeconds(int seconds) {
        this.connectTimeoutSeconds = seconds;
        return this;
    }

    /**
     * Sets the read timeout.
     *
     * @param seconds timeout in seconds
     * @return this Rest instance
     */
    public Rest readTimeoutSeconds(int seconds) {
        this.readTimeoutSeconds = seconds;
        return this;
    }


    /**
     * Executes the GET request and returns the parsed JSON as a Map.
     *
     * @return the parsed response body
     * @throws RestException if the request fails
     */
    public RestResponse get() throws RestException {
        try {
            HttpResponse<String> response;
            try (HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                    .build()) {

                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .timeout(Duration.ofSeconds(readTimeoutSeconds))
                        .GET();

                headers.forEach(builder::header);

                HttpRequest request = builder.build();
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return new RestResponse(response.statusCode(), response.body().getBytes(StandardCharsets.UTF_8));
            } else {
                throw new RestException("HTTP GET failed with status code: " + statusCode + " - " + response.body());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestException("Thread was interrupted during GET request", e);
        } catch (IOException e) {
            throw new RestException("I/O error during GET request to Vault", e);
        } catch (Exception e) {
            throw new RestException("Unexpected error during GET request to Vault", e);
        }
    }


}
