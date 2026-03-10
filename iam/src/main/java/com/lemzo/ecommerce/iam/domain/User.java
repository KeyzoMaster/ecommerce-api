package com.lemzo.ecommerce.iam.domain;

import com.lemzo.ecommerce.core.entity.AbstractEntity;
import com.lemzo.ecommerce.core.entity.converter.JsonbConverter;
import jakarta.persistence.*;
import com.lemzo.ecommerce.core.domain.Address;
import java.time.OffsetDateTime;
import java.util.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

/**
 * Entité représentant un utilisateur du système.
 */
@Entity
@Table(name = "iam_users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    /**
     * Moyens de paiement enregistrés (Mocks).
     */
    @Convert(converter = JsonbConverter.class)
    @Column(name = "payment_methods", columnDefinition = "text")
    private List<Map<String, Object>> paymentMethods = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "iam_user_addresses", joinColumns = @JoinColumn(name = "user_id"))
    private List<Address> addresses = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "iam_user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Permissions spécifiques accordées directement à l'utilisateur (Ad-hoc).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "iam_user_adhoc_permissions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> adhocPermissions = new HashSet<>();

    public User(final String username, final String email, final String password) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public void addPaymentMethod(final String type, final Map<String, Object> details) {
        this.paymentMethods = Optional.ofNullable(this.paymentMethods)
                .orElseGet(ArrayList::new);
        final Map<String, Object> method = new HashMap<>(details);
        method.put("type", type);
        this.paymentMethods.add(method);
    }
}
