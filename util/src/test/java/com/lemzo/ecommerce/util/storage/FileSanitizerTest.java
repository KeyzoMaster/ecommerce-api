package com.lemzo.ecommerce.util.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour FileSanitizer.
 */
@DisplayName("FileSanitizer Unit Tests")
class FileSanitizerTest {

    private final FileSanitizer sanitizer = new FileSanitizer();

    @Test
    @DisplayName("Should sanitize dangerous characters for Excel/CSV")
    void shouldSanitizeExcelInjection() {
        assertEquals("'=1+2", sanitizer.sanitizeForExcel("=1+2"));
        assertEquals("'+test", sanitizer.sanitizeForExcel("+test"));
        assertEquals("'-abc", sanitizer.sanitizeForExcel("-abc"));
        assertEquals("'@mail", sanitizer.sanitizeForExcel("@mail"));
        assertEquals("normal", sanitizer.sanitizeForExcel("normal"));
    }

    @Test
    @DisplayName("Should sanitize file names")
    void shouldSanitizeFileName() {
        assertEquals("my_image.jpg", sanitizer.sanitizeFileName("my image.jpg"));
        assertEquals("file_name.png", sanitizer.sanitizeFileName("file/name.png"));
        assertEquals("test_file.txt", sanitizer.sanitizeFileName("test$file.txt"));
    }

    @Test
    @DisplayName("Should identify safe images")
    void shouldIdentifySafeImages() {
        assertTrue(sanitizer.isSafeImage("image/png"));
        assertTrue(sanitizer.isSafeImage("image/jpeg"));
        assertTrue(sanitizer.isSafeImage("image/webp"));
        assertFalse(sanitizer.isSafeImage("text/plain"));
        assertFalse(sanitizer.isSafeImage(null));
    }
}
