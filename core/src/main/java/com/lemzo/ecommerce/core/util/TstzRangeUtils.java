package com.lemzo.ecommerce.core.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Utilitaire pour formater et parser les tstzrange PostgreSQL.
 */
public final class TstzRangeUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx");

    private TstzRangeUtils() {}

    public static String format(final LocalDateTime start, final LocalDateTime end) {
        final String s = Optional.ofNullable(start)
                .map(t -> t.atOffset(ZoneOffset.UTC).format(FORMATTER))
                .orElse("");
                
        final String e = Optional.ofNullable(end)
                .map(t -> t.atOffset(ZoneOffset.UTC).format(FORMATTER))
                .orElse("");

        return (s.isEmpty() && e.isEmpty()) ? null : String.format("[%s, %s)", s, e);
    }

    public static Optional<LocalDateTime> parseStart(final String range) {
        return parsePart(range, 0);
    }

    public static Optional<LocalDateTime> parseEnd(final String range) {
        return parsePart(range, 1);
    }

    private static Optional<LocalDateTime> parsePart(final String range, final int partIndex) {
        return Optional.ofNullable(range)
                .filter(r -> r.contains(",") && r.length() > 2)
                .map(r -> r.substring(1, r.length() - 1))
                .map(r -> r.split(","))
                .filter(parts -> parts.length > partIndex)
                .map(parts -> parts[partIndex].trim())
                .filter(p -> !p.isEmpty())
                .map(p -> {
                    try {
                        // PostgreSQL format can be "2026-03-01 00:00:00+00"
                        // OffsetDateTime handles the +00 part
                        return OffsetDateTime.parse(p, FORMATTER).toLocalDateTime();
                    } catch (Exception ex) {
                        return null;
                    }
                });
    }
}
