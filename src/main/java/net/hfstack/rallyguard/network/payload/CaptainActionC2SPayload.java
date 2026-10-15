package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public record CaptainActionC2SPayload(CaptainAction action) implements CustomPacketPayload {
    public static final Type<CaptainActionC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "captain_action")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CaptainActionC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, CaptainActionC2SPayload payload) {
            buf.writeVarInt(actionId(payload.action()));
        }

        @Override
        public CaptainActionC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new CaptainActionC2SPayload(actionFromId(buf.readVarInt()));
        }
    };

    public CaptainActionC2SPayload {
        Objects.requireNonNull(action, "action");
    }

    static int actionId(CaptainAction action) {
        return switch (action) {
            case NONE -> 0;
            case PROMOTE -> 1;
            case DEMOTE -> 2;
        };
    }

    static CaptainAction actionFromId(int id) {
        return switch (id) {
            case 1 -> CaptainAction.PROMOTE;
            case 2 -> CaptainAction.DEMOTE;
            default -> CaptainAction.NONE;
        };
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
