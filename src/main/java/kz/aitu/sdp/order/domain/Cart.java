package kz.aitu.sdp.order.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What the customer wants to buy: products together with their quantities.
 * It knows how to add lines and how to total them up, and has no idea that stock,
 * payment or shipping exist.
 */
public final class Cart {

    private final Map<Product, Integer> quantityByProduct = new LinkedHashMap<>();

    public void add(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, but was " + quantity);
        }
        quantityByProduct.merge(product, quantity, Integer::sum);
    }

    public Map<Product, Integer> items() {
        return Collections.unmodifiableMap(quantityByProduct);
    }

    public boolean isEmpty() {
        return quantityByProduct.isEmpty();
    }

    public double total() {
        return quantityByProduct.entrySet().stream()
                .mapToDouble(line -> line.getKey().price() * line.getValue())
                .sum();
    }

    public double totalWeightKg() {
        return quantityByProduct.entrySet().stream()
                .mapToDouble(line -> line.getKey().weightKg() * line.getValue())
                .sum();
    }
}
