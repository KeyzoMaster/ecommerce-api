package com.lemzo.ecommerce.audit.service;

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

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Unit Tests (JUnit 6)")
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
        when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
        auditService.init();
    }

    @Test
    @DisplayName("Should submit audit log to executor")
    void shouldSubmitAuditLogToExecutor() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String action = "TEST_ACTION";
        
        // Mock executor.submit to execute the runnable immediately
        when(executor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return mock(Future.class);
        });

        // Act
        auditService.log(userId, action, "ResourceType", "ResId", "Details", "127.0.0.1", "Agent");

        // Assert
        verify(executor).submit(any(Runnable.class));
        
        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(mongoCollection).insertOne(captor.capture());
        
        Document captured = captor.getValue();
        assertEquals(userId.toString(), captured.getString("userId"));
        assertEquals(action, captured.getString("action"));
        assertEquals("ResourceType", captured.getString("resourceType"));
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
        auditService.log(null, "ANONYMOUS_ACTION", "Res", "ID", "Det", "IP", "Agent");

        // Assert
        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(mongoCollection).insertOne(captor.capture());
        
        Document captured = captor.getValue();
        assertEquals(null, captured.getString("userId"));
        assertEquals("ANONYMOUS_ACTION", captured.getString("action"));
    }
}
