package net.hfstack.rallyguard.api.captain;

import net.minecraft.network.chat.Component;

import java.util.Objects;

public sealed interface CaptainDecision {
    record Allow() implements CaptainDecision {
    }

    record Deny(Component reason) implements CaptainDecision {
        public Deny {
            Objects.requireNonNull(reason, "reason");
        }
    }

    static CaptainDecision allow() {
        return new Allow();
    }

    static CaptainDecision deny(Component reason) {
        return new Deny(reason);
    }
}
