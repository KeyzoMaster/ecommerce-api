package com.lemzo.ecommerce.audit.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service d'audit utilisant MongoDB et les Threads Virtuels (Jakarta EE 11).
 */
@ApplicationScoped
public class AuditService {

    private static final Logger LOGGER = Logger.getLogger(AuditService.class.getName());
    private static final String COLLECTION_NAME = "audit_logs";

    @Inject
    private MongoDatabase mongoDatabase;

    /**
     * Utilise le ManagedExecutorService standard du serveur.
     * Dans Jakarta EE 11 / GlassFish 8, ce pool peut être configuré pour utiliser les Virtual Threads (Project Loom).
     */
    @Inject
    private ManagedExecutorService executor;

    private MongoCollection<Document> collection;

    @PostConstruct
    public void init() {
        this.collection = mongoDatabase.getCollection(COLLECTION_NAME);
    }

    /**
     * Enregistre un log d'audit de manière asynchrone sur un Thread Virtuel.
     */
    public void log(UUID userId, String action, String resourceType, String resourceId, 
                    String details, String clientIp, String userAgent) {
        
        executor.submit(() -> {
            Document doc = new Document()
                    .append("userId", userId != null ? userId.toString() : null)
                    .append("action", action)
                    .append("resourceType", resourceType)
                    .append("resourceId", resourceId)
                    .append("details", details)
                    .append("clientIp", clientIp)
                    .append("userAgent", userAgent)
                    .append("timestamp", LocalDateTime.now(ZoneId.of("UTC")).toString());

            try {
                collection.insertOne(doc);
                LOGGER.fine("Audit log sauvegardé via Virtual Thread: " + action);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Échec de sauvegarde asynchrone du log d'audit", e);
            }
        });
    }
}
