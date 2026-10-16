package kz.aitu.sdp.order;

import kz.aitu.sdp.order.domain.Cart;
import kz.aitu.sdp.order.domain.Order;
import kz.aitu.sdp.order.facade.OrderFacade;
import kz.aitu.sdp.order.facade.OrderNotFoundException;
import kz.aitu.sdp.order.facade.OrderRejectedException;
import kz.aitu.sdp.order.facade.OrderSupportFacade;

/**
 * The client. It uses the two facades and plain domain objects and never mentions a
 * subsystem class - that is exactly what the pattern is for.
 */
public final class App {

    public static void main(String[] args) throws OrderRejectedException, OrderNotFoundException {
        OrderSystem shop = OrderSystem.sampleShop();
        OrderFacade orders = shop.orders();
        OrderSupportFacade support = shop.support();

        Order firstOrder = demoPlaceOrder(orders, support);
        demoDeclinedPayment(orders, support);
        demoUnsupportedDestination(orders, support);
        demoCancellation(orders, firstOrder, support);
        demoSupportDesk(support);

        System.out.println("\nDemo finished.");
    }

    private static Order demoPlaceOrder(OrderFacade orders, OrderSupportFacade support)
            throws OrderRejectedException {
        title("1. Place an order: one facade call instead of four subsystem steps");

        Cart cart = new Cart();
        cart.add(DemoShop.LAPTOP, 1);
        cart.add(DemoShop.MOUSE, 2);

        line("cart total", money(cart.total()));
        line("parcel weight", kilograms(cart.totalWeightKg()));
        line("laptops in stock before", support.stockLevel(DemoShop.LAPTOP.id()));
        line("Alice balance before", money(support.balanceOf(DemoShop.ALICE.id())));

        Order order = orders.placeOrder(cart, DemoShop.ALICE);

        line("order", order.id() + " / " + order.status());
        line("tracking number", order.trackingNumber());
        line("laptops in stock after", support.stockLevel(DemoShop.LAPTOP.id()));
        line("Alice balance after", money(support.balanceOf(DemoShop.ALICE.id())));

        return order;
    }

    private static void demoDeclinedPayment(OrderFacade orders, OrderSupportFacade support) {
        title("2. Payment declined: the facade gives the reserved stock back");

        Cart cart = new Cart();
        cart.add(DemoShop.MONITOR, 1);

        line("cart total", money(cart.total()));
        line("Bob balance", money(support.balanceOf(DemoShop.BOB.id())));
        line("monitors in stock before", support.stockLevel(DemoShop.MONITOR.id()));

        try {
            orders.placeOrder(cart, DemoShop.BOB);
            line("unexpected", "the order went through");
        } catch (OrderRejectedException rejected) {
            line("rejected", rejected.getMessage());
        }

        line("monitors in stock after", support.stockLevel(DemoShop.MONITOR.id()));
        line("Bob balance after", money(support.balanceOf(DemoShop.BOB.id())));
    }

    private static void demoUnsupportedDestination(OrderFacade orders, OrderSupportFacade support) {
        title("3. Destination not served: the charge is refunded and the stock returned");

        Cart cart = new Cart();
        cart.add(DemoShop.KEYBOARD, 1);

        line("customer city", DemoShop.CAROL.city());
        line("Carol balance before", money(support.balanceOf(DemoShop.CAROL.id())));
        line("keyboards in stock before", support.stockLevel(DemoShop.KEYBOARD.id()));

        try {
            orders.placeOrder(cart, DemoShop.CAROL);
            line("unexpected", "the order went through");
        } catch (OrderRejectedException rejected) {
            line("rejected", rejected.getMessage());
            line("cause kept", rejected.getCause().getMessage());
        }

        line("keyboards in stock after", support.stockLevel(DemoShop.KEYBOARD.id()));
        line("Carol balance after", money(support.balanceOf(DemoShop.CAROL.id())));
    }

    private static void demoCancellation(OrderFacade orders, Order order, OrderSupportFacade support)
            throws OrderNotFoundException {
        title("4. Cancel the order of step 1: the pipeline runs backwards");

        line("order", order.id() + " / " + order.status());
        line("laptops in stock", support.stockLevel(DemoShop.LAPTOP.id()));
        line("Alice balance", money(support.balanceOf(DemoShop.ALICE.id())));

        Order cancelled = orders.cancelOrder(order.id());

        line("after cancelOrder", cancelled.id() + " / " + cancelled.status());
        line("laptops in stock", support.stockLevel(DemoShop.LAPTOP.id()));
        line("Alice balance", money(support.balanceOf(DemoShop.ALICE.id())));
    }

    private static void demoSupportDesk(OrderSupportFacade support) {
        title("5. Second facade: read-only questions, no order involved");

        line("laptops in stock", support.stockLevel(DemoShop.LAPTOP.id()));
        line("20 laptops available?", support.isAvailable(DemoShop.LAPTOP.id(), 20));
        line("cities served", support.servedCities().stream().sorted().toList());
    }

    private static void title(String text) {
        System.out.println("\n=== " + text + " ===");
    }

    private static void line(String label, Object value) {
        System.out.printf("  %-26s %s%n", label, value);
    }

    private static String money(double amount) {
        return String.format("%,.2f KZT", amount);
    }

    private static String kilograms(double weight) {
        return String.format("%.1f kg", weight);
    }
}
