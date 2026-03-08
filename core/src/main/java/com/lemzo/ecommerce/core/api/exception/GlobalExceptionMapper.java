package com.lemzo.ecommerce.core.api.exception;

import com.lemzo.ecommerce.core.api.dto.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mapper global d'exceptions pour transformer toute Throwable en ErrorResponse standardisée.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        return switch (exception) {
            case ResourceNotFoundException ex -> build(Response.Status.NOT_FOUND, ex.getMessage());
            case ResourceConflictException ex -> build(Response.Status.CONFLICT, ex.getMessage());
            case BusinessRuleException ex -> build(Response.Status.BAD_REQUEST, ex.getMessage());
            case WebApplicationException ex -> {
                var status = Optional.ofNullable(Response.Status.fromStatusCode(ex.getResponse().getStatus()))
                        .orElse(Response.Status.INTERNAL_SERVER_ERROR);
                yield build(status, ex.getMessage());
            }
            default -> {
                LOGGER.log(Level.SEVERE, "Exception non gérée détectée", exception);
                yield build(Response.Status.INTERNAL_SERVER_ERROR, "Une erreur inattendue est survenue.");
            }
        };
    }

    private Response build(Response.Status status, String message) {
        var errorResponse = ErrorResponse.of(
                status.getStatusCode(),
                status.getReasonPhrase(),
                message
        );
        return Response.status(status)
                .entity(errorResponse)
                .build();
    }
}
