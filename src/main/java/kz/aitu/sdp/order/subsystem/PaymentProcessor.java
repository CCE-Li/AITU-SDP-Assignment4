package kz.aitu.sdp.order.subsystem;

import java.util.HashMap;
import java.util.Map;

import kz.aitu.sdp.order.domain.Customer;

/**
 * Subsystem service that stands in for the bank gateway: it keeps one account per
 * customer and can charge it or pay money back. It has no idea why it is being
 * asked to move money.
 */
public class PaymentProcessor {

    private final Map<String, Double> balanceByCustomerId = new HashMap<>();
    private int receiptCounter;

    /**
     * Puts money into the customer's account, opening it when it does not exist yet.
     */
    public void topUp(String customerId, double amount) {
        balanceByCustomerId.merge(customerId, amount, Double::sum);
    }

    /**
     * Charges the amount to the customer's account.
     *
     * @throws PaymentDeclinedException when the account holds less than the amount,
     *                                  in which case no money moves
     */
    public PaymentReceipt charge(Customer customer, double amount) throws PaymentDeclinedException {
        double balance = balanceOf(customer.id());
        if (amount > balance) {
            throw new PaymentDeclinedException(String.format(
                    "Insufficient funds for %s: needed %.2f, available %.2f",
                    customer.name(), amount, balance));
        }
        balanceByCustomerId.put(customer.id(), balance - amount);
        return new PaymentReceipt("RCPT-" + (++receiptCounter), customer.id(), amount);
    }

    /**
     * Pays the money of a receipt back to the customer.
     */
    public void refund(PaymentReceipt receipt) {
        balanceByCustomerId.merge(receipt.customerId(), receipt.amount(), Double::sum);
    }

    public double balanceOf(String customerId) {
        return balanceByCustomerId.getOrDefault(customerId, 0.0);
    }
}
