package net.hfstack.rallyguard.api.eligibility;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Objects;

/**
 * Shared policy chain for recruitment, rally participation, commands and list visibility.
 */
public final class GuardEligibilityEvents {
    private GuardEligibilityEvents() {
    }

    public static final Event<Check> CHECK = EventFactory.createArrayBacked(
            Check.class,
            listeners -> context -> {
                for (Check listener : listeners) {
                    GuardEligibilityDecision decision = Objects.requireNonNull(
                            listener.evaluate(context),
                            "Guard eligibility listener returned null"
                    );
                    if (decision instanceof GuardEligibilityDecision.Deny) {
                        return decision;
                    }
                }
                return GuardEligibilityDecision.allow();
            }
    );

    @FunctionalInterface
    public interface Check {
        GuardEligibilityDecision evaluate(GuardEligibilityContext context);
    }
}
