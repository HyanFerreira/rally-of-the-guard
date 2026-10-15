package net.hfstack.rallyguard.client.move;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardUnitCategory;

import java.util.Objects;
import java.util.Optional;

public final class MoveTargetingSession {
    public static final long TIMEOUT_TICKS = 300;

    public enum Order {
        MOVE_HERE,
        DEFEND_AREA
    }

    public enum Update {
        INACTIVE,
        ACTIVE,
        EXPIRED,
        CANCELLED
    }

    public record Armed(
            Order order,
            GuardCommandScope scope,
            GuardUnitCategory category,
            int handId,
            long expiresAtTick
    ) {
        public Armed {
            Objects.requireNonNull(order, "order");
            Objects.requireNonNull(scope, "scope");
            Objects.requireNonNull(category, "category");
            if (handId < 0 || handId > 1) throw new IllegalArgumentException("Unknown hand: " + handId);
        }
    }

    private Armed armed;

    public void arm(GuardCommandScope scope, GuardUnitCategory category, int handId, long currentTick) {
        arm(Order.MOVE_HERE, scope, category, handId, currentTick);
    }

    public void arm(
            Order order,
            GuardCommandScope scope,
            GuardUnitCategory category,
            int handId,
            long currentTick
    ) {
        armed = new Armed(order, scope, category, handId, currentTick + TIMEOUT_TICKS);
    }

    public boolean isArmed() {
        return armed != null;
    }

    public Optional<Armed> current() {
        return Optional.ofNullable(armed);
    }

    public Update update(long currentTick, boolean playerAlive, boolean ledgerStillHeld) {
        if (armed == null) return Update.INACTIVE;
        if (!playerAlive || !ledgerStillHeld) {
            armed = null;
            return Update.CANCELLED;
        }
        if (currentTick >= armed.expiresAtTick()) {
            armed = null;
            return Update.EXPIRED;
        }
        return Update.ACTIVE;
    }

    public Optional<Armed> consume(long currentTick, boolean playerAlive, boolean ledgerStillHeld) {
        if (update(currentTick, playerAlive, ledgerStillHeld) != Update.ACTIVE) return Optional.empty();
        Armed result = armed;
        armed = null;
        return Optional.of(result);
    }

    public boolean cancel() {
        if (armed == null) return false;
        armed = null;
        return true;
    }

    public void reset() {
        armed = null;
    }
}
