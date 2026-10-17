package net.hfstack.rallyguard.raid;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class RaidGuardTaskAttachments {
    public static final AttachmentType<CompoundTag> SUSPENDED_TASK = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "raid_suspended_task"),
            CompoundTag.CODEC
    );

    private RaidGuardTaskAttachments() {
    }

    public static void initialize() {
    }
}
