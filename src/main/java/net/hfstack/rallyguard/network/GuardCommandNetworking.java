package net.hfstack.rallyguard.network;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.command.GuardCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandService;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardAttackTargetC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.GuardRouteUpdateC2SPayload;
import net.hfstack.rallyguard.network.payload.OpenGuardCommandC2SPayload;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRouteState;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GuardCommandNetworking {
    private GuardCommandNetworking() {
    }

    private static boolean REGISTERED = false;
    private static final GuardCommandService COMMANDS = RallyGuardApi.guardCommands();

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

        ServerPlayNetworking.registerGlobalReceiver(GuardRouteUpdateC2SPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> handleRouteUpdate(player, payload));
        });

        ServerPlayNetworking.registerGlobalReceiver(GuardAttackTargetC2SPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> handleAttackTarget(player, payload));
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
            if (!(g instanceof GuardEntity guard)) continue;
            boolean patrolling = guard.isPatrolling();
            GuardRouteState route = GuardRoutes.get(guard);
            list.add(new GuardListS2CPayload.Entry(
                    g.getId(),
                    g.getName().getString(),
                    patrolling,
                    GuardOrders.statusOf(guard),
                    route.active(),
                    Math.max(0, route.waitTicks() / 20),
                    route.points().stream()
                            .map(p -> new GuardListS2CPayload.Point(p.getX(), p.getY(), p.getZ()))
                            .toList()
            ));
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
        GuardCommandResult result = switch (action) {
            case NetworkConstants.ACTION_SUMMON -> COMMANDS.summon(player, guard);
            case NetworkConstants.ACTION_FOLLOW -> COMMANDS.follow(player, guard);
            case NetworkConstants.ACTION_WAIT -> COMMANDS.wait(player, guard);
            case NetworkConstants.ACTION_TOGGLE_PATROL -> guard.isPatrolling()
                    ? COMMANDS.stopPatrol(player, guard)
                    : COMMANDS.patrol(player, guard, player.getBlockPos());
            case NetworkConstants.ACTION_ROUTE_PLACEHOLDER -> {
                player.sendMessage(Text.translatable("gui.rallyguard.command.route_soon"), true);
                yield null;
            }
            default -> null;
        };

        if (result != null) {
            player.sendMessage(result.feedback(), true);
        }
    }

    private static void stopGuardActions(GuardEntity guard) {
        guard.setTarget(null);
        guard.setAttacking(false);
        guard.getNavigation().stop();
    }

    private static void handleRouteUpdate(ServerPlayerEntity player, GuardRouteUpdateC2SPayload payload) {
        ServerWorld world = player.getEntityWorld();
        Entity e = world.getEntityById(payload.entityId());

        if (!(e instanceof GuardEntity guard)) {
            player.sendMessage(Text.translatable("gui.rallyguard.command.not_found"), true);
            return;
        }
        if (!GuardOwnership.isOwnedBy(guard, player.getUuid())) {
            player.sendMessage(Text.translatable("gui.rallyguard.command.not_owner"), true);
            return;
        }

        List<BlockPos> points = payload.points().stream()
                .limit(RallyConfig.routeMaxPoints())
                .map(p -> new BlockPos(p.x(), p.y(), p.z()))
                .toList();
        int waitTicks = Math.max(0, Math.min(600, payload.waitSeconds())) * 20;

        switch (payload.action()) {
            case NetworkConstants.ROUTE_SAVE -> {
                GuardRoutes.set(guard, new GuardRouteState(false, 0, waitTicks, 0, points));
                player.sendMessage(Text.translatable("gui.rallyguard.route.saved"), true);
            }
            case NetworkConstants.ROUTE_START -> {
                if (points.size() < 2) {
                    player.sendMessage(Text.translatable("gui.rallyguard.route.need_points"), true);
                    return;
                }
                GuardRouteState route = new GuardRouteState(true, 0, waitTicks, 0, points);
                GuardRoutes.applyToGuard(guard, route);
                stopGuardActions(guard);
                guard.setPatrolPos(route.currentPoint());
                guard.setPatrolling(true);
                player.sendMessage(Text.translatable("gui.rallyguard.route.started"), true);
            }
            case NetworkConstants.ROUTE_PAUSE -> {
                GuardRouteState current = GuardRoutes.get(guard);
                GuardRoutes.set(guard, new GuardRouteState(false, current.currentIndex(), waitTicks, 0, points));
                guard.setPatrolling(false);
                guard.setFollowing(false);
                guard.setPatrolPos(null);
                GuardOrders.setWaiting(guard, true);
                stopGuardActions(guard);
                player.sendMessage(Text.translatable("gui.rallyguard.route.paused"), true);
            }
            case NetworkConstants.ROUTE_CLEAR -> {
                GuardRoutes.clear(guard);
                guard.setPatrolling(false);
                guard.setFollowing(false);
                guard.setPatrolPos(null);
                GuardOrders.setWaiting(guard, true);
                stopGuardActions(guard);
                player.sendMessage(Text.translatable("gui.rallyguard.route.cleared"), true);
            }
            default -> {
            }
        }
    }

    private static void handleAttackTarget(ServerPlayerEntity player, GuardAttackTargetC2SPayload payload) {
        ServerWorld world = player.getEntityWorld();
        double attackTargetRange = RallyConfig.combatTargetRange();
        Entity targetEntity = payload.targetEntityId() >= 0
                ? world.getEntityById(payload.targetEntityId())
                : findLookedTarget(player, attackTargetRange);

        if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) {
            player.sendMessage(Text.translatable("gui.rallyguard.combat.no_target"), true);
            return;
        }
        if (target == player || GuardOwnership.isOwnedBy(target, player.getUuid())) {
            player.sendMessage(Text.translatable("gui.rallyguard.combat.invalid_target"), true);
            return;
        }
        if (target instanceof PlayerEntity && !RallyConfig.combatAllowPlayerTargets()) {
            player.sendMessage(Text.translatable("gui.rallyguard.combat.invalid_target"), true);
            return;
        }
        if (!(target instanceof HostileEntity) && !(target instanceof PlayerEntity) && !RallyConfig.combatAllowPassiveTargets()) {
            player.sendMessage(Text.translatable("gui.rallyguard.combat.invalid_target"), true);
            return;
        }
        if (target.squaredDistanceTo(player) > attackTargetRange * attackTargetRange) {
            player.sendMessage(Text.translatable("gui.rallyguard.combat.target_too_far"), true);
            return;
        }

        Identifier guardTypeId = Identifier.of("guardvillagers", "guard");
        List<? extends Entity> guards = world.getEntitiesByType(
                Registries.ENTITY_TYPE.get(guardTypeId),
                e -> e instanceof GuardEntity guard
                        && GuardOwnership.isOwnedBy(e, player.getUuid())
                        && (guard.isFollowing() || GuardOrders.isRallied(guard))
                        && !guard.isPatrolling()
                        && !GuardOrders.isWaiting(guard)
                        && !GuardRoutes.get(guard).active()
                        && e.squaredDistanceTo(player) <= RallyConfig.combatGuardSearchRadius() * RallyConfig.combatGuardSearchRadius()
        );

        int ordered = 0;
        for (Entity e : guards) {
            if (!(e instanceof GuardEntity guard)) continue;
            if (!matchesAttackMode(guard, payload.mode())) continue;

            guard.setTarget(target);
            guard.setAttacking(true);
            if (!isRangedGuard(guard)) {
                guard.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), 1.2);
            }
            ordered++;
        }

        if (ordered == 0) {
            player.sendMessage(Text.translatable("gui.rallyguard.combat.no_guards"), true);
        } else {
            player.sendMessage(Text.translatable("gui.rallyguard.combat.attack_ordered", ordered), true);
        }
    }

    private static boolean matchesAttackMode(GuardEntity guard, int mode) {
        boolean ranged = isRangedGuard(guard);
        return switch (mode) {
            case NetworkConstants.ATTACK_INFANTRY -> !ranged;
            case NetworkConstants.ATTACK_RANGED -> ranged;
            default -> true;
        };
    }

    private static boolean isRangedGuard(GuardEntity guard) {
        return isRangedWeapon(guard.getMainHandStack()) || isRangedWeapon(guard.getOffHandStack());
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        return stack.isOf(Items.BOW) || stack.isOf(Items.CROSSBOW);
    }

    private static Entity findLookedTarget(ServerPlayerEntity player, double range) {
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(range));
        Box searchBox = player.getBoundingBox().stretch(direction.multiply(range)).expand(1.0);

        Entity best = null;
        double bestDistance = range * range;

        List<Entity> candidates = player.getEntityWorld().getOtherEntities(
                player,
                searchBox,
                entity -> entity instanceof LivingEntity living && living.isAlive()
        );

        for (Entity candidate : candidates) {
            Box box = candidate.getBoundingBox().expand(candidate.getTargetingMargin());
            Optional<Vec3d> hit = box.raycast(start, end);
            if (hit.isEmpty()) continue;

            double distance = start.squaredDistanceTo(hit.get());
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }

        return best;
    }
}
