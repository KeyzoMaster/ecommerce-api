package com.lemzo.ecommerce.util.storage;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.Set;

/**
 * Utilitaire de sécurisation des contenus de fichiers.
 * Prévient notamment l'injection de code via CSV (CSV Injection).
 */
@ApplicationScoped
public class FileSanitizer {

    private static final Set<Character> DANGEROUS_CHARS = Set.of('=', '+', '-', '@');

    public FileSanitizer() {
        // Constructeur explicite
    }

    /**
     * Assainit une chaîne de caractères pour l'export Excel/CSV.
     */
    public String sanitizeForExcel(final String input) {
        return Optional.ofNullable(input)
                .filter(s -> !s.isEmpty())
                .map(s -> DANGEROUS_CHARS.contains(s.charAt(0)) ? "'" + s : s)
                .orElse(input);
    }

    /**
     * Assainit un objet quelconque.
     */
    public Object sanitizeObject(final Object value) {
        if (value instanceof String s) {
            return sanitizeForExcel(s);
        }
        return value;
    }

    /**
     * Assainit un nom de fichier pour éviter les injections de chemin.
     */
    public String sanitizeFileName(final String fileName) {
        return Optional.ofNullable(fileName)
                .map(f -> f.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_"))
                .orElseGet(() -> "file_" + System.currentTimeMillis());
    }

    /**
     * Vérifie si le type MIME est une image sécurisée.
     */
    public boolean isSafeImage(final String contentType) {
        return Optional.ofNullable(contentType)
                .map(c -> c.startsWith("image/png") || c.startsWith("image/jpeg") || c.startsWith("image/webp"))
                .orElse(false);
    }
}
