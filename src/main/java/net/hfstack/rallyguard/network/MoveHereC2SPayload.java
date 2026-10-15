package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MoveHereC2SPayload(
        GuardCommandScope scope,
        int categoryId,
        BlockPos clickedBlock,
        int faceId
) implements CustomPacketPayload {
    public static final Type<MoveHereC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "move_here")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, MoveHereC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, MoveHereC2SPayload value) {
            GuardCommandScopeCodec.encode(buf, value.scope());
            buf.writeVarInt(value.categoryId());
            buf.writeLong(value.clickedBlock().asLong());
            buf.writeVarInt(value.faceId());
        }

        @Override
        public MoveHereC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new MoveHereC2SPayload(
                    GuardCommandScopeCodec.decode(buf),
                    buf.readVarInt(),
                    BlockPos.of(buf.readLong()),
                    buf.readVarInt()
            );
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
