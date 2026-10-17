package net.hfstack.rallyguard.order.temporary;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class GuardTaskAttachments {
    public static final AttachmentType<CompoundTag> SUSPENDED_TASK = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "suspended_task"),
            CompoundTag.CODEC
    );

    private GuardTaskAttachments() {
    }

    public static void initialize() {
    }
}
