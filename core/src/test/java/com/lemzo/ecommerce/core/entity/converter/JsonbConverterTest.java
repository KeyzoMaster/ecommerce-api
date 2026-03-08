package com.lemzo.ecommerce.core.entity.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonbConverter Unit Tests (JUnit 6)")
class JsonbConverterTest {

    private final JsonbConverter converter = new JsonbConverter();

    @Test
    @DisplayName("Should convert Map to JSON string")
    void shouldConvertMapToJson() {
        Map<String, Object> data = Map.of("key", "value", "num", 123);
        String json = converter.convertToDatabaseColumn(data);

        assertNotNull(json);
        assertTrue(json.contains("\"key\":\"value\""));
        assertTrue(json.contains("\"num\":123"));
    }

    @Test
    @DisplayName("Should convert JSON string back to Map")
    void shouldConvertJsonToMap() {
        String json = "{\"color\":\"red\",\"size\":42}";
        Map<String, Object> result = converter.convertToEntityAttribute(json);

        assertNotNull(result);
        assertEquals("red", result.get("color"));
        assertEquals(42, ((Number) result.get("size")).intValue());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNull() {
        assertNull(converter.convertToDatabaseColumn(null));
        
        Map<String, Object> result = converter.convertToEntityAttribute(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
