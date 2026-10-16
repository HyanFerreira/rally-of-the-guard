package net.hfstack.rallyguard.network;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.commandpost.CommandPostBlockEntity;
import net.hfstack.rallyguard.commandpost.CommandPostController;
import net.hfstack.rallyguard.commandpost.CommandPostState;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.formation.RallyFormation;
import net.hfstack.rallyguard.network.payload.CommandPostSnapshotS2CPayload;
import net.hfstack.rallyguard.network.payload.CommandPostUpdateC2SPayload;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CommandPostNetworking {
    private static final Identifier GUARD_TYPE = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static boolean registered;

    private CommandPostNetworking() {
    }

    public static synchronized void registerServer() {
        if (registered) return;
        registered = true;
        ServerPlayNetworking.registerGlobalReceiver(CommandPostUpdateC2SPayload.ID, (payload, context) ->
                context.server().execute(() -> handleUpdate(context.player(), payload))
        );
    }

    public static void open(ServerPlayer player, BlockPos pos, CommandPostBlockEntity post) {
        if (!post.canEdit(player.getUUID())) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.command_post.not_owner"));
            return;
        }
        sendSnapshot(player, pos, post);
    }

    private static void handleUpdate(ServerPlayer player, CommandPostUpdateC2SPayload payload) {
        if (!canReach(player, payload.pos())
                || !(player.level().getBlockEntity(payload.pos()) instanceof CommandPostBlockEntity post)
                || !post.canEdit(player.getUUID())) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.command_post.invalid"));
            return;
        }

        if (payload.action() == CommandPostUpdateC2SPayload.UPDATE_SETTINGS) {
            RallyFormation formation = RallyFormation.findById(payload.formationId()).orElse(null);
            if (formation == null) return;
            post.updateSettings(player.getUUID(), payload.name(), payload.active(), payload.radius(), formation);
            CommandPostController.reconcile((net.minecraft.server.level.ServerLevel) player.level(), payload.pos(), post.state());
            sendSnapshot(player, payload.pos(), post);
            return;
        }

        Entity entity = player.level().getEntity(payload.guardEntityId());
        if (!(entity instanceof GuardEntity guard) || !GuardOwnership.isOwnedBy(guard, player.getUUID())) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.command.not_found"));
            return;
        }
        boolean assigning = payload.action() == CommandPostUpdateC2SPayload.ASSIGN_GUARD;
        if (!assigning && payload.action() != CommandPostUpdateC2SPayload.UNASSIGN_GUARD) return;
        if (post.setGuardAssigned(player.getUUID(), guard.getUUID(), assigning)) {
            if (assigning && post.state().active()) {
                int index = post.state().assignedGuardIds().indexOf(guard.getUUID());
                DefendAreas.setFromCommandPost(
                        guard, payload.pos(), CommandPostController.formationPost(payload.pos(), post.state(), index),
                        post.state().radius(), post.state().postId()
                );
            } else if (!assigning) {
                DefendAreas.clearIfFromCommandPost(guard, post.state().postId());
            }
        }
        sendSnapshot(player, payload.pos(), post);
    }

    private static void sendSnapshot(ServerPlayer player, BlockPos pos, CommandPostBlockEntity post) {
        CommandPostState state = post.state();
        List<? extends Entity> loaded = player.level().getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_TYPE),
                entity -> GuardOwnership.isOwnedBy(entity, player.getUUID())
        );
        List<CommandPostSnapshotS2CPayload.GuardEntry> guards = new ArrayList<>();
        for (Entity entity : loaded) {
            if (!(entity instanceof GuardEntity guard)) continue;
            String squadName = RallyGuardApi.squads().find(player, guard).squad().map(squad -> squad.name()).orElse("");
            boolean here = state.assignedGuardIds().contains(guard.getUUID());
            boolean elsewhere = DefendAreas.active(guard)
                    .flatMap(order -> order.commandPostId())
                    .filter(id -> !id.equals(state.postId()))
                    .isPresent();
            guards.add(new CommandPostSnapshotS2CPayload.GuardEntry(
                    guard.getId(), guard.getUUID(), guard.getName().getString(), squadName, here, elsewhere
            ));
        }
        guards.sort(Comparator.comparing(CommandPostSnapshotS2CPayload.GuardEntry::name, String.CASE_INSENSITIVE_ORDER));
        ServerPlayNetworking.send(player, new CommandPostSnapshotS2CPayload(
                pos, state.postId(), state.name(), state.active(), state.radius(), state.formation().id(), guards
        ));
    }

    private static boolean canReach(ServerPlayer player, BlockPos pos) {
        return player.blockPosition().distSqr(pos) <= 64.0;
    }
}
