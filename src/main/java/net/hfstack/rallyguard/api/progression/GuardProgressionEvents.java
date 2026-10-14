package net.hfstack.rallyguard.api.progression;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.hfstack.rallyguard.RallyOfTheGuard;

import java.util.Objects;

public final class GuardProgressionEvents {
    private GuardProgressionEvents() {
    }

    public static final Event<BeforeXpAwarded> BEFORE_XP_AWARDED = EventFactory.createArrayBacked(
            BeforeXpAwarded.class,
            listeners -> (context, proposedXpMilli) -> {
                long current = Math.max(0, proposedXpMilli);
                for (BeforeXpAwarded listener : listeners) {
                    try {
                        XpAwardDecision decision = Objects.requireNonNull(
                                listener.evaluate(context, current),
                                "BEFORE_XP_AWARDED listener returned null"
                        );
                        if (decision instanceof XpAwardDecision.Deny) return decision;
                        current = ((XpAwardDecision.Allow) decision).xpMilli();
                    } catch (RuntimeException exception) {
                        RallyOfTheGuard.LOGGER.error("BEFORE_XP_AWARDED listener failed", exception);
                    }
                }
                return XpAwardDecision.allow(current);
            }
    );

    public static final Event<AfterXpAwarded> AFTER_XP_AWARDED = EventFactory.createArrayBacked(
            AfterXpAwarded.class,
            listeners -> context -> notifySafely("AFTER_XP_AWARDED", listeners, listener -> listener.onAwarded(context))
    );

    public static final Event<LevelChanged> LEVEL_CHANGED = EventFactory.createArrayBacked(
            LevelChanged.class,
            listeners -> context -> notifySafely("LEVEL_CHANGED", listeners, listener -> listener.onLevelChanged(context))
    );

    public static final Event<CombatStatRecorded> COMBAT_STAT_RECORDED = EventFactory.createArrayBacked(
            CombatStatRecorded.class,
            listeners -> context -> notifySafely(
                    "COMBAT_STAT_RECORDED",
                    listeners,
                    listener -> listener.onRecorded(context)
            )
    );

    private static <T> void notifySafely(String eventName, T[] listeners, ListenerCall<T> call) {
        for (T listener : listeners) {
            try {
                call.invoke(listener);
            } catch (RuntimeException exception) {
                RallyOfTheGuard.LOGGER.error("{} listener failed", eventName, exception);
            }
        }
    }

    @FunctionalInterface
    public interface BeforeXpAwarded {
        XpAwardDecision evaluate(XpAwardContext context, long proposedXpMilli);
    }

    @FunctionalInterface
    public interface AfterXpAwarded {
        void onAwarded(XpAwardedContext context);
    }

    @FunctionalInterface
    public interface LevelChanged {
        void onLevelChanged(LevelChangedContext context);
    }

    @FunctionalInterface
    public interface CombatStatRecorded {
        void onRecorded(CombatStatContext context);
    }

    @FunctionalInterface
    private interface ListenerCall<T> {
        void invoke(T listener);
    }
}
