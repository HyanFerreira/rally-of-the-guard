package net.hfstack.rallyguard.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class RallyConfig extends MidnightConfig {
    public RallyConfig() {
    }

    @Entry(category = "economy", min = 0, max = 999)
    public static int hireCost = 3;

    @Entry(category = "economy")
    public static String hireItem = "minecraft:emerald";

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

    public static void load() {
        MidnightConfig.init(RallyOfTheGuard.MOD_ID, RallyConfig.class);
    }

    public static int hireCost() {
        return clamp(hireCost, 0, 999);
    }

    public static Item hireItem() {
        Identifier id = Identifier.tryParse(hireItem);
        if (id == null) return Items.EMERALD;

        Item item = Registries.ITEM.get(id);
        return item == Items.AIR ? Items.EMERALD : item;
    }

    public static String hireItemId() {
        return hireItem;
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
