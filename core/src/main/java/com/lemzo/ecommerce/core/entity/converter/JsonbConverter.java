package com.lemzo.ecommerce.core.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.Map;
import java.util.Optional;

/**
 * Convertisseur générique pour stocker des Maps en JSONB (PostgreSQL).
 * Utilise Jakarta JSON Binding (JSON-B).
 */
@Converter
public class JsonbConverter implements AttributeConverter<Map<String, Object>, String> {

    private final Jsonb jsonb = JsonbBuilder.create();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        return Optional.ofNullable(attribute)
                .map(jsonb::toJson)
                .orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        return Optional.ofNullable(dbData)
                .map(data -> jsonb.fromJson(data, Map.class))
                .orElse(Map.of());
    }
}
