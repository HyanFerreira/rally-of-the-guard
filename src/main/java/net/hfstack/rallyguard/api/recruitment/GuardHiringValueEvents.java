package net.hfstack.rallyguard.api.recruitment;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.hfstack.rallyguard.RallyOfTheGuard;

public final class GuardHiringValueEvents {
    private GuardHiringValueEvents() {
    }

    public static final Event<Calculated> CALCULATED = EventFactory.createArrayBacked(
            Calculated.class,
            listeners -> context -> {
                for (Calculated listener : listeners) {
                    try {
                        listener.onCalculated(context);
                    } catch (RuntimeException exception) {
                        RallyOfTheGuard.LOGGER.error("HIRING_VALUE_CALCULATED listener failed", exception);
                    }
                }
            }
    );

    @FunctionalInterface
    public interface Calculated {
        void onCalculated(HiringValueCalculatedContext context);
    }
}
