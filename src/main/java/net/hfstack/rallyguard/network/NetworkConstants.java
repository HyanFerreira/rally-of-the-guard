package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.util.Identifier;

public final class NetworkConstants {
    private NetworkConstants() {
    }

    public static final Identifier OPEN_GUARD_COMMAND = Identifier.of(RallyOfTheGuard.MOD_ID, "open_guard_command");
    public static final Identifier GUARD_LIST = Identifier.of(RallyOfTheGuard.MOD_ID, "guard_list");
    public static final Identifier GUARD_ACTION = Identifier.of(RallyOfTheGuard.MOD_ID, "guard_action");

    // Ações
    public static final int ACTION_SUMMON = 1;
    public static final int ACTION_TOGGLE_PATROL = 2;
}
