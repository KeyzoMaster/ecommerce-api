package com.lemzo.ecommerce.domain.inventory.service;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.core.inventory.InventoryPort;
import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import com.lemzo.ecommerce.domain.inventory.repository.StockRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Service de gestion des stocks.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class InventoryService implements InventoryPort {

    private static final Logger LOGGER = Logger.getLogger(InventoryService.class.getName());
    private final StockRepository stockRepository;

    @Override
    public boolean isAvailable(final UUID productId, final int requestedQuantity) {
        return stockRepository.findByProductId(productId)
                .map(stock -> stock.getQuantity() >= requestedQuantity)
                .orElse(false);
    }

    @Override
    @Transactional
    public void updateStock(final UUID productId, final int quantityChange) {
        final Optional<Stock> existing = stockRepository.findByProductId(productId);
        
        final Stock stock = existing.orElseGet(() -> new Stock(productId, 0));

        final int newQuantity = stock.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new BusinessRuleException("error.inventory.insufficient_stock");
        }

        stock.setQuantity(newQuantity);
        
        if (existing.isPresent()) {
            stockRepository.update(stock);
        } else {
            stockRepository.insert(stock);
        }
        
        checkLowStock(stock);
    }

    private void checkLowStock(final Stock stock) {
        if (stock.getQuantity() <= stock.getLowStockThreshold()) {
            LOGGER.warning(() -> "STOCK FAIBLE détecté pour le produit : " + stock.getProductId());
        }
    }

    public List<Stock> getLowStocks() {
        return stockRepository.findLowStocks();
    }

    @Transactional
    public Stock setStock(final UUID productId, final int quantity, final Integer threshold) {
        final Optional<Stock> existing = stockRepository.findByProductId(productId);
        final Stock stock = existing.orElseGet(() -> new Stock(productId, 0));
        
        stock.setQuantity(quantity);
        Optional.ofNullable(threshold).ifPresent(stock::setLowStockThreshold);
        
        if (existing.isPresent()) {
            return stockRepository.update(stock);
        } else {
            return stockRepository.insert(stock);
        }
    }

    public Optional<Stock> getStockByProduct(final UUID productId) {
        return stockRepository.findByProductId(productId);
    }
}
