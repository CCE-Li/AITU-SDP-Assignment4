package kz.aitu.sdp.order.facade;

/**
 * Raised when the facade cannot complete an order. Every step that already ran has
 * been undone before this exception leaves the facade, so a rejected order leaves no
 * half-finished state behind.
 */
public class OrderRejectedException extends Exception {

    public OrderRejectedException(String message) {
        super(message);
    }

    public OrderRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
