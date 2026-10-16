package net.hfstack.rallyguard.combat.stance;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Objects;

public final class CombatStances {
    private CombatStances() {
    }

    public static CombatStanceRead read(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        CompoundTag stored = entity.getAttached(CombatStanceAttachments.STANCE);
        return CombatStanceCodec.decode(stored);
    }

    public static boolean set(Entity entity, GuardCombatStance stance, BlockPos anchor) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(stance, "stance");
        if (read(entity) instanceof CombatStanceRead.Unsupported) return false;
        entity.setAttached(CombatStanceAttachments.STANCE, CombatStanceCodec.encode(
                CombatStanceState.of(stance, stance == GuardCombatStance.HOLD_POSITION ? anchor : null)
        ));
        return true;
    }

    public static GuardCombatStance current(Entity entity) {
        CombatStanceRead read = read(entity);
        return read instanceof CombatStanceRead.Available available
                ? available.state().stance()
                : GuardCombatStance.DEFENSIVE;
    }
}
