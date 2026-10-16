package net.hfstack.rallyguard.route;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.combat.GuardCombatState;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRouteState;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.hfstack.rallyguard.order.move.MoveOrders;
import net.hfstack.rallyguard.service.GuardCommandTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DefaultNamedRouteService {
    private static final DefaultNamedRouteService INSTANCE = new DefaultNamedRouteService();
    private final NamedRouteRegistryRepository registries = NamedRouteRegistryRepository.instance();

    private DefaultNamedRouteService() {
    }

    public static DefaultNamedRouteService instance() {
        return INSTANCE;
    }

    public NamedRouteMutationResult create(ServerPlayer owner, String name, int waitSeconds, List<BlockPos> points) {
        Optional<NamedRouteRegistry> current = registry(owner);
        if (current.isEmpty()) return unsupported();
        NamedRouteRegistryEditor.EditResult edit = NamedRouteRegistryEditor.create(
                current.get(), UUID.randomUUID(), name, dimensionId(owner), waitSeconds, points,
                RallyConfig.maxNamedRoutesPerPlayer(), RallyConfig.maxNamedRouteNameLength(), RallyConfig.routeMaxPoints()
        );
        return persist(owner, edit, "gui.rallyguard.named_route.created");
    }

    public NamedRouteMutationResult update(ServerPlayer owner, UUID routeId, String name,
                                           int waitSeconds, List<BlockPos> points) {
        Optional<NamedRouteRegistry> current = registry(owner);
        if (current.isEmpty()) return unsupported();
        NamedRouteRegistryEditor.EditResult edit = NamedRouteRegistryEditor.update(
                current.get(), routeId, name, dimensionId(owner), waitSeconds, points,
                RallyConfig.maxNamedRouteNameLength(), RallyConfig.routeMaxPoints()
        );
        NamedRouteMutationResult result = persist(owner, edit, "gui.rallyguard.named_route.updated");
        if (result.succeeded() && edit instanceof NamedRouteRegistryEditor.Success success) {
            synchronizeLoadedAssignments(owner, success.affected());
        }
        return result;
    }

    public NamedRouteMutationResult delete(ServerPlayer owner, UUID routeId) {
        Optional<NamedRouteRegistry> current = registry(owner);
        if (current.isEmpty()) return unsupported();
        NamedRouteRegistryEditor.EditResult edit = NamedRouteRegistryEditor.delete(current.get(), routeId);
        if (!(edit instanceof NamedRouteRegistryEditor.Success success)) return editorFailure(edit);
        if (!registries.save(owner, success.registry())) return unsupported();
        for (GuardEntity guard : loadedOwnedGuards(owner)) {
            GuardRouteState state = GuardRoutes.get(guard);
            if (state.namedRouteId().filter(routeId::equals).isPresent()) unlinkAndPause(guard, state);
        }
        return NamedRouteMutationResult.success("gui.rallyguard.named_route.deleted", routeId);
    }

    public NamedRouteMutationResult assign(ServerPlayer owner, GuardEntity guard, Optional<UUID> requestedRoute) {
        if (guard == null || !guard.isAlive() || guard.level() != owner.level()) {
            return rejected(NamedRouteMutationResult.Outcome.NOT_AVAILABLE,
                    "gui.rallyguard.named_route.guard_unavailable");
        }
        if (!GuardOwnership.isOwnedBy(guard, owner.getUUID())) {
            return rejected(NamedRouteMutationResult.Outcome.NOT_OWNER, "gui.rallyguard.command.not_owner");
        }
        if (requestedRoute == null || requestedRoute.isEmpty()) {
            GuardRouteState current = GuardRoutes.get(guard);
            unlinkAndPause(guard, current);
            return NamedRouteMutationResult.success("gui.rallyguard.named_route.unassigned", null);
        }
        Optional<NamedRouteRegistry> registry = registry(owner);
        if (registry.isEmpty()) return unsupported();
        NamedRouteDefinition route = registry.get().routes().stream()
                .filter(value -> value.id().equals(requestedRoute.get())).findFirst().orElse(null);
        if (route == null) {
            return rejected(NamedRouteMutationResult.Outcome.NOT_FOUND, "gui.rallyguard.named_route.not_found");
        }
        if (!route.dimensionId().equals(dimensionId(owner))) {
            return rejected(NamedRouteMutationResult.Outcome.WRONG_DIMENSION,
                    "gui.rallyguard.named_route.wrong_dimension");
        }
        GuardRouteState state = new GuardRouteState(
                true, 0, route.waitSeconds() * 20, 0, route.points(), Optional.of(route.id())
        );
        MoveOrders.clear(guard);
        DefendAreas.clear(guard);
        GuardRoutes.applyToGuard(guard, state);
        GuardCombatState.disengage(guard);
        return NamedRouteMutationResult.success("gui.rallyguard.named_route.assigned", route.id());
    }

    public void repairMissingAssignment(GuardEntity guard, NamedRouteRegistry registry) {
        GuardRouteState current = GuardRoutes.get(guard);
        if (current.namedRouteId().isEmpty()) return;
        boolean exists = registry.routes().stream()
                .anyMatch(route -> route.id().equals(current.namedRouteId().orElseThrow()));
        if (!exists) unlinkAndPause(guard, current);
    }

    private NamedRouteMutationResult persist(ServerPlayer owner, NamedRouteRegistryEditor.EditResult edit,
                                             String successKey) {
        if (!(edit instanceof NamedRouteRegistryEditor.Success success)) return editorFailure(edit);
        if (!registries.save(owner, success.registry())) return unsupported();
        return NamedRouteMutationResult.success(successKey, success.affected().id());
    }

    private void synchronizeLoadedAssignments(ServerPlayer owner, NamedRouteDefinition definition) {
        for (GuardEntity guard : loadedOwnedGuards(owner)) {
            GuardRouteState current = GuardRoutes.get(guard);
            if (current.namedRouteId().filter(definition.id()::equals).isEmpty()) continue;
            GuardRouteState updated = new GuardRouteState(
                    current.active(), 0, definition.waitSeconds() * 20, 0, definition.points(),
                    Optional.of(definition.id())
            );
            if (updated.active()) GuardRoutes.applyToGuard(guard, updated);
            else GuardRoutes.set(guard, updated);
        }
    }

    private static void unlinkAndPause(GuardEntity guard, GuardRouteState current) {
        if (current.points().isEmpty()) {
            GuardRoutes.clear(guard);
        } else {
            GuardRoutes.set(guard, new GuardRouteState(
                    false, current.currentIndex(), current.waitTicks(), 0, current.points(), Optional.empty()
            ));
        }
        guard.setPatrolling(false);
        guard.setFollowing(false);
        guard.setPatrolPos(null);
        GuardOrders.setWaiting(guard, true);
        GuardCombatState.disengage(guard);
    }

    private Optional<NamedRouteRegistry> registry(ServerPlayer owner) {
        NamedRouteRegistryReadResult result = registries.readAndRepair(owner);
        return result instanceof NamedRouteRegistryReadResult.Available available
                ? Optional.of(available.registry()) : Optional.empty();
    }

    private List<GuardEntity> loadedOwnedGuards(ServerPlayer owner) {
        return GuardCommandTargets.loadedScopedGuards(owner, net.hfstack.rallyguard.api.command.GuardCommandScope.all());
    }

    private static String dimensionId(ServerPlayer player) {
        return player.level().dimension().identifier().toString();
    }

    private static NamedRouteMutationResult editorFailure(NamedRouteRegistryEditor.EditResult edit) {
        NamedRouteRegistryEditor.FailureReason reason = ((NamedRouteRegistryEditor.Failure) edit).reason();
        return switch (reason) {
            case INVALID_NAME -> rejected(NamedRouteMutationResult.Outcome.INVALID_NAME,
                    "gui.rallyguard.named_route.invalid_name");
            case DUPLICATE_NAME -> rejected(NamedRouteMutationResult.Outcome.DUPLICATE_NAME,
                    "gui.rallyguard.named_route.duplicate_name");
            case LIMIT_REACHED -> rejected(NamedRouteMutationResult.Outcome.LIMIT_REACHED,
                    "gui.rallyguard.named_route.limit");
            case NOT_FOUND -> rejected(NamedRouteMutationResult.Outcome.NOT_FOUND,
                    "gui.rallyguard.named_route.not_found");
            case INVALID_ROUTE -> rejected(NamedRouteMutationResult.Outcome.INVALID_ROUTE,
                    "gui.rallyguard.named_route.invalid");
            case DUPLICATE_ID -> rejected(NamedRouteMutationResult.Outcome.NOT_AVAILABLE,
                    "gui.rallyguard.named_route.unavailable");
        };
    }

    private static NamedRouteMutationResult unsupported() {
        return rejected(NamedRouteMutationResult.Outcome.UNSUPPORTED_DATA,
                "gui.rallyguard.named_route.unsupported_data");
    }

    private static NamedRouteMutationResult rejected(NamedRouteMutationResult.Outcome outcome, String key) {
        return NamedRouteMutationResult.rejected(outcome, key);
    }
}
