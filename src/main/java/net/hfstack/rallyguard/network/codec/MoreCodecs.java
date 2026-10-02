package net.hfstack.rallyguard.network.codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/**
 * Codecs extras para versões onde ByteBufCodecs não expõe certos tipos.
 */
public final class MoreCodecs {
    private MoreCodecs() {
    }

    /**
     * Codec de UUID compatível com 1.21.1 (lê/grava como dois longs).
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID_CODEC = new StreamCodec<>() {
        @Override
        public UUID decode(RegistryFriendlyByteBuf buf) {
            long msb = buf.readLong();
            long lsb = buf.readLong();
            return new UUID(msb, lsb);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, UUID value) {
            buf.writeLong(value.getMostSignificantBits());
            buf.writeLong(value.getLeastSignificantBits());
        }
    };
}
