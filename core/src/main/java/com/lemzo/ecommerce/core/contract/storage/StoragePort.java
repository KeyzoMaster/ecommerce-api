package com.lemzo.ecommerce.core.contract.storage;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import java.io.InputStream;
import java.util.UUID;

/**
 * Port pour l'abstraction du stockage de fichiers (S3, MinIO, etc.).
 */
public interface StoragePort {

    /**
     * Stocke un fichier de manière partitionnée en respectant la hiérarchie.
     * Exemple : users/{userId}/stores/{storeId}/products/{productId}/image.png
     */
    String storePartitioned(InputStream content, String fileName, String contentType, 
                            UUID userId, ResourceType type, UUID... resourceIds);

    /**
     * Stocke un fichier et retourne son chemin d'accès relatif.
     */
    String store(InputStream content, String fileName, String contentType, long size);

    /**
     * Stocke un flux de taille inconnue (streaming).
     */
    default String store(InputStream content, String fileName, String contentType) {
        return store(content, fileName, contentType, -1L);
    }

    /**
     * Récupère un fichier sous forme de flux.
     */
    InputStream fetch(String path);

    /**
     * Supprime un fichier.
     */
    void delete(String path);

    /**
     * Récupère la taille du fichier en octets.
     */
    long getFileSize(String path);

    /**
     * Génère une URL temporaire de consultation.
     */
    String getPresignedUrl(String path, int expiryMinutes);
}
