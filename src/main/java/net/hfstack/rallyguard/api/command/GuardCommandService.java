package net.hfstack.rallyguard.api.command;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;

/**
 * Server-side tactical operations shared by Rally's networking layer and other mods.
 */
public interface GuardCommandService {
    GuardCommandResult summon(ServerPlayer commander, GuardEntity guard);

    GuardCommandResult follow(ServerPlayer commander, GuardEntity guard);

    GuardCommandResult wait(ServerPlayer commander, GuardEntity guard);

    GuardCommandResult patrol(ServerPlayer commander, GuardEntity guard, BlockPos position);

    GuardCommandResult stopPatrol(ServerPlayer commander, GuardEntity guard);
}
