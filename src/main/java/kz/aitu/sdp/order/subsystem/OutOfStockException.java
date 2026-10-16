package kz.aitu.sdp.order.subsystem;

/**
 * Raised by {@link InventoryService} when the warehouse cannot cover a reservation.
 */
public class OutOfStockException extends Exception {

    public OutOfStockException(String message) {
        super(message);
    }
}
