package net.hfstack.rallyguard.api.presentation;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.profile.GuardProfileAccess;
import net.hfstack.rallyguard.api.profile.GuardProfileView;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;
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
        return withProfileFallback(resolveExplicit(context), GuardProfileAccess.instance().find(context.guard()));
    }

    public static GuardPresentation resolveExplicit(GuardPresentationContext context) {
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
                        context.guard().getUUID(),
                        exception
                );
            }
        }
        return resolved;
    }

    static GuardPresentation withProfileFallback(
            GuardPresentation resolved,
            Optional<GuardProfileView> profile
    ) {
        Objects.requireNonNull(resolved, "resolved");
        Objects.requireNonNull(profile, "profile");
        GuardPresentation fallback = profile
                .map(view -> GuardPresentation.of(
                        Component.translatable(view.rank().translationKey()),
                        null
                ))
                .orElseGet(GuardPresentation::empty);
        return resolved.merge(fallback);
    }
}
