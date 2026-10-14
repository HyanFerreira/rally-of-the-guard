package net.hfstack.rallyguard.api.squad;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SquadService {
    List<SquadView> list(ServerPlayer owner);

    GuardSquadView find(ServerPlayer owner, GuardEntity guard);

    SquadMutationResult create(ServerPlayer owner, String name);

    SquadMutationResult rename(ServerPlayer owner, UUID squadId, String name);

    SquadMutationResult delete(ServerPlayer owner, UUID squadId);

    SquadMutationResult assign(ServerPlayer owner, GuardEntity guard, Optional<UUID> squadId);

    SquadMutationResult setOfficer(ServerPlayer owner, UUID squadId, Optional<GuardEntity> officer);
}
