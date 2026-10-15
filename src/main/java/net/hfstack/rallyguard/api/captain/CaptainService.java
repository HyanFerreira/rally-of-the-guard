package net.hfstack.rallyguard.api.captain;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.level.ServerPlayer;

public interface CaptainService {
    CaptainActionView inspect(ServerPlayer owner, GuardEntity guard);

    CaptainMutationResult promote(ServerPlayer owner, GuardEntity guard);

    CaptainMutationResult demote(ServerPlayer owner, GuardEntity guard);
}
