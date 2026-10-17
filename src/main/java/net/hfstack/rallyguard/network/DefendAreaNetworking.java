package net.hfstack.rallyguard.network;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.combat.GuardCombatRole;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.order.move.MoveDestinationPolicy;
import net.hfstack.rallyguard.order.move.MoveOrderSlotResolver;
import net.hfstack.rallyguard.order.defend.DefendAreaBoundaryPolicy;
import net.hfstack.rallyguard.service.GuardCommandTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class DefendAreaNetworking {
    private static final double MAX_INTERACTION_RANGE = 8.0;
    private static boolean registered;

    private DefendAreaNetworking() {
    }

    public static synchronized void registerServer() {
        if (registered) return;
        registered = true;
        ServerPlayNetworking.registerGlobalReceiver(DefendAreaC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handle(player, payload));
        });
    }

    private static void handle(ServerPlayer player, DefendAreaC2SPayload payload) {
        ServerLevel world = player.level();
        BlockPos clicked = payload.clickedBlock();
        boolean knownFace = payload.faceId() >= 0 && payload.faceId() < Direction.values().length;
        BlockPos center = knownFace
                ? MoveDestinationPolicy.anchor(clicked, Direction.from3DDataValue(payload.faceId()))
                : clicked;
        boolean loaded = world.hasChunkAt(clicked) && world.hasChunkAt(center);
        boolean withinBorder = world.getWorldBorder().isWithinBounds(clicked)
                && world.getWorldBorder().isWithinBounds(center);
        boolean centerSafe = loaded && withinBorder
                && MoveOrderSlotResolver.resolve(world, center, 0).isPresent();

        MoveHereRequestPolicy.Result validation = MoveHereRequestPolicy.validate(
                payload.categoryId(),
                payload.faceId(),
                GuardCommandTargets.validScope(player, payload.scope()),
                MoveDestinationPolicy.withinInteractionRange(player.getEyePosition(), clicked, MAX_INTERACTION_RANGE),
                loaded,
                withinBorder,
                centerSafe
        );
        if (!(validation instanceof MoveHereRequestPolicy.Valid valid)) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.defend.invalid_destination"));
            return;
        }

        int radius = RallyConfig.defendAreaRadius();
        GuardBatchCommandResult result = MoveHereBatchPolicy.execute(
                GuardCommandTargets.loadedScopedGuards(player, payload.scope()),
                GuardEntity::getUUID,
                guard -> GuardCombatRole.matches(valid.category(), guard),
                (guard, slotIndex) -> MoveOrderSlotResolver.resolve(world, center, slotIndex, guard)
                        .filter(post -> DefendAreaBoundaryPolicy.withinRadius(
                                center,
                                post.getX() + 0.5,
                                post.getY() + 0.5,
                                post.getZ() + 0.5,
                                radius
                        )),
                (guard, post) -> RallyGuardApi.guardCommands().defendArea(
                        player, guard, center, post, radius
                )
        );
        player.sendOverlayMessage(Component.translatable(
                "gui.rallyguard.squad.batch_result",
                result.succeeded(),
                result.rejected(),
                result.unavailable()
        ));
    }
}
