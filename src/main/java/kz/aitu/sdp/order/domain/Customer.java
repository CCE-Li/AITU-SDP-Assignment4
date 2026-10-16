package kz.aitu.sdp.order.domain;

/**
 * A customer, including the city the parcel has to reach.
 */
public record Customer(String id, String name, String email, String city) {
}
