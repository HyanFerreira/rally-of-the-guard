package net.hfstack.rallyguard.profile;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class GuardProfileAttachments {
    public static final AttachmentType<CompoundTag> PROFILE = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "profile"),
            CompoundTag.CODEC
    );

    private GuardProfileAttachments() {
    }

    public static void initialize() {
        // Forces registration during common initialization.
    }
}
