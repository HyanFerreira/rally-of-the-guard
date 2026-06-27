package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class HireGuardScreenHandler extends ScreenHandler {

    private final int guardEntityId;           // no CLIENTE fica -1 (não usado)
    private final PlayerInventory playerInventory;

    // Construtor CLIENTE (2 args) — usado pela fábrica registrada em ModScreenHandlers
    public HireGuardScreenHandler(int syncId, PlayerInventory inv) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.playerInventory = inv;
        this.guardEntityId = -1;
    }

    // Construtor SERVIDOR (3 args) — usado no SimpleNamedScreenHandlerFactory
    public HireGuardScreenHandler(int syncId, PlayerInventory inv, int guardEntityId) {
        super(ModScreenHandlers.HIRE_HANDLER, syncId);
        this.playerInventory = inv;
        this.guardEntityId = guardEntityId;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    /**
     * id == 0 => botão "Contratar"
     */
    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!(player instanceof ServerPlayerEntity sp)) return false;
        if (id != 0) return false;

        ServerWorld world = sp.getEntityWorld();
        Entity guard = world.getEntityById(this.guardEntityId);

        // Guarda inexistente -> fecha
        if (guard == null || !GuardOwnership.isGuard(guard)) {
            sp.closeHandledScreen();
            return true;
        }

        // Já tem dono -> fecha e avisa
        if (GuardOwnership.hasOwner(guard)) {
            sp.closeHandledScreen();
            sp.sendMessage(Text.translatable("gui.rallyguard.hire.already_owned"), true); // overlay
            return true;
        }

        int cost = RallyConfig.hireCost();
        Item hireItem = RallyConfig.hireItem();
        int paymentItems = 0;

        // Conta esmeraldas
        for (int i = 0; i < playerInventory.size(); i++) {
            ItemStack s = playerInventory.getStack(i);
            if (s.isOf(hireItem)) paymentItems += s.getCount();
        }

        // Não tem o suficiente -> fecha e avisa em overlay
        if (paymentItems < cost) {
            sp.closeHandledScreen();
            sp.sendMessage(Text.translatable("gui.rallyguard.hire.not_enough", cost, hireItem.getName()), true);
            return true;
        }

        // Desconta custo
        int remaining = cost;
        for (int i = 0; i < playerInventory.size() && remaining > 0; i++) {
            ItemStack s = playerInventory.getStack(i);
            if (s.isOf(hireItem)) {
                int take = Math.min(remaining, s.getCount());
                s.decrement(take);
                remaining -= take;
            }
        }

        // Define dono (aplica nome dourado + mensagem "apresentando-se" em chat)
        GuardOwnership.setOwner(guard, sp);

        // Mensagem de sucesso em overlay (apenas essa fica na tela)
        sp.sendMessage(Text.translatable("gui.rallyguard.hire.success"), true);

        // Fecha a tela
        sp.closeHandledScreen();
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }
}
