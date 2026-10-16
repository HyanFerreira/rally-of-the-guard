package net.hfstack.rallyguard.commandpost;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final BlockEntityType<CommandPostBlockEntity> COMMAND_POST = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "command_post"),
            FabricBlockEntityTypeBuilder.create(CommandPostBlockEntity::new, ModBlocks.COMMAND_POST).build()
    );

    private ModBlockEntities() {
    }

    public static void register() {
        RallyOfTheGuard.LOGGER.info("Registering block entities for {}", RallyOfTheGuard.MOD_ID);
    }
}
