package kz.aitu.sdp.order.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kz.aitu.sdp.order.DemoShop;
import kz.aitu.sdp.order.domain.Cart;
import kz.aitu.sdp.order.domain.Order;
import kz.aitu.sdp.order.domain.OrderStatus;
import kz.aitu.sdp.order.subsystem.InventoryService;
import kz.aitu.sdp.order.subsystem.NotificationService;
import kz.aitu.sdp.order.subsystem.PaymentProcessor;
import kz.aitu.sdp.order.subsystem.ShippingService;
import kz.aitu.sdp.order.subsystem.UnsupportedDestinationException;

/**
 * The tests behave like an advanced client: they wire the subsystem themselves and
 * check what the facade left behind in it on every path.
 */
class OrderFacadeTest {

    private static final double ALICE_BUDGET = 1_000_000;
    private static final double BOB_BUDGET = 20_000;
    private static final double CAROL_BUDGET = 200_000;
    private static final double DELTA = 0.01;

    private InventoryService inventory;
    private PaymentProcessor payments;
    private NotificationService notifications;
    private OrderFacade orders;

    @BeforeEach
    void setUp() {
        inventory = new InventoryService();
        payments = new PaymentProcessor();
        notifications = new NotificationService();
        orders = new OrderFacade(inventory, payments, new ShippingService(), notifications);

        inventory.restock(DemoShop.LAPTOP.id(), 5);
        inventory.restock(DemoShop.MOUSE.id(), 20);
        inventory.restock(DemoShop.MONITOR.id(), 3);
        inventory.restock(DemoShop.KEYBOARD.id(), 10);

        payments.topUp(DemoShop.ALICE.id(), ALICE_BUDGET);
        payments.topUp(DemoShop.BOB.id(), BOB_BUDGET);
        payments.topUp(DemoShop.CAROL.id(), CAROL_BUDGET);
    }

    @Test
    void placingAnOrderRunsTheWholePipeline() throws OrderRejectedException {
        Cart cart = new Cart();
        cart.add(DemoShop.LAPTOP, 1);
        cart.add(DemoShop.MOUSE, 2);

        Order order = orders.placeOrder(cart, DemoShop.ALICE);

        assertEquals("ORD-1", order.id());
        assertEquals(OrderStatus.CONFIRMED, order.status());
        assertEquals(474_000, order.total(), DELTA);
        assertTrue(order.trackingNumber().startsWith("TRK-"));
        assertEquals(4, inventory.stockLevel(DemoShop.LAPTOP.id()));
        assertEquals(ALICE_BUDGET - 474_000, payments.balanceOf(DemoShop.ALICE.id()), DELTA);
        assertEquals(1, notifications.sentMessages().size());
    }

    @Test
    void missingStockRejectsTheOrderAndChangesNothing() {
        Cart cart = new Cart();
        cart.add(DemoShop.LAPTOP, 6);

        assertThrows(OrderRejectedException.class, () -> orders.placeOrder(cart, DemoShop.ALICE));

        assertEquals(5, inventory.stockLevel(DemoShop.LAPTOP.id()));
        assertEquals(ALICE_BUDGET, payments.balanceOf(DemoShop.ALICE.id()), DELTA);
        assertTrue(notifications.sentMessages().isEmpty());
    }

    @Test
    void declinedPaymentReleasesTheReservedStock() {
        Cart cart = new Cart();
        cart.add(DemoShop.MONITOR, 1);

        OrderRejectedException rejected =
                assertThrows(OrderRejectedException.class, () -> orders.placeOrder(cart, DemoShop.BOB));

        assertTrue(rejected.getMessage().contains("Insufficient funds"));
        assertEquals(3, inventory.stockLevel(DemoShop.MONITOR.id()));
        assertEquals(BOB_BUDGET, payments.balanceOf(DemoShop.BOB.id()), DELTA);
        assertTrue(notifications.sentMessages().isEmpty());
    }

    @Test
    void rejectedOrderIsNeverRegistered() {
        Cart cart = new Cart();
        cart.add(DemoShop.MONITOR, 1);

        assertThrows(OrderRejectedException.class, () -> orders.placeOrder(cart, DemoShop.BOB));
        assertThrows(OrderNotFoundException.class, () -> orders.cancelOrder("ORD-1"));
    }

    @Test
    void unservedDestinationRefundsThePayment() {
        Cart cart = new Cart();
        cart.add(DemoShop.KEYBOARD, 1);

        OrderRejectedException rejected =
                assertThrows(OrderRejectedException.class, () -> orders.placeOrder(cart, DemoShop.CAROL));

        assertInstanceOf(UnsupportedDestinationException.class, rejected.getCause());
        assertEquals(10, inventory.stockLevel(DemoShop.KEYBOARD.id()));
        assertEquals(CAROL_BUDGET, payments.balanceOf(DemoShop.CAROL.id()), DELTA);
    }

    @Test
    void cancellingAnOrderReturnsStockAndMoney() throws OrderRejectedException, OrderNotFoundException {
        Cart cart = new Cart();
        cart.add(DemoShop.LAPTOP, 1);
        Order order = orders.placeOrder(cart, DemoShop.ALICE);

        Order cancelled = orders.cancelOrder(order.id());

        assertEquals(OrderStatus.CANCELLED, cancelled.status());
        assertEquals(5, inventory.stockLevel(DemoShop.LAPTOP.id()));
        assertEquals(ALICE_BUDGET, payments.balanceOf(DemoShop.ALICE.id()), DELTA);
        assertEquals(2, notifications.sentMessages().size());
    }

    @Test
    void cancellingAnUnknownOrderFails() {
        assertThrows(OrderNotFoundException.class, () -> orders.cancelOrder("ORD-404"));
    }

    @Test
    void cancellingTwiceFails() throws OrderRejectedException, OrderNotFoundException {
        Cart cart = new Cart();
        cart.add(DemoShop.LAPTOP, 1);
        String orderId = orders.placeOrder(cart, DemoShop.ALICE).id();

        orders.cancelOrder(orderId);

        assertThrows(IllegalStateException.class, () -> orders.cancelOrder(orderId));
    }

    @Test
    void emptyCartIsRejected() {
        assertThrows(OrderRejectedException.class, () -> orders.placeOrder(new Cart(), DemoShop.ALICE));

        assertEquals(5, inventory.stockLevel(DemoShop.LAPTOP.id()));
        assertEquals(ALICE_BUDGET, payments.balanceOf(DemoShop.ALICE.id()), DELTA);
        assertTrue(notifications.sentMessages().isEmpty());
    }
}
