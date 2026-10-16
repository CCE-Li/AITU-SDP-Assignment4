package kz.aitu.sdp.order.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kz.aitu.sdp.order.domain.Cart;
import kz.aitu.sdp.order.domain.Customer;
import kz.aitu.sdp.order.domain.Order;
import kz.aitu.sdp.order.domain.OrderStatus;
import kz.aitu.sdp.order.domain.Product;
import kz.aitu.sdp.order.subsystem.InventoryService;
import kz.aitu.sdp.order.subsystem.NotificationService;
import kz.aitu.sdp.order.subsystem.OutOfStockException;
import kz.aitu.sdp.order.subsystem.PaymentDeclinedException;
import kz.aitu.sdp.order.subsystem.PaymentProcessor;
import kz.aitu.sdp.order.subsystem.PaymentReceipt;
import kz.aitu.sdp.order.subsystem.Shipment;
import kz.aitu.sdp.order.subsystem.ShippingService;
import kz.aitu.sdp.order.subsystem.UnsupportedDestinationException;

/**
 * Facade over the whole order pipeline. The client calls the two business methods
 * {@link #placeOrder(Cart, Customer)} and {@link #cancelOrder(String)} and never sees
 * stock, payments, carriers or notifications. Internally the facade runs the four
 * subsystem steps in the right order and, when a later step fails, undoes the steps
 * that already succeeded.
 */
public class OrderFacade {

    private final InventoryService inventory;
    private final PaymentProcessor payments;
    private final ShippingService shipping;
    private final NotificationService notifications;

    private final Map<String, Order> ordersById = new HashMap<>();
    private final Map<String, PaymentReceipt> receiptsByOrderId = new HashMap<>();
    private int orderCounter;

    public OrderFacade(InventoryService inventory, PaymentProcessor payments,
                       ShippingService shipping, NotificationService notifications) {
        this.inventory = inventory;
        this.payments = payments;
        this.shipping = shipping;
        this.notifications = notifications;
    }

    /**
     * Places an order: reserves the stock, charges the customer, books the shipment
     * and notifies the customer. If a step fails, the steps before it are undone and
     * the client gets one order-level exception instead of a subsystem exception.
     *
     * @throws OrderRejectedException when a subsystem step refuses, after the rollback
     */
    public Order placeOrder(Cart cart, Customer customer) throws OrderRejectedException {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(customer, "customer");
        if (cart.isEmpty()) {
            throw new OrderRejectedException("Cart is empty, there is nothing to order");
        }

        String orderId = "ORD-" + (++orderCounter);
        List<StockReservation> reservations = new ArrayList<>();
        PaymentReceipt receipt = null;

        try {
            reserveStock(cart, reservations);
            receipt = payments.charge(customer, cart.total());
            Shipment shipment = shipping.schedule(cart, customer.city());

            Order order = new Order(orderId, customer, cart.items(), cart.total(),
                    shipment.trackingNumber(), OrderStatus.CONFIRMED);
            ordersById.put(orderId, order);
            receiptsByOrderId.put(orderId, receipt);
            notifications.sendOrderConfirmed(customer, order);

            return order;
        } catch (OutOfStockException | PaymentDeclinedException | UnsupportedDestinationException failure) {
            releaseStock(reservations);
            refund(receipt);
            throw new OrderRejectedException(orderId + " was rejected: " + failure.getMessage(), failure);
        }
    }

    /**
     * Cancels a placed order: gives the stock back, refunds the customer and tells them.
     *
     * @throws OrderNotFoundException when no order with that id exists
     */
    public Order cancelOrder(String orderId) throws OrderNotFoundException {
        Order order = ordersById.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException("No order with id " + orderId);
        }
        if (order.status() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(orderId + " is already cancelled");
        }

        releaseStock(reservationsOf(order));
        refund(receiptsByOrderId.remove(orderId));

        Order cancelled = order.withStatus(OrderStatus.CANCELLED);
        ordersById.put(orderId, cancelled);
        notifications.sendOrderCancelled(order.customer(), cancelled);

        return cancelled;
    }

    private void reserveStock(Cart cart, List<StockReservation> reservations) throws OutOfStockException {
        for (Map.Entry<Product, Integer> line : cart.items().entrySet()) {
            Product product = line.getKey();
            int quantity = line.getValue();
            inventory.reserve(product.id(), quantity);
            reservations.add(new StockReservation(product, quantity));
        }
    }

    private void releaseStock(List<StockReservation> reservations) {
        for (int i = reservations.size() - 1; i >= 0; i--) {
            StockReservation reservation = reservations.get(i);
            inventory.release(reservation.product().id(), reservation.quantity());
        }
    }

    private List<StockReservation> reservationsOf(Order order) {
        return order.items().entrySet().stream()
                .map(line -> new StockReservation(line.getKey(), line.getValue()))
                .toList();
    }

    private void refund(PaymentReceipt receipt) {
        if (receipt != null) {
            payments.refund(receipt);
        }
    }

    /**
     * Stock the facade has taken out of the warehouse, remembered so that it can be
     * given back when the order is undone.
     */
    private record StockReservation(Product product, int quantity) {
    }
}
