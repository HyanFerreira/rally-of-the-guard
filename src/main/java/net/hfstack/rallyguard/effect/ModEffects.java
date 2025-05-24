package net.hfstack.rallyguard.effect;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEffects {

    public static final RegistryEntry<StatusEffect> RALLY_COMMANDER = registerStatusEffect("rally_commander",
            new RallyCommanderEffect());

    private static RegistryEntry<StatusEffect> registerStatusEffect(String name, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(RallyOfTheGuard.MOD_ID, name), statusEffect);
    }

    public static void registerModEffects() {
        RallyOfTheGuard.LOGGER.info("Registering Mod Effects for " + RallyOfTheGuard.MOD_ID);
    }
}
