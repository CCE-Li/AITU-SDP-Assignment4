package kz.aitu.sdp.order.subsystem;

/**
 * Raised by {@link PaymentProcessor} when the customer cannot pay the amount.
 */
public class PaymentDeclinedException extends Exception {

    public PaymentDeclinedException(String message) {
        super(message);
    }
}
