package net.hfstack.rallyguard.service;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.squad.GuardScopeMatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class GuardCommandTargets {
    private static final Identifier GUARD_TYPE = Identifier.fromNamespaceAndPath("guardvillagers", "guard");

    private GuardCommandTargets() {
    }

    public static boolean validScope(ServerPlayer player, GuardCommandScope scope) {
        if (!RallyConfig.squadsEnabled()) return scope.equals(GuardCommandScope.all());
        return scope.squadId().isEmpty() || RallyGuardApi.squads().list(player).stream()
                .anyMatch(squad -> squad.id().equals(scope.squadId().orElseThrow()));
    }

    public static List<GuardEntity> loadedScopedGuards(ServerPlayer player, GuardCommandScope scope) {
        return player.level().getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_TYPE),
                entity -> entity instanceof GuardEntity guard
                        && GuardOwnership.isOwnedBy(guard, player.getUUID())
                        && GuardScopeMatcher.matches(
                                scope,
                                RallyGuardApi.squads().find(player, guard).squad().map(value -> value.id())
                        )
        ).stream().filter(GuardEntity.class::isInstance).map(GuardEntity.class::cast).toList();
    }
}
