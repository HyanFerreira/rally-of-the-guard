package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.hfstack.rallyguard.network.codec.MoreCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record ScopedNamedRouteAssignmentC2SPayload(GuardCommandScope scope, UUID routeId)
        implements CustomPacketPayload {
    public static final Type<ScopedNamedRouteAssignmentC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "scoped_named_route_assignment")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ScopedNamedRouteAssignmentC2SPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buf, ScopedNamedRouteAssignmentC2SPayload value) {
                    GuardCommandScopeCodec.encode(buf, value.scope());
                    MoreCodecs.UUID_CODEC.encode(buf, value.routeId());
                }

                @Override
                public ScopedNamedRouteAssignmentC2SPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ScopedNamedRouteAssignmentC2SPayload(
                            GuardCommandScopeCodec.decode(buf), MoreCodecs.UUID_CODEC.decode(buf)
                    );
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
