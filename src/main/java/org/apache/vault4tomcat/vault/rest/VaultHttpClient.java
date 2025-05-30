package org.apache.vault4tomcat.vault.rest;

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
public class VaultHttpClient {

    private final Map<String, String> headers = new TreeMap<>();
    private String url;
    private final HttpClient client;
    private int connectTimeoutSeconds;
    private int readTimeoutSeconds;

    public VaultHttpClient() {
        this(10, 30);
    }

    public VaultHttpClient(int connectTimeoutSeconds, int readTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
        this.readTimeoutSeconds = readTimeoutSeconds;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
    }

    /**
     * Sets the full URL to be used for the request.
     *
     * @param url the target URL
     * @return this Rest instance for chaining
     */
    public VaultHttpClient url(String url) {
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
    public VaultHttpClient header(final String name, final String value) {
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
    public VaultHttpClient connectTimeoutSeconds(int seconds) {
        this.connectTimeoutSeconds = seconds;
        return this;
    }

    /**
     * Sets the read timeout.
     *
     * @param seconds timeout in seconds
     * @return this Rest instance
     */
    public VaultHttpClient readTimeoutSeconds(int seconds) {
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

    public RestResponse post(String body) throws RestException {
        try {
            HttpResponse<String> response;

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .timeout(Duration.ofSeconds(readTimeoutSeconds))
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

            headers.forEach(builder::header);
            builder.header("Content-Type", "application/json");

            HttpRequest request = builder.build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());


            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return new RestResponse(response.statusCode(), response.body().getBytes(StandardCharsets.UTF_8));
            } else {
                throw new RestException("HTTP POST failed with status code: " + statusCode + " - " + response.body());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestException("Thread was interrupted during POST request", e);
        } catch (IOException e) {
            throw new RestException("I/O error during POST request to Vault", e);
        } catch (Exception e) {
            throw new RestException("Unexpected error during POST request to Vault", e);
        }
    }


//    public abstract RestResponse get(String url, Map<String, String> headers);
//
//    public abstract RestResponse post(String url, String body, Map<String, String> headers);
}
