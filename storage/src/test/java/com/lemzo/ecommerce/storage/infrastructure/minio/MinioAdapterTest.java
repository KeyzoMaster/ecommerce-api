package com.lemzo.ecommerce.storage.infrastructure.minio;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MinioAdapter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MinioAdapter Unit Tests")
class MinioAdapterTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioAdapter minioAdapter;

    @Test
    @DisplayName("Should successfully generate a presigned URL")
    void shouldGeneratePresignedUrl() throws Exception {
        // Arrange
        final String path = "users/123/catalog/product.jpg";
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://presigned-url.com");

        // Act
        final String url = minioAdapter.getPresignedUrl(path, 60);

        // Assert
        assertEquals("http://presigned-url.com", url);
    }

    @Test
    @DisplayName("Should build correct hierarchy path and store object")
    void shouldStorePartitioned() throws Exception {
        // Arrange
        final UUID userId = UUID.randomUUID();
        final UUID productId = UUID.randomUUID();
        final String fileName = "test.txt";
        final var content = new ByteArrayInputStream("hello".getBytes());

        // Act
        final String resultPath = minioAdapter.storePartitioned(
                content, fileName, "text/plain", userId, ResourceType.PRODUCT, productId);

        // Assert
        assertNotNull(resultPath);
        assertTrue(resultPath.contains("users/" + userId));
        assertTrue(resultPath.contains("catalog/product")); // Hiérarchie : platform -> catalog -> product
        assertTrue(resultPath.contains(productId.toString()));
        assertTrue(resultPath.contains("test.txt"));
        
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }
}
