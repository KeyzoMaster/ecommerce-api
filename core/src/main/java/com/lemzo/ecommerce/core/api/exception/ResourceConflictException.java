package com.lemzo.ecommerce.core.api.exception;

/**
 * Exception lancée lors d'un conflit d'état de ressource.
 */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(final String message) {
        super(message);
    }
}
