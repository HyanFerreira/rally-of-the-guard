package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;

public final class MoveOrderCodec {
    private static final String SCHEMA_VERSION = "schema_version";
    private static final String ACTIVE = "active";
    private static final String TARGET_X = "target_x";
    private static final String TARGET_Y = "target_y";
    private static final String TARGET_Z = "target_z";

    private MoveOrderCodec() {
    }

    public static CompoundTag encode(MoveOrderState state) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(SCHEMA_VERSION, state.schemaVersion());
        tag.putBoolean(ACTIVE, state.active());
        tag.putInt(TARGET_X, state.target().getX());
        tag.putInt(TARGET_Y, state.target().getY());
        tag.putInt(TARGET_Z, state.target().getZ());
        return tag;
    }

    public static MoveOrderRead decode(CompoundTag tag) {
        if (tag == null) return new MoveOrderRead.Available(Optional.empty());

        Integer schemaVersion = strictInt(tag.get(SCHEMA_VERSION));
        if (schemaVersion != null && schemaVersion > MoveOrderState.SCHEMA_VERSION) {
            return new MoveOrderRead.Unsupported(schemaVersion);
        }
        if (schemaVersion == null || schemaVersion != MoveOrderState.SCHEMA_VERSION) {
            return new MoveOrderRead.Available(Optional.empty());
        }

        Boolean active = strictBoolean(tag.get(ACTIVE));
        Integer x = strictInt(tag.get(TARGET_X));
        Integer y = strictInt(tag.get(TARGET_Y));
        Integer z = strictInt(tag.get(TARGET_Z));
        if (active == null || x == null || y == null || z == null) {
            return new MoveOrderRead.Available(Optional.empty());
        }

        return new MoveOrderRead.Available(Optional.of(new MoveOrderState(
                MoveOrderState.SCHEMA_VERSION,
                active,
                new BlockPos(x, y, z)
        )));
    }

    private static Integer strictInt(Tag tag) {
        if (!(tag instanceof NumericTag numeric) || tag.getId() != Tag.TAG_INT) return null;
        return numeric.intValue();
    }

    private static Boolean strictBoolean(Tag tag) {
        if (!(tag instanceof NumericTag numeric) || tag.getId() != Tag.TAG_BYTE) return null;
        long value = numeric.longValue();
        if (value != 0 && value != 1) return null;
        return value == 1;
    }
}
