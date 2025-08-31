package net.hfstack.rallyguard.component;

import com.mojang.serialization.Codec;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Data Components (1.21+): substituem o NBT para dados do ItemStack.
 * Aqui registramos um booleano simples para controlar o "glint" do Pergaminho.
 */
public final class ModComponents {
    private ModComponents() {}

    // Ex.: rallyguard:active -> Boolean
    public static final ComponentType<Boolean> ACTIVE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(RallyOfTheGuard.MOD_ID, "active"),
            ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
    );

    public static void initialize() {
        // Somente para garantir que a classe seja carregada.
        RallyOfTheGuard.LOGGER.info("[{}] Data Components registrados.", RallyOfTheGuard.MOD_ID);
    }
}
