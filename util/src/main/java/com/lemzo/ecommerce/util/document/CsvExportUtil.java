package com.lemzo.ecommerce.util.document;

import com.lemzo.ecommerce.core.contract.util.CsvExportPort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utilitaire pour l'export de données au format CSV.
 */
@ApplicationScoped
public class CsvExportUtil implements CsvExportPort {

    public CsvExportUtil() {
        // Constructeur explicite
    }

    /**
     * Génère une chaîne CSV à partir d'une liste d'objets.
     */
    @Override
    public <T> String generateCsv(final List<String> headers, final List<T> data, final Function<T, List<String>> rowMapper) {
        final StringBuilder csv = new StringBuilder();

        // En-têtes
        csv.append(String.join(",", headers)).append("\n");

        // Données
        data.stream()
                .map(rowMapper)
                .map(row -> row.stream()
                        .map(this::escapeCsv)
                        .collect(Collectors.joining(",")))
                .forEach(line -> csv.append(line).append("\n"));

        return csv.toString();
    }

    private String escapeCsv(final String value) {
        return Optional.ofNullable(value)
                .map(val -> {
                    final String escaped = val.replace("\"", "\"\"");
                    return (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\""))
                            ? "\"" + escaped + "\""
                            : escaped;
                })
                .orElse("");
    }
}
