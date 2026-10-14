package net.hfstack.rallyguard.api.profile;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.hfstack.rallyguard.profile.GuardProfileRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Read-only access to persisted guard profiles.
 */
public final class GuardProfileAccess {
    private static final GuardProfileAccess INSTANCE =
            new GuardProfileAccess(GuardProfileRepository.instance());

    private final GuardProfileRepository repository;

    private GuardProfileAccess(GuardProfileRepository repository) {
        this.repository = repository;
    }

    public static GuardProfileAccess instance() {
        return INSTANCE;
    }

    public Optional<GuardProfileView> find(GuardEntity guard) {
        Objects.requireNonNull(guard, "guard");
        return viewOf(repository.read(guard));
    }

    static Optional<GuardProfileView> viewOf(GuardProfileReadResult result) {
        Objects.requireNonNull(result, "result");
        if (result instanceof GuardProfileReadResult.Available available) {
            return Optional.of(available.profile());
        }
        return Optional.empty();
    }
}
