package net.hfstack.rallyguard.order;

import net.hfstack.rallyguard.order.defend.DefendAreaTransitionPolicy;

public final class RallyParticipationPolicy {
    private RallyParticipationPolicy() {
    }

    public static boolean mayJoin(boolean patrolling, boolean defendingArea, boolean waiting, boolean routeActive) {
        return !waiting
                && !routeActive
                && DefendAreaTransitionPolicy.mayJoinRally(patrolling, defendingArea);
    }
}
