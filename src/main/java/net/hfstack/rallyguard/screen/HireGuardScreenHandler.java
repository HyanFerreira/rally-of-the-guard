package net.hfstack.rallyguard.screen;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentService;
import net.hfstack.rallyguard.api.recruitment.RecruitmentResult;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class HireGuardScreenHandler extends ScreenHandler {
    private static final GuardRecruitmentService RECRUITMENT = RallyGuardApi.guardRecruitment();
    private static final double MAX_USE_DISTANCE_SQUARED = 8.0 * 8.0;

    private final int guardEntityId;

    // Construtor CLIENTE (2 args) — usado pela fábrica registrada em ModScreenHandlers
    public HireGuardScreenHandler(int syncId, PlayerInventory inv) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.guardEntityId = -1;
    }

    // Construtor SERVIDOR (3 args) — usado no SimpleNamedScreenHandlerFactory
    public HireGuardScreenHandler(int syncId, PlayerInventory inv, int guardEntityId) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.guardEntityId = guardEntityId;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return true;
        }

        Entity entity = serverPlayer.getEntityWorld().getEntityById(guardEntityId);
        return entity instanceof GuardEntity guard
                && guard.isAlive()
                && !guard.isRemoved()
                && !GuardOwnership.hasOwner(guard)
                && serverPlayer.squaredDistanceTo(guard) <= MAX_USE_DISTANCE_SQUARED;
    }

    /**
     * id == 0 => botão "Contratar"
     */
    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!(player instanceof ServerPlayerEntity sp)) return false;
        if (id != 0) return false;

        ServerWorld world = sp.getEntityWorld();
        Entity entity = world.getEntityById(this.guardEntityId);

        if (!(entity instanceof GuardEntity guard)) {
            sp.closeHandledScreen();
            sp.sendMessage(Text.translatable("gui.rallyguard.hire.invalid"), true);
            return true;
        }

        RecruitmentResult result = RECRUITMENT.recruit(sp, guard);
        sp.sendMessage(result.feedback(), true);
        sp.closeHandledScreen();
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }
}
