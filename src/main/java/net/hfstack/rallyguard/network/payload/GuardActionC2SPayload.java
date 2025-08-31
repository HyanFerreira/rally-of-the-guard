package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S: cliente manda ação para um guarda específico.
 */
public record GuardActionC2SPayload(int entityId, int action) implements CustomPayload {
    public static final Id<GuardActionC2SPayload> ID =
            new Id<>(Identifier.of(RallyOfTheGuard.MOD_ID, "guard_action"));

    public static final PacketCodec<RegistryByteBuf, GuardActionC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, GuardActionC2SPayload::entityId,
                    PacketCodecs.VAR_INT, GuardActionC2SPayload::action,
                    GuardActionC2SPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
