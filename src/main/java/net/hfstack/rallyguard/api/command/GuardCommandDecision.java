package net.hfstack.rallyguard.api.command;

import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * Result returned by a tactical-order policy listener.
 */
public sealed interface GuardCommandDecision {
    record Allow() implements GuardCommandDecision {
    }

    record Deny(Component reason) implements GuardCommandDecision {
        public Deny {
            Objects.requireNonNull(reason, "reason");
        }
    }

    static GuardCommandDecision allow() {
        return new Allow();
    }

    static GuardCommandDecision deny(Component reason) {
        return new Deny(reason);
    }
}
