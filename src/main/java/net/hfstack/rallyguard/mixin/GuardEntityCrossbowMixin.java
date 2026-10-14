package net.hfstack.rallyguard.mixin;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.compat.GuardCrossbowBallisticsPolicy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GuardEntity.class)
abstract class GuardEntityCrossbowMixin {
    @ModifyArg(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/sterner/guardvillagers/common/entity/GuardEntity;performCrossbowAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"
            ),
            index = 1
    )
    private float rallyguard$useVanillaCrossbowProjectileSpeed(float originalSpeed) {
        return GuardCrossbowBallisticsPolicy.projectileSpeed(originalSpeed);
    }
}
