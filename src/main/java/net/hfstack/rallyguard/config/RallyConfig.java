package net.hfstack.rallyguard.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class RallyConfig extends MidnightConfig {
    public RallyConfig() {
    }

    @Entry(category = "economy", min = 0, max = 999)
    public static int hireCost = 3;

    @Entry(category = "economy")
    public static String hireItem = "minecraft:emerald";

    @Entry(category = "economy")
    public static boolean dynamicHireCostEnabled = true;

    @Entry(category = "economy", min = 0.0, max = 100.0)
    public static double dynamicHireCostMultiplier = 1.0;

    @Entry(category = "economy", min = 0, max = 999)
    public static int dynamicHireCostMinimum = 1;

    @Entry(category = "economy", min = 0, max = 999)
    public static int dynamicHireCostMaximum = 999;

    @Entry(category = "economy", min = 0, max = 999)
    public static int captainHireCostBonus = 4;

    @Entry(category = "economy", min = 0, max = 999)
    public static int captainPromotionCost = 4;

    @Entry(category = "progression")
    public static boolean progressionEnabled = true;

    @Entry(category = "progression")
    public static boolean naturalVariationEnabled = true;

    @Entry(category = "progression")
    public static boolean statGrowthEnabled = true;

    @Entry(category = "progression")
    public static boolean advancedNaturalGuardsEnabled = true;

    @Entry(category = "progression")
    public static boolean captainSpawnEnabled = true;

    @Entry(category = "progression", min = 0.0, max = 1.0)
    public static double captainSpawnChance = 0.05;

    @Entry(category = "progression", min = 1, max = 10)
    public static int captainMinimumLevel = 5;

    @Entry(category = "progression", min = 1.0, max = 512.0)
    public static double captainRegionalRadius = 128.0;

    @Entry(category = "progression", min = 0, max = 32)
    public static int captainRegionalLimit = 2;

    @Entry(category = "progression")
    public static boolean captainPromotionEnabled = true;

    @Entry(category = "progression", min = 1, max = 10)
    public static int captainPromotionMinimumLevel = 7;

    @Entry(category = "progression", min = 0.0, max = 100.0)
    public static double xpMultiplier = 1.0;

    @Entry(category = "progression", min = 1, max = 600)
    public static int combatTimeoutSeconds = 30;

    @Entry(category = "progression", min = 0.0, max = 1.0)
    public static double damageContributionShare = 0.80;

    @Entry(category = "progression", min = 0.0, max = 1.0)
    public static double finalBlowShare = 0.20;

    @Entry(category = "progression", min = 0.0, max = 1000.0)
    public static double assistMinimumDamage = 1.0;

    @Entry(category = "progression", min = 0.0, max = 1.0)
    public static double assistMinimumShare = 0.05;

    @Entry(category = "rally", min = 1, max = 512)
    public static int rallyRadius = 50;

    @Entry(category = "rally", min = 1, max = 256)
    public static int rallyMaxGuards = 16;

    @Entry(category = "rally")
    public static boolean rallyTeleportEnabled = true;

    @Entry(category = "rally", min = 0.0, max = 512.0)
    public static double rallyTeleportMinDistance = 8.0;

    @Entry(category = "rally", min = 7, max = 600)
    public static int rallyEffectTimerSeconds = 18;

    @Entry(category = "rally")
    public static boolean rallyFormationEnabled = true;

    @Entry(category = "rally")
    public static boolean rallyProtectRalliedGuardsFromOwner = true;

    @Entry(category = "rally")
    public static boolean rallyRetreatEnabled = true;

    @Entry(category = "rally", min = 1.0, max = 512.0)
    public static double rallyRetreatDistance = 24.0;

    @Entry(category = "formation", min = 0.5, max = 8.0)
    public static double formationColumnSpacing = 1.35;

    @Entry(category = "formation", min = 0.5, max = 8.0)
    public static double formationRowSpacing = 1.8;

    @Entry(category = "formation", min = 0.1, max = 4.0)
    public static double formationReturnSpeed = 1.0;

    @Entry(category = "formation", min = 2.0, max = 512.0)
    public static double formationTeleportDistance = 30.0;

    @Entry(category = "combat", min = 1.0, max = 256.0)
    public static double combatTargetRange = 64.0;

    @Entry(category = "combat", min = 1.0, max = 512.0)
    public static double combatGuardSearchRadius = 100.0;

    @Entry(category = "combat")
    public static boolean combatAllowPassiveTargets = true;

    @Entry(category = "combat")
    public static boolean combatAllowPlayerTargets = false;

    @Entry(category = "route", min = 2, max = 10)
    public static int routeMaxPoints = 5;

    @Entry(category = "route", min = 0, max = 600)
    public static int routeDefaultWaitSeconds = 30;

    @Entry(category = "route", min = 0.1, max = 4.0)
    public static double routeMoveSpeed = 1.0;

    @Entry(category = "route")
    public static boolean routeTeleportIfStuck = true;

    @Entry(category = "route", min = 2.0, max = 512.0)
    public static double routeTeleportDistance = 30.0;

    @Entry(category = "squads")
    public static boolean squadsEnabled = true;

    @Entry(category = "squads", min = 1, max = 64)
    public static int maxSquadsPerPlayer = 16;

    @Entry(category = "squads", min = 1, max = 256)
    public static int maxMembersPerSquad = 32;

    @Entry(category = "squads", min = 1, max = 48)
    public static int maxSquadNameLength = 24;

    public static void load() {
        MidnightConfig.init(RallyOfTheGuard.MOD_ID, RallyConfig.class);
    }

    public static int hireCost() {
        return clamp(hireCost, 0, 999);
    }

    public static Item hireItem() {
        Identifier id = Identifier.tryParse(hireItem);
        if (id == null) return Items.EMERALD;

        Item item = BuiltInRegistries.ITEM.getValue(id);
        return item == Items.AIR ? Items.EMERALD : item;
    }

    public static String hireItemId() {
        return hireItem;
    }

    public static boolean dynamicHireCostEnabled() {
        return dynamicHireCostEnabled;
    }

    public static double dynamicHireCostMultiplier() {
        return finiteClamp(dynamicHireCostMultiplier, 0.0, 100.0, 1.0);
    }

    public static int dynamicHireCostMinimum() {
        return clamp(dynamicHireCostMinimum, 0, 999);
    }

    public static int dynamicHireCostMaximum() {
        return Math.max(dynamicHireCostMinimum(), clamp(dynamicHireCostMaximum, 0, 999));
    }

    public static int captainHireCostBonus() {
        return clamp(captainHireCostBonus, 0, 999);
    }

    public static int captainPromotionCost() {
        return clamp(captainPromotionCost, 0, 999);
    }

    public static boolean progressionEnabled() {
        return progressionEnabled;
    }

    public static boolean naturalVariationEnabled() {
        return naturalVariationEnabled;
    }

    public static boolean statGrowthEnabled() {
        return statGrowthEnabled;
    }

    public static boolean advancedNaturalGuardsEnabled() {
        return advancedNaturalGuardsEnabled;
    }

    public static boolean naturalCaptainsEnabled() {
        return advancedNaturalGuardsEnabled() && captainSpawnEnabled;
    }

    public static double captainSpawnChance() {
        return finiteClamp(captainSpawnChance, 0.0, 1.0, 0.05);
    }

    public static int captainMinimumLevel() {
        return clamp(captainMinimumLevel, 1, 10);
    }

    public static double captainRegionalRadius() {
        return finiteClamp(captainRegionalRadius, 1.0, 512.0, 128.0);
    }

    public static int captainRegionalLimit() {
        return clamp(captainRegionalLimit, 0, 32);
    }

    public static boolean captainPromotionEnabled() {
        return captainPromotionEnabled;
    }

    public static int captainPromotionMinimumLevel() {
        return clamp(captainPromotionMinimumLevel, 1, 10);
    }

    public static double xpMultiplier() {
        return finiteClamp(xpMultiplier, 0.0, 100.0, 1.0);
    }

    public static int combatTimeoutTicks() {
        return clamp(combatTimeoutSeconds, 1, 600) * 20;
    }

    public static double damageContributionShare() {
        return finiteClamp(damageContributionShare, 0.0, 1.0, 0.80);
    }

    public static double finalBlowShare() {
        return finiteClamp(finalBlowShare, 0.0, 1.0, 0.20);
    }

    public static double assistMinimumDamage() {
        return finiteClamp(assistMinimumDamage, 0.0, 1000.0, 1.0);
    }

    public static double assistMinimumShare() {
        return finiteClamp(assistMinimumShare, 0.0, 1.0, 0.05);
    }

    public static int rallyRadius() {
        return clamp(rallyRadius, 1, 512);
    }

    public static int rallyMaxGuards() {
        return clamp(rallyMaxGuards, 1, 256);
    }

    public static boolean rallyTeleportEnabled() {
        return rallyTeleportEnabled;
    }

    public static double rallyTeleportMinDistance() {
        return clamp(rallyTeleportMinDistance, 0.0, 512.0);
    }

    public static int rallyEffectTimerSeconds() {
        return clamp(rallyEffectTimerSeconds, 7, 600);
    }

    public static boolean rallyFormationEnabled() {
        return rallyFormationEnabled;
    }

    public static boolean rallyProtectRalliedGuardsFromOwner() {
        return rallyProtectRalliedGuardsFromOwner;
    }

    public static boolean rallyRetreatEnabled() {
        return rallyRetreatEnabled;
    }

    public static double rallyRetreatDistance() {
        return clamp(rallyRetreatDistance, 1.0, 512.0);
    }

    public static double formationColumnSpacing() {
        return clamp(formationColumnSpacing, 0.5, 8.0);
    }

    public static double formationRowSpacing() {
        return clamp(formationRowSpacing, 0.5, 8.0);
    }

    public static double formationReturnSpeed() {
        return clamp(formationReturnSpeed, 0.1, 4.0);
    }

    public static double formationTeleportDistance() {
        return clamp(formationTeleportDistance, 2.0, 512.0);
    }

    public static double combatTargetRange() {
        return clamp(combatTargetRange, 1.0, 256.0);
    }

    public static double combatGuardSearchRadius() {
        return clamp(combatGuardSearchRadius, 1.0, 512.0);
    }

    public static boolean combatAllowPassiveTargets() {
        return combatAllowPassiveTargets;
    }

    public static boolean combatAllowPlayerTargets() {
        return combatAllowPlayerTargets;
    }

    public static int routeMaxPoints() {
        return clamp(routeMaxPoints, 2, 10);
    }

    public static int routeDefaultWaitSeconds() {
        return clamp(routeDefaultWaitSeconds, 0, 600);
    }

    public static double routeMoveSpeed() {
        return clamp(routeMoveSpeed, 0.1, 4.0);
    }

    public static boolean routeTeleportIfStuck() {
        return routeTeleportIfStuck;
    }

    public static double routeTeleportDistance() {
        return clamp(routeTeleportDistance, 2.0, 512.0);
    }

    public static boolean squadsEnabled() {
        return squadsEnabled;
    }

    public static int maxSquadsPerPlayer() {
        return clamp(maxSquadsPerPlayer, 1, 64);
    }

    public static int maxMembersPerSquad() {
        return clamp(maxMembersPerSquad, 1, 256);
    }

    public static int maxSquadNameLength() {
        return clamp(maxSquadNameLength, 1, 48);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double finiteClamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? clamp(value, min, max) : fallback;
    }
}
