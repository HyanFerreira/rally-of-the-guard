package net.hfstack.rallyguard.raid;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.resources.Identifier;

public final class RaidBehaviorAttachments {
    public static final AttachmentType<String> BEHAVIOR = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "raid_behavior"),
            builder -> builder.persistent(Codec.STRING).copyOnDeath()
    );

    private RaidBehaviorAttachments() {
    }

    public static void initialize() {
    }
}
