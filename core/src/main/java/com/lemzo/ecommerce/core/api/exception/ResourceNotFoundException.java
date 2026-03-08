package com.lemzo.ecommerce.core.api.exception;

import jakarta.ws.rs.core.Response;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
