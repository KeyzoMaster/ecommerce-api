package com.lemzo.ecommerce.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe de base pour toutes les entités.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity implements Serializable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID entityId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected AbstractEntity() {
        // Requis par JPA
    }

    public UUID getId() {
        return entityId;
    }

    @PrePersist
    protected void onCreate() {
        if (entityId == null) {
            this.entityId = UUID.randomUUID();
        }
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(final Object other) {
        boolean isEqual = false;
        if (this == other) {
            isEqual = true;
        } else if (other instanceof AbstractEntity that) {
            isEqual = Objects.equals(entityId, that.entityId);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId);
    }
}
