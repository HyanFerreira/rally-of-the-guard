package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.util.Identifier;

public final class NetworkConstants {
    private NetworkConstants() {
    }

    public static final Identifier OPEN_GUARD_COMMAND = Identifier.of(RallyOfTheGuard.MOD_ID, "open_guard_command");
    public static final Identifier GUARD_LIST = Identifier.of(RallyOfTheGuard.MOD_ID, "guard_list");
    public static final Identifier GUARD_ACTION = Identifier.of(RallyOfTheGuard.MOD_ID, "guard_action");
    public static final Identifier GUARD_ROUTE_UPDATE = Identifier.of(RallyOfTheGuard.MOD_ID, "guard_route_update");
    public static final Identifier GUARD_ATTACK_TARGET = Identifier.of(RallyOfTheGuard.MOD_ID, "guard_attack_target");

    // Ações
    public static final int ACTION_SUMMON = 1;
    public static final int ACTION_TOGGLE_PATROL = 2;
    public static final int ACTION_FOLLOW = 3;
    public static final int ACTION_WAIT = 4;
    public static final int ACTION_ROUTE_PLACEHOLDER = 5;

    public static final int ROUTE_SAVE = 1;
    public static final int ROUTE_START = 2;
    public static final int ROUTE_PAUSE = 3;
    public static final int ROUTE_CLEAR = 4;

    public static final int ATTACK_ALL = 1;
    public static final int ATTACK_INFANTRY = 2;
    public static final int ATTACK_RANGED = 3;
}
