package kz.aitu.sdp.order.subsystem;

/**
 * Raised by {@link ShippingService} when no carrier covers the destination.
 */
public class UnsupportedDestinationException extends Exception {

    public UnsupportedDestinationException(String message) {
        super(message);
    }
}
