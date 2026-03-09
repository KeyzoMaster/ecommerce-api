package com.lemzo.ecommerce.audit.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Représentation d'un log d'audit pour MongoDB.
 */
@Data
@Builder
public class AuditLog {
    private String logId;
    private String userId;
    private String action;
    private String resourceType;
    private String resourceId;
    private Map<String, Object> details;
    private String clientIp;
    private String userAgent;
    private LocalDateTime timestamp;
}
