package net.hfstack.rallyguard.muster;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.presentation.GuardPresentation;
import net.hfstack.rallyguard.api.presentation.GuardPresentationContext;
import net.hfstack.rallyguard.api.presentation.GuardPresentationRegistry;
import net.hfstack.rallyguard.api.profile.GuardProfileView;
import net.hfstack.rallyguard.api.recruitment.RecruitmentOffer;
import net.hfstack.rallyguard.api.recruitment.RecruitmentQuoteResult;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Optional;
import java.util.UUID;

public final class GuardProfileSnapshotFactory {
    private GuardProfileSnapshotFactory() {
    }

    public static Optional<GuardProfileSnapshot> create(ServerPlayer viewer, GuardEntity guard) {
        Optional<GuardProfileView> profileResult = RallyGuardApi.profiles().find(guard);
        if (profileResult.isEmpty()) return Optional.empty();
        GuardProfileView profile = profileResult.orElseThrow();

        GuardPresentation explicit = GuardPresentationRegistry.resolveExplicit(
                new GuardPresentationContext(viewer, guard, viewer.level())
        );
        Optional<String> rankOverride = explicit.rank()
                .map(component -> component.getString().trim())
                .filter(value -> !value.isEmpty());
        GuardServiceStatus status = serviceStatus(viewer, guard);
        Optional<RecruitmentOffer> offer = status == GuardServiceStatus.UNOWNED
                ? currentOffer(viewer, guard)
                : Optional.empty();

        String displayName = guard.getCustomName() != null
                ? guard.getCustomName().getString()
                : guard.getName().getString();
        return Optional.of(new GuardProfileSnapshot(
                guard.getId(),
                guard.getUUID(),
                displayName,
                profile.level(),
                profile.rank(),
                rankOverride,
                profile.captain(),
                profile.qualityStars(),
                profile.xpIntoLevelMilli(),
                profile.xpForNextLevelMilli(),
                guard.getAttributeValue(Attributes.MAX_HEALTH),
                guard.getAttributeValue(Attributes.ATTACK_DAMAGE),
                guard.getAttributeValue(Attributes.ARMOR),
                guard.getAttributeValue(Attributes.MOVEMENT_SPEED),
                profile.kills(),
                profile.assists(),
                status,
                offer
        ));
    }

    private static GuardServiceStatus serviceStatus(ServerPlayer viewer, GuardEntity guard) {
        UUID owner = GuardOwnership.getOwner(guard);
        if (owner == null) return GuardServiceStatus.UNOWNED;
        return owner.equals(viewer.getUUID())
                ? GuardServiceStatus.UNDER_YOUR_COMMAND
                : GuardServiceStatus.ALREADY_IN_SERVICE;
    }

    private static Optional<RecruitmentOffer> currentOffer(ServerPlayer viewer, GuardEntity guard) {
        RecruitmentQuoteResult result = RallyGuardApi.guardRecruitment().quote(viewer, guard);
        return result.quote().map(quote -> quote.offer());
    }
}
