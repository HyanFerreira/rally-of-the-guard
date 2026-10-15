package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.profile.GuardRank;
import net.hfstack.rallyguard.api.recruitment.RecruitmentOffer;
import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainActionView;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.hfstack.rallyguard.api.captain.CaptainOffer;
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
                    buf.writeBoolean(value.captainAction().isPresent());
                    if (value.captainAction().isPresent()) {
                        writeCaptainAction(buf, value.captainAction().orElseThrow());
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
                    Optional<CaptainActionView> captainAction = buf.readBoolean()
                            ? Optional.of(readCaptainAction(buf))
                            : Optional.empty();
                    return new MusterRollSnapshotS2CPayload(open, new GuardProfileSnapshot(
                            entityId, uuid, name, level, rank, rankOverride, captain, stars,
                            xpInto, xpNext, health, attack, armor, speed, kills, assists, service, offer,
                            captainAction
                    ));
                }
            };

    private static void writeCaptainAction(RegistryFriendlyByteBuf buf, CaptainActionView view) {
        buf.writeBoolean(view.captain());
        buf.writeVarInt(CaptainActionC2SPayload.actionId(view.action()));
        buf.writeBoolean(view.allowed());
        buf.writeBoolean(view.blockingOutcome().isPresent());
        view.blockingOutcome().ifPresent(outcome -> buf.writeVarInt(outcomeId(outcome)));
        buf.writeVarInt(view.minimumLevel());
        buf.writeBoolean(view.offer().isPresent());
        if (view.offer().isPresent()) {
            CaptainOffer offer = view.offer().orElseThrow();
            buf.writeUtf(offer.paymentItemId().toString(), 128);
            buf.writeVarInt(offer.cost());
        }
    }

    private static CaptainActionView readCaptainAction(RegistryFriendlyByteBuf buf) {
        boolean captain = buf.readBoolean();
        CaptainAction action = CaptainActionC2SPayload.actionFromId(buf.readVarInt());
        boolean allowed = buf.readBoolean();
        Optional<CaptainMutationResult.Outcome> blocking = buf.readBoolean()
                ? Optional.of(outcomeFromId(buf.readVarInt()))
                : Optional.empty();
        int minimumLevel = buf.readVarInt();
        Optional<CaptainOffer> offer = Optional.empty();
        if (buf.readBoolean()) {
            Identifier item = Identifier.tryParse(buf.readUtf(128));
            int cost = buf.readVarInt();
            if (item != null && cost >= 0) offer = Optional.of(new CaptainOffer(item, cost));
        }
        return new CaptainActionView(captain, action, allowed, blocking, minimumLevel, offer);
    }

    private static int outcomeId(CaptainMutationResult.Outcome outcome) {
        return switch (outcome) {
            case SUCCESS -> 0;
            case DISABLED -> 1;
            case NOT_AVAILABLE -> 2;
            case NOT_OWNER -> 3;
            case UNSUPPORTED_PROFILE -> 4;
            case ALREADY_CAPTAIN -> 5;
            case NOT_CAPTAIN -> 6;
            case LEVEL_TOO_LOW -> 7;
            case INSUFFICIENT_PAYMENT -> 8;
            case ACTIVE_OFFICER -> 9;
            case PERSISTENCE_ERROR -> 10;
            case DENIED -> 11;
        };
    }

    private static CaptainMutationResult.Outcome outcomeFromId(int id) {
        return switch (id) {
            case 0 -> CaptainMutationResult.Outcome.SUCCESS;
            case 1 -> CaptainMutationResult.Outcome.DISABLED;
            case 2 -> CaptainMutationResult.Outcome.NOT_AVAILABLE;
            case 3 -> CaptainMutationResult.Outcome.NOT_OWNER;
            case 4 -> CaptainMutationResult.Outcome.UNSUPPORTED_PROFILE;
            case 5 -> CaptainMutationResult.Outcome.ALREADY_CAPTAIN;
            case 6 -> CaptainMutationResult.Outcome.NOT_CAPTAIN;
            case 7 -> CaptainMutationResult.Outcome.LEVEL_TOO_LOW;
            case 8 -> CaptainMutationResult.Outcome.INSUFFICIENT_PAYMENT;
            case 9 -> CaptainMutationResult.Outcome.ACTIVE_OFFICER;
            case 10 -> CaptainMutationResult.Outcome.PERSISTENCE_ERROR;
            case 11 -> CaptainMutationResult.Outcome.DENIED;
            default -> CaptainMutationResult.Outcome.NOT_AVAILABLE;
        };
    }

    private static <T> T enumValue(T[] values, int ordinal, T fallback) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
