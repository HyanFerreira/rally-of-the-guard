package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class HireGuardScreenHandler extends ScreenHandler {

    private final int guardEntityId; // só no servidor
    private static final int PRICE = 3;

    // CLIENTE: sem id
    public HireGuardScreenHandler(int syncId, PlayerInventory inv) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.guardEntityId = -1;
    }

    // SERVIDOR: com id
    public HireGuardScreenHandler(int syncId, PlayerInventory inv, int guardEntityId) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.guardEntityId = guardEntityId;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    // Sem slots -> EMPTY
    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id != 0) return false; // 0 = contratar
        if (!(player instanceof ServerPlayerEntity sp)) return true;

        Entity guard = (guardEntityId >= 0) ? sp.getWorld().getEntityById(guardEntityId) : null;
        if (guard == null || !GuardOwnership.isGuard(guard)) {
            sp.closeHandledScreen();
            return true;
        }

        if (GuardOwnership.isOwnedBy(guard, sp.getUuid())) {
            sp.sendMessage(Text.translatable("gui.rallyguard.hire.already_owned"), true);
            return true;
        }

        if (!sp.getAbilities().creativeMode && !sp.getInventory().contains(new ItemStack(Items.EMERALD, PRICE))) {
            sp.sendMessage(Text.translatable("gui.rallyguard.hire.not_enough"), true);
            return true;
        }

        if (!sp.getAbilities().creativeMode) {
            int remaining = PRICE;
            for (int i = 0; i < sp.getInventory().size() && remaining > 0; i++) {
                ItemStack s = sp.getInventory().getStack(i);
                if (!s.isOf(Items.EMERALD)) continue;
                int take = Math.min(remaining, s.getCount());
                s.decrement(take);
                remaining -= take;
            }
        }

        GuardOwnership.setOwner(guard, sp);
        sp.sendMessage(Text.translatable("gui.rallyguard.hire.success"), false);
        sp.closeHandledScreen();
        return true;
    }
}
