package com.lemzo.ecommerce.iam.domain;

import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant une permission spécifique sur une ressource.
 */
@Entity
@Table(name = "iam_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"resource_type", "action"})
})
@Getter
@Setter
@NoArgsConstructor
public class Permission extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PbacAction action;

    public Permission(ResourceType resourceType, PbacAction action) {
        this.resourceType = resourceType;
        this.action = action;
    }

    public String getSlug() {
        return resourceType.name().toLowerCase() + ":" + action.name().toLowerCase();
    }
}
