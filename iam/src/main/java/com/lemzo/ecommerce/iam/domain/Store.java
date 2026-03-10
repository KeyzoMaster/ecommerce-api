package com.lemzo.ecommerce.iam.domain;

import com.lemzo.ecommerce.core.api.security.Ownable;
import com.lemzo.ecommerce.core.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import java.util.Optional;
import java.util.UUID;

/**
 * Entité représentant une boutique.
 */
@Entity
@Table(name = "iam_stores")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends AbstractEntity implements Ownable {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Store(final String name, final String slug, final User owner) {
        super();
        this.name = name;
        this.slug = slug;
        this.owner = owner;
    }

    @Override
    public UUID getOwnerId() {
        return Optional.ofNullable(owner)
                .map(User::getId)
                .orElse(null);
    }
}
