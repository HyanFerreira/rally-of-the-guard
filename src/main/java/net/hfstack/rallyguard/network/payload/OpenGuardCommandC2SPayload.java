package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: cliente pede a lista de guardas para abrir a GUI. Não carrega dados.
 */
public record OpenGuardCommandC2SPayload() implements CustomPacketPayload {
    public static final Type<OpenGuardCommandC2SPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "open_guard_command"));

    // sem campos: codec vazio
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGuardCommandC2SPayload> CODEC =
            StreamCodec.of((buf, p) -> {
            }, buf -> new OpenGuardCommandC2SPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
