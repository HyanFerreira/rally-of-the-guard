package net.hfstack.rallyguard.order.defend;

import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public record DefendAreaState(
        int schemaVersion,
        boolean active,
        BlockPos center,
        BlockPos post,
        int radius,
        Optional<UUID> commandPostId,
        OptionalInt raidId
) {
    public static final int SCHEMA_VERSION = 3;
    public static final int MAX_RADIUS = 64;

    public DefendAreaState {
        center = Objects.requireNonNull(center, "center").immutable();
        post = Objects.requireNonNull(post, "post").immutable();
        commandPostId = commandPostId == null ? Optional.empty() : commandPostId;
        raidId = raidId == null ? OptionalInt.empty() : raidId;
        if (radius < 1 || radius > MAX_RADIUS) {
            throw new IllegalArgumentException("Defend radius must be between 1 and " + MAX_RADIUS);
        }
        if (raidId.isPresent() && raidId.getAsInt() < 0) {
            throw new IllegalArgumentException("raidId cannot be negative");
        }
        if (commandPostId.isPresent() && raidId.isPresent()) {
            throw new IllegalArgumentException("A defense order cannot have multiple origins");
        }
    }

    public static DefendAreaState active(BlockPos center, BlockPos post, int radius) {
        return new DefendAreaState(
                SCHEMA_VERSION, true, center, post, radius, Optional.empty(), OptionalInt.empty()
        );
    }

    public static DefendAreaState fromCommandPost(BlockPos center, BlockPos post, int radius, UUID commandPostId) {
        return new DefendAreaState(
                SCHEMA_VERSION, true, center, post, radius,
                Optional.of(Objects.requireNonNull(commandPostId, "commandPostId")), OptionalInt.empty()
        );
    }

    public static DefendAreaState fromRaid(BlockPos center, BlockPos post, int radius, int raidId) {
        return new DefendAreaState(
                SCHEMA_VERSION, true, center, post, radius, Optional.empty(), OptionalInt.of(raidId)
        );
    }
}
