package net.hfstack.rallyguard.network;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.OpenGuardCommandC2SPayload;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class GuardCommandNetworking {
    private GuardCommandNetworking() {
    }

    private static boolean REGISTERED = false;

    public static synchronized void registerServer() {
        if (REGISTERED) return;
        REGISTERED = true;

        ServerPlayNetworking.registerGlobalReceiver(OpenGuardCommandC2SPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> sendGuardList(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(GuardActionC2SPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            int entityId = payload.entityId();
            int action = payload.action();
            context.server().execute(() -> handleAction(player, entityId, action));
        });
    }

    private static void sendGuardList(ServerPlayerEntity player) {
        ServerWorld world = player.getEntityWorld();
        Identifier guardTypeId = Identifier.of("guardvillagers", "guard");

        List<? extends Entity> guards = world.getEntitiesByType(
                Registries.ENTITY_TYPE.get(guardTypeId),
                e -> GuardOwnership.isOwnedBy(e, player.getUuid())
        );

        List<GuardListS2CPayload.Entry> list = new ArrayList<>(guards.size());
        for (Entity g : guards) {
            boolean patrolling = g instanceof GuardEntity gv && gv.isPatrolling();
            list.add(new GuardListS2CPayload.Entry(g.getId(), g.getName().getString(), patrolling));
        }

        ServerPlayNetworking.send(player, new GuardListS2CPayload(list));
    }

    private static void handleAction(ServerPlayerEntity player, int entityId, int action) {
        ServerWorld world = player.getEntityWorld();
        Entity e = world.getEntityById(entityId);

        if (!(e instanceof GuardEntity guard)) {
            player.sendMessage(Text.translatable("gui.rallyguard.command.not_found"), true);
            return;
        }
        if (!GuardOwnership.isOwnedBy(guard, player.getUuid())) {
            player.sendMessage(Text.translatable("gui.rallyguard.command.not_owner"), true);
            return;
        }

        switch (action) {
            case NetworkConstants.ACTION_SUMMON -> {
                double ox = (player.getRandom().nextDouble() - 0.5) * 2.5;
                double oz = (player.getRandom().nextDouble() - 0.5) * 2.5;
                guard.refreshPositionAndAngles(player.getX() + ox, player.getY(), player.getZ() + oz,
                        guard.getYaw(), guard.getPitch());
                guard.setFollowing(false);
                stopGuardActions(guard);
                player.sendMessage(Text.translatable("gui.rallyguard.command.summoned"), true);
            }
            case NetworkConstants.ACTION_TOGGLE_PATROL -> {
                if (guard.isPatrolling()) {
                    guard.setPatrolling(false);
                    guard.setFollowing(false);
                    guard.setPatrolPos(null);
                    stopGuardActions(guard);
                    player.sendMessage(Text.translatable("gui.rallyguard.command.patrol_off"), true);
                } else {
                    guard.setFollowing(false);
                    guard.setPatrolPos(player.getBlockPos());
                    guard.setPatrolling(true);
                    stopGuardActions(guard);
                    player.sendMessage(Text.translatable("gui.rallyguard.command.patrol_on"), true);
                }
            }
            default -> {
            }
        }
    }

    private static void stopGuardActions(GuardEntity guard) {
        guard.setTarget(null);
        guard.setAttacking(false);
        guard.getNavigation().stop();
    }
}
