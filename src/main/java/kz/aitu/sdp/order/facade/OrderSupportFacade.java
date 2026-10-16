package kz.aitu.sdp.order.facade;

import java.util.Set;

import kz.aitu.sdp.order.subsystem.InventoryService;
import kz.aitu.sdp.order.subsystem.PaymentProcessor;
import kz.aitu.sdp.order.subsystem.ShippingService;

/**
 * Second, deliberately narrow facade for the read-only questions a support desk asks:
 * availability, delivery reach and account balance. They live here so that the
 * order-placement facade stays focused on placing and cancelling orders.
 */
public class OrderSupportFacade {

    private final InventoryService inventory;
    private final PaymentProcessor payments;
    private final ShippingService shipping;

    public OrderSupportFacade(InventoryService inventory, PaymentProcessor payments, ShippingService shipping) {
        this.inventory = inventory;
        this.payments = payments;
        this.shipping = shipping;
    }

    public int stockLevel(String productId) {
        return inventory.stockLevel(productId);
    }

    public boolean isAvailable(String productId, int quantity) {
        return inventory.stockLevel(productId) >= quantity;
    }

    public Set<String> servedCities() {
        return shipping.servedCities();
    }

    public double balanceOf(String customerId) {
        return payments.balanceOf(customerId);
    }
}
