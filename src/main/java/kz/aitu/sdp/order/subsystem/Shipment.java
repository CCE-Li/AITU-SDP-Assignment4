package kz.aitu.sdp.order.subsystem;

/**
 * A booked shipment. The facade copies the tracking number into the order and keeps
 * the rest of the booking to itself.
 */
public record Shipment(String trackingNumber, String carrier, double cost, int estimatedDeliveryDays) {
}
