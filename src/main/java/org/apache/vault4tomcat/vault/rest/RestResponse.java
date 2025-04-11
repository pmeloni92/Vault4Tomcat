package org.apache.vault4tomcat.vault.rest;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class contains the metadata and data that was downloaded by <code>Rest</code> from an HTTP
 * response.
 */
public record RestResponse(int status, byte[] body) implements Serializable {

    /**
     * @param status The HTTP status code issues for the response (e.g. <code>200 == OK</code>).
     * @param body   The binary payload of the response body.
     */
    public RestResponse(final int status, final byte[] body) {
        this.status = status;
        this.body = body == null ? null : Arrays.copyOf(body, body.length);
    }

    /**
     * @return The HTTP status code issues for the response (e.g. <code>200 == OK</code>).
     */
    @Override
    public int status() {
        return status;
    }

    /**
     * @return The binary payload of the response body.
     */
    @Override
    public byte[] body() {
        return Arrays.copyOf(body, body.length);
    }

}
