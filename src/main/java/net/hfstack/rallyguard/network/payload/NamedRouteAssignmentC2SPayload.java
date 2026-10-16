package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;

public record NamedRouteAssignmentC2SPayload(int entityId, Optional<UUID> routeId)
        implements CustomPacketPayload {
    public static final Type<NamedRouteAssignmentC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "named_route_assignment")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NamedRouteAssignmentC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, NamedRouteAssignmentC2SPayload value) {
            buf.writeVarInt(value.entityId());
            GuardCommandScopeCodec.writeOptionalUuid(buf, value.routeId());
        }

        @Override
        public NamedRouteAssignmentC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new NamedRouteAssignmentC2SPayload(
                    buf.readVarInt(), GuardCommandScopeCodec.readOptionalUuid(buf)
            );
        }
    };

    public NamedRouteAssignmentC2SPayload {
        routeId = routeId == null ? Optional.empty() : routeId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
