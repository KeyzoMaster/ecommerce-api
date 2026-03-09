package com.lemzo.ecommerce.core.contract.storage;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import java.io.InputStream;
import java.util.UUID;

/**
 * Port pour la gestion du stockage de fichiers (S3/MinIO).
 */
public interface StoragePort {
    
    /**
     * Stocke un fichier avec partitionnement automatique.
     */
    String storePartitioned(
            InputStream content, 
            String fileName, 
            String contentType,
            UUID userId,
            ResourceType resourceType,
            UUID... resourceIds
    );

    /**
     * Récupère une URL temporaire pour accéder au fichier.
     */
    String getPresignedUrl(String path, int expiryMinutes);

    /**
     * Supprime un fichier.
     */
    void delete(String path);
}
