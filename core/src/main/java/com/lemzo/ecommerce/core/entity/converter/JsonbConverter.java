package com.lemzo.ecommerce.core.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.Map;
import java.util.Optional;

/**
 * Convertisseur JPA pour stocker des Map dans des colonnes JSONB.
 */
@Converter
public class JsonbConverter implements AttributeConverter<Map<String, Object>, String> {

    private final Jsonb jsonb = JsonbBuilder.create();

    public JsonbConverter() {
        // Constructeur par défaut requis par JPA
    }

    @Override
    public String convertToDatabaseColumn(final Map<String, Object> attribute) {
        return Optional.ofNullable(attribute)
                .map(jsonb::toJson)
                .orElse("{}");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToEntityAttribute(final String dbData) {
        return Optional.ofNullable(dbData)
                .map(data -> jsonb.fromJson(data, Map.class))
                .orElse(Map.of());
    }
}
