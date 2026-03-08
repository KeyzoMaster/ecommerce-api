package com.lemzo.ecommerce.storage.infrastructure.minio;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import io.minio.MinioClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MinioConfig {

    @ConfigProperty(name = "MINIO_URL", defaultValue = "http://localhost:9000")
    private String url;

    @ConfigProperty(name = "MINIO_ACCESS_KEY", defaultValue = "minio_user")
    private String accessKey;

    @ConfigProperty(name = "MINIO_SECRET_KEY", defaultValue = "minio_password")
    private String secretKey;

    @Produces
    @ApplicationScoped
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
