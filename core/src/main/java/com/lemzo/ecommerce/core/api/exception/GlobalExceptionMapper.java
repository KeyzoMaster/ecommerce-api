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
 * Mapper global d'exceptions.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(final Throwable throwable) {
        return switch (throwable) {
            case ResourceNotFoundException _ -> build(Response.Status.NOT_FOUND, throwable.getMessage());
            case ResourceConflictException _ -> build(Response.Status.CONFLICT, throwable.getMessage());
            case BusinessRuleException _ -> build(Response.Status.BAD_REQUEST, throwable.getMessage());
            case WebApplicationException exception -> {
                final Response.Status status = Optional.ofNullable(Response.Status.fromStatusCode(exception.getResponse().getStatus()))
                        .orElse(Response.Status.INTERNAL_SERVER_ERROR);
                yield build(status, exception.getMessage());
            }
            default -> {
                LOGGER.log(Level.SEVERE, "Exception non gérée détectée", throwable);
                yield build(Response.Status.INTERNAL_SERVER_ERROR, "Une erreur inattendue est survenue.");
            }
        };
    }

    private Response build(final Response.Status status, final String message) {
        final ErrorResponse errorResponse = ErrorResponse.create(
                status.getStatusCode(),
                status.getReasonPhrase(),
                message
        );
        return Response.status(status)
                .entity(errorResponse)
                .build();
    }
}
