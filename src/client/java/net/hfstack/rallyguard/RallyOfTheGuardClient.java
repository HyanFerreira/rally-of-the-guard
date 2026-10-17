package net.hfstack.rallyguard;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.MusterRollSnapshotS2CPayload;
import net.hfstack.rallyguard.network.payload.CloseMusterRollS2CPayload;
import net.hfstack.rallyguard.network.payload.CommandPostSnapshotS2CPayload;
import net.hfstack.rallyguard.network.payload.GlobalGuardRegistryS2CPayload;
import net.hfstack.rallyguard.client.move.MoveTargetingClient;
import net.hfstack.rallyguard.screen.GuardCombatScreen;
import net.hfstack.rallyguard.screen.HireGuardScreen;
import net.hfstack.rallyguard.screen.ModScreenHandlers;
import net.hfstack.rallyguard.screen.QuickCommandScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class RallyOfTheGuardClient implements ClientModInitializer {
    private static KeyMapping combatOrdersKey;
    private static KeyMapping quickCommandKey;
    private static final KeyMapping.Category RALLYGUARD_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "combat_orders"));

    @Override
    public void onInitializeClient() {
        MoveTargetingClient.register();
        MenuScreens.register(ModScreenHandlers.HIRE_HANDLER, HireGuardScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(
                GuardListS2CPayload.ID,
                (payload, context) -> net.hfstack.rallyguard.screen.GuardCommandScreen.openFromPayload(payload)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                MusterRollSnapshotS2CPayload.ID,
                (payload, context) -> net.hfstack.rallyguard.screen.MusterRollScreen.accept(payload)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                CloseMusterRollS2CPayload.ID,
                (payload, context) -> net.hfstack.rallyguard.screen.MusterRollScreen.closeFromServer()
        );
        ClientPlayNetworking.registerGlobalReceiver(
                CommandPostSnapshotS2CPayload.ID,
                (payload, context) -> net.hfstack.rallyguard.screen.CommandPostScreen.accept(payload)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                GlobalGuardRegistryS2CPayload.ID,
                (payload, context) -> net.hfstack.rallyguard.screen.GlobalGuardRegistryScreen.accept(payload)
        );

        combatOrdersKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.rallyguard.combat_orders",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                RALLYGUARD_CATEGORY
        ));
        quickCommandKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.rallyguard.quick_command",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                RALLYGUARD_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (combatOrdersKey.consumeClick()) {
                openCombatOrders(client);
            }
            while (quickCommandKey.consumeClick()) {
                openQuickCommand(client);
            }
        });
    }

    private static void openCombatOrders(Minecraft client) {
        if (client.player == null || client.level == null || client.screen != null) return;
        client.setScreen(new GuardCombatScreen(null));
    }

    private static void openQuickCommand(Minecraft client) {
        if (client.player == null || client.level == null || client.screen != null) return;
        client.setScreen(new QuickCommandScreen());
    }
}
