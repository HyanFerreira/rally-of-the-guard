package net.hfstack.rallyguard.registry;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class GlobalGuardRegistryAttachments {
    public static final AttachmentType<CompoundTag> REGISTRY = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "global_guard_registry"),
            builder -> builder.persistent(CompoundTag.CODEC).copyOnDeath()
    );

    private GlobalGuardRegistryAttachments() {
    }

    public static void initialize() {
    }
}
