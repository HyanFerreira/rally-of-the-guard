package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Objects;
import java.util.Optional;

public final class MoveOrders {
    private MoveOrders() {
    }

    public static MoveOrderRead read(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        CompoundTag stored = entity.getAttached(MoveOrderAttachments.ORDER);
        return MoveOrderCodec.decode(stored);
    }

    public static boolean set(Entity entity, BlockPos target) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(target, "target");
        if (!MoveOrderPersistencePolicy.canReplace(read(entity))) return false;
        entity.setAttached(MoveOrderAttachments.ORDER, MoveOrderCodec.encode(MoveOrderState.active(target)));
        return true;
    }

    public static void clear(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        entity.removeAttached(MoveOrderAttachments.ORDER);
    }

    public static Optional<MoveOrderState> active(Entity entity) {
        MoveOrderRead current = read(entity);
        if (!(current instanceof MoveOrderRead.Available available)) return Optional.empty();
        return available.state().filter(MoveOrderState::active);
    }
}
