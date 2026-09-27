package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: cliente manda ação para um guarda específico.
 */
public record GuardActionC2SPayload(int entityId, int action) implements CustomPacketPayload {
    public static final Type<GuardActionC2SPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "guard_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GuardActionC2SPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GuardActionC2SPayload::entityId,
                    ByteBufCodecs.VAR_INT, GuardActionC2SPayload::action,
                    GuardActionC2SPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
