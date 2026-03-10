package com.lemzo.ecommerce.storage.infrastructure.minio;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.contract.storage.StoragePort;
import io.minio.*;
import io.minio.http.Method;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adaptateur MinIO pour le stockage des fichiers avec support du partitionnement hiérarchique.
 */
@ApplicationScoped
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class MinioAdapter implements StoragePort {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");
    private static final long DEFAULT_PART_SIZE = 10_485_760L; // 10MB

    private final MinioClient minioClient;
    private final String bucketName;

    @Inject
    public MinioAdapter(
            final MinioClient minioClient,
            @ConfigProperty(name = "MINIO_BUCKET_NAME", defaultValue = "ecommerce") final String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public String storePartitioned(final InputStream content, final String fileName, final String contentType,
                                   final UUID userId, final ResourceType type, final UUID... resourceIds) {
        try {
            final var hierarchyPath = buildHierarchyPath(userId, type, resourceIds);
            final var sanitizedName = sanitizeFileName(fileName);
            final var fullPath = hierarchyPath + "/" + sanitizedName;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullPath)
                    .stream(content, -1, DEFAULT_PART_SIZE)
                    .contentType(contentType)
                    .build());

            return fullPath;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du stockage partitionné de " + fileName, e);
        }
    }

    private String buildHierarchyPath(final UUID userId, final ResourceType type, final UUID[] resourceIds) {
        final var userSegment = Stream.of("users", userId.toString());

        final var typeHierarchy = Stream.iterate(type, Objects::nonNull, ResourceType::getParent)
                .map(t -> t.name().toLowerCase())
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Collections.reverse(list);
                    return list.stream();
                }));

        final var resourceSegments = Arrays.stream(Optional.ofNullable(resourceIds).orElse(new UUID[0]))
                .map(UUID::toString);

        return Stream.concat(Stream.concat(userSegment, typeHierarchy), resourceSegments)
                .collect(Collectors.joining("/"));
    }

    private String sanitizeFileName(final String rawName) {
        return Optional.ofNullable(rawName)
                .map(name -> Paths.get(name).getFileName().toString().replaceAll("[^a-zA-Z0-9.\\-_]", "_"))
                .orElseGet(() -> "file_" + System.currentTimeMillis());
    }

    @Override
    public String getPresignedUrl(final String path, final int expiryMinutes) {
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

    @Override
    public void delete(final String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier " + path, e);
        }
    }
}