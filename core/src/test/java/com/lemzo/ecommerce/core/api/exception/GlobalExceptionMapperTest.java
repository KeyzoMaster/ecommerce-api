package com.lemzo.ecommerce.core.api.exception;

import com.lemzo.ecommerce.core.api.dto.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionMapper Unit Tests (JUnit 6)")
class GlobalExceptionMapperTest {

    private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

    @Test
    @DisplayName("Should map ResourceNotFoundException to 404")
    void shouldMapResourceNotFound() {
        var ex = new ResourceNotFoundException("Not found");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        var entity = (ErrorResponse) response.getEntity();
        assertEquals("Not found", entity.message());
    }

    @Test
    @DisplayName("Should map BusinessRuleException to 400")
    void shouldMapBusinessRuleException() {
        var ex = new BusinessRuleException("error.rule");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @DisplayName("Should map WebApplicationException to its specific status")
    void shouldMapWebException() {
        var ex = new WebApplicationException("Forbidden", Response.Status.FORBIDDEN);
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    @DisplayName("Should map unknown exception to 500")
    void shouldMapUnknownException() {
        var ex = new RuntimeException("Crash");
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
}
