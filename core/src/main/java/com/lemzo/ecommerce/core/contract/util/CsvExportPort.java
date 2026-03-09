package com.lemzo.ecommerce.core.contract.util;

import java.util.List;
import java.util.function.Function;

/**
 * Port pour l'export de données.
 */
public interface CsvExportPort {
    <T> String generateCsv(List<String> headers, List<T> data, Function<T, List<String>> rowMapper);
}
