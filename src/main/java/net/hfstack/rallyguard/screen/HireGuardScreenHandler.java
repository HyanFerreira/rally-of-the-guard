package net.hfstack.rallyguard.screen;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentService;
import net.hfstack.rallyguard.api.recruitment.RecruitmentResult;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;

public class HireGuardScreenHandler extends AbstractContainerMenu {
    private static final GuardRecruitmentService RECRUITMENT = RallyGuardApi.guardRecruitment();
    private static final double MAX_USE_DISTANCE_SQUARED = 8.0 * 8.0;

    private final int guardEntityId;

    // Construtor CLIENTE (2 args) — usado pela fábrica registrada em ModScreenHandlers
    public HireGuardScreenHandler(int syncId, Inventory inv) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.guardEntityId = -1;
    }

    // Construtor SERVIDOR (3 args) — usado no SimpleMenuProvider
    public HireGuardScreenHandler(int syncId, Inventory inv, int guardEntityId) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.guardEntityId = guardEntityId;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }

        Entity entity = serverPlayer.level().getEntity(guardEntityId);
        return entity instanceof GuardEntity guard
                && guard.isAlive()
                && !guard.isRemoved()
                && !GuardOwnership.hasOwner(guard)
                && serverPlayer.distanceToSqr(guard) <= MAX_USE_DISTANCE_SQUARED;
    }

    /**
     * id == 0 => botão "Contratar"
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer sp)) return false;
        if (id != 0) return false;

        ServerLevel world = sp.level();
        Entity entity = world.getEntity(this.guardEntityId);

        if (!(entity instanceof GuardEntity guard)) {
            sp.closeContainer();
            sp.sendOverlayMessage(Component.translatable("gui.rallyguard.hire.invalid"));
            return true;
        }

        RecruitmentResult result = RECRUITMENT.recruit(sp, guard);
        sp.sendOverlayMessage(result.feedback());
        sp.closeContainer();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }
}
