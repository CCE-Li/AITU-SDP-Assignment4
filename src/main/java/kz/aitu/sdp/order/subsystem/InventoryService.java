package kz.aitu.sdp.order.subsystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Subsystem service that owns the stock ledger: how many units of every product sit
 * in the warehouse, how many are taken by open orders and how many come back when an
 * order is undone. It knows nothing about payments, shipping or the facade.
 */
public class InventoryService {

    private final Map<String, Integer> unitsByProductId = new HashMap<>();

    /**
     * Adds units to the warehouse, as a delivery would.
     */
    public void restock(String productId, int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Units to restock must be positive, but were " + units);
        }
        unitsByProductId.merge(productId, units, Integer::sum);
    }

    /**
     * Takes units out of the warehouse for an order.
     *
     * @throws OutOfStockException when the warehouse holds fewer units than requested,
     *                             in which case nothing is taken
     */
    public void reserve(String productId, int quantity) throws OutOfStockException {
        int available = stockLevel(productId);
        if (available < quantity) {
            throw new OutOfStockException("Only " + available + " unit(s) of " + productId
                    + " in stock, but " + quantity + " requested");
        }
        unitsByProductId.put(productId, available - quantity);
    }

    /**
     * Puts previously reserved units back into the warehouse.
     */
    public void release(String productId, int quantity) {
        unitsByProductId.merge(productId, quantity, Integer::sum);
    }

    public int stockLevel(String productId) {
        return unitsByProductId.getOrDefault(productId, 0);
    }
}
