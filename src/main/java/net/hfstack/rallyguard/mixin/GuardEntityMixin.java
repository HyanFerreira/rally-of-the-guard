package net.hfstack.rallyguard.mixin;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.profile.GuardSpawnContextTracker;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuardEntity.class)
abstract class GuardEntityMixin {
    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void rallyguard$initializeNaturalProfile(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData,
            CallbackInfoReturnable<SpawnGroupData> callback
    ) {
        GuardEntity guard = (GuardEntity) (Object) this;
        GuardSpawnContextTracker.record(guard.getUUID(), spawnReason);
    }
}
