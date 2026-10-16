package kz.aitu.sdp.order;

import kz.aitu.sdp.order.facade.OrderFacade;
import kz.aitu.sdp.order.facade.OrderSupportFacade;
import kz.aitu.sdp.order.subsystem.InventoryService;
import kz.aitu.sdp.order.subsystem.NotificationService;
import kz.aitu.sdp.order.subsystem.PaymentProcessor;
import kz.aitu.sdp.order.subsystem.ShippingService;

/**
 * Composition root: the only place that knows the subsystem classes. It builds them
 * once, hands them to the facades and gives the client nothing but facades - so both
 * facades work on the same stock and the same accounts.
 */
public class OrderSystem {

    private final InventoryService inventory;
    private final PaymentProcessor payments;
    private final ShippingService shipping;

    private final OrderFacade orderFacade;
    private final OrderSupportFacade supportFacade;

    public OrderSystem() {
        inventory = new InventoryService();
        payments = new PaymentProcessor();
        shipping = new ShippingService();
        NotificationService notifications = new NotificationService();

        orderFacade = new OrderFacade(inventory, payments, shipping, notifications);
        supportFacade = new OrderSupportFacade(inventory, payments, shipping);
    }

    /**
     * A shop stocked with the demo products and customers, ready to run.
     */
    public static OrderSystem sampleShop() {
        OrderSystem shop = new OrderSystem();

        shop.inventory.restock(DemoShop.LAPTOP.id(), 5);
        shop.inventory.restock(DemoShop.MOUSE.id(), 20);
        shop.inventory.restock(DemoShop.MONITOR.id(), 3);
        shop.inventory.restock(DemoShop.KEYBOARD.id(), 10);

        shop.payments.topUp(DemoShop.ALICE.id(), 1_000_000);
        shop.payments.topUp(DemoShop.BOB.id(), 20_000);
        shop.payments.topUp(DemoShop.CAROL.id(), 200_000);

        return shop;
    }

    public OrderFacade orders() {
        return orderFacade;
    }

    public OrderSupportFacade support() {
        return supportFacade;
    }
}
