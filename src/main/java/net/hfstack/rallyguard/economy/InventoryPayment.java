package net.hfstack.rallyguard.economy;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class InventoryPayment {
    private InventoryPayment() {
    }

    public static int count(Container inventory, Item item) {
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(item, "item");
        return count(new ContainerSlots(inventory, item));
    }

    static int count(Slots inventory) {
        int total = 0;
        for (int slot = 0; slot < inventory.size(); slot++) {
            if (inventory.matches(slot)) total += inventory.count(slot);
        }
        return total;
    }

    public static void remove(Container inventory, Item item, int amount) {
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(item, "item");
        remove(new ContainerSlots(inventory, item), amount);
    }

    static void remove(Slots inventory, int amount) {
        int remaining = Math.max(0, amount);
        for (int slot = 0; slot < inventory.size() && remaining > 0; slot++) {
            if (!inventory.matches(slot)) continue;
            int removed = Math.min(remaining, inventory.count(slot));
            inventory.shrink(slot, removed);
            remaining -= removed;
        }
    }

    public static void refund(Inventory inventory, Item item, int amount) {
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(item, "item");
        int safeAmount = Math.max(0, amount);
        if (safeAmount > 0) inventory.placeItemBackInInventory(new ItemStack(item, safeAmount));
    }

    interface Slots {
        int size();

        boolean matches(int slot);

        int count(int slot);

        void shrink(int slot, int amount);
    }

    private record ContainerSlots(Container inventory, Item item) implements Slots {
        @Override
        public int size() {
            return inventory.getContainerSize();
        }

        @Override
        public boolean matches(int slot) {
            return inventory.getItem(slot).is(item);
        }

        @Override
        public int count(int slot) {
            return inventory.getItem(slot).getCount();
        }

        @Override
        public void shrink(int slot, int amount) {
            inventory.getItem(slot).shrink(amount);
        }
    }
}
