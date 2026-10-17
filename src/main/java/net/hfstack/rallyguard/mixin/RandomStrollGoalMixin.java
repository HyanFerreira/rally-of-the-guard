package net.hfstack.rallyguard.mixin;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomStrollGoal.class)
abstract class RandomStrollGoalMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @Inject(method = {"canUse", "canContinueToUse"}, at = @At("HEAD"), cancellable = true)
    private void rallyguard$disableRandomStrollDuringOwnedPatrol(CallbackInfoReturnable<Boolean> callback) {
        if (mob instanceof GuardEntity guard
                && GuardOwnership.hasOwner(guard)
                && guard.isPatrolling()) {
            callback.setReturnValue(false);
        }
    }
}
