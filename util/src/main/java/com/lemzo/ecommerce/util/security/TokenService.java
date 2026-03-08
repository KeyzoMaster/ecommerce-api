package com.lemzo.ecommerce.util.security;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Service de gestion des jetons temporaires (activation, réinitialisation de mot de passe).
 */
@ApplicationScoped
public class TokenService {

    private final Map<String, TokenData> tokenStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final long ACTIVATION_EXPIRY_HOURS = 24;
    private static final long RESET_EXPIRY_HOURS = 2;

    @PostConstruct
    public void init() {
        // Nettoyage périodique des jetons expirés toutes les heures
        scheduler.scheduleAtFixedRate(this::removeExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }

    public String generateActivationToken(UUID userId, String email) {
        return createToken(userId, email, TokenType.ACTIVATION, ACTIVATION_EXPIRY_HOURS);
    }

    public String generatePasswordResetToken(UUID userId, String email) {
        return createToken(userId, email, TokenType.PASSWORD_RESET, RESET_EXPIRY_HOURS);
    }

    public TokenData validateActivationToken(String token) {
        return validateToken(token, TokenType.ACTIVATION);
    }

    public TokenData validatePasswordResetToken(String token) {
        return validateToken(token, TokenType.PASSWORD_RESET);
    }

    public void invalidateToken(String token) {
        tokenStore.remove(token);
    }

    private String createToken(UUID userId, String email, TokenType type, long expiryHours) {
        String token = generateSafeTokenString();
        TokenData data = new TokenData(userId, email, type, System.currentTimeMillis(), expiryHours);
        tokenStore.put(token, data);
        return token;
    }

    private TokenData validateToken(String token, TokenType expectedType) {
        return Optional.ofNullable(token)
                .map(tokenStore::get)
                .filter(data -> data.type().equals(expectedType))
                .filter(data -> !isExpired(data))
                .or(() -> {
                    Optional.ofNullable(token).ifPresent(tokenStore::remove);
                    return Optional.empty();
                })
                .orElse(null);
    }

    private boolean isExpired(TokenData data) {
        long age = System.currentTimeMillis() - data.createdAt();
        return age > TimeUnit.HOURS.toMillis(data.expiryHours());
    }

    private void removeExpiredTokens() {
        tokenStore.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    private String generateSafeTokenString() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public enum TokenType {
        ACTIVATION, PASSWORD_RESET
    }

    public record TokenData(UUID userId, String email, TokenType type, long createdAt, long expiryHours) {
    }
}
