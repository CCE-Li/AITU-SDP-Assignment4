package kz.aitu.sdp.order.subsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import kz.aitu.sdp.order.DemoShop;
import kz.aitu.sdp.order.domain.Cart;

/**
 * The carrier and the delivery time are decided by the shipping rules, not by the
 * facade, so they are tested here directly.
 */
class ShippingServiceTest {

    private final ShippingService shipping = new ShippingService();

    @Test
    void sendsLightParcelsWithTheCourier() throws UnsupportedDestinationException {
        Cart cart = new Cart();
        cart.add(DemoShop.MOUSE, 2);

        Shipment shipment = shipping.schedule(cart, "Almaty");

        assertEquals("KazPost Express", shipment.carrier());
        assertEquals(1, shipment.estimatedDeliveryDays());
        assertTrue(shipment.trackingNumber().startsWith("TRK-"));
    }

    @Test
    void sendsHeavyParcelsWithFreight() throws UnsupportedDestinationException {
        Cart cart = new Cart();
        cart.add(DemoShop.MONITOR, 1);

        assertEquals("DHL Freight", shipping.schedule(cart, "Astana").carrier());
    }

    @Test
    void refusesCitiesThatAreNotServed() {
        Cart cart = new Cart();
        cart.add(DemoShop.MOUSE, 1);

        assertThrows(UnsupportedDestinationException.class, () -> shipping.schedule(cart, "Semey"));
    }
}
