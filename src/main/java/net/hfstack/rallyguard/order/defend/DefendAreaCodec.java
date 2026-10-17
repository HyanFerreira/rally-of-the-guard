package net.hfstack.rallyguard.order.defend;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public final class DefendAreaCodec {
    private static final String SCHEMA_VERSION = "schema_version";
    private static final String ACTIVE = "active";
    private static final String CENTER_X = "center_x";
    private static final String CENTER_Y = "center_y";
    private static final String CENTER_Z = "center_z";
    private static final String POST_X = "post_x";
    private static final String POST_Y = "post_y";
    private static final String POST_Z = "post_z";
    private static final String RADIUS = "radius";
    private static final String COMMAND_POST_ID = "command_post_id";
    private static final String RAID_ID = "raid_id";

    private DefendAreaCodec() {
    }

    public static CompoundTag encode(DefendAreaState state) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(SCHEMA_VERSION, state.schemaVersion());
        tag.putBoolean(ACTIVE, state.active());
        tag.putInt(CENTER_X, state.center().getX());
        tag.putInt(CENTER_Y, state.center().getY());
        tag.putInt(CENTER_Z, state.center().getZ());
        tag.putInt(POST_X, state.post().getX());
        tag.putInt(POST_Y, state.post().getY());
        tag.putInt(POST_Z, state.post().getZ());
        tag.putInt(RADIUS, state.radius());
        state.commandPostId().ifPresent(id -> tag.putString(COMMAND_POST_ID, id.toString()));
        state.raidId().ifPresent(id -> tag.putInt(RAID_ID, id));
        return tag;
    }

    public static DefendAreaRead decode(CompoundTag tag) {
        if (tag == null) return inactive();

        Integer schemaVersion = strictInt(tag.get(SCHEMA_VERSION));
        if (schemaVersion != null && schemaVersion > DefendAreaState.SCHEMA_VERSION) {
            return new DefendAreaRead.Unsupported(schemaVersion);
        }
        if (schemaVersion == null || schemaVersion < 1) return inactive();

        Boolean active = strictBoolean(tag.get(ACTIVE));
        Integer centerX = strictInt(tag.get(CENTER_X));
        Integer centerY = strictInt(tag.get(CENTER_Y));
        Integer centerZ = strictInt(tag.get(CENTER_Z));
        Integer postX = strictInt(tag.get(POST_X));
        Integer postY = strictInt(tag.get(POST_Y));
        Integer postZ = strictInt(tag.get(POST_Z));
        Integer radius = strictInt(tag.get(RADIUS));
        if (active == null || centerX == null || centerY == null || centerZ == null
                || postX == null || postY == null || postZ == null || radius == null
                || radius < 1 || radius > DefendAreaState.MAX_RADIUS) {
            return inactive();
        }

        Optional<UUID> commandPostId = Optional.empty();
        if (schemaVersion >= 2 && tag.get(COMMAND_POST_ID) != null) {
            commandPostId = tag.getString(COMMAND_POST_ID).flatMap(DefendAreaCodec::parseUuid);
            if (commandPostId.isEmpty()) return inactive();
        }

        OptionalInt raidId = OptionalInt.empty();
        if (schemaVersion >= 3 && tag.get(RAID_ID) != null) {
            Integer decodedRaidId = strictInt(tag.get(RAID_ID));
            if (decodedRaidId == null || decodedRaidId < 0 || commandPostId.isPresent()) return inactive();
            raidId = OptionalInt.of(decodedRaidId);
        }

        return new DefendAreaRead.Available(Optional.of(new DefendAreaState(
                DefendAreaState.SCHEMA_VERSION,
                active,
                new BlockPos(centerX, centerY, centerZ),
                new BlockPos(postX, postY, postZ),
                radius,
                commandPostId,
                raidId
        )));
    }

    private static DefendAreaRead.Available inactive() {
        return new DefendAreaRead.Available(Optional.empty());
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

    private static Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
