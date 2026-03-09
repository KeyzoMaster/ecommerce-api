package com.lemzo.ecommerce.core.api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires pour PbacAction.
 */
@DisplayName("PbacAction Unit Tests")
class PbacActionTest {

    @Test
    @DisplayName("Should include MANAGE in required possessed actions for any action")
    void shouldIncludeManageInRequiredActions() {
        final Set<PbacAction> requiredForRead = PbacAction.READ.getRequiredPossessedActions();
        final Set<PbacAction> requiredForCreate = PbacAction.CREATE.getRequiredPossessedActions();

        assertTrue(requiredForRead.contains(PbacAction.MANAGE));
        assertTrue(requiredForCreate.contains(PbacAction.MANAGE));
    }

    @Test
    @DisplayName("READ should be granted by UPDATE, CREATE or DELETE")
    void shouldGrantReadViaImplicitPermissions() {
        final Set<PbacAction> required = PbacAction.READ.getRequiredPossessedActions();

        assertTrue(required.contains(PbacAction.UPDATE), "UPDATE should grant READ");
        assertTrue(required.contains(PbacAction.CREATE), "CREATE should grant READ");
        assertTrue(required.contains(PbacAction.DELETE), "DELETE should grant READ");
    }

    @Test
    @DisplayName("Action should always require itself")
    void shouldIncludeSelfInRequiredActions() {
        for (final PbacAction action : PbacAction.values()) {
            assertTrue(action.getRequiredPossessedActions().contains(action), 
                action.name() + " should require itself");
        }
    }
}
