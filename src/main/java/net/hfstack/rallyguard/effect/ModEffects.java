package net.hfstack.rallyguard.effect;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

public class ModEffects {
    public static final Holder<MobEffect> RALLY_COMMANDER = registerStatusEffect("rally_commander",
            new RallyCommanderEffect());

    private static Holder<MobEffect> registerStatusEffect(String name, MobEffect statusEffect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, name), statusEffect);
    }

    public static void registerModEffects() {
        RallyOfTheGuard.LOGGER.info("Registering Mod Effects for " + RallyOfTheGuard.MOD_ID);
    }
}
