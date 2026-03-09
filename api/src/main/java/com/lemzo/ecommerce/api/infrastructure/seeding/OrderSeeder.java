package com.lemzo.ecommerce.api.infrastructure.seeding;

import com.lemzo.ecommerce.core.api.seeding.DataSeeder;
import com.lemzo.ecommerce.domain.core.iam.UserPort;
import com.lemzo.ecommerce.domain.catalog.domain.Product;
import com.lemzo.ecommerce.domain.catalog.repository.ProductRepository;
import com.lemzo.ecommerce.domain.sales.domain.Order;
import com.lemzo.ecommerce.domain.sales.domain.OrderItem;
import com.lemzo.ecommerce.domain.sales.domain.SalesFactory;
import com.lemzo.ecommerce.domain.sales.repository.OrderRepository;
import com.lemzo.ecommerce.iam.domain.User;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Seeder pour générer un historique de commandes.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class OrderSeeder implements DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(OrderSeeder.class.getName());

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserPort userPort;

    @Override
    @Transactional
    public void seed() {
        if (orderRepository.count() > 0) {
            return;
        }

        LOGGER.info("Seeding Orders history...");

        // Correction PageRequest signature (Jakarta Data 1.0)
        final var productsPage = productRepository.findAll(PageRequest.ofPage(1, 10, true));
        final var products = productsPage.content();
        
        final User client = (User) userPort.findByIdentifier("admin").orElseThrow();

        if (products.size() >= 2) {
            createFakeOrder(client, products.get(0), 2);
            createFakeOrder(client, products.get(1), 1);
        }

        LOGGER.info("Orders seeding completed.");
    }

    private void createFakeOrder(final User user, final Product product, final int qty) {
        final Order order = SalesFactory.createOrder(user.getId(), "SEED-ORD-" + System.nanoTime());
        order.setStatus(Order.OrderStatus.DELIVERED);
        
        final OrderItem item = SalesFactory.createItem(
                product.getId(), product.getStoreId(), qty, product.getPrice());
        
        order.addItem(item);
        order.setTotalAmount(product.getPrice().multiply(new BigDecimal(qty)));
        
        if (!user.getAddresses().isEmpty()) {
            order.setShippingAddress(user.getAddresses().get(0));
        }
        
        orderRepository.insert(order);
    }

    @Override
    public int priority() {
        return 3;
    }
}
