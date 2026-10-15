package net.hfstack.rallyguard.order.move;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class MoveOrderAttachments {
    public static final AttachmentType<CompoundTag> ORDER = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "move_order"),
            CompoundTag.CODEC
    );

    private MoveOrderAttachments() {
    }

    public static void initialize() {
    }
}
