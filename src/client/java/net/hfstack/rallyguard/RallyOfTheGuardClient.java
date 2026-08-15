package net.hfstack.rallyguard;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.screen.GuardCombatScreen;
import net.hfstack.rallyguard.screen.HireGuardScreen;
import net.hfstack.rallyguard.screen.ModScreenHandlers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RallyOfTheGuardClient implements ClientModInitializer {
    private static KeyBinding combatOrdersKey;
    private static final String RALLYGUARD_CATEGORY = "key.category.rallyguard.combat_orders";

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.HIRE_HANDLER, HireGuardScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(
                GuardListS2CPayload.ID,
                (payload, context) -> net.hfstack.rallyguard.screen.GuardCommandScreen.openFromPayload(payload)
        );

        combatOrdersKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rallyguard.combat_orders",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                RALLYGUARD_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (combatOrdersKey.wasPressed()) {
                openCombatOrders(client);
            }
        });
    }

    private static void openCombatOrders(MinecraftClient client) {
        if (client.player == null || client.world == null || client.currentScreen != null) return;
        client.setScreen(new GuardCombatScreen(null));
    }
}
