package kz.aitu.sdp.order.subsystem;

import java.util.Map;
import java.util.Set;

import kz.aitu.sdp.order.domain.Cart;

/**
 * Subsystem service that books the delivery: it knows which cities it serves, which
 * carrier takes a parcel of a given weight and how long a route usually takes.
 */
public class ShippingService {

    private static final double MAX_COURIER_PARCEL_KG = 5.0;
    private static final double BASE_FEE = 500;
    private static final double FEE_PER_KG = 120;

    private static final Map<String, Integer> DELIVERY_DAYS_BY_CITY = Map.of(
            "Almaty", 1,
            "Astana", 2,
            "Shymkent", 3,
            "Aktobe", 4);

    private int trackingCounter;

    /**
     * Books a shipment for the parcel described by the cart.
     *
     * @throws UnsupportedDestinationException when no carrier serves the city
     */
    public Shipment schedule(Cart cart, String destinationCity) throws UnsupportedDestinationException {
        Integer deliveryDays = DELIVERY_DAYS_BY_CITY.get(destinationCity);
        if (deliveryDays == null) {
            throw new UnsupportedDestinationException("No carrier serves " + destinationCity);
        }
        double parcelWeightKg = cart.totalWeightKg();
        String carrier = parcelWeightKg <= MAX_COURIER_PARCEL_KG ? "KazPost Express" : "DHL Freight";
        double cost = BASE_FEE + FEE_PER_KG * parcelWeightKg;
        return new Shipment("TRK-" + (++trackingCounter), carrier, cost, deliveryDays);
    }

    public Set<String> servedCities() {
        return DELIVERY_DAYS_BY_CITY.keySet();
    }
}
