package net.hfstack.rallyguard.component;

import com.mojang.serialization.Codec;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

/**
 * Data Components (1.21+): substituem o NBT para dados do ItemStack.
 * Aqui registramos um booleano simples para controlar o "glint" do Pergaminho.
 */
public final class ModComponents {
    private ModComponents() {}

    // Ex.: rallyguard:active -> Boolean
    public static final DataComponentType<Boolean> ACTIVE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "active"),
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );

    public static void initialize() {
        // Somente para garantir que a classe seja carregada.
        RallyOfTheGuard.LOGGER.info("[{}] Data Components registrados.", RallyOfTheGuard.MOD_ID);
    }
}
