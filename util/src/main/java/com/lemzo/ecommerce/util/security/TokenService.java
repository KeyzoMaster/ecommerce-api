package com.lemzo.ecommerce.util.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service utilitaire pour la gestion des jetons opaques (activation, reset).
 */
@ApplicationScoped
public class TokenService {

    private static final int BYTE_LENGTH = 32;

    public TokenService() {
        // Constructeur explicite
    }

    /**
     * Génère un jeton aléatoire sécurisé encodé en Base64.
     */
    public String generateSecureToken() {
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] bytes = new byte[BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hache un jeton pour stockage en base de données (SHA-256).
     */
    public String hashToken(final String token) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            final StringBuilder hexString = new StringBuilder();
            for (final byte b : hash) {
                final String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (final NoSuchAlgorithmException exception) {
            throw new RuntimeException("Erreur hachage", exception);
        }
    }
}
