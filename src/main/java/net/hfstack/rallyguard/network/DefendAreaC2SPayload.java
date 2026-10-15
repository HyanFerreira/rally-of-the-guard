package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DefendAreaC2SPayload(
        GuardCommandScope scope,
        int categoryId,
        BlockPos clickedBlock,
        int faceId
) implements CustomPacketPayload {
    public static final Type<DefendAreaC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "defend_area")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, DefendAreaC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, DefendAreaC2SPayload value) {
            GuardCommandScopeCodec.encode(buf, value.scope());
            buf.writeVarInt(value.categoryId());
            buf.writeLong(value.clickedBlock().asLong());
            buf.writeVarInt(value.faceId());
        }

        @Override
        public DefendAreaC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new DefendAreaC2SPayload(
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
