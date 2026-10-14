package net.hfstack.rallyguard.squad;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class SquadAttachments {
    public static final AttachmentType<CompoundTag> REGISTRY = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "squad_registry"),
            CompoundTag.CODEC
    );
    public static final AttachmentType<CompoundTag> ASSIGNMENT = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "squad_assignment"),
            CompoundTag.CODEC
    );

    private SquadAttachments() {
    }

    public static void initialize() {
    }
}
