package net.hfstack.rallyguard.compat;

public final class GuardMeleeBackstepPolicy {
    private GuardMeleeBackstepPolicy() {
    }

    public static boolean shouldBackstep(boolean hasShield, boolean blocking) {
        return hasShield && !blocking;
    }
}
