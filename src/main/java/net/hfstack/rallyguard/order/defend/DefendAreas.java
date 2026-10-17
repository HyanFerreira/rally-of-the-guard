package net.hfstack.rallyguard.order.defend;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class DefendAreas {
    private DefendAreas() {
    }

    public static DefendAreaRead read(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        CompoundTag stored = entity.getAttached(DefendAreaAttachments.ORDER);
        return DefendAreaCodec.decode(stored);
    }

    public static boolean set(Entity entity, BlockPos center, BlockPos post, int radius) {
        Objects.requireNonNull(entity, "entity");
        if (!DefendAreaPersistencePolicy.canReplace(read(entity))) return false;
        entity.setAttached(DefendAreaAttachments.ORDER, DefendAreaCodec.encode(
                DefendAreaState.active(center, post, radius)
        ));
        return true;
    }

    public static boolean setFromCommandPost(Entity entity, BlockPos center, BlockPos post, int radius, UUID commandPostId) {
        Objects.requireNonNull(entity, "entity");
        if (!DefendAreaPersistencePolicy.canReplace(read(entity))) return false;
        entity.setAttached(DefendAreaAttachments.ORDER, DefendAreaCodec.encode(
                DefendAreaState.fromCommandPost(center, post, radius, commandPostId)
        ));
        return true;
    }

    public static boolean setFromRaid(Entity entity, BlockPos center, BlockPos post, int radius, int raidId) {
        Objects.requireNonNull(entity, "entity");
        if (!DefendAreaPersistencePolicy.canReplace(read(entity))) return false;
        entity.setAttached(DefendAreaAttachments.ORDER, DefendAreaCodec.encode(
                DefendAreaState.fromRaid(center, post, radius, raidId)
        ));
        return true;
    }

    public static boolean clearIfFromCommandPost(Entity entity, UUID commandPostId) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(commandPostId, "commandPostId");
        Optional<DefendAreaState> current = active(entity);
        if (current.flatMap(DefendAreaState::commandPostId).filter(commandPostId::equals).isEmpty()) return false;
        clear(entity);
        return true;
    }

    public static boolean clearIfFromRaid(Entity entity, int raidId, BlockPos center) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(center, "center");
        Optional<DefendAreaState> current = active(entity);
        if (current.filter(state -> state.raidId().isPresent()
                && state.raidId().getAsInt() == raidId
                && state.center().equals(center)).isEmpty()) return false;
        clear(entity);
        return true;
    }

    public static void clear(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        entity.removeAttached(DefendAreaAttachments.ORDER);
    }

    public static Optional<DefendAreaState> active(Entity entity) {
        DefendAreaRead current = read(entity);
        if (!(current instanceof DefendAreaRead.Available available)) return Optional.empty();
        return available.state().filter(DefendAreaState::active);
    }
}
