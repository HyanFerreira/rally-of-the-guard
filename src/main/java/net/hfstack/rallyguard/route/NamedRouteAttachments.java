package net.hfstack.rallyguard.route;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class NamedRouteAttachments {
    public static final AttachmentType<CompoundTag> REGISTRY = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "named_route_registry"),
            builder -> builder.persistent(CompoundTag.CODEC).copyOnDeath()
    );

    private NamedRouteAttachments() {
    }

    public static void initialize() {
    }
}
