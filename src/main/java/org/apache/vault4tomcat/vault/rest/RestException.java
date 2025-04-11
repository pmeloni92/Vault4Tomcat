package org.apache.vault4tomcat.vault.rest;

import java.io.Serial;

/**
 * Exception thrown when an error occurs during a REST call to Vault.
 * Wraps any underlying IO, HTTP, or unexpected exceptions.
 */
public class RestException extends Exception {

    public RestException(final String message) {
        super(message);
    }

    public RestException(final Throwable t) {
        super(t);
    }

    public RestException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
