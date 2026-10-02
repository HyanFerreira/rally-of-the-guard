package net.hfstack.rallyguard.api.recruitment;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.level.ServerPlayer;

public interface GuardRecruitmentService {
    RecruitmentResult recruit(ServerPlayer player, GuardEntity guard);
}
