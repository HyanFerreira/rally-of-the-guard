package net.hfstack.rallyguard.progression;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.Optional;

public final class GuardDamageResolver {
    private GuardDamageResolver() {
    }

    public static Optional<GuardEntity> resolve(DamageSource source) {
        Entity causing = source.getEntity();
        if (causing instanceof GuardEntity guard) {
            return Optional.of(guard);
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof GuardEntity guard) {
            return Optional.of(guard);
        }
        if (direct instanceof GuardEntity guard) {
            return Optional.of(guard);
        }
        return Optional.empty();
    }
}
