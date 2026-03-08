package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.domain.User;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
import com.lemzo.ecommerce.security.infrastructure.jwt.JwtService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.lemzo.ecommerce.iam.domain.Permission;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service d'authentification.
 */
@ApplicationScoped
public class AuthenticationService {

    @Inject
    private UserService userService;

    @Inject
    private PasswordService passwordService;

    @Inject
    private JwtService jwtService;

    public record LoginResult(String accessToken, String refreshToken, User user) {}

    /**
     * Authentifie un utilisateur et génère les jetons JWT.
     */
    @Audit(action = "USER_LOGIN")
    public LoginResult login(String identifier, String password) {
        User user = userService.findByIdentifier(identifier)
                .orElseThrow(() -> new BusinessRuleException("error.iam.invalid_credentials"));

        if (!user.isEnabled()) {
            throw new BusinessRuleException("error.iam.account_disabled");
        }

        if (!passwordService.verify(user.getPassword(), password.toCharArray())) {
            throw new BusinessRuleException("error.iam.invalid_credentials");
        }

        // Calcul dynamique des permissions (Rôles -> Permissions + Ad-hoc Permissions)
        Set<String> permissions = Stream.concat(
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream()),
                user.getAdhocPermissions().stream()
        )
        .map(Permission::getSlug)
        .collect(Collectors.toSet());

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), permissions);
        String refreshToken = jwtService.generateToken(user.getId(), user.getEmail(), permissions); 

        return new LoginResult(accessToken, refreshToken, user);
    }
}
