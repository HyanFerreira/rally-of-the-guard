package net.hfstack.rallyguard.network;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.command.GuardCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandService;
import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardUnitCategory;
import net.hfstack.rallyguard.api.eligibility.GuardEligibility;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityContext;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityDecision;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityOperation;
import net.hfstack.rallyguard.api.presentation.GuardPresentation;
import net.hfstack.rallyguard.api.presentation.GuardPresentationContext;
import net.hfstack.rallyguard.api.presentation.GuardPresentationRegistry;
import net.hfstack.rallyguard.combat.GuardCombatState;
import net.hfstack.rallyguard.combat.GuardCombatRole;
import net.hfstack.rallyguard.combat.ProtectedAllyPolicy;
import net.hfstack.rallyguard.combat.ScopedAttackPolicy;
import net.hfstack.rallyguard.combat.stance.CombatStances;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardAttackTargetC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.GuardRouteUpdateC2SPayload;
import net.hfstack.rallyguard.network.payload.OpenGuardCommandC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardSquadAssignmentC2SPayload;
import net.hfstack.rallyguard.network.payload.ScopedGuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadDefinitionC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadOfficerC2SPayload;
import net.hfstack.rallyguard.network.payload.NamedRouteAssignmentC2SPayload;
import net.hfstack.rallyguard.network.payload.NamedRouteMutationC2SPayload;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRouteState;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.order.move.MoveOrders;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.hfstack.rallyguard.route.DefaultNamedRouteService;
import net.hfstack.rallyguard.service.GuardBatchCommandExecutor;
import net.hfstack.rallyguard.service.GuardCommandTargets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GuardCommandNetworking {
    private GuardCommandNetworking() {
    }

    private static boolean REGISTERED = false;
    private static final GuardCommandService COMMANDS = RallyGuardApi.guardCommands();
    private static final DefaultNamedRouteService NAMED_ROUTES = DefaultNamedRouteService.instance();

    public static synchronized void registerServer() {
        if (REGISTERED) return;
        REGISTERED = true;

        ServerPlayNetworking.registerGlobalReceiver(OpenGuardCommandC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> sendGuardList(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(GuardActionC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            int entityId = payload.entityId();
            int action = payload.action();
            context.server().execute(() -> handleAction(player, entityId, action));
        });

        ServerPlayNetworking.registerGlobalReceiver(GuardRouteUpdateC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleRouteUpdate(player, payload));
        });

        ServerPlayNetworking.registerGlobalReceiver(GuardAttackTargetC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleAttackTarget(player, payload));
        });

        ServerPlayNetworking.registerGlobalReceiver(SquadDefinitionC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleSquadDefinition(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(GuardSquadAssignmentC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleSquadAssignment(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(SquadOfficerC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleSquadOfficer(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(ScopedGuardActionC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleScopedAction(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(NamedRouteMutationC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleNamedRouteMutation(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(NamedRouteAssignmentC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleNamedRouteAssignment(player, payload));
        });
    }

    private static void sendGuardList(ServerPlayer player) {
        ServerPlayNetworking.send(player, GuardLedgerSnapshotFactory.create(player));
    }

    private static void handleSquadDefinition(ServerPlayer player, SquadDefinitionC2SPayload payload) {
        var result = switch (payload.action()) {
            case NetworkConstants.SQUAD_CREATE -> RallyGuardApi.squads().create(player, payload.name());
            case NetworkConstants.SQUAD_RENAME -> payload.squadId()
                    .map(id -> RallyGuardApi.squads().rename(player, id, payload.name()))
                    .orElse(null);
            case NetworkConstants.SQUAD_DELETE -> payload.squadId()
                    .map(id -> RallyGuardApi.squads().delete(player, id))
                    .orElse(null);
            default -> null;
        };
        if (result == null) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.squad.invalid_request"));
            return;
        }
        player.sendOverlayMessage(result.feedback());
        if (result.succeeded()) sendGuardList(player);
    }

    private static void handleSquadAssignment(ServerPlayer player, GuardSquadAssignmentC2SPayload payload) {
        Entity entity = player.level().getEntity(payload.entityId());
        if (!(entity instanceof GuardEntity guard)) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.command.not_found"));
            return;
        }
        var result = RallyGuardApi.squads().assign(player, guard, payload.squadId());
        player.sendOverlayMessage(result.feedback());
        if (result.succeeded()) sendGuardList(player);
    }

    private static void handleSquadOfficer(ServerPlayer player, SquadOfficerC2SPayload payload) {
        Optional<GuardEntity> officer = Optional.empty();
        if (!payload.clearsOfficer()) {
            Entity entity = player.level().getEntity(payload.officerEntityId());
            if (!(entity instanceof GuardEntity guard)) {
                player.sendOverlayMessage(Component.translatable("gui.rallyguard.command.not_found"));
                return;
            }
            officer = Optional.of(guard);
        }
        var result = RallyGuardApi.squads().setOfficer(player, payload.squadId(), officer);
        player.sendOverlayMessage(result.feedback());
        if (result.succeeded()) sendGuardList(player);
    }

    private static void handleScopedAction(ServerPlayer player, ScopedGuardActionC2SPayload payload) {
        if (!RallyConfig.squadsEnabled()) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.squad.disabled"));
            return;
        }
        if (!GuardCommandTargets.validScope(player, payload.scope())) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.squad.not_found"));
            return;
        }
        List<GuardEntity> guards = GuardCommandTargets.loadedScopedGuards(player, payload.scope());
        GuardBatchCommandResult result = GuardBatchCommandExecutor.executeCandidates(guards, guard -> switch (payload.action()) {
            case NetworkConstants.ACTION_SUMMON -> COMMANDS.summon(player, guard);
            case NetworkConstants.ACTION_FOLLOW -> COMMANDS.follow(player, guard);
            case NetworkConstants.ACTION_WAIT -> COMMANDS.wait(player, guard);
            case NetworkConstants.ACTION_TOGGLE_PATROL -> COMMANDS.patrol(player, guard, player.blockPosition());
            default -> GuardCommandResult.invalidGuard();
        });
        player.sendOverlayMessage(Component.translatable(
                "gui.rallyguard.squad.batch_result", result.succeeded(), result.rejected(), result.unavailable()
        ));
        sendGuardList(player);
    }

    private static void handleAction(ServerPlayer player, int entityId, int action) {
        ServerLevel world = player.level();
        Entity e = world.getEntity(entityId);

        if (!(e instanceof GuardEntity guard)) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.command.not_found"));
            return;
        }
        GuardCommandResult result = switch (action) {
            case NetworkConstants.ACTION_SUMMON -> COMMANDS.summon(player, guard);
            case NetworkConstants.ACTION_FOLLOW -> COMMANDS.follow(player, guard);
            case NetworkConstants.ACTION_WAIT -> COMMANDS.wait(player, guard);
            case NetworkConstants.ACTION_TOGGLE_PATROL -> guard.isPatrolling()
                    ? COMMANDS.stopPatrol(player, guard)
                    : COMMANDS.patrol(player, guard, player.blockPosition());
            case NetworkConstants.ACTION_ROUTE_PLACEHOLDER -> {
                player.sendOverlayMessage(Component.translatable("gui.rallyguard.command.route_soon"));
                yield null;
            }
            default -> null;
        };

        if (result != null) {
            player.sendOverlayMessage(result.feedback());
            sendGuardList(player);
        }
    }

    private static void handleRouteUpdate(ServerPlayer player, GuardRouteUpdateC2SPayload payload) {
        ServerLevel world = player.level();
        Entity e = world.getEntity(payload.entityId());

        if (!(e instanceof GuardEntity guard)) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.command.not_found"));
            return;
        }
        if (!GuardOwnership.isOwnedBy(guard, player.getUUID())) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.command.not_owner"));
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
                player.sendOverlayMessage(Component.translatable("gui.rallyguard.route.saved"));
            }
            case NetworkConstants.ROUTE_START -> {
                if (points.size() < 2) {
                    player.sendOverlayMessage(Component.translatable("gui.rallyguard.route.need_points"));
                    return;
                }
                GuardRouteState route = new GuardRouteState(true, 0, waitTicks, 0, points);
                MoveOrders.clear(guard);
                DefendAreas.clear(guard);
                GuardRoutes.applyToGuard(guard, route);
                GuardCombatState.disengage(guard);
                guard.setPatrolPos(route.currentPoint());
                guard.setPatrolling(true);
                player.sendOverlayMessage(Component.translatable("gui.rallyguard.route.started"));
            }
            case NetworkConstants.ROUTE_PAUSE -> {
                GuardRouteState current = GuardRoutes.get(guard);
                GuardRoutes.set(guard, new GuardRouteState(false, current.currentIndex(), waitTicks, 0, points));
                guard.setPatrolling(false);
                guard.setFollowing(false);
                guard.setPatrolPos(null);
                GuardOrders.setWaiting(guard, true);
                GuardCombatState.disengage(guard);
                player.sendOverlayMessage(Component.translatable("gui.rallyguard.route.paused"));
            }
            case NetworkConstants.ROUTE_CLEAR -> {
                GuardRoutes.clear(guard);
                guard.setPatrolling(false);
                guard.setFollowing(false);
                guard.setPatrolPos(null);
                GuardOrders.setWaiting(guard, true);
                GuardCombatState.disengage(guard);
                player.sendOverlayMessage(Component.translatable("gui.rallyguard.route.cleared"));
            }
            default -> {
            }
        }
    }

    private static void handleNamedRouteMutation(ServerPlayer player, NamedRouteMutationC2SPayload payload) {
        List<BlockPos> points = payload.points().stream()
                .map(point -> new BlockPos(point.x(), point.y(), point.z()))
                .toList();
        var result = switch (payload.action()) {
            case NetworkConstants.NAMED_ROUTE_CREATE -> NAMED_ROUTES.create(
                    player, payload.name(), payload.waitSeconds(), points
            );
            case NetworkConstants.NAMED_ROUTE_UPDATE -> payload.routeId()
                    .map(id -> NAMED_ROUTES.update(player, id, payload.name(), payload.waitSeconds(), points))
                    .orElse(null);
            case NetworkConstants.NAMED_ROUTE_DELETE -> payload.routeId()
                    .map(id -> NAMED_ROUTES.delete(player, id)).orElse(null);
            default -> null;
        };
        if (result == null) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.named_route.invalid_request"));
            return;
        }
        player.sendOverlayMessage(result.feedback());
        if (result.succeeded()) sendGuardList(player);
    }

    private static void handleNamedRouteAssignment(ServerPlayer player, NamedRouteAssignmentC2SPayload payload) {
        Entity entity = player.level().getEntity(payload.entityId());
        if (!(entity instanceof GuardEntity guard)) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.command.not_found"));
            return;
        }
        var result = NAMED_ROUTES.assign(player, guard, payload.routeId());
        player.sendOverlayMessage(result.feedback());
        if (result.succeeded()) sendGuardList(player);
    }

    private static void handleAttackTarget(ServerPlayer player, GuardAttackTargetC2SPayload payload) {
        ServerLevel world = player.level();
        double attackTargetRange = RallyConfig.combatTargetRange();
        Entity targetEntity = payload.targetEntityId() >= 0
                ? world.getEntity(payload.targetEntityId())
                : findLookedTarget(player, attackTargetRange);

        if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.combat.no_target"));
            return;
        }
        if (ProtectedAllyPolicy.isProtectedAlly(player.getUUID(), target)) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.combat.invalid_target"));
            return;
        }
        if (target instanceof Player && !RallyConfig.combatAllowPlayerTargets()) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.combat.invalid_target"));
            return;
        }
        if (!(target instanceof Monster) && !(target instanceof Player) && !RallyConfig.combatAllowPassiveTargets()) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.combat.invalid_target"));
            return;
        }
        if (target.distanceToSqr(player) > attackTargetRange * attackTargetRange) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.combat.target_too_far"));
            return;
        }

        if (!GuardCommandTargets.validScope(player, payload.scope())) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.squad.not_found"));
            return;
        }
        Identifier guardTypeId = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
        List<? extends Entity> guards = world.getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(guardTypeId),
                e -> e instanceof GuardEntity guard
                        && GuardOwnership.isOwnedBy(e, player.getUUID())
                        && (guard.isFollowing() || GuardOrders.isRallied(guard))
                        && !guard.isPatrolling()
                        && !GuardOrders.isWaiting(guard)
                        && !GuardRoutes.get(guard).active()
                        && ScopedAttackPolicy.mayReceiveOrder(
                                payload.scope(),
                                RallyGuardApi.squads().find(player, guard).squad().map(value -> value.id()),
                                true,
                                (guard.isFollowing() || GuardOrders.isRallied(guard))
                                        && !guard.isPatrolling()
                                        && !GuardOrders.isWaiting(guard)
                                        && !GuardRoutes.get(guard).active(),
                                e.distanceToSqr(player) <= RallyConfig.combatGuardSearchRadius()
                                        * RallyConfig.combatGuardSearchRadius(),
                                CombatStances.current(guard)
                        )
        );

        int ordered = 0;
        for (Entity e : guards) {
            if (!(e instanceof GuardEntity guard)) continue;
            if (!matchesAttackMode(guard, payload.mode())) continue;

            guard.setTarget(target);
            guard.setAggressive(true);
            if (!GuardCombatRole.isRanged(guard)) {
                guard.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.2);
            }
            ordered++;
        }

        if (ordered == 0) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.combat.no_guards"));
        } else {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.combat.attack_ordered", ordered));
        }
    }

    private static boolean matchesAttackMode(GuardEntity guard, int mode) {
        GuardUnitCategory category = GuardUnitCategory.fromWireId(mode).orElse(GuardUnitCategory.ALL);
        return GuardCombatRole.matches(category, guard);
    }

    private static Entity findLookedTarget(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0F);
        Vec3 end = start.add(direction.scale(range));
        AABB searchBox = player.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0);

        Entity best = null;
        double bestDistance = range * range;

        List<Entity> candidates = player.level().getEntities(
                player,
                searchBox,
                entity -> entity instanceof LivingEntity living && living.isAlive()
        );

        for (Entity candidate : candidates) {
            AABB box = candidate.getBoundingBox().inflate(candidate.getPickRadius());
            Optional<Vec3> hit = box.clip(start, end);
            if (hit.isEmpty()) continue;

            double distance = start.distanceToSqr(hit.get());
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }

        return best;
    }
}
