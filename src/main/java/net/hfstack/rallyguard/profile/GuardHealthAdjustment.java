package net.hfstack.rallyguard.profile;

public final class GuardHealthAdjustment {
    private GuardHealthAdjustment() {
    }

    public static float adjustedHealth(
            float currentHealth,
            float oldMaximum,
            float newMaximum,
            GuardAttributeApplicationCause cause
    ) {
        float boundedNewMaximum = Math.max(1.0F, newMaximum);
        float boundedCurrent = Math.max(0.0F, currentHealth);
        if (Math.abs(oldMaximum - boundedNewMaximum) < 0.0001F) {
            return Math.min(boundedCurrent, boundedNewMaximum);
        }
        if (cause == GuardAttributeApplicationCause.LEVEL_UP) {
            float increase = Math.max(0.0F, boundedNewMaximum - Math.max(1.0F, oldMaximum));
            return Math.min(boundedCurrent + increase, boundedNewMaximum);
        }
        float ratio = oldMaximum <= 0.0F ? 1.0F : boundedCurrent / oldMaximum;
        return Math.min(Math.max(0.0F, ratio * boundedNewMaximum), boundedNewMaximum);
    }
}
