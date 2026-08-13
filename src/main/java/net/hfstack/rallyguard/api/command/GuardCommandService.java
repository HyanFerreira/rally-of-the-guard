package net.hfstack.rallyguard.api.command;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Server-side tactical operations shared by Rally's networking layer and other mods.
 */
public interface GuardCommandService {
    GuardCommandResult summon(ServerPlayerEntity commander, GuardEntity guard);

    GuardCommandResult follow(ServerPlayerEntity commander, GuardEntity guard);

    GuardCommandResult wait(ServerPlayerEntity commander, GuardEntity guard);

    GuardCommandResult patrol(ServerPlayerEntity commander, GuardEntity guard, BlockPos position);

    GuardCommandResult stopPatrol(ServerPlayerEntity commander, GuardEntity guard);
}
