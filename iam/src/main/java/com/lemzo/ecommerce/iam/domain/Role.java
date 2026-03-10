package com.lemzo.ecommerce.iam.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un rôle utilisateur.
 */
@Entity
@Table(name = "iam_roles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Column(name = "is_system_role", nullable = false)
    private boolean systemRole = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "iam_role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Role(final String name, final String description) {
        super();
        this.name = name;
        this.description = description;
    }
}
