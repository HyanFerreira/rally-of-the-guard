package net.hfstack.rallyguard.api.recruitment;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface GuardRecruitmentService {
    RecruitmentResult recruit(ServerPlayerEntity player, GuardEntity guard);
}
