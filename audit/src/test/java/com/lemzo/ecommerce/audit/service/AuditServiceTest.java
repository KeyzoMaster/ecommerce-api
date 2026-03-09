package com.lemzo.ecommerce.audit.service;

import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Unit Tests")
class AuditServiceTest {

    @Mock
    private MongoDatabase mongoDatabase;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @Mock
    private ManagedExecutorService executor;

    @InjectMocks
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        lenient().when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
        auditService.init();
    }

    @Test
    @DisplayName("Should submit audit log to executor")
    void shouldSubmitAuditLogToExecutor() {
        // Arrange
        final UUID userId = UUID.randomUUID();
        final String action = "TEST_ACTION";
        
        // Mock executor.submit to execute the runnable immediately
        when(executor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            final Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return mock(Future.class);
        });

        // Act
        auditService.log(userId, action, ResourceType.PRODUCT, "ResId", Map.of("key", "value"), "127.0.0.1", "Agent");

        // Assert
        verify(executor).submit(any(Runnable.class));
        
        final ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(mongoCollection).insertOne(captor.capture());
        
        final Document captured = captor.getValue();
        assertEquals(userId.toString(), captured.getString("userId"));
        assertEquals(action, captured.getString("action"));
        assertEquals("PRODUCT", captured.getString("resourceType"));
        assertNotNull(captured.getString("timestamp"));
    }

    @Test
    @DisplayName("Should handle null userId")
    void shouldHandleNullUserId() {
        // Arrange
        when(executor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return mock(Future.class);
        });

        // Act
        auditService.log(null, "ANONYMOUS_ACTION", ResourceType.PLATFORM, "ID", null, "IP", "Agent");

        // Assert
        final ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(mongoCollection).insertOne(captor.capture());
        
        final Document captured = captor.getValue();
        assertEquals("anonymous", captured.getString("userId"));
        assertEquals("ANONYMOUS_ACTION", captured.getString("action"));
    }
}
