package net.hfstack.rallyguard.service;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.profile.GuardProfileView;
import net.hfstack.rallyguard.api.recruitment.GuardHiringValueEvents;
import net.hfstack.rallyguard.api.recruitment.GuardHiringValueService;
import net.hfstack.rallyguard.api.recruitment.HiringValueCalculatedContext;
import net.hfstack.rallyguard.api.recruitment.RecruitmentOffer;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.hfstack.rallyguard.profile.GuardProfileRepository;
import net.hfstack.rallyguard.recruitment.GuardHiringValueModel;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Objects;
import java.util.Optional;

public final class DefaultGuardHiringValueService implements GuardHiringValueService {
    @Override
    public RecruitmentOffer calculate(GuardEntity guard) {
        Objects.requireNonNull(guard, "guard");
        Optional<GuardProfileView> profile = profile(guard);
        boolean dynamic = RallyConfig.dynamicHireCostEnabled() && profile.isPresent();
        int cost = dynamic
                ? GuardHiringValueModel.cost(
                        profile.orElseThrow(),
                        RallyConfig.dynamicHireCostMultiplier(),
                        RallyConfig.captainHireCostBonus(),
                        RallyConfig.dynamicHireCostMinimum(),
                        RallyConfig.dynamicHireCostMaximum()
                )
                : RallyConfig.hireCost();
        RecruitmentOffer offer = new RecruitmentOffer(
                BuiltInRegistries.ITEM.getKey(RallyConfig.hireItem()),
                cost
        );
        GuardHiringValueEvents.CALCULATED.invoker().onCalculated(
                new HiringValueCalculatedContext(guard, profile, dynamic, offer)
        );
        return offer;
    }

    private static Optional<GuardProfileView> profile(GuardEntity guard) {
        GuardProfileReadResult result = GuardProfileRepository.instance().read(guard);
        return result instanceof GuardProfileReadResult.Available available
                ? Optional.of(available.profile())
                : Optional.empty();
    }
}
