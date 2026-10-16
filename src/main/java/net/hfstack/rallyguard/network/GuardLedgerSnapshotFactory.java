package net.hfstack.rallyguard.network;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.eligibility.GuardEligibility;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityContext;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityDecision;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityOperation;
import net.hfstack.rallyguard.api.presentation.GuardPresentation;
import net.hfstack.rallyguard.api.presentation.GuardPresentationContext;
import net.hfstack.rallyguard.api.presentation.GuardPresentationRegistry;
import net.hfstack.rallyguard.api.profile.GuardProfileView;
import net.hfstack.rallyguard.api.squad.GuardSquadView;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRouteState;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.route.NamedRouteRegistry;
import net.hfstack.rallyguard.route.NamedRouteRegistryReadResult;
import net.hfstack.rallyguard.route.NamedRouteRegistryRepository;
import net.hfstack.rallyguard.route.DefaultNamedRouteService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public final class GuardLedgerSnapshotFactory {
    private static final Identifier GUARD_TYPE = Identifier.fromNamespaceAndPath("guardvillagers", "guard");

    private GuardLedgerSnapshotFactory() {
    }

    public static GuardListS2CPayload create(ServerPlayer player) {
        List<GuardListS2CPayload.SquadEntry> squads = RallyGuardApi.squads().list(player).stream()
                .map(value -> new GuardListS2CPayload.SquadEntry(
                        value.id(), value.name(), value.officerGuardId()
                )).toList();
        NamedRouteRegistryReadResult routeResult = NamedRouteRegistryRepository.instance().readAndRepair(player);
        NamedRouteRegistry routeRegistry = routeResult instanceof NamedRouteRegistryReadResult.Available available
                ? available.registry() : NamedRouteRegistry.empty();
        List<GuardListS2CPayload.NamedRouteEntry> namedRoutes = routeRegistry.routes().stream()
                .map(route -> new GuardListS2CPayload.NamedRouteEntry(
                        route.id(), route.name(), route.dimensionId(), route.waitSeconds(),
                        route.points().stream()
                                .map(point -> new GuardListS2CPayload.Point(point.getX(), point.getY(), point.getZ()))
                                .toList()
                )).toList();
        List<? extends Entity> loaded = player.level().getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_TYPE),
                entity -> GuardOwnership.isOwnedBy(entity, player.getUUID())
        );
        List<GuardListS2CPayload.Entry> guards = new ArrayList<>(loaded.size());
        for (Entity entity : loaded) {
            if (!(entity instanceof GuardEntity guard)) continue;
            GuardEligibilityDecision visibility = GuardEligibility.evaluate(
                    GuardEligibilityContext.of(player, guard, GuardEligibilityOperation.SHOW_IN_COMMAND_LIST)
            );
            if (visibility instanceof GuardEligibilityDecision.Deny) continue;
            DefaultNamedRouteService.instance().repairMissingAssignment(guard, routeRegistry);
            GuardRouteState route = GuardRoutes.get(guard);
            GuardPresentation presentation = GuardPresentationRegistry.resolve(
                    new GuardPresentationContext(player, guard, player.level())
            );
            GuardSquadView squad = RallyGuardApi.squads().find(player, guard);
            boolean captain = RallyGuardApi.profiles().find(guard)
                    .map(GuardProfileView::captain)
                    .orElse(false);
            guards.add(new GuardListS2CPayload.Entry(
                    guard.getId(),
                    guard.getUUID(),
                    guard.getName().getString(),
                    guard.isPatrolling(),
                    GuardOrders.statusOf(guard),
                    route.active(),
                    Math.max(0, route.waitTicks() / 20),
                    route.points().stream()
                            .map(point -> new GuardListS2CPayload.Point(point.getX(), point.getY(), point.getZ()))
                            .toList(),
                    presentation.rank(),
                    presentation.settlement(),
                    squad.squad().map(value -> value.id()),
                    route.namedRouteId(),
                    captain
            ));
        }
        return new GuardListS2CPayload(RallyConfig.squadsEnabled(), squads, namedRoutes, guards);
    }
}
