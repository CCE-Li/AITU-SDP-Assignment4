package kz.aitu.sdp.order.subsystem;

/**
 * Proof of a successful charge. It stays inside the subsystem: the facade only uses
 * it to refund the customer when an order has to be undone.
 */
public record PaymentReceipt(String id, String customerId, double amount) {
}
