package com.lemzo.ecommerce.domain.core.catalog;

import java.util.Optional;
import java.util.UUID;

/**
 * Port pour les opérations liées au catalogue.
 */
public interface CatalogPort {
    Optional<? extends Object> findProductById(UUID productId);
}
