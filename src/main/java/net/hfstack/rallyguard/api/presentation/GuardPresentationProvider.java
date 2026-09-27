package net.hfstack.rallyguard.api.presentation;

@FunctionalInterface
public interface GuardPresentationProvider {
    GuardPresentation provide(GuardPresentationContext context);
}
