package com.lemzo.ecommerce.iam.domain;

import com.lemzo.ecommerce.core.api.security.PbacAction;
import com.lemzo.ecommerce.core.api.security.ResourceType;
import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

/**
 * Entité représentant une permission.
 */
@Entity
@Table(name = "iam_permissions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private PbacAction action;

    public Permission(final ResourceType resourceType, final PbacAction action) {
        super();
        this.resourceType = resourceType;
        this.action = action;
    }

    public String getSlug() {
        return resourceType.name().toLowerCase() + ":" + action.name().toLowerCase();
    }
}
