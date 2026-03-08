package com.lemzo.ecommerce.iam.domain;

import java.util.UUID;

/**
 * Factory pour la création d'utilisateurs de test.
 */
public class UserFactory {

    public static User createDefaultAdmin() {
        User user = new User("admin", "admin@lemzo.com", "admin1234");
        user.setEnabled(true);
        return user;
    }

    public static User createClient(String username) {
        User user = new User(username, username + "@example.com", "password123");
        user.setEnabled(true);
        return user;
    }
}
