package net.hfstack.rallyguard.order.defend;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Objects;
import java.util.Optional;

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
