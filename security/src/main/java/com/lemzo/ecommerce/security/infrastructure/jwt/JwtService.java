package com.lemzo.ecommerce.security.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.Set;

/**
 * Service de gestion des JSON Web Tokens (JWT).
 */
@ApplicationScoped
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationTimeMs;

    @Inject
    public JwtService(
            @ConfigProperty(name = "JWT_SECRET", defaultValue = "votre_secret_tres_long_et_securise_pour_jwt") final String secret,
            @ConfigProperty(name = "JWT_EXPIRATION_MS", defaultValue = "3600000") final long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTimeMs = expirationMs;
    }

    public String generateToken(final UUID userId, final String email, final Set<String> permissions) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + expirationTimeMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .claim("email", email)
                .claim("permissions", permissions)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public Claims validateToken(final String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserIdFromToken(final String token) {
        return UUID.fromString(validateToken(token).getSubject());
    }

    @SuppressWarnings("unchecked")
    public Set<String> getPermissionsFromToken(final String token) {
        return (Set<String>) validateToken(token).get("permissions", Set.class);
    }
}
