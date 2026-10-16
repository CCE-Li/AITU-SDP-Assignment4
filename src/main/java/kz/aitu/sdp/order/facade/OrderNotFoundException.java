package kz.aitu.sdp.order.facade;

/**
 * Raised when the client asks about an order the facade never registered.
 */
public class OrderNotFoundException extends Exception {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
