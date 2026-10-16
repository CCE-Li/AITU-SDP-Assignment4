package kz.aitu.sdp.order.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import kz.aitu.sdp.order.DemoShop;
import kz.aitu.sdp.order.OrderSystem;
import kz.aitu.sdp.order.domain.Cart;

/**
 * The second facade reads the very same subsystem instances the first one writes to,
 * which is what the composition root guarantees.
 */
class OrderSupportFacadeTest {

    private static final double DELTA = 0.01;

    @Test
    void seesTheStockTheOrderFacadeChanged() throws OrderRejectedException {
        OrderSystem shop = OrderSystem.sampleShop();
        int stockBefore = shop.support().stockLevel(DemoShop.LAPTOP.id());

        Cart cart = new Cart();
        cart.add(DemoShop.LAPTOP, 2);
        shop.orders().placeOrder(cart, DemoShop.ALICE);

        assertEquals(stockBefore - 2, shop.support().stockLevel(DemoShop.LAPTOP.id()));
        assertFalse(shop.support().isAvailable(DemoShop.LAPTOP.id(), stockBefore));
    }

    @Test
    void reportsCitiesAndBalances() {
        OrderSupportFacade support = OrderSystem.sampleShop().support();

        assertTrue(support.servedCities().contains("Almaty"));
        assertFalse(support.servedCities().contains("Semey"));
        assertEquals(1_000_000, support.balanceOf(DemoShop.ALICE.id()), DELTA);
    }
}
