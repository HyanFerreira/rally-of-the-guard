package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.raid.RaidReportSummary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record RaidReportS2CPayload(
        int guardsDeployed, int enemiesDefeated, int guardsLost,
        Optional<String> mvpName, int mvpKills, boolean incomplete
) implements CustomPacketPayload {
    public static final Type<RaidReportS2CPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "raid_report")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RaidReportS2CPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, RaidReportS2CPayload value) {
            buf.writeVarInt(value.guardsDeployed());
            buf.writeVarInt(value.enemiesDefeated());
            buf.writeVarInt(value.guardsLost());
            buf.writeBoolean(value.mvpName().isPresent());
            value.mvpName().ifPresent(name -> buf.writeUtf(name, 256));
            buf.writeVarInt(value.mvpKills());
            buf.writeBoolean(value.incomplete());
        }

        @Override
        public RaidReportS2CPayload decode(RegistryFriendlyByteBuf buf) {
            int deployed = buf.readVarInt();
            int defeated = buf.readVarInt();
            int lost = buf.readVarInt();
            Optional<String> mvp = buf.readBoolean() ? Optional.of(buf.readUtf(256)) : Optional.empty();
            return new RaidReportS2CPayload(deployed, defeated, lost, mvp, buf.readVarInt(), buf.readBoolean());
        }
    };

    public RaidReportS2CPayload(RaidReportSummary summary) {
        this(summary.guardsDeployed(), summary.enemiesDefeated(), summary.guardsLost(),
                summary.mvpName(), summary.mvpKills(), summary.incomplete());
    }

    public RaidReportS2CPayload {
        new RaidReportSummary(guardsDeployed, enemiesDefeated, guardsLost, mvpName, mvpKills, incomplete);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
