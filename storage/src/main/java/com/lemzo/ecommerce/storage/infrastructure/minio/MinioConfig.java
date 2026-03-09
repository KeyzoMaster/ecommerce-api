package com.lemzo.ecommerce.storage.infrastructure.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.logging.Logger;
import java.util.Optional;

@ApplicationScoped
public class MinioConfig {

    private static final Logger LOGGER = Logger.getLogger(MinioConfig.class.getName());

    @ConfigProperty(name = "MINIO_URL", defaultValue = "http://localhost:9000")
    private String url;

    @ConfigProperty(name = "MINIO_ACCESS_KEY", defaultValue = "minio_user")
    private String accessKey;

    @ConfigProperty(name = "MINIO_SECRET_KEY", defaultValue = "minio_password")
    private String secretKey;

    @ConfigProperty(name = "MINIO_BUCKET_NAME", defaultValue = "ecommerce")
    private String bucketName;

    private MinioClient client;

    @PostConstruct
    public void init() {
        this.client = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();

        Optional.ofNullable(client).ifPresent(c -> {
            try {
                if (!c.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                    LOGGER.info("Création du bucket MinIO : " + bucketName);
                    c.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                }
            } catch (Exception e) {
                LOGGER.severe("Impossible d'initialiser le bucket MinIO : " + e.getMessage());
            }
        });
    }

    @Produces
    @Singleton
    public MinioClient minioClient() {
        return client;
    }
}
