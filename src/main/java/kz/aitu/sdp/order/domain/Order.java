package kz.aitu.sdp.order.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A placed order as the facade hands it back to the client, so it carries business
 * data only: never a receipt, a shipment or any other subsystem object.
 */
public record Order(String id, Customer customer, Map<Product, Integer> items,
                    double total, String trackingNumber, OrderStatus status) {

    public Order {
        // Defensive copy: editing the cart afterwards must not rewrite the order.
        items = Collections.unmodifiableMap(new LinkedHashMap<>(items));
    }

    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, customer, items, total, trackingNumber, newStatus);
    }
}
