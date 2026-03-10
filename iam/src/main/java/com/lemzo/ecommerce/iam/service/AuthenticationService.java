package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.api.dto.AuthResponse;
import com.lemzo.ecommerce.iam.domain.Permission;
import com.lemzo.ecommerce.iam.repository.UserRepository;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
import com.lemzo.ecommerce.security.infrastructure.jwt.JwtService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service d'authentification.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse login(final String identifier, final String password) {
        final var user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new BusinessRuleException("error.iam.invalid_credentials"));

        LOGGER.info(() -> "Login attempt for user: " + user.getUsername());
        LOGGER.info(() -> "Stored hash: " + user.getPassword());
        
        final boolean match = passwordService.verify(user.getPassword(), password.toCharArray());
        LOGGER.info(() -> "Password match: " + match);

        if (!match) {
            throw new BusinessRuleException("error.iam.invalid_credentials");
        }

        if (!user.isEnabled()) {
            throw new BusinessRuleException("error.iam.account_disabled");
        }

        final String accessToken = jwtService.generateToken(user.getId(), user.getEmail());
        final String refreshToken = jwtService.generateToken(user.getId(), user.getEmail());

        final Set<String> permissions = Stream.concat(
                user.getRoles().stream().flatMap(role -> role.getPermissions().stream()),
                user.getAdhocPermissions().stream()
        )
        .map(Permission::getSlug)
        .collect(Collectors.toSet());

    @Transactional
    public AuthResponse refreshToken(final String refreshToken) {
        final var claims = jwtService.validateToken(refreshToken);
        final var userId = java.util.UUID.fromString(claims.getSubject());
        
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("error.iam.user_not_found"));

        if (!user.isEnabled()) {
            throw new BusinessRuleException("error.iam.account_disabled");
        }

        final String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail());
        final String newRefreshToken = jwtService.generateToken(user.getId(), user.getEmail());

        final Set<String> permissions = Stream.concat(
                user.getRoles().stream().flatMap(role -> role.getPermissions().stream()),
                user.getAdhocPermissions().stream()
        )
        .map(Permission::getSlug)
        .collect(Collectors.toSet());

        return new AuthResponse(newAccessToken, newRefreshToken, user.getEmail(), permissions);
    }
}
