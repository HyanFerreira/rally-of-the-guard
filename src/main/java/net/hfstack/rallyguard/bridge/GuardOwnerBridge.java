package net.hfstack.rallyguard.bridge;

import java.util.UUID;

/**
 * Interface "bridge" (sem @Mixin) para acessar o dono do Guard.
 * O mixin vai implementar isto na classe do Guard Villagers.
 */
public interface GuardOwnerBridge {
    void rallyguard$setOwnerUuid(UUID uuid);

    UUID rallyguard$getOwnerUuid();
}
