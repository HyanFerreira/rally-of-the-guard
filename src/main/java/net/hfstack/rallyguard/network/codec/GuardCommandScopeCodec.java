package net.hfstack.rallyguard.network.codec;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardCommandScopeKind;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.Optional;
import java.util.UUID;

public final class GuardCommandScopeCodec {
    private GuardCommandScopeCodec() {
    }

    public static void encode(RegistryFriendlyByteBuf buf, GuardCommandScope scope) {
        buf.writeByte(scope.kind().ordinal());
        writeOptionalUuid(buf, scope.squadId());
    }

    public static GuardCommandScope decode(RegistryFriendlyByteBuf buf) {
        int ordinal = buf.readUnsignedByte();
        GuardCommandScopeKind[] values = GuardCommandScopeKind.values();
        if (ordinal >= values.length) throw new IllegalArgumentException("Unknown guard command scope: " + ordinal);
        return new GuardCommandScope(values[ordinal], readOptionalUuid(buf));
    }

    public static void writeOptionalUuid(RegistryFriendlyByteBuf buf, Optional<UUID> value) {
        buf.writeBoolean(value.isPresent());
        value.ifPresent(uuid -> MoreCodecs.UUID_CODEC.encode(buf, uuid));
    }

    public static Optional<UUID> readOptionalUuid(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? Optional.of(MoreCodecs.UUID_CODEC.decode(buf)) : Optional.empty();
    }
}
