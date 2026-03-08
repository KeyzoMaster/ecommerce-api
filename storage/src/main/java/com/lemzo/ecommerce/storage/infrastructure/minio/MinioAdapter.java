package com.lemzo.ecommerce.storage.infrastructure.minio;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.storage.StoragePort;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Adaptateur MinIO pour le stockage des fichiers avec support du partitionnement hiérarchique.
 */
@ApplicationScoped
public class MinioAdapter implements StoragePort {

    private static final Logger LOGGER = Logger.getLogger(MinioAdapter.class.getName());
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    @Inject
    private MinioClient minioClient;

    @ConfigProperty(name = "MINIO_BUCKET_NAME", defaultValue = "ecommerce-bucket")
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                LOGGER.info("Création du bucket MinIO: " + bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur initialisation bucket MinIO " + bucketName, e);
        }
    }

    @Override
    public String storePartitioned(InputStream content, String fileName, String contentType, 
                                   UUID userId, ResourceType type, UUID... resourceIds) {
        try {
            String hierarchyPath = buildHierarchyPath(userId, type, resourceIds);
            String sanitizedName = sanitizeFileName(fileName);
            String fullPath = hierarchyPath + "/" + sanitizedName;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullPath)
                    .stream(content, -1, 10485760) // Part size 10MB
                    .contentType(contentType)
                    .build());

            return fullPath;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du stockage partitionné de " + fileName, e);
        }
    }

    private String buildHierarchyPath(UUID userId, ResourceType type, UUID[] resourceIds) {
        List<String> segments = new ArrayList<>();
        segments.add("users");
        segments.add(userId.toString());

        // Remonter la hiérarchie des types
        List<String> typeHierarchy = new ArrayList<>();
        ResourceType current = type;
        while (current != null) {
            typeHierarchy.add(current.name().toLowerCase());
            current = current.getParent();
        }
        Collections.reverse(typeHierarchy);
        segments.addAll(typeHierarchy);

        // Ajouter les IDs de ressources fournis
        for (UUID id : resourceIds) {
            segments.add(id.toString());
        }

        return String.join("/", segments);
    }

    private String sanitizeFileName(String rawName) {
        return Paths.get(rawName).getFileName().toString().replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }

    private String getSecurePath(String rawPath) {
        Path path = Paths.get(rawPath).normalize();
        if (path.toString().contains("..") || path.isAbsolute()) {
            throw new SecurityException("Tentative de traversée de répertoire détectée.");
        }
        String sanitizedName = sanitizeFileName(rawPath);
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        return String.format("%s/%s", datePath, sanitizedName);
    }

    @Override
    public String store(InputStream content, String fileName, String contentType, long size) {
        try {
            String securePath = getSecurePath(fileName);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(securePath)
                    .stream(content, size, -1)
                    .contentType(contentType)
                    .build());
            return securePath;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du stockage du fichier " + fileName, e);
        }
    }

    @Override
    public InputStream fetch(String path) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération du fichier " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier " + path, e);
        }
    }

    @Override
    public long getFileSize(String path) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build()).size();
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public String getPresignedUrl(String path, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(path)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération URL présignée", e);
        }
    }
}
