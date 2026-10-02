package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record GuardAttackTargetC2SPayload(int targetEntityId, int mode) implements CustomPacketPayload {
    public static final Type<GuardAttackTargetC2SPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "guard_attack_target"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GuardAttackTargetC2SPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GuardAttackTargetC2SPayload::targetEntityId,
                    ByteBufCodecs.VAR_INT, GuardAttackTargetC2SPayload::mode,
                    GuardAttackTargetC2SPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
