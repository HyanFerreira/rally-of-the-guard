package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

/**
 * Registra APENAS o ScreenHandlerType (lado comum).
 * Nada de classes cliente aqui.
 */
public final class ModScreenHandlers {
    private ModScreenHandlers() {
    }

    public static final ScreenHandlerType<HireGuardScreenHandler> HIRE_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(RallyOfTheGuard.MOD_ID, "hire_handler"),
                    new ScreenHandlerType<>(
                            (syncId, inv) -> new HireGuardScreenHandler(syncId, inv),
                            FeatureFlags.VANILLA_FEATURES
                    )
            );

    public static void init() { /* no-op */ }
}
