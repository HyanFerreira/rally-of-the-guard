package net.hfstack.rallyguard.mixin;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.order.GuardFollowAssistPolicy;
import net.hfstack.rallyguard.order.GuardOrders;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "dev.sterner.guardvillagers.common.entity.GuardEntity$FollowHeroGoal")
abstract class GuardFollowHeroGoalMixin {
    @Shadow
    @Final
    public GuardEntity guard;

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 0.7))
    private double rallyguard$assistSprintFollowing(double originalSpeed) {
        if (!(guard.getOwner() instanceof ServerPlayer owner)) return originalSpeed;
        boolean fighting = guard.getTarget() != null && guard.getTarget().isAlive();
        return GuardFollowAssistPolicy.speed(
                guard.isFollowing(),
                GuardOrders.isRallied(guard),
                fighting,
                owner.isSprinting(),
                owner.isPassenger(),
                guard.distanceToSqr(owner)
        ).orElse(originalSpeed);
    }
}
