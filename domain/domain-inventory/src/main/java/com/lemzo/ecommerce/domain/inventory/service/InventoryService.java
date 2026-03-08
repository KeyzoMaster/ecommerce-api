package com.lemzo.ecommerce.domain.inventory.service;

import com.lemzo.ecommerce.core.annotation.Audit;
import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import com.lemzo.ecommerce.domain.inventory.domain.Stock;
import com.lemzo.ecommerce.domain.inventory.repository.StockRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Service de gestion des stocks.
 */
@ApplicationScoped
public class InventoryService {

    private static final Logger LOGGER = Logger.getLogger(InventoryService.class.getName());

    @Inject
    private StockRepository stockRepository;

    /**
     * Vérifie si un produit est disponible en quantité suffisante.
     */
    public boolean isAvailable(UUID productId, int requestedQuantity) {
        return stockRepository.findByProductId(productId)
                .map(stock -> stock.getQuantity() >= requestedQuantity)
                .orElse(false);
    }

    /**
     * Met à jour le stock d'un produit.
     */
    @Transactional
    @Audit(action = "STOCK_UPDATE")
    public void updateStock(UUID productId, int quantityChange) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseGet(() -> {
                    Stock newStock = new Stock(productId, 0);
                    return stockRepository.insert(newStock);
                });

        int newQuantity = stock.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new BusinessRuleException("error.inventory.insufficient_stock");
        }

        stock.setQuantity(newQuantity);
        stockRepository.save(stock);

        checkThreshold(stock);
    }

    private void checkThreshold(Stock stock) {
        if (stock.getQuantity() <= stock.getLowStockThreshold()) {
            LOGGER.warning(String.format("ALERTE STOCK BAS : Produit %s, Quantité restante : %d (Seuil : %d)", 
                    stock.getProductId(), stock.getQuantity(), stock.getLowStockThreshold()));
            // Ici, on pourrait déclencher un événement CDI pour envoyer un email ou une notification
        }
    }

    /**
     * Réserve du stock pour une commande.
     */
    @Transactional
    @Audit(action = "STOCK_RESERVE")
    public void reserveStock(UUID productId, int quantity) {
        if (!isAvailable(productId, quantity)) {
            throw new BusinessRuleException("error.inventory.insufficient_stock");
        }
        updateStock(productId, -quantity);
    }

    /**
     * Récupère la liste des stocks bas.
     */
    public List<Stock> getLowStocks() {
        return stockRepository.findLowStocks();
    }
}
