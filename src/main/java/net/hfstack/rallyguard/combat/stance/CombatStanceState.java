package net.hfstack.rallyguard.combat.stance;

import net.hfstack.rallyguard.api.command.GuardCombatStance;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Optional;

public record CombatStanceState(
        int schemaVersion,
        GuardCombatStance stance,
        Optional<BlockPos> anchor
) {
    public static final int SCHEMA_VERSION = 1;

    public CombatStanceState {
        stance = Objects.requireNonNull(stance, "stance");
        anchor = Objects.requireNonNull(anchor, "anchor").map(BlockPos::immutable);
        if (stance == GuardCombatStance.HOLD_POSITION && anchor.isEmpty()) {
            throw new IllegalArgumentException("Hold position requires an anchor");
        }
        if (stance != GuardCombatStance.HOLD_POSITION) {
            anchor = Optional.empty();
        }
    }

    public static CombatStanceState defensive() {
        return of(GuardCombatStance.DEFENSIVE, null);
    }

    public static CombatStanceState of(GuardCombatStance stance, BlockPos anchor) {
        return new CombatStanceState(SCHEMA_VERSION, stance, Optional.ofNullable(anchor));
    }
}
