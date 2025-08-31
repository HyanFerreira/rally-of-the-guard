package net.hfstack.rallyguard.network.codec;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.UUID;

/**
 * Codecs extras para versões onde PacketCodecs não expõe certos tipos.
 */
public final class MoreCodecs {
    private MoreCodecs() {
    }

    /**
     * Codec de UUID compatível com 1.21.1 (lê/grava como dois longs).
     */
    public static final PacketCodec<RegistryByteBuf, UUID> UUID_CODEC = new PacketCodec<>() {
        @Override
        public UUID decode(RegistryByteBuf buf) {
            long msb = buf.readLong();
            long lsb = buf.readLong();
            return new UUID(msb, lsb);
        }

        @Override
        public void encode(RegistryByteBuf buf, UUID value) {
            buf.writeLong(value.getMostSignificantBits());
            buf.writeLong(value.getLeastSignificantBits());
        }
    };
}
