package net.hfstack.rallyguard.muster;

import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MusterCaptainActionHandler {
    private MusterCaptainActionHandler() {
    }

    public static void handle(ServerPlayer player, CaptainAction action) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(action, "action");
        if (action == CaptainAction.NONE) return;

        boolean handled = handle(
                action,
                () -> MusterRollSessions.guardFor(player),
                (guard, requested) -> requested == CaptainAction.PROMOTE
                        ? RallyGuardApi.captains().promote(player, guard)
                        : RallyGuardApi.captains().demote(player, guard),
                player::sendOverlayMessage,
                () -> MusterRollSessions.refresh(player)
        );
        if (!handled) MusterRollSessions.closeAndNotify(player);
    }

    static <G> boolean handle(
            CaptainAction action,
            Supplier<Optional<G>> guardResolver,
            ActionExecutor<G> service,
            Consumer<Component> feedback,
            Runnable refresh
    ) {
        Objects.requireNonNull(action, "action");
        if (action == CaptainAction.NONE) return false;
        Optional<G> guard = guardResolver.get();
        if (guard.isEmpty()) return false;

        CaptainMutationResult result = service.execute(guard.orElseThrow(), action);
        feedback.accept(result.feedback());
        if (result.succeeded()) refresh.run();
        return true;
    }

    @FunctionalInterface
    interface ActionExecutor<G> {
        CaptainMutationResult execute(G guard, CaptainAction action);
    }
}
