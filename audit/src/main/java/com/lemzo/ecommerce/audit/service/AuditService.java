package com.lemzo.ecommerce.audit.service;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service d'audit utilisant MongoDB et les Virtual Threads.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuditService {

    private static final Logger LOGGER = Logger.getLogger(AuditService.class.getName());
    private static final String COLLECTION_NAME = "audit_logs";

    private final MongoDatabase mongoDatabase;
    private final ManagedExecutorService executor;

    private MongoCollection<Document> auditCollection;

    @PostConstruct
    public void init() {
        this.auditCollection = mongoDatabase.getCollection(COLLECTION_NAME);
    }

    public void log(final UUID userId, final String action, final ResourceType resourceType, 
                    final String resourceId, final Map<String, Object> details, 
                    final String clientIp, final String userAgent) {
        
        executor.submit(() -> {
            final Document auditDocument = new Document()
                    .append("userId", Optional.ofNullable(userId).map(UUID::toString).orElse("anonymous"))
                    .append("action", action)
                    .append("resourceType", Optional.ofNullable(resourceType).map(Enum::name).orElse("PLATFORM"))
                    .append("resourceId", resourceId)
                    .append("details", new Document(Optional.ofNullable(details).orElse(Map.of())))
                    .append("clientIp", clientIp)
                    .append("userAgent", userAgent)
                    .append("timestamp", LocalDateTime.now(ZoneId.of("UTC")).toString());

            try {
                auditCollection.insertOne(auditDocument);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(() -> "Audit sauvegardé: " + action);
                }
            } catch (final Exception exception) {
                LOGGER.log(Level.SEVERE, "Erreur audit", exception);
            }
        });
    }
}
