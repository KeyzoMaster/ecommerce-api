package com.lemzo.ecommerce.core.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.Optional;

/**
 * Convertisseur générique pour les colonnes PostgreSQL JSONB.
 * Gère le type PGobject retourné par le driver JDBC.
 */
@Converter
public class JsonbConverter implements AttributeConverter<Object, Object> {

    private static final Jsonb jsonb = JsonbBuilder.create();

    @Override
    public Object convertToDatabaseColumn(final Object attribute) {
        return Optional.ofNullable(attribute)
                .map(jsonb::toJson)
                .orElse(null);
    }

    @Override
    public Object convertToEntityAttribute(final Object dbData) {
        if (dbData == null) return null;
        
        final String json;
        if (dbData instanceof String s) {
            json = s;
        } else {
            // Probablement un org.postgresql.util.PGobject
            json = dbData.toString(); 
        }

        return jsonb.fromJson(json, Object.class);
    }
}
