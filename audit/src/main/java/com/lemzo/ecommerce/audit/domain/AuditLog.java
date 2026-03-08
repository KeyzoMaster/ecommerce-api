package com.lemzo.ecommerce.audit.domain;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modèle de log d'audit pour MongoDB.
 */
@Builder
public record AuditLog(
        String id,
        UUID userId,
        String action,
        String resourceType,
        String resourceId,
        String details,
        String clientIp,
        String userAgent,
        LocalDateTime timestamp
) {
}
