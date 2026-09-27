package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.resources.Identifier;

public final class ModScreenHandlers {
    private ModScreenHandlers() {
    }

    // Fábrica CLIENTE (2 args). No SERVER você cria com (syncId, inv, guardId).
    public static final MenuType<HireGuardScreenHandler> HIRE_HANDLER =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "hire_handler"),
                    new MenuType<>(
                            // fábrica usada no CLIENTE ao abrir a tela
                            (syncId, inv) -> new HireGuardScreenHandler(syncId, inv),
                            FeatureFlags.VANILLA_SET
                    )
            );

    public static void init() {
        // chamado opcionalmente no ModInitializer, se quiser
    }
}
