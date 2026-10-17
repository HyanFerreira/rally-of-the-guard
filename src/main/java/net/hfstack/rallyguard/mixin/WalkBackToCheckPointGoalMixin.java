package net.hfstack.rallyguard.mixin;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.contract.GuardOwnership;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.sterner.guardvillagers.common.entity.goal.WalkBackToCheckPointGoal")
abstract class WalkBackToCheckPointGoalMixin {
    @Shadow
    @Final
    private GuardEntity guard;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void rallyguard$useCalmOwnedPatrolMovement(CallbackInfo callback) {
        if (GuardOwnership.hasOwner(guard)) callback.cancel();
    }
}
