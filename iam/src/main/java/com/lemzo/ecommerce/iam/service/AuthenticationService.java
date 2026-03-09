package com.lemzo.ecommerce.iam.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.iam.api.dto.AuthResponse;
import com.lemzo.ecommerce.iam.domain.Permission;
import com.lemzo.ecommerce.iam.domain.Role;
import com.lemzo.ecommerce.iam.repository.UserRepository;
import com.lemzo.ecommerce.security.infrastructure.hashing.PasswordService;
import com.lemzo.ecommerce.security.infrastructure.jwt.JwtService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service d'authentification.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthResponse login(final String identifier, final String password) {
        final var user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .filter(u -> passwordService.verify(u.getPassword(), password.toCharArray()))
                .orElseThrow(() -> new BusinessRuleException("error.iam.invalid_credentials"));

        if (!user.isEnabled()) {
            throw new BusinessRuleException("error.iam.account_disabled");
        }

        final var permissions = Stream.concat(
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream()),
                user.getAdhocPermissions().stream()
        )
        .map(Permission::getSlug)
        .collect(Collectors.toSet());

        final var accessToken = jwtService.generateToken(user.getId(), user.getEmail(), permissions);
        final var refreshToken = jwtService.generateToken(user.getId(), user.getEmail(), permissions);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), permissions);
    }
}
