package net.hfstack.rallyguard.api.command;

/**
 * Stable identifiers for tactical orders exposed by {@link GuardCommandService}.
 */
public enum GuardCommandType {
    SUMMON,
    FOLLOW,
    WAIT,
    PATROL,
    STOP_PATROL
}
