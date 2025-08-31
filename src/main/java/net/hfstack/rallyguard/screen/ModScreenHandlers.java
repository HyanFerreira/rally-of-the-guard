package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {
    private ModScreenHandlers() {
    }

    // Fábrica CLIENTE (2 args). No SERVER você cria com (syncId, inv, guardId).
    public static final ScreenHandlerType<HireGuardScreenHandler> HIRE_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(RallyOfTheGuard.MOD_ID, "hire_handler"),
                    new ScreenHandlerType<>(
                            // fábrica usada no CLIENTE ao abrir a tela
                            (syncId, inv) -> new HireGuardScreenHandler(syncId, inv),
                            FeatureFlags.VANILLA_FEATURES
                    )
            );

    public static void init() {
        // chamado opcionalmente no ModInitializer, se quiser
    }
}
