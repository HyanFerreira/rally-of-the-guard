package net.hfstack.rallyguard.api.captain;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.hfstack.rallyguard.RallyOfTheGuard;

import java.util.Objects;

public final class CaptainEvents {
    private CaptainEvents() {
    }

    public static final Event<Before> BEFORE = EventFactory.createArrayBacked(
            Before.class,
            listeners -> context -> {
                for (Before listener : listeners) {
                    CaptainDecision decision = Objects.requireNonNull(
                            listener.evaluate(context),
                            "Captain BEFORE listener returned null"
                    );
                    if (decision instanceof CaptainDecision.Deny) return decision;
                }
                return CaptainDecision.allow();
            }
    );

    public static final Event<After> AFTER = EventFactory.createArrayBacked(
            After.class,
            listeners -> (context, result) -> {
                for (After listener : listeners) {
                    try {
                        listener.onChanged(context, result);
                    } catch (RuntimeException exception) {
                        RallyOfTheGuard.LOGGER.error("Captain AFTER listener failed", exception);
                    }
                }
            }
    );

    @FunctionalInterface
    public interface Before {
        CaptainDecision evaluate(CaptainContext context);
    }

    @FunctionalInterface
    public interface After {
        void onChanged(CaptainContext context, CaptainMutationResult result);
    }
}
