package com.lemzo.ecommerce.util.storage;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.Set;

/**
 * Utilitaire de sécurisation des contenus de fichiers.
 * Prévient notamment la CSV/Excel Formula Injection.
 */
@ApplicationScoped
public class FileSanitizer {

    private static final Set<Character> DANGEROUS_CHARS = Set.of('=', '+', '-', '@', '\t', '\r');

    /**
     * Assainit une chaîne de caractères pour un export Excel/CSV.
     * Si la chaîne commence par un caractère dangereux, elle est préfixée par une apostrophe (').
     */
    public String sanitizeForExcel(String input) {
        return Optional.ofNullable(input)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    char firstChar = s.charAt(0);
                    if (DANGEROUS_CHARS.contains(firstChar)) {
                        return "'" + s;
                    }
                    return s;
                }).orElse(input);
    }

    /**
     * Assainit un objet quelconque.
     */
    public Object sanitizeObject(Object value) {
        if (value instanceof String s) {
            return sanitizeForExcel(s);
        }
        return value;
    }

    /**
     * Assainit un nom de fichier pour éviter les injections de chemin.
     */
    public String sanitizeFileName(String fileName) {
        if (fileName == null) return "file_" + System.currentTimeMillis();
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }

    /**
     * Vérifie si le type MIME est une image sécurisée.
     */
    public boolean isSafeImage(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/png") || 
               contentType.startsWith("image/jpeg") || 
               contentType.startsWith("image/webp");
    }
}
