package kz.aitu.sdp.order.subsystem;

import java.util.ArrayList;
import java.util.List;

import kz.aitu.sdp.order.domain.Customer;
import kz.aitu.sdp.order.domain.Order;

/**
 * Subsystem service that is the only one talking to the customer. Printing stands in
 * for sending the e-mail; the messages are kept so that they can be inspected.
 */
public class NotificationService {

    private final List<String> sentMessages = new ArrayList<>();

    public void sendOrderConfirmed(Customer customer, Order order) {
        send(customer, "Order " + order.id() + " is confirmed. Tracking number: " + order.trackingNumber());
    }

    public void sendOrderCancelled(Customer customer, Order order) {
        send(customer, "Order " + order.id() + " was cancelled and the money refunded.");
    }

    public List<String> sentMessages() {
        return List.copyOf(sentMessages);
    }

    private void send(Customer customer, String message) {
        String entry = "To " + customer.email() + ": " + message;
        sentMessages.add(entry);
        System.out.println("[notification] " + entry);
    }
}
