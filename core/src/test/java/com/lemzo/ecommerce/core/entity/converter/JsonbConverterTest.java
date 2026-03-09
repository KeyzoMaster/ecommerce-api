package com.lemzo.ecommerce.core.entity.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour JsonbConverter.
 */
@DisplayName("JsonbConverter Unit Tests")
class JsonbConverterTest {

    private final JsonbConverter converter = new JsonbConverter();

    @Test
    @DisplayName("Should convert Map to JSON string")
    void shouldConvertMapToJson() {
        final var data = Map.<String, Object>of("key", "value", "num", 123);
        final Object json = converter.convertToDatabaseColumn(data);

        assertNotNull(json);
        assertTrue(json.toString().contains("\"key\":\"value\""));
        assertTrue(json.toString().contains("\"num\":123"));
    }

    @Test
    @DisplayName("Should convert JSON string back to Map")
    @SuppressWarnings("unchecked")
    void shouldConvertJsonToMap() {
        final String json = "{\"color\":\"red\",\"size\":42}";
        final Object result = converter.convertToEntityAttribute(json);

        assertNotNull(result);
        assertTrue(result instanceof Map);
        final Map<String, Object> map = (Map<String, Object>) result;
        
        assertEquals("red", map.get("color"));
        
        Optional.ofNullable(map.get("size"))
                .map(Number.class::cast)
                .ifPresent(size -> assertEquals(42, size.intValue()));
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNull() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }
}
