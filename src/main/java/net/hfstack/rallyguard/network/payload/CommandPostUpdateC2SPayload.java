package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CommandPostUpdateC2SPayload(
        BlockPos pos, int action, int guardEntityId, String name, boolean active, int radius, String formationId
) implements CustomPacketPayload {
    public static final int UPDATE_SETTINGS = 0;
    public static final int ASSIGN_GUARD = 1;
    public static final int UNASSIGN_GUARD = 2;

    public static final Type<CommandPostUpdateC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "command_post_update")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CommandPostUpdateC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, CommandPostUpdateC2SPayload value) {
            buf.writeBlockPos(value.pos());
            buf.writeVarInt(value.action());
            buf.writeVarInt(value.guardEntityId());
            buf.writeUtf(value.name(), 128);
            buf.writeBoolean(value.active());
            buf.writeVarInt(value.radius());
            buf.writeUtf(value.formationId(), 32);
        }

        @Override
        public CommandPostUpdateC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new CommandPostUpdateC2SPayload(
                    buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(128),
                    buf.readBoolean(), buf.readVarInt(), buf.readUtf(32)
            );
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
