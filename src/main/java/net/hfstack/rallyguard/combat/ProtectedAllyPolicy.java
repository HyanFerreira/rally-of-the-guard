package net.hfstack.rallyguard.combat;

import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.OwnableEntity;

import java.util.Objects;
import java.util.UUID;

public final class ProtectedAllyPolicy {
    private ProtectedAllyPolicy() {
    }

    public static boolean isProtectedAlly(UUID commanderId, Entity target) {
        Objects.requireNonNull(commanderId, "commanderId");
        Objects.requireNonNull(target, "target");

        UUID persistentOwnerId = GuardOwnership.getOwner(target);
        if (persistentOwnerId == null && target instanceof OwnableEntity ownable) {
            var ownerReference = ownable.getOwnerReference();
            if (ownerReference != null) {
                persistentOwnerId = ownerReference.getUUID();
            }
        }

        boolean commanderRidingTarget = target.getPassengers().stream()
                .anyMatch(passenger -> commanderId.equals(passenger.getUUID()));
        boolean targetLeashedToCommander = target instanceof Leashable leashable
                && leashable.getLeashHolder() != null
                && commanderId.equals(leashable.getLeashHolder().getUUID());

        return matchesRelationship(
                commanderId,
                target.getUUID(),
                persistentOwnerId,
                commanderRidingTarget,
                targetLeashedToCommander
        );
    }

    static boolean matchesRelationship(
            UUID commanderId,
            UUID targetId,
            UUID persistentOwnerId,
            boolean commanderRidingTarget,
            boolean targetLeashedToCommander
    ) {
        Objects.requireNonNull(commanderId, "commanderId");
        Objects.requireNonNull(targetId, "targetId");
        return commanderId.equals(targetId)
                || commanderId.equals(persistentOwnerId)
                || commanderRidingTarget
                || targetLeashedToCommander;
    }
}
