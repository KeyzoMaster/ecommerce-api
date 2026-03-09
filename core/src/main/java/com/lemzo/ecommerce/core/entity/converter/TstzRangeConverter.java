package com.lemzo.ecommerce.core.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertisseur JPA pour PostgreSQL tstzrange.
 * Mappe le range PostgreSQL vers un String Java (ex: "[2026-03-01 00:00:00+00, 2026-04-01 00:00:00+00)").
 */
@Converter
public class TstzRangeConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
}
