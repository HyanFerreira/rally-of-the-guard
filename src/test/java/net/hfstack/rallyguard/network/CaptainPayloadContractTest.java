package net.hfstack.rallyguard.network;

import io.netty.buffer.Unpooled;
import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainActionView;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.hfstack.rallyguard.api.captain.CaptainOffer;
import net.hfstack.rallyguard.api.profile.GuardRank;
import net.hfstack.rallyguard.muster.GuardProfileSnapshot;
import net.hfstack.rallyguard.muster.GuardServiceStatus;
import net.hfstack.rallyguard.network.payload.CaptainActionC2SPayload;
import net.hfstack.rallyguard.network.payload.MusterRollSnapshotS2CPayload;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaptainPayloadContractTest {
    @Test
    void promotionAndDemotionActionsRoundTripWithExplicitWireIds() {
        assertEquals(CaptainAction.PROMOTE, roundTrip(new CaptainActionC2SPayload(CaptainAction.PROMOTE)).action());
        assertEquals(CaptainAction.DEMOTE, roundTrip(new CaptainActionC2SPayload(CaptainAction.DEMOTE)).action());
    }

    @Test
    void unknownActionIdDecodesToNoneForServerRejection() {
        RegistryFriendlyByteBuf buffer = buffer();
        buffer.writeVarInt(99);

        assertEquals(CaptainAction.NONE, CaptainActionC2SPayload.CODEC.decode(buffer).action());
    }

    @Test
    void musterSnapshotRoundTripsCaptainActionState() {
        CaptainActionView action = new CaptainActionView(
                false,
                CaptainAction.PROMOTE,
                false,
                Optional.of(CaptainMutationResult.Outcome.LEVEL_TOO_LOW),
                7,
                Optional.of(new CaptainOffer(
                        Identifier.fromNamespaceAndPath("minecraft", "emerald"), 4
                ))
        );
        GuardProfileSnapshot snapshot = new GuardProfileSnapshot(
                4,
                UUID.fromString("10000000-0000-0000-0000-000000000001"),
                "Aldric",
                6,
                GuardRank.SOLDIER,
                Optional.empty(),
                false,
                3,
                2_000,
                10_000,
                24,
                5,
                2,
                0.3,
                8,
                2,
                GuardServiceStatus.UNDER_YOUR_COMMAND,
                Optional.empty(),
                Optional.of(action)
        );
        RegistryFriendlyByteBuf buffer = buffer();
        MusterRollSnapshotS2CPayload.CODEC.encode(buffer, new MusterRollSnapshotS2CPayload(true, snapshot));

        MusterRollSnapshotS2CPayload decoded = MusterRollSnapshotS2CPayload.CODEC.decode(buffer);

        assertEquals(Optional.of(action), decoded.snapshot().captainAction());
    }

    private static CaptainActionC2SPayload roundTrip(CaptainActionC2SPayload payload) {
        RegistryFriendlyByteBuf buffer = buffer();
        CaptainActionC2SPayload.CODEC.encode(buffer, payload);
        return CaptainActionC2SPayload.CODEC.decode(buffer);
    }

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }
}
