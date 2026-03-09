package com.lemzo.ecommerce.core.api.exception;

/**
 * Exception lancée lorsqu'une ressource demandée n'existe pas.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(final String message) {
        super(message);
    }

    public ResourceNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
