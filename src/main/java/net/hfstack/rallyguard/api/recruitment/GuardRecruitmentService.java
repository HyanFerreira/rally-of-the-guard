package net.hfstack.rallyguard.api.recruitment;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.level.ServerPlayer;

public interface GuardRecruitmentService {
    RecruitmentQuoteResult quote(ServerPlayer player, GuardEntity guard);

    RecruitmentResult recruit(ServerPlayer player, GuardEntity guard, RecruitmentQuote expectedQuote);

    RecruitmentResult recruit(ServerPlayer player, GuardEntity guard);
}
