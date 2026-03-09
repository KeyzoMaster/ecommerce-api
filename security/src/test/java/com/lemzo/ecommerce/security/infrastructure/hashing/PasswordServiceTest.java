package com.lemzo.ecommerce.security.infrastructure.hashing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PasswordService (Argon2).
 */
@DisplayName("PasswordService Unit Tests")
class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    @DisplayName("Should hash and verify password correctly")
    void shouldHashAndVerify() {
        // Arrange
        final char[] password = "secret_password".toCharArray();

        // Act
        final String hash = passwordService.hash(password);
        
        // Assert
        assertNotNull(hash);
        assertTrue(hash.startsWith("$argon2"));
        
        // On doit pouvoir vérifier avec le même mot de passe
        assertTrue(passwordService.verify(hash, "secret_password".toCharArray()));
        
        // On doit échouer avec un mauvais mot de passe
        assertFalse(passwordService.verify(hash, "wrong_password".toCharArray()));
    }

    @Test
    @DisplayName("Should wipe password array after use")
    void shouldWipePassword() {
        final char[] password = {'s', 'e', 'c', 'r', 'e', 't'};
        passwordService.hash(password);
        
        // L'implémentation Argon2Factory.create().hash() finit par appeler wipeArray
        // On vérifie que le tableau n'est pas "secret" mais contient des zéros (ou est modifié)
        boolean allNull = true;
        for (char c : password) {
            if (c != '\0') {
                allNull = false;
                break;
            }
        }
        assertTrue(allNull, "Le tableau de caractères doit être effacé (mis à zéro)");
    }
}
