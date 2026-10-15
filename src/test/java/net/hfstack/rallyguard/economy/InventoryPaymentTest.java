package net.hfstack.rallyguard.economy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryPaymentTest {
    @Test
    void countsAndRemovesExactPaymentAcrossMultipleStacks() {
        TestSlots inventory = new TestSlots(
                new boolean[]{true, false, true},
                new int[]{2, 5, 4}
        );

        assertEquals(6, InventoryPayment.count(inventory));

        InventoryPayment.remove(inventory, 5);

        assertEquals(1, InventoryPayment.count(inventory));
        assertEquals(0, inventory.count(0));
        assertEquals(5, inventory.count(1));
        assertEquals(1, inventory.count(2));
    }

    @Test
    void oversizedRemovalConsumesAvailableItemsWithoutUnderflow() {
        TestSlots inventory = new TestSlots(new boolean[]{true}, new int[]{3});

        InventoryPayment.remove(inventory, 99);

        assertEquals(0, InventoryPayment.count(inventory));
        assertEquals(0, inventory.count(0));
    }

    @Test
    void zeroAndNegativeAmountsDoNotChangeInventory() {
        TestSlots inventory = new TestSlots(new boolean[]{true}, new int[]{3});

        InventoryPayment.remove(inventory, 0);
        InventoryPayment.remove(inventory, -4);

        assertEquals(3, InventoryPayment.count(inventory));
    }

    private static final class TestSlots implements InventoryPayment.Slots {
        private final boolean[] matching;
        private final int[] counts;

        private TestSlots(boolean[] matching, int[] counts) {
            this.matching = matching;
            this.counts = counts;
        }

        @Override
        public int size() {
            return counts.length;
        }

        @Override
        public boolean matches(int slot) {
            return matching[slot];
        }

        @Override
        public int count(int slot) {
            return counts[slot];
        }

        @Override
        public void shrink(int slot, int amount) {
            counts[slot] -= amount;
        }
    }
}
