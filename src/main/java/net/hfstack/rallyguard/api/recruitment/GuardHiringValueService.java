package net.hfstack.rallyguard.api.recruitment;

import dev.sterner.guardvillagers.common.entity.GuardEntity;

public interface GuardHiringValueService {
    RecruitmentOffer calculate(GuardEntity guard);
}
