package net.hfstack.rallyguard.order.defend;

public final class DefendAreaTransitionPolicy {
    private DefendAreaTransitionPolicy() {
    }

    public static boolean mayJoinRally(boolean patrolling, boolean defending) {
        return !patrolling || defending;
    }
}
