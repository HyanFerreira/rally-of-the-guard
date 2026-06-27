package net.hfstack.rallyguard.order;

public final class GuardOrderStatus {
    private GuardOrderStatus() {
    }

    public static final int IDLE = 0;
    public static final int FOLLOWING = 1;
    public static final int WAITING = 2;
    public static final int PATROLLING = 3;
}
