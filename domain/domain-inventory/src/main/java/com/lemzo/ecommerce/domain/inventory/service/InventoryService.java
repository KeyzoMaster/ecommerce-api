package com.lemzo.ecommerce.domain.inventory.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.core.inventory.InventoryPort;
import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import com.lemzo.ecommerce.domain.inventory.repository.StockRepository;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de gestion des stocks.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class InventoryService implements InventoryPort {

    private final StockRepository stockRepository;

    public Optional<Stock> getStockByProduct(final UUID productId) {
        return stockRepository.findByProductId(productId);
    }

    public List<Stock> getLowStocks() {
        // Limitation à une grande page par défaut car findAll requiert PageRequest
        final var page = stockRepository.findAll(PageRequest.ofPage(1, 1000, true));
        return page.content().stream()
                .filter(s -> s.getQuantity() <= s.getLowStockThreshold())
                .collect(Collectors.toList());
    }

    @Transactional
    @Audit(action = "STOCK_UPDATE")
    public Stock setStock(final UUID productId, final int quantity, final Integer threshold) {
        final var stock = stockRepository.findByProductId(productId)
                .orElse(new Stock(productId, 0));

        stock.setQuantity(quantity);
        if (threshold != null) {
            stock.setLowStockThreshold(threshold);
        }

        return stock.getId() == null ? stockRepository.insert(stock) : stockRepository.update(stock);
    }

    // --- IMPLÉMENTATION DU PORT INVENTORYPORT ---

    @Override
    public boolean isAvailable(final UUID productId, final int quantity) {
        return stockRepository.findByProductId(productId)
                .map(stock -> stock.getQuantity() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional
    @Audit(action = "STOCK_INCREASE_INTERNAL")
    public void increaseStock(final UUID productId, final int quantity) {
        if (quantity < 0) {
            throw new BusinessRuleException("La quantité à ajouter ne peut pas être négative");
        }
        final Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessRuleException("Stock introuvable pour le produit : " + productId));

        stock.setQuantity(stock.getQuantity() + quantity);
        stockRepository.update(stock);
    }

    @Override
    @Transactional
    @Audit(action = "STOCK_DECREASE_INTERNAL")
    public void decreaseStock(final UUID productId, final int quantity) {
        if (quantity < 0) {
            throw new BusinessRuleException("La quantité à soustraire ne peut pas être négative");
        }
        final Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessRuleException("Stock introuvable pour le produit : " + productId));

        final int newQuantity = stock.getQuantity() - quantity;

        if (newQuantity < 0) {
            throw new BusinessRuleException("La quantité en stock ne peut pas être négative");
        }

        stock.setQuantity(newQuantity);
        stockRepository.update(stock);
    }
}