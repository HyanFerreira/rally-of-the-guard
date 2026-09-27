package net.hfstack.rallyguard.api.command;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Objects;

/**
 * Policy and observation hooks around server-authoritative tactical orders.
 */
public final class GuardCommandEvents {
    private GuardCommandEvents() {
    }

    /**
     * Policies run in registration order. The first denial stops evaluation and prevents the order.
     */
    public static final Event<Before> BEFORE = EventFactory.createArrayBacked(
            Before.class,
            listeners -> context -> {
                for (Before listener : listeners) {
                    GuardCommandDecision decision = Objects.requireNonNull(
                            listener.evaluate(context),
                            "Guard command BEFORE listener returned null"
                    );
                    if (decision instanceof GuardCommandDecision.Deny) {
                        return decision;
                    }
                }
                return GuardCommandDecision.allow();
            }
    );

    /**
     * Notification emitted after Rally has applied an order successfully.
     */
    public static final Event<After> AFTER = EventFactory.createArrayBacked(
            After.class,
            listeners -> (context, result) -> {
                for (After listener : listeners) {
                    listener.onCommanded(context, result);
                }
            }
    );

    @FunctionalInterface
    public interface Before {
        GuardCommandDecision evaluate(GuardCommandContext context);
    }

    @FunctionalInterface
    public interface After {
        void onCommanded(GuardCommandContext context, GuardCommandResult result);
    }
}
