package net.hfstack.rallyguard.combat.stance;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class CombatStanceAttachments {
    public static final AttachmentType<CompoundTag> STANCE = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "combat_stance"),
            CompoundTag.CODEC
    );

    private CombatStanceAttachments() {
    }

    public static void initialize() {
    }
}
