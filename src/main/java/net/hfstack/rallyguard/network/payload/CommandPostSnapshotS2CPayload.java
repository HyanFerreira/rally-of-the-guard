package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.network.codec.MoreCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CommandPostSnapshotS2CPayload(
        BlockPos pos,
        UUID postId,
        String name,
        boolean active,
        int radius,
        String formationId,
        List<GuardEntry> guards
) implements CustomPacketPayload {
    public record GuardEntry(int entityId, UUID guardId, String name, String squadName,
                             boolean assignedHere, boolean assignedElsewhere) {
    }

    public static final Type<CommandPostSnapshotS2CPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "command_post_snapshot")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CommandPostSnapshotS2CPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, CommandPostSnapshotS2CPayload value) {
            buf.writeBlockPos(value.pos());
            MoreCodecs.UUID_CODEC.encode(buf, value.postId());
            buf.writeUtf(value.name(), 32);
            buf.writeBoolean(value.active());
            buf.writeVarInt(value.radius());
            buf.writeUtf(value.formationId(), 32);
            buf.writeVarInt(value.guards().size());
            for (GuardEntry guard : value.guards()) {
                buf.writeVarInt(guard.entityId());
                MoreCodecs.UUID_CODEC.encode(buf, guard.guardId());
                buf.writeUtf(guard.name(), 128);
                buf.writeUtf(guard.squadName(), 48);
                buf.writeBoolean(guard.assignedHere());
                buf.writeBoolean(guard.assignedElsewhere());
            }
        }

        @Override
        public CommandPostSnapshotS2CPayload decode(RegistryFriendlyByteBuf buf) {
            BlockPos pos = buf.readBlockPos();
            UUID postId = MoreCodecs.UUID_CODEC.decode(buf);
            String name = buf.readUtf(32);
            boolean active = buf.readBoolean();
            int radius = buf.readVarInt();
            String formation = buf.readUtf(32);
            int count = buf.readVarInt();
            if (count < 0 || count > 4096) throw new IllegalArgumentException("Invalid command post guard count");
            List<GuardEntry> guards = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                guards.add(new GuardEntry(
                        buf.readVarInt(), MoreCodecs.UUID_CODEC.decode(buf), buf.readUtf(128), buf.readUtf(48),
                        buf.readBoolean(), buf.readBoolean()
                ));
            }
            return new CommandPostSnapshotS2CPayload(pos, postId, name, active, radius, formation, guards);
        }
    };

    public CommandPostSnapshotS2CPayload {
        guards = List.copyOf(guards);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
