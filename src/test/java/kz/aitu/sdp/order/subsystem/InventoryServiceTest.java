package kz.aitu.sdp.order.subsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * The subsystem is testable on its own: no facade is needed, which shows that it does
 * not depend on the facade in any way.
 */
class InventoryServiceTest {

    private final InventoryService inventory = new InventoryService();

    @Test
    void reserveTakesUnitsOutOfTheWarehouse() throws OutOfStockException {
        inventory.restock("P-1", 3);

        inventory.reserve("P-1", 2);

        assertEquals(1, inventory.stockLevel("P-1"));
    }

    @Test
    void reserveFailsAndChangesNothingWhenThereAreTooFewUnits() {
        inventory.restock("P-1", 1);

        assertThrows(OutOfStockException.class, () -> inventory.reserve("P-1", 2));

        assertEquals(1, inventory.stockLevel("P-1"));
    }

    @Test
    void releasePutsUnitsBack() throws OutOfStockException {
        inventory.restock("P-1", 2);
        inventory.reserve("P-1", 2);

        inventory.release("P-1", 2);

        assertEquals(2, inventory.stockLevel("P-1"));
    }

    @Test
    void restockRejectsNonPositiveUnits() {
        assertThrows(IllegalArgumentException.class, () -> inventory.restock("P-1", 0));
    }
}
