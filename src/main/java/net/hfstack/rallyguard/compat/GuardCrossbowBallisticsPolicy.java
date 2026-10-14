package net.hfstack.rallyguard.compat;

public final class GuardCrossbowBallisticsPolicy {
    static final float GUARD_VILLAGERS_PROJECTILE_SPEED = 6.0F;
    static final float VANILLA_CROSSBOW_MOB_PROJECTILE_SPEED = 1.6F;

    private GuardCrossbowBallisticsPolicy() {
    }

    public static float projectileSpeed(float originalSpeed) {
        if (Float.compare(originalSpeed, GUARD_VILLAGERS_PROJECTILE_SPEED) == 0) {
            return VANILLA_CROSSBOW_MOB_PROJECTILE_SPEED;
        }
        return originalSpeed;
    }
}
