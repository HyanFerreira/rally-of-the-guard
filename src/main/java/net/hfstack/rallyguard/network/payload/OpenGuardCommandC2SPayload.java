package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S: cliente pede a lista de guardas para abrir a GUI. Não carrega dados.
 */
public record OpenGuardCommandC2SPayload() implements CustomPayload {
    public static final Id<OpenGuardCommandC2SPayload> ID =
            new Id<>(Identifier.of(RallyOfTheGuard.MOD_ID, "open_guard_command"));

    // sem campos: codec vazio
    public static final PacketCodec<RegistryByteBuf, OpenGuardCommandC2SPayload> CODEC =
            PacketCodec.of((buf, p) -> {
            }, buf -> new OpenGuardCommandC2SPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
