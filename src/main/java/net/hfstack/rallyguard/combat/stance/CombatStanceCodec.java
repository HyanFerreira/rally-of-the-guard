package net.hfstack.rallyguard.combat.stance;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

public final class CombatStanceCodec {
    private static final String SCHEMA_VERSION = "schema_version";
    private static final String STANCE = "stance";
    private static final String ANCHOR_X = "anchor_x";
    private static final String ANCHOR_Y = "anchor_y";
    private static final String ANCHOR_Z = "anchor_z";

    private CombatStanceCodec() {
    }

    public static CompoundTag encode(CombatStanceState state) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(SCHEMA_VERSION, state.schemaVersion());
        tag.putInt(STANCE, state.stance().wireId());
        state.anchor().ifPresent(anchor -> {
            tag.putInt(ANCHOR_X, anchor.getX());
            tag.putInt(ANCHOR_Y, anchor.getY());
            tag.putInt(ANCHOR_Z, anchor.getZ());
        });
        return tag;
    }

    public static CombatStanceRead decode(CompoundTag tag) {
        if (tag == null) return defensive();

        Integer schemaVersion = strictInt(tag.get(SCHEMA_VERSION));
        if (schemaVersion != null && schemaVersion > CombatStanceState.SCHEMA_VERSION) {
            return new CombatStanceRead.Unsupported(schemaVersion);
        }
        if (schemaVersion == null || schemaVersion != CombatStanceState.SCHEMA_VERSION) return defensive();

        Integer stanceId = strictInt(tag.get(STANCE));
        GuardCombatStance stance = stanceId == null
                ? null
                : GuardCombatStance.fromWireId(stanceId).orElse(null);
        if (stance == null) return defensive();
        if (stance != GuardCombatStance.HOLD_POSITION) {
            return new CombatStanceRead.Available(CombatStanceState.of(stance, null));
        }

        Integer anchorX = strictInt(tag.get(ANCHOR_X));
        Integer anchorY = strictInt(tag.get(ANCHOR_Y));
        Integer anchorZ = strictInt(tag.get(ANCHOR_Z));
        if (anchorX == null || anchorY == null || anchorZ == null) return defensive();
        return new CombatStanceRead.Available(CombatStanceState.of(
                stance,
                new BlockPos(anchorX, anchorY, anchorZ)
        ));
    }

    private static CombatStanceRead.Available defensive() {
        return new CombatStanceRead.Available(CombatStanceState.defensive());
    }

    private static Integer strictInt(Tag tag) {
        if (!(tag instanceof NumericTag numeric) || tag.getId() != Tag.TAG_INT) return null;
        return numeric.intValue();
    }
}
