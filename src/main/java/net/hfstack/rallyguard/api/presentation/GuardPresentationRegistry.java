package net.hfstack.rallyguard.api.presentation;

import net.hfstack.rallyguard.RallyOfTheGuard;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ordered registry of read-only guard presentation providers.
 */
public final class GuardPresentationRegistry {
    private static final CopyOnWriteArrayList<GuardPresentationProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private GuardPresentationRegistry() {
    }

    public static void register(GuardPresentationProvider provider) {
        PROVIDERS.add(Objects.requireNonNull(provider, "provider"));
    }

    public static GuardPresentation resolve(GuardPresentationContext context) {
        Objects.requireNonNull(context, "context");
        GuardPresentation resolved = GuardPresentation.empty();
        for (GuardPresentationProvider provider : PROVIDERS) {
            try {
                GuardPresentation contribution = Objects.requireNonNull(
                        provider.provide(context),
                        "Guard presentation provider returned null"
                );
                resolved = resolved.merge(contribution);
            } catch (RuntimeException exception) {
                RallyOfTheGuard.LOGGER.error(
                        "Guard presentation provider failed for guard {}",
                        context.guard().getUuid(),
                        exception
                );
            }
        }
        return resolved;
    }
}
