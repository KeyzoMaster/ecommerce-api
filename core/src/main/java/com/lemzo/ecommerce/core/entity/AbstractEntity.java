package com.lemzo.ecommerce.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe de base pour toutes les entités du système.
 * Gère l'identifiant unique et l'audit de base.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID entityId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return entityId;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        boolean isEqual = false;
        if (other instanceof AbstractEntity that) {
            isEqual = Objects.equals(entityId, that.entityId);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId);
    }
}
