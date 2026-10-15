package net.hfstack.rallyguard.order.defend;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class DefendAreaAttachments {
    public static final AttachmentType<CompoundTag> ORDER = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "defend_area"),
            CompoundTag.CODEC
    );

    private DefendAreaAttachments() {
    }

    public static void initialize() {
    }
}
