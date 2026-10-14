package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.profile.GuardRank;
import net.hfstack.rallyguard.api.recruitment.RecruitmentOffer;
import net.hfstack.rallyguard.muster.GuardProfileSnapshot;
import net.hfstack.rallyguard.muster.GuardServiceStatus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;

public record MusterRollSnapshotS2CPayload(boolean openScreen, GuardProfileSnapshot snapshot)
        implements CustomPacketPayload {
    public static final Type<MusterRollSnapshotS2CPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "muster_roll_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MusterRollSnapshotS2CPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buf, MusterRollSnapshotS2CPayload payload) {
                    GuardProfileSnapshot value = payload.snapshot();
                    buf.writeBoolean(payload.openScreen());
                    buf.writeVarInt(value.entityId());
                    buf.writeLong(value.entityUuid().getMostSignificantBits());
                    buf.writeLong(value.entityUuid().getLeastSignificantBits());
                    buf.writeUtf(value.displayName(), GuardProfileSnapshot.MAX_DISPLAY_NAME_LENGTH);
                    buf.writeVarInt(value.level());
                    buf.writeVarInt(value.rank().ordinal());
                    buf.writeBoolean(value.rankOverride().isPresent());
                    value.rankOverride().ifPresent(label ->
                            buf.writeUtf(label, GuardProfileSnapshot.MAX_RANK_OVERRIDE_LENGTH));
                    buf.writeBoolean(value.captain());
                    buf.writeVarInt(value.stars());
                    buf.writeVarLong(value.xpIntoLevelMilli());
                    buf.writeVarLong(value.xpForNextLevelMilli());
                    buf.writeDouble(value.maxHealth());
                    buf.writeDouble(value.attackDamage());
                    buf.writeDouble(value.armor());
                    buf.writeDouble(value.movementSpeed());
                    buf.writeVarLong(value.kills());
                    buf.writeVarLong(value.assists());
                    buf.writeVarInt(value.serviceStatus().ordinal());
                    buf.writeBoolean(value.hiringOffer().isPresent());
                    if (value.hiringOffer().isPresent()) {
                        RecruitmentOffer offer = value.hiringOffer().orElseThrow();
                        buf.writeUtf(offer.paymentItemId().toString(), 128);
                        buf.writeVarInt(offer.cost());
                    }
                }

                @Override
                public MusterRollSnapshotS2CPayload decode(RegistryFriendlyByteBuf buf) {
                    boolean open = buf.readBoolean();
                    int entityId = buf.readVarInt();
                    UUID uuid = new UUID(buf.readLong(), buf.readLong());
                    String name = buf.readUtf(GuardProfileSnapshot.MAX_DISPLAY_NAME_LENGTH);
                    int level = buf.readVarInt();
                    GuardRank rank = enumValue(GuardRank.values(), buf.readVarInt(), GuardRank.RECRUIT);
                    Optional<String> rankOverride = buf.readBoolean()
                            ? Optional.of(buf.readUtf(GuardProfileSnapshot.MAX_RANK_OVERRIDE_LENGTH))
                            : Optional.empty();
                    boolean captain = buf.readBoolean();
                    int stars = buf.readVarInt();
                    long xpInto = buf.readVarLong();
                    long xpNext = buf.readVarLong();
                    double health = buf.readDouble();
                    double attack = buf.readDouble();
                    double armor = buf.readDouble();
                    double speed = buf.readDouble();
                    long kills = buf.readVarLong();
                    long assists = buf.readVarLong();
                    GuardServiceStatus service = enumValue(
                            GuardServiceStatus.values(),
                            buf.readVarInt(),
                            GuardServiceStatus.ALREADY_IN_SERVICE
                    );
                    Optional<RecruitmentOffer> offer = Optional.empty();
                    if (buf.readBoolean()) {
                        Identifier item = Identifier.tryParse(buf.readUtf(128));
                        int cost = buf.readVarInt();
                        if (item != null && cost >= 0) offer = Optional.of(new RecruitmentOffer(item, cost));
                    }
                    return new MusterRollSnapshotS2CPayload(open, new GuardProfileSnapshot(
                            entityId, uuid, name, level, rank, rankOverride, captain, stars,
                            xpInto, xpNext, health, attack, armor, speed, kills, assists, service, offer
                    ));
                }
            };

    private static <T> T enumValue(T[] values, int ordinal, T fallback) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
