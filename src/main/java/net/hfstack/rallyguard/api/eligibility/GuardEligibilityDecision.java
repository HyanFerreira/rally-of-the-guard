package net.hfstack.rallyguard.api.eligibility;

import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * Structured result returned by a guard eligibility policy.
 */
public sealed interface GuardEligibilityDecision {
    record Allow() implements GuardEligibilityDecision {
    }

    record Deny(Component reason) implements GuardEligibilityDecision {
        public Deny {
            Objects.requireNonNull(reason, "reason");
        }
    }

    static GuardEligibilityDecision allow() {
        return new Allow();
    }

    static GuardEligibilityDecision deny(Component reason) {
        return new Deny(reason);
    }
}
