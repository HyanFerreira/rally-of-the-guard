package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GuardAttackTargetC2SPayload(int targetEntityId, int mode) implements CustomPayload {
    public static final Id<GuardAttackTargetC2SPayload> ID =
            new Id<>(Identifier.of(RallyOfTheGuard.MOD_ID, "guard_attack_target"));

    public static final PacketCodec<RegistryByteBuf, GuardAttackTargetC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, GuardAttackTargetC2SPayload::targetEntityId,
                    PacketCodecs.VAR_INT, GuardAttackTargetC2SPayload::mode,
                    GuardAttackTargetC2SPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
