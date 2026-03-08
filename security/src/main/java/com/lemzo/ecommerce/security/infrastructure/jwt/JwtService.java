package com.lemzo.ecommerce.security.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

/**
 * Service de gestion des JSON Web Tokens (JWT).
 */
@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "JWT_SECRET", defaultValue = "votre_secret_tres_long_et_securise_pour_jwt")
    private String secret;

    @ConfigProperty(name = "JWT_EXPIRATION_MS", defaultValue = "3600000") // 1 heure par défaut
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UUID userId, String email, Set<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // JTI unique pour révocation
                .subject(userId.toString())
                .claim("email", email)
                .claim("permissions", permissions)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(validateToken(token).getSubject());
    }

    @SuppressWarnings("unchecked")
    public Set<String> getPermissionsFromToken(String token) {
        return (Set<String>) validateToken(token).get("permissions", Set.class);
    }
}
