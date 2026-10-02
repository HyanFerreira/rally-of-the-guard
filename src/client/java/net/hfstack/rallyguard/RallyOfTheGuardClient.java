package net.hfstack.rallyguard;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.screen.GuardCombatScreen;
import net.hfstack.rallyguard.screen.HireGuardScreen;
import net.hfstack.rallyguard.screen.ModScreenHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class RallyOfTheGuardClient implements ClientModInitializer {
    private static KeyMapping combatOrdersKey;
    private static final KeyMapping.Category RALLYGUARD_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "combat_orders"));

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.HIRE_HANDLER, HireGuardScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(
                GuardListS2CPayload.ID,
                (payload, context) -> net.hfstack.rallyguard.screen.GuardCommandScreen.openFromPayload(payload)
        );

        combatOrdersKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.rallyguard.combat_orders",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                RALLYGUARD_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (combatOrdersKey.consumeClick()) {
                openCombatOrders(client);
            }
        });
    }

    private static void openCombatOrders(Minecraft client) {
        if (client.player == null || client.level == null || client.screen != null) return;
        client.setScreen(new GuardCombatScreen(null));
    }
}
