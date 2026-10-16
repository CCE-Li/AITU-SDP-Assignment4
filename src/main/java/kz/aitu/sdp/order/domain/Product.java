package kz.aitu.sdp.order.domain;

/**
 * A product of the shop. Price and parcel weight are the only things the ordering
 * pipeline needs from it.
 */
public record Product(String id, String name, double price, double weightKg) {
}
