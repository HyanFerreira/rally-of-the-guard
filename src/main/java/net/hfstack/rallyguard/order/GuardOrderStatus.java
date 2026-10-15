package net.hfstack.rallyguard.order;

public final class GuardOrderStatus {
    private GuardOrderStatus() {
    }

    public static final int IDLE = 0;
    public static final int FOLLOWING = 1;
    public static final int WAITING = 2;
    public static final int PATROLLING = 3;
    public static final int ROUTING = 4;
    public static final int MOVING = 5;

    public static int resolve(boolean moving, boolean routing, boolean patrolling, boolean waiting, boolean following) {
        if (moving) return MOVING;
        if (routing) return ROUTING;
        if (patrolling) return PATROLLING;
        if (waiting) return WAITING;
        if (following) return FOLLOWING;
        return IDLE;
    }
}
