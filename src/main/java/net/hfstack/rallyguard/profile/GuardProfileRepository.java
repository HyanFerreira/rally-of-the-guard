package net.hfstack.rallyguard.profile;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public final class GuardProfileRepository {
    private static final GuardProfileRepository INSTANCE = new GuardProfileRepository();

    private GuardProfileRepository() {
    }

    public static GuardProfileRepository instance() {
        return INSTANCE;
    }

    public GuardProfileReadResult read(GuardEntity guard) {
        Objects.requireNonNull(guard, "guard");
        CompoundTag stored = guard.getAttached(GuardProfileAttachments.PROFILE);
        return GuardProfileCodec.decode(stored);
    }

    public GuardProfileReadResult getOrCreateLegacy(GuardEntity guard) {
        Objects.requireNonNull(guard, "guard");
        GuardProfileReadResult current = read(guard);
        if (!(current instanceof GuardProfileReadResult.Absent)) {
            return current;
        }

        GuardProfile profile = GuardProfileGenerator.generate(
                guard.getUUID(),
                GuardProfileOrigin.LEGACY,
                false
        );
        guard.setAttached(GuardProfileAttachments.PROFILE, GuardProfileCodec.encode(profile));
        return new GuardProfileReadResult.Available(profile, false);
    }

    public boolean save(GuardEntity guard, GuardProfile profile) {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(profile, "profile");

        GuardProfileReadResult current = read(guard);
        if (!GuardProfilePersistencePolicy.canWrite(current)) {
            return false;
        }

        guard.setAttached(GuardProfileAttachments.PROFILE, GuardProfileCodec.encode(profile));
        return true;
    }
}
