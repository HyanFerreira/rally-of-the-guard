package net.hfstack.rallyguard.mixin;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.compat.GuardMeleeBackstepPolicy;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "dev.sterner.guardvillagers.common.entity.GuardEntity$GuardEntityMeleeGoal")
abstract class GuardEntityMeleeGoalMixin {
    @Shadow
    @Final
    public GuardEntity guard;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/control/MoveControl;strafe(FF)V"
            )
    )
    private void rallyguard$preventShieldlessBackstep(MoveControl moveControl, float forward, float sideways) {
        boolean hasShield = guard.getOffhandItem().is(Items.SHIELD);
        if (GuardMeleeBackstepPolicy.shouldBackstep(hasShield, guard.isBlocking())) {
            moveControl.strafe(forward, sideways);
        }
    }
}
