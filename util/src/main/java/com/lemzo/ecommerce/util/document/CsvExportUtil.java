package com.lemzo.ecommerce.util.document;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utilitaire générique pour la génération de fichiers CSV.
 */
@ApplicationScoped
public class CsvExportUtil {

    private static final String DEFAULT_SEPARATOR = ";";
    private static final String DEFAULT_NEWLINE = "\n";

    /**
     * Génère une chaîne CSV à partir d'une liste de données et d'une fonction d'extraction.
     */
    public <T> String generateCsv(List<String> headers, List<T> data, Function<T, List<String>> rowMapper) {
        StringBuilder csv = new StringBuilder();
        
        // En-têtes
        Optional.ofNullable(headers)
                .filter(h -> !h.isEmpty())
                .ifPresent(h -> csv.append(String.join(DEFAULT_SEPARATOR, h)).append(DEFAULT_NEWLINE));
        
        // Données
        Optional.ofNullable(data)
                .orElse(List.of())
                .stream()
                .map(rowMapper)
                .map(row -> row.stream()
                        .map(this::sanitize)
                        .collect(Collectors.joining(DEFAULT_SEPARATOR)))
                .forEach(sanitizedRow -> csv.append(sanitizedRow).append(DEFAULT_NEWLINE));
        
        return csv.toString();
    }

    private String sanitize(String value) {
        return Optional.ofNullable(value)
                .map(v -> v.replace(DEFAULT_SEPARATOR, ",")
                           .replace("\n", " ")
                           .replace("\r", " "))
                .map(v -> (v.contains(",") || v.contains("\"")) ? "\"" + v.replace("\"", "\"\"") + "\"" : v)
                .orElse("");
    }
}
