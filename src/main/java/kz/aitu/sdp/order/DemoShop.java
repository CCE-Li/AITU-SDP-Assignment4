package kz.aitu.sdp.order;

import kz.aitu.sdp.order.domain.Customer;
import kz.aitu.sdp.order.domain.Product;

/**
 * The products and customers of the demo shop, defined in one place so that the ids
 * used by the sample data and by the client can never drift apart.
 */
public final class DemoShop {

    public static final Product LAPTOP = new Product("P-1", "Laptop", 450_000, 2.0);
    public static final Product MOUSE = new Product("P-2", "Wireless Mouse", 12_000, 0.2);
    public static final Product MONITOR = new Product("P-3", "27-inch Monitor", 130_000, 5.5);
    public static final Product KEYBOARD = new Product("P-4", "Mechanical Keyboard", 35_000, 0.9);

    public static final Customer ALICE = new Customer("C-1", "Alice", "alice@example.com", "Almaty");
    public static final Customer BOB = new Customer("C-2", "Bob", "bob@example.com", "Astana");
    public static final Customer CAROL = new Customer("C-3", "Carol", "carol@example.com", "Semey");

    private DemoShop() {
    }
}
