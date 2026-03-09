package com.lemzo.ecommerce.core.api.exception;

import com.lemzo.ecommerce.core.api.dto.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests unitaires pour GlobalExceptionMapper.
 */
@DisplayName("GlobalExceptionMapper Unit Tests")
class GlobalExceptionMapperTest {

    private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

    @Test
    @DisplayName("Should map ResourceNotFoundException to 404")
    void shouldMapResourceNotFound() {
        final var exception = new ResourceNotFoundException("Not found");
        final var response = mapper.toResponse(exception);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        
        Optional.ofNullable(response.getEntity())
                .map(ErrorResponse.class::cast)
                .ifPresent(entity -> assertEquals("Not found", entity.message()));
    }

    @Test
    @DisplayName("Should map BusinessRuleException to 400")
    void shouldMapBusinessRuleException() {
        final var exception = new BusinessRuleException("error.rule");
        final var response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @DisplayName("Should map WebApplicationException to its specific status")
    void shouldMapWebException() {
        final var exception = new WebApplicationException("Forbidden", Response.Status.FORBIDDEN);
        final var response = mapper.toResponse(exception);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    @DisplayName("Should map unknown exception to 500")
    void shouldMapUnknownException() {
        final var exception = new RuntimeException("Crash");
        final var response = mapper.toResponse(exception);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
}
