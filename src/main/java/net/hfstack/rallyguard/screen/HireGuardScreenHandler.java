package net.hfstack.rallyguard.screen;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentService;
import net.hfstack.rallyguard.api.recruitment.RecruitmentQuote;
import net.hfstack.rallyguard.api.recruitment.RecruitmentQuoteResult;
import net.hfstack.rallyguard.api.recruitment.RecruitmentResult;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.interaction.GuardInteractionLocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.UUID;

public class HireGuardScreenHandler extends AbstractContainerMenu {
    private static final GuardRecruitmentService RECRUITMENT = RallyGuardApi.guardRecruitment();
    private static final double MAX_USE_DISTANCE_SQUARED = 8.0 * 8.0;

    private final int guardEntityId;
    private final DataSlot quotedCost = DataSlot.standalone();
    private final DataSlot quotedItem = DataSlot.standalone();
    private RecruitmentQuote expectedQuote;
    private final UUID interactingGuardId;

    // Construtor CLIENTE (2 args) — usado pela fábrica registrada em ModScreenHandlers
    public HireGuardScreenHandler(int syncId, Inventory inv) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.guardEntityId = -1;
        this.interactingGuardId = null;
        this.addDataSlot(quotedCost);
        this.addDataSlot(quotedItem);
    }

    // Construtor SERVIDOR (3 args) — usado no SimpleMenuProvider
    public HireGuardScreenHandler(int syncId, Inventory inv, int guardEntityId, RecruitmentQuote quote) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.guardEntityId = guardEntityId;
        this.interactingGuardId = inv.player instanceof ServerPlayer serverPlayer
                ? beginInteraction(serverPlayer, guardEntityId)
                : null;
        this.addDataSlot(quotedCost);
        this.addDataSlot(quotedItem);
        updateQuote(quote);
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

        if (expectedQuote == null) {
            sp.sendOverlayMessage(Component.translatable("gui.rallyguard.hire.quote_unavailable"));
            sp.closeContainer();
            return true;
        }

        RecruitmentResult result = RECRUITMENT.recruit(sp, guard, expectedQuote);
        sp.sendOverlayMessage(result.feedback());
        if (result.outcome() == RecruitmentResult.Outcome.OFFER_CHANGED) {
            RecruitmentQuoteResult refreshed = RECRUITMENT.quote(sp, guard);
            if (refreshed.available()) {
                updateQuote(refreshed.quote().orElseThrow());
                this.broadcastChanges();
                return true;
            }
            sp.sendOverlayMessage(refreshed.feedback());
        }
        sp.closeContainer();
        return true;
    }

    public boolean hasQuote() {
        return quotedCost.get() > 0 && quotedItem.get() > 0;
    }

    public int quotedCost() {
        return Math.max(0, quotedCost.get() - 1);
    }

    public Item quotedItem() {
        Item item = BuiltInRegistries.ITEM.byId(quotedItem.get() - 1);
        return item == null ? Items.EMERALD : item;
    }

    private void updateQuote(RecruitmentQuote quote) {
        this.expectedQuote = quote;
        this.quotedCost.set(quote.offer().cost() + 1);
        Item item = BuiltInRegistries.ITEM.getValue(quote.offer().paymentItemId());
        this.quotedItem.set(BuiltInRegistries.ITEM.getId(item) + 1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer && interactingGuardId != null) {
            GuardInteractionLocks.release(serverPlayer.getUUID(), interactingGuardId);
        }
    }

    private static UUID beginInteraction(ServerPlayer player, int entityId) {
        Entity entity = player.level().getEntity(entityId);
        if (!(entity instanceof GuardEntity guard)) return null;
        GuardInteractionLocks.acquire(player, guard);
        return guard.getUUID();
    }
}
