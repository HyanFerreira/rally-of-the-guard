package net.hfstack.rallyguard.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.profile.GuardProfileView;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class RallyGuardCommands {
    private static final double NEARBY_RANGE = 8.0;
    private static final double MAX_XP_POINTS_PER_COMMAND = 1_000_000.0;

    private RallyGuardCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("rallyguard")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("xp")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(
                                                        0.001,
                                                        MAX_XP_POINTS_PER_COMMAND
                                                ))
                                                .executes(context -> addToNearby(
                                                        context,
                                                        DoubleArgumentType.getDouble(context, "amount")
                                                )))
                                        .then(Commands.argument("targets", EntityArgument.entities())
                                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(
                                                                0.001,
                                                                MAX_XP_POINTS_PER_COMMAND
                                                        ))
                                                        .executes(context -> addToTargets(
                                                                context,
                                                                EntityArgument.getEntities(context, "targets"),
                                                                DoubleArgumentType.getDouble(context, "amount")
                                                        )))))
                                .then(Commands.literal("get")
                                        .executes(RallyGuardCommands::queryNearby)
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(context -> queryTarget(
                                                        context,
                                                        EntityArgument.getEntity(context, "target")
                                                ))))
                        )
        ));
    }

    private static int addToNearby(CommandContext<CommandSourceStack> context, double xpPoints) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable("commands.rallyguard.guard.player_required"));
            return 0;
        }
        Optional<GuardEntity> guard = nearestGuard(player);
        if (guard.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.rallyguard.guard.nearby_not_found", (int) NEARBY_RANGE));
            return 0;
        }
        return addXp(context.getSource(), List.of(guard.orElseThrow()), xpPoints);
    }

    private static int addToTargets(
            CommandContext<CommandSourceStack> context,
            Collection<? extends Entity> targets,
            double xpPoints
    ) {
        List<GuardEntity> guards = targets.stream()
                .filter(GuardEntity.class::isInstance)
                .map(GuardEntity.class::cast)
                .filter(Entity::isAlive)
                .toList();
        if (guards.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.rallyguard.guard.targets_not_found"));
            return 0;
        }
        return addXp(context.getSource(), guards, xpPoints);
    }

    private static int addXp(CommandSourceStack source, Collection<GuardEntity> guards, double xpPoints) {
        long requestedXpMilli = toXpMilli(xpPoints);
        long awardedXpMilli = 0;
        int updated = 0;
        GuardEntity single = guards.size() == 1 ? guards.iterator().next() : null;

        for (GuardEntity guard : guards) {
            long awarded = RallyGuardApi.progression().awardXp(guard, requestedXpMilli);
            if (awarded > 0) {
                awardedXpMilli = saturatedAdd(awardedXpMilli, awarded);
                updated++;
            }
        }

        if (updated == 0) {
            source.sendFailure(Component.translatable("commands.rallyguard.xp.add.failed"));
            return 0;
        }

        String awarded = formatXp(awardedXpMilli);
        if (single != null) {
            Optional<GuardProfileView> profile = RallyGuardApi.profiles().find(single);
            int level = profile.map(GuardProfileView::level).orElse(1);
            source.sendSuccess(() -> Component.translatable(
                    "commands.rallyguard.xp.add.success.single",
                    awarded,
                    single.getDisplayName(),
                    level
            ), true);
        } else {
            int updatedCount = updated;
            source.sendSuccess(() -> Component.translatable(
                    "commands.rallyguard.xp.add.success.multiple",
                    awarded,
                    updatedCount
            ), true);
        }
        return updated;
    }

    private static int queryNearby(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable("commands.rallyguard.guard.player_required"));
            return 0;
        }
        Optional<GuardEntity> guard = nearestGuard(player);
        if (guard.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.rallyguard.guard.nearby_not_found", (int) NEARBY_RANGE));
            return 0;
        }
        return queryGuard(context.getSource(), guard.orElseThrow());
    }

    private static int queryTarget(CommandContext<CommandSourceStack> context, Entity target) {
        if (!(target instanceof GuardEntity guard) || !guard.isAlive()) {
            context.getSource().sendFailure(Component.translatable("commands.rallyguard.guard.targets_not_found"));
            return 0;
        }
        return queryGuard(context.getSource(), guard);
    }

    private static int queryGuard(CommandSourceStack source, GuardEntity guard) {
        Optional<GuardProfileView> result = RallyGuardApi.profiles().find(guard);
        if (result.isEmpty()) {
            source.sendFailure(Component.translatable("commands.rallyguard.profile.unavailable", guard.getDisplayName()));
            return 0;
        }

        GuardProfileView profile = result.orElseThrow();
        String progress = profile.level() >= 10
                ? formatXp(profile.totalXpMilli())
                : formatXp(profile.xpIntoLevelMilli()) + " / " + formatXp(profile.xpForNextLevelMilli());
        source.sendSuccess(() -> Component.translatable(
                "commands.rallyguard.xp.query.success",
                guard.getDisplayName(),
                profile.level(),
                progress,
                formatXp(profile.totalXpMilli())
        ), false);
        return profile.level();
    }

    private static Optional<GuardEntity> nearestGuard(ServerPlayer player) {
        double maximumDistanceSquared = NEARBY_RANGE * NEARBY_RANGE;
        return player.level().getEntitiesOfClass(
                        GuardEntity.class,
                        player.getBoundingBox().inflate(NEARBY_RANGE),
                        guard -> guard.isAlive() && guard.distanceToSqr(player) <= maximumDistanceSquared
                ).stream()
                .min(Comparator.comparingDouble(guard -> guard.distanceToSqr(player)));
    }

    static long toXpMilli(double xpPoints) {
        return BigDecimal.valueOf(xpPoints)
                .movePointRight(3)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    static String formatXp(long xpMilli) {
        return BigDecimal.valueOf(Math.max(0, xpMilli), 3)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static long saturatedAdd(long left, long right) {
        return right > 0 && left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }
}
